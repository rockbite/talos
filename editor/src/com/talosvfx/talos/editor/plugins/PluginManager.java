package com.talosvfx.talos.editor.plugins;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.sun.nio.file.SensitivityWatchEventModifier;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static java.nio.file.StandardWatchEventKinds.*;

public class PluginManager {


    private WatchService watchService;
    private HashMap<WatchKey, FileHandle> watching = new HashMap<>();

    private HashMap<String, URLClassLoader> classLoaders = new HashMap<>();
    private HashMap<String, TalosPluginProvider> nameToPluginProviders = new HashMap<>();

    public PluginManager () {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread fileWatchingThread = new Thread(new Runnable() {
            @Override
            public void run () {
                tickFileWatching();
            }
        });


        fileWatchingThread.start();
    }

    private void tickFileWatching () {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watchService.poll(300, TimeUnit.MILLISECONDS);
            } catch (InterruptedException x) {
                return;
            }

            FileHandle dir = watching.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();

                //Any event just reload them all

                System.out.println();
                System.out.println("======================");
                System.out.println("HOT RELOADING PLUGINS");
                System.out.println("======================");
                System.out.println();

                reloadAllPlugins(dir);
                break;
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                watching.remove(key);

                // all directories are inaccessible
                if (watching.isEmpty()) {
                    break;
                }
            }
        }
    }


    private void addFileTracker (FileHandle pluginDir) {

        if (watching.containsValue(pluginDir)) {
            return;
        }

        WatchKey register = null;
        try {
            register = pluginDir.file().toPath().register(watchService, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
            watching.put(register, pluginDir);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadPlugins () {
        reloadAllPlugins(Gdx.files.local("plugins/"));
    }

    public void reloadAllPlugins (FileHandle pluginDir) {
        unloadAllPlugins();

        if (pluginDir.exists()) {
            FileHandle[] list = pluginDir.list();

            for (FileHandle fileHandle : list) {
                if (fileHandle.extension().equalsIgnoreCase("jar")) {
                    loadPluginProvider(fileHandle);
                }
            }
            addFileTracker(pluginDir);

        }

    }

    private void unloadAllPlugins () {
        for (TalosPluginProvider value : nameToPluginProviders.values()) {
            unloadPluginProvider(value.getPluginDefinition());
        }
    }

    private void unloadPluginProvider (PluginDefinition pluginDefinition) {
        URLClassLoader classLoader = classLoaders.get(pluginDefinition.name);
        TalosPluginProvider remove = nameToPluginProviders.remove(pluginDefinition.name);

        remove.dispose();

        if (classLoader != null) {

            try {
                classLoader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            classLoaders.remove(pluginDefinition.name);
            System.gc();
        }
    }


    private void loadPluginProvider (FileHandle pluginJar) {

        try {
            JarFile jarFile = new JarFile(pluginJar.file());

            ZipEntry entry = jarFile.getEntry("plugin.yaml");

            if (entry != null) {
                Yaml yaml = new Yaml(new Constructor(PluginDefinition.class));

                InputStream yamlConfig = jarFile.getInputStream(entry);

                PluginDefinition pluginDefinition = yaml.load(yamlConfig);

                System.out.println("Loaded plugin: ");
                System.out.println(pluginDefinition);


                URL[] urls = {new URL("jar:file:" + pluginJar.path() + "!/")};
                URLClassLoader cl = URLClassLoader.newInstance(urls);


                classLoaders.put(pluginDefinition.name, cl);


                Enumeration<JarEntry> jarEntries = jarFile.entries();

                HashMap<String, Class<?>> classes = new HashMap<>();

                while (jarEntries.hasMoreElements()) {
                    JarEntry je = jarEntries.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }

                    // -6 because of .class
                    String className = je.getName().substring(0, je.getName().length() - 6);
                    className = className.replace('/', '.');
                    Class c = cl.loadClass(className);

                    classes.put(className, c);
                }

                System.out.println("Loaded " + classes.size() + " classes");

                String pluginProvider = pluginDefinition.pluginProvider;
                Class providerClazz = classes.get(pluginProvider);
                if (providerClazz == null) {
                    System.out.println("Skippinig invalid plugin, no PluginProvider found with name: " + pluginProvider);
                } else {
                    registerPluginsForPluginDefinition(pluginDefinition, providerClazz, classes);
                }

            } else {
                System.out.println("Skipping invalid plugin: No plugin.yaml found for : " + pluginJar.name());
            }

        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void registerPluginsForPluginDefinition (PluginDefinition pluginDefinition, Class<? extends TalosPluginProvider> providerClazz, HashMap<String, Class<?>> classes) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        TalosPluginProvider talosPluginProvider = providerClazz.newInstance();
        talosPluginProvider.setPluginDefinition(pluginDefinition);
        talosPluginProvider.loadPlugins(classes);

        nameToPluginProviders.put(pluginDefinition.name, talosPluginProvider);

        talosPluginProvider.init();

    }


    private TalosPlugin findPlugin (String pluginName) {
        for (TalosPluginProvider pluginProvider : nameToPluginProviders.values()) {
            ArrayList<TalosPlugin> plugins = pluginProvider.getPlugins();
            for (TalosPlugin plugin : plugins) {
                if (plugin.getClass().getName().equalsIgnoreCase(pluginName)) {
                    return plugin;
                }
            }
        }
        return null;
    }

    public Class<? extends NodeWidget> getCustomNodeWidget (String className) {
        for (TalosPluginProvider pluginProvider : nameToPluginProviders.values()) {
            Class<? extends NodeWidget> customNodeWidget = pluginProvider.getCustomNodeWidget(className);
            if (customNodeWidget != null) {
                return customNodeWidget;
            }
        }
        return null;
    }

    public <T extends TalosPluginProvider> T getPluginManagerForClass (Class<? extends NodeWidget> clazz) {
        for (TalosPluginProvider value : nameToPluginProviders.values()) {
            Class<? extends NodeWidget> customNodeWidget = value.getCustomNodeWidget(clazz.getSimpleName());
            if (customNodeWidget != null) {
                return (T) value;
            }
        }
        return null;
    }


}
