package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;

public class ProjectSerializer {

    private NodeStage stage;

    private IntMap<ModuleWrapper> idMap = new IntMap<>();

    public ProjectSerializer(NodeStage stage) {
        this.stage = stage;
    }

    public void read(FileHandle fileHandle) {
        idMap.clear();

        if(!fileHandle.exists()) return;

        JsonValue root = new JsonReader().parse(fileHandle);

        JsonValue effect = root.get("effect");

        JsonValue metadata = root.get("metadata");

        JsonValue emitters = effect.get("emitters");

        for(JsonValue emitter: emitters) {
            String name = emitter.getString("name");
            stage.createNewEmitter(name);

            // now let's add modules to that emitter
            JsonValue modules = emitter.get("modules");
            JsonValue connections = emitter.get("connections");

            if(modules != null) {
                for (JsonValue module : modules) {
                    String clazz = module.getString("ref");
                    float posX = module.getFloat("x");
                    float posY = module.getFloat("y");
                    ModuleWrapper wrapper = stage.moduleBoardWidget.createModule(clazz, posX, posY);
                    wrapper.setPosition(posX, posY);

                    JsonValue configuration = module.get("config");
                    if(configuration != null) {
                        wrapper.read(configuration);
                    }

                    int id = module.getInt("id");
                    idMap.put(id, wrapper);
                }
            }

            // now make connections
            if(connections != null) {
                for (JsonValue connection : connections) {
                    ModuleWrapper moduleFrom = idMap.get(connection.getInt("moduleFrom"));
                    ModuleWrapper moduleTo = idMap.get(connection.getInt("moduleTo"));
                    int slotFrom = connection.getInt("slotFrom");
                    int slotTo = connection.getInt("slotTo");

                    stage.moduleBoardWidget.makeConnection(moduleFrom, moduleTo, slotFrom, slotTo);
                }
            }
        }
    }

    public void write(FileHandle fileHandle) {

        JsonValue root = new JsonValue(JsonValue.ValueType.object);

        root.addChild("metadata", new JsonValue(JsonValue.ValueType.object));

        JsonValue effect = new JsonValue(JsonValue.ValueType.object);
        root.addChild("effect", effect);

        JsonValue emitters = new JsonValue(JsonValue.ValueType.array);
        effect.addChild("emitters", emitters);
        // iterate through emitters
        for(EmitterWrapper emitterWrapper: stage.moduleBoardWidget.moduleWrappers.keys()) {

            JsonValue emitter = new JsonValue(JsonValue.ValueType.object);
            emitter.addChild("name", new JsonValue(emitterWrapper.getName()));

            JsonValue groups = new JsonValue(JsonValue.ValueType.array);
            emitter.addChild("groups", groups);

            JsonValue modules = new JsonValue(JsonValue.ValueType.array);
            emitter.addChild("modules", modules);

            JsonValue connections = new JsonValue(JsonValue.ValueType.array);
            emitter.addChild("connections", connections);

            // iterate through modules
            for(ModuleWrapper moduleWrapper: stage.moduleBoardWidget.moduleWrappers.get(emitterWrapper)) {

                JsonValue module = new JsonValue(JsonValue.ValueType.object);
                module.addChild("id", new JsonValue(moduleWrapper.getId()));
                module.addChild("ref", new JsonValue(moduleWrapper.getModule().getClass().getSimpleName()));
                module.addChild("x", new JsonValue(moduleWrapper.getX()));
                module.addChild("y", new JsonValue( moduleWrapper.getY()));

                JsonValue configuration = new JsonValue(JsonValue.ValueType.object);
                moduleWrapper.write(configuration);
                module.addChild("config", configuration);

                modules.addChild(module);
            }

            //iterate through connections
            for(ModuleBoardWidget.NodeConnection nodeConnection: stage.moduleBoardWidget.nodeConnections.get(emitterWrapper)) {
                JsonValue connection = new JsonValue(JsonValue.ValueType.object);
                connection.addChild("moduleFrom", new JsonValue(nodeConnection.fromModule.getId()));
                connection.addChild("moduleTo", new JsonValue(nodeConnection.toModule.getId()));
                connection.addChild("slotFrom", new JsonValue(nodeConnection.fromSlot));
                connection.addChild("slotTo", new JsonValue(nodeConnection.toSlot));
                connections.addChild(connection);
            }

            emitters.addChild(emitter);
        }

        JsonValue.PrettyPrintSettings settings = new JsonValue.PrettyPrintSettings();
        settings.outputType = JsonWriter.OutputType.javascript;
        String data = root.prettyPrint(settings);

        //String data = toJson.toJson(JsonWriter.OutputType.json);
        fileHandle.writeString(data, false);
    }
}
