package com.talosvfx.talos.editor.plugins;

import java.util.ArrayList;

public class PluginDefinition {

    public String name;
    public String version;
    public String pluginProvider;
    public ArrayList<String> plugins;

    public PluginDefinition () {

    }

    @Override
    public String toString () {
        return "Name: " + name + "\n" +
                "Version: " + version + "\n" +
                "PluginProvider: " + pluginProvider + "\n" +
                "Plugins: " + pluginsToString(plugins);
    }

    private String pluginsToString (ArrayList<String> plugins) {
        StringBuilder buffer = new StringBuilder();
        for (String plugin : plugins) {
            buffer.append("\n\t").append(plugin);
        }
        return buffer.toString();
    }
}
