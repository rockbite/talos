package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Scene extends SavableContainer  {

    private static final Logger logger = LoggerFactory.getLogger(Scene.class);

    public Scene() {
        super();
    }

    public Scene(String path) {
        super(path);
    }


    @Override
    public String getName () {
        FileHandle fileHandle = Gdx.files.absolute(path);
        return fileHandle.nameWithoutExtension();
    }

    @Override
    public void setName (String name) {
        root.setName(name);
    }

    @Override
    protected void writeData (Json json) {
        json.writeValue("name", getName());
        super.writeData(json);
    }

}
