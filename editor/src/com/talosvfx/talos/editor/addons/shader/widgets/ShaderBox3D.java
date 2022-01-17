package com.talosvfx.talos.editor.addons.shader.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
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
    private TextureRegion region;

    private float a = 0;

    protected Texture white;

    public ShaderBox3D() {
        white = new Texture(Gdx.files.internal("white.png")); //TODO: not cool

        region = new TextureRegion();
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 512, 512, true, false);
        cam = new PerspectiveCamera(67, 3, 3);
        cam.position.set(1f, 1f, 1f);
        cam.lookAt(0,0,0);
        cam.near = 0f;
        cam.far = 10f;
        cam.update();
        spriteShader = new ShaderProgram(Gdx.files.internal("shaders/ui/default.vert"), Gdx.files.internal("shaders/ui/default.frag"));

        // load 2d model here for now
        FileHandle handle = Gdx.files.internal("addons/shader/models/tinyboat.obj");
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
        a+= Gdx.graphics.getDeltaTime();
        cam.position.set(MathUtils.cos(a) * 1f, MathUtils.sin(a) *1f, 1f);
        cam.lookAt(0,0,0);
        cam.near = 0.01f;
        cam.far = 100f;
        cam.update();

        super.draw(batch, parentAlpha);
    }

    @Override
    protected void drawCall (Batch batch) {
        if (shaderProgram== null) return;

        batch.end();

        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        frameBuffer.begin();

        Gdx.gl.glClearColor(0f, 0f, 0.1f, 1f);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projTrans", cam.combined);

        white.bind(0);
        shaderProgram.setUniformi("u_texture", 0);

        mesh.render(shaderProgram, GL20.GL_TRIANGLES);
        shaderProgram.end();
        frameBuffer.end();

        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Texture colorBufferTexture = frameBuffer.getColorBufferTexture();
        batch.setShader(spriteShader);
        batch.begin();
        region.setTexture(colorBufferTexture);
        region.setU(0);region.setV(0);
        region.setU2(1);region.setV2(1);
        batch.draw(region, getX(), getY(), getWidth()/2f, getHeight()/2f, getWidth(), getHeight(), 1f, -1f, 0);
    }
}
