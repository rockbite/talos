package com.talosvfx.talos.editor.addons.shader.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.talosvfx.talos.editor.addons.shader.ShaderProject;
import com.talosvfx.talos.editor.utils.WavefrontReader;

public class ShaderBox3D extends ShaderBox {

    private FrameBuffer frameBuffer;
    private Viewport viewport;
    private final Mesh mesh;
    private ShaderProgram spriteShader;

    public ShaderBox3D() {
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 512, 512, false, false);
        viewport = new FitViewport(3f, 3f);
        spriteShader = new ShaderProgram(Gdx.files.internal("shaders/ui/default.vert"), Gdx.files.internal("shaders/ui/default.frag"));

        // load 2d model here for now
        FileHandle handle = Gdx.files.internal("addons/shader/models/boat.obj");
        WavefrontReader reader = new WavefrontReader();
        reader.parseFile(handle);

        mesh = reader.getMesh();
    }

    @Override
    protected void drawCall (Batch batch) {
        ShaderProgram fancyShader = batch.getShader();
        batch.end();

        frameBuffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mesh.render(fancyShader, GL20.GL_TRIANGLES, 0, mesh.getVertexSize());

        frameBuffer.end();
        Texture colorBufferTexture = frameBuffer.getColorBufferTexture();

        batch.setShader(spriteShader);
        batch.begin();
        batch.draw(colorBufferTexture, getWidth(), getHeight());
    }
}
