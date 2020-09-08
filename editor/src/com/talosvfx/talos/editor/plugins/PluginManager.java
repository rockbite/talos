package com.talosvfx.talos.editor.plugins;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static java.nio.file.StandardWatchEventKinds.*;

public class PluginManager {

    private ArrayList<TalosPluginProvider> pluginProviders = new ArrayList<>();

    private WatchService watchService;
    private HashMap<WatchKey, FileHandle> watching = new HashMap<>();

    public PluginManager () {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread fileWatchingThread = new Thread(new Runnable() {
            @Override
            public void run () {
                while (true) {
                    try {
                        tickFileWatching();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        fileWatchingThread.start();
    }

    private void tickFileWatching () {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException x) {
                return;
            }

            FileHandle dir = watching.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
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
            register = pluginDir.file().toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            watching.put(register, pluginDir);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadInternalPlugins () {
        reloadAllPlugins(Gdx.files.internal("plugins/"));

        //Register file watcher for internal (only locally actually, in deployment internal is stuck inside the jar
    }

    public void reloadAllPlugins (FileHandle pluginDir) {

        FileHandle[] list = pluginDir.list();

        for (FileHandle fileHandle : list) {
            if (fileHandle.extension().equalsIgnoreCase("jar")) {
                loadPluginProvider(fileHandle);
            }
        }

        addFileTracker(pluginDir);
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

        pluginProviders.add(talosPluginProvider);

        talosPluginProvider.init();

    }


    private TalosPlugin findPlugin (String pluginName) {
        for (TalosPluginProvider pluginProvider : pluginProviders) {
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
        for (TalosPluginProvider pluginProvider : pluginProviders) {
            Class<? extends NodeWidget> customNodeWidget = pluginProvider.getCustomNodeWidget(className);
            if (customNodeWidget != null) {
                return customNodeWidget;
            }
        }
        return null;
    }


    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(new ApplicationAdapter() {
            @Override
            public void create () {

                PluginManager pluginManager = new PluginManager();
                pluginManager.loadInternalPlugins();

                TalosPlugin testPlugin = pluginManager.findPlugin("com.talosvfx.talos.plugins.TestPlugin");

                if (testPlugin != null) {
                    TalosPluginProvider provider = testPlugin.getProvider();
                } else {
                    System.out.println("No plugin found");
                }

            }
        }, config);
    }

}
