package com.talosvfx.talos.editor.addons.shader.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.utils.WavefrontReader;

public class ShaderBox3D extends ShaderBox {

    public ShaderBox3D() {
        // load 2d model here for now
        FileHandle handle = Gdx.files.internal("addons/shader/models/boat.obj");
        WavefrontReader reader = new WavefrontReader();
        reader.parseFile(handle);
    }

    @Override
    protected void drawCall (Batch batch) {


    }
}
