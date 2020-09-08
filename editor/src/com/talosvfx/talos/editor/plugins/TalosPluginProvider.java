package com.talosvfx.talos.editor.plugins;


import com.talosvfx.talos.editor.nodes.NodeWidget;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class TalosPluginProvider {

    private PluginDefinition pluginDefinition;

    private ArrayList<TalosPlugin> plugins = new ArrayList<>();

    private HashMap<String, Class<? extends NodeWidget>> customNodeWidgets = new HashMap<>();

    private boolean initialized;

    public TalosPluginProvider () {
        //Load from plugin resource

    }

    private boolean isInitialized () {
        return initialized;
    }

    /**
     * Used for loading all resources that may be related to the provider's plugins.
     * Useful for lazy loading of plugin's resources for efficiency
     */
    protected abstract void initialize ();

    public void init () {
        initialize();
        for (TalosPlugin plugin : plugins) {
            plugin.onPluginProviderInitialized();
        }
    }

    public void setPluginDefinition (PluginDefinition pluginDefinition) {
        this.pluginDefinition = pluginDefinition;
    }

    public Class<? extends NodeWidget> getCustomNodeWidget (String className) {
        return customNodeWidgets.get(className);
    }

    public ArrayList<TalosPlugin> getPlugins () {
        return plugins;
    }

    @SuppressWarnings("unchecked")
    public void loadPlugins (HashMap<String, Class<?>> classes) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        for (String plugin : pluginDefinition.plugins) {
            if (!classes.containsKey(plugin)) {
                System.out.println("Ignoring plugin: " + plugin + " as not found");
            } else {
                Class<?> pluginClazz = classes.get(plugin);
                if (TalosPlugin.class.isAssignableFrom(pluginClazz)) {
                    loadPlugin((Class<? extends TalosPlugin<?>>) pluginClazz);
                } else {
                    System.out.println("Ignoring plugin: " + plugin + " as does not extend " + TalosPlugin.class.getName());
                }
            }
        }
        for (String customNode : pluginDefinition.customNodes) {
            if (!classes.containsKey(customNode)) {
                System.out.println("Ignoring custom NodeWidget: " + customNode + " as not found");
            } else {
                Class<?> pluginClazz = classes.get(customNode);
                if (NodeWidget.class.isAssignableFrom(pluginClazz)) {
                    customNodeWidgets.put(customNode, (Class<? extends NodeWidget>) pluginClazz);
                } else {
                    System.out.println("Ignoring custom NodeWidget: " + customNode + " as does not extend " + TalosPlugin.class.getName());
                }
            }
        }
    }

    private void loadPlugin (Class<? extends TalosPlugin<?>> talosPluginClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends TalosPlugin<?>> constructor = talosPluginClass.getConstructor(getClass());
        TalosPlugin<?> talosPlugin = constructor.newInstance(this);
        plugins.add(talosPlugin);
    }

}
