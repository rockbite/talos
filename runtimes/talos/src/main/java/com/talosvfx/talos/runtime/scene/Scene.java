package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.utils.TempHackUtil;
import lombok.Getter;

public class Scene extends SavableContainer  {

    protected transient String name;
    private transient String talosIdentifier;

    @Getter
    private boolean optimized;

    public Scene() {
        super();
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public void setName (String name) {
        this.name = name;
        root.setName(name);
    }

    @Override
    protected void writeData (Json json) {
        json.writeValue("name", getName());
        json.writeValue("optimized", optimized);
        super.writeData(json);
    }

    @Override
    public void loadFromHandle(FileHandle handle) {
        super.loadFromHandle(handle);
        setName(handle.nameWithoutExtension());

        //Shit is nasty, we bypass normal json idk why
        JsonValue jsonValue = new JsonReader().parse(TempHackUtil.hackIt(handle.readString()));
        optimized = jsonValue.getBoolean("optimized", false);

    }

    @Override
    public String getTalosIdentifier () {
       return talosIdentifier;
    }

    @Override
    public void setTalosIdentifier (String identifier) {
        this.talosIdentifier = identifier;
    }
}
