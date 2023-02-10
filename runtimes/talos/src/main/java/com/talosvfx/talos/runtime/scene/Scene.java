package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class Scene extends SavableContainer  {

    protected transient String name;

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
        super.writeData(json);
    }

    @Override
    public void loadFromHandle(FileHandle handle) {
        super.loadFromHandle(handle);
        setName(handle.nameWithoutExtension());
    }
}
