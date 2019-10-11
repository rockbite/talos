package com.rockbite.tools.talos.runtime.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.rockbite.tools.talos.runtime.modules.Module;

public class ExportData {

    public Array<EmitterExportData> emitters = new Array<>();

    public ExportMetadata metadata = new ExportMetadata();

    public static class ExportMetadata {
        public Array<String> resources = new Array<>();
    }

    public static class EmitterExportData {
        public String name;
        public Array<Module> modules = new Array<>();
        public Array<ConnectionData> connections = new Array<>();

        @Override
        public String toString () {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(name);
            stringBuilder.append("\n");

            for (Module module : modules) {
                stringBuilder.append("\t");
                stringBuilder.append("ModuleID: ");
                stringBuilder.append(module.getIndex());
                stringBuilder.append("\n");
            }

            stringBuilder.append("\n");
            for (ConnectionData connection : connections) {
                stringBuilder.append("\t");
                stringBuilder.append("Connection: ");
                stringBuilder.append(connection);
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        }
    }

}
