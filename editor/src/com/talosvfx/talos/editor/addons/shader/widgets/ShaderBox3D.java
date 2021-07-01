package com.talosvfx.talos.editor.addons.shader.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.shader.ShaderProject;
import com.talosvfx.talos.editor.utils.WavefrontReader;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

public class ShaderBox3D extends ShaderBox {

    private FrameBuffer frameBuffer;
    private final Mesh mesh;
    private ShaderProgram spriteShader;
    public PerspectiveCamera cam;

    public ShaderBox3D() {
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 512, 512, true, false);
        cam = new PerspectiveCamera(67, 3, 3);
        cam.position.set(3f, 3f, 3f);
        cam.lookAt(0,0,0);
        cam.near = 0f;
        cam.far = 10f;
        cam.update();
        spriteShader = new ShaderProgram(Gdx.files.internal("shaders/ui/default.vert"), Gdx.files.internal("shaders/ui/default.frag"));

        // load 2d model here for now
        FileHandle handle = Gdx.files.internal("addons/shader/models/cube.obj");
        WavefrontReader reader = new WavefrontReader();
        reader.parseFile(handle);

        mesh = reader.getMesh();

        addListener(new InputListener() {

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
            }
        });
    }

    @Override
    public void setShader(ShaderBuilder shaderBuilder) {
        super.setShader(shaderBuilder);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    @Override
    protected void drawCall (Batch batch) {
        batch.end();

        Gdx.gl20.glViewport(0, 0, 512, 512);

        frameBuffer.begin();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projTrans", cam.combined);

        mesh.render(shaderProgram, GL20.GL_TRIANGLES, 0, mesh.getVertexSize());
        shaderProgram.end();
        frameBuffer.end();

        Texture colorBufferTexture = frameBuffer.getColorBufferTexture();
        batch.setShader(spriteShader);
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        batch.draw(colorBufferTexture, getX(), getY(), getWidth(), getHeight());
    }
}
