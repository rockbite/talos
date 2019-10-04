package com.rockbite.tools.talos.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.modules.Module;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

import java.io.File;
import java.net.URISyntaxException;

public class LegacyCompareTest extends ApplicationAdapter implements Runnable {

    Stage stage;
    Stage uiStage;

    SpriteBatchParticleRenderer talosRenderer;

    private static ScopePayload scope = new ScopePayload();

    private int index = 0;

    Array<String> legacyList;
    Array<String> talosList;
    TextureAtlas atlas;

    Skin skin;

    Label leftTime, rightTime, comparedTime;

    Array<Long> leftTimes = new Array<>();
    Array<Long> rightTimes = new Array<>();
    private float talosAverageTimeMS;
    private float legacyAverageTimeMS;

    private ParticleEffectInstance particleEffect;

    public LegacyCompareTest() {

    }

    @Override
    public void run () {
        while (true) {
            try {
                particleEffect.update(1/60f);
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class LegacyActor extends Actor {

        ParticleEffect particleEffect;

        public LegacyActor(FileHandle effect, TextureAtlas atlas) {
            particleEffect = new ParticleEffect();
            particleEffect.loadEmitters(effect);
            particleEffect.loadEmitterImages(atlas);
            particleEffect.start();
        }

        @Override
        public void act(float delta) {
            long nano = TimeUtils.nanoTime();
            particleEffect.setPosition(getX(), getY());
            particleEffect.update(delta);

            if(particleEffect.isComplete()) {
                particleEffect.start();
            }

            long diff = TimeUtils.nanoTime() - nano;
            leftTimes.add(diff);
            if(leftTimes.size > 1000) leftTimes.removeIndex(0);
            long sum = 0;
            for(int i = 0; i < leftTimes.size; i++) {
                sum += leftTimes.get(i);
            }
            long avg = sum/leftTimes.size;
            legacyAverageTimeMS = avg / 1000000f;
            leftTime.setText(legacyAverageTimeMS + "ms");
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            particleEffect.draw(batch);
        }
    }

    private class TalosActor extends Actor {

        ParticleEffectDescriptor particleEffectDescriptor = new ParticleEffectDescriptor();
        ParticleRenderer renderer;

        public TalosActor(FileHandle effect, TextureAtlas atlas, ParticleRenderer renderer) {
            this.renderer = renderer;
            particleEffectDescriptor.setTextureAtlas(atlas);
            particleEffectDescriptor.load(effect);
            particleEffect = particleEffectDescriptor.createEffectInstance();
            particleEffect.setScope(scope);

        }

        @Override
        public void act(float delta) {
            super.act(delta);

            float x = getX();
            float y = getY();
            long nano = System.nanoTime();
            particleEffect.setPosition(x, y);
            particleEffect.update(Gdx.graphics.getDeltaTime());
            long diff = System.nanoTime() - nano;

            rightTimes.add(diff);
            if(rightTimes.size > 1000) rightTimes.removeIndex(0);
            long sum = 0;
            for(int i = 0; i < rightTimes.size; i++) {
                sum += rightTimes.get(i);
            }
            long avg = sum/rightTimes.size;
            talosAverageTimeMS = avg / 1000000f;
            rightTime.setText(talosAverageTimeMS + "ms");

            comparedTime.setText((int)(talosAverageTimeMS/legacyAverageTimeMS  * 100) + "%");

            Module.inputSlotMethodCall = 0;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            particleEffect.render(renderer);
        }
    }

    @Override
    public void create() {
        super.create();

        TextureAtlas skinAtlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.addRegions(skinAtlas);

        uiStage = new Stage();

        stage = new Stage();
        ((OrthographicCamera)stage.getViewport().getCamera()).zoom = 1f/64;

        talosRenderer = new SpriteBatchParticleRenderer(stage.getBatch());

        String mainPath = "C:\\Users\\Tom\\Desktop\\vfx\\";

        //mainPath = getLocalPath() + "\\";

        String assetPath =   mainPath + "Particles-Assets";
        String legacyPath =  mainPath + "RefactoredProduction";
        String talosExport = mainPath + "Converted\\runtime";

        atlas = new TextureAtlas(Gdx.files.absolute(mainPath + "Atlas\\particleAssets.atlas"));

        legacyList = new Array<>();
        talosList = new Array<>();

        traverseFolder(Gdx.files.absolute(legacyPath), legacyList, "p", 0);
        traverseFolder(Gdx.files.absolute(talosExport), talosList, "p", 0);

        addNextEffect();

        Gdx.input.setInputProcessor(stage);

//        Thread thread = new Thread(this);
//        thread.start();
    }

    private void addNextEffect() {
        stage.clear();
        LegacyActor legacyActor = new LegacyActor(Gdx.files.absolute(legacyList.get(index)), atlas);
        legacyActor.setPosition(stage.getWidth()/2f - 3f, stage.getHeight()/2f);
        stage.addActor(legacyActor);

        TalosActor talosActor = new TalosActor(Gdx.files.absolute(talosList.get(index)), atlas, talosRenderer);
        talosActor.setPosition(stage.getWidth()/2f + 3f, stage.getHeight()/2f);
        stage.addActor(talosActor);

        String leftName = legacyList.get(index).substring(legacyList.get(index).lastIndexOf("/")+1);

        uiStage.clear();
        Label mainLbl = new Label(leftName, skin);
        mainLbl.setPosition(uiStage.getWidth()/2f - mainLbl.getPrefWidth()/2f, 100);
        uiStage.addActor(mainLbl);

        leftTime = new Label("", skin);
        rightTime = new Label("", skin);
        comparedTime = new Label("", skin);

        leftTime.setPosition(uiStage.getWidth()/2f - 200,  200);
        rightTime.setPosition(uiStage.getWidth()/2f + 200, 200);
        comparedTime.setPosition(uiStage.getWidth()/2f, 200);

        uiStage.addActor(leftTime);
        uiStage.addActor(rightTime);
        uiStage.addActor(comparedTime);


        index++;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();

        uiStage.act();
        uiStage.draw();

        if(Gdx.input.justTouched()) {
            addNextEffect();
        }
    }

    private void traverseFolder(FileHandle folder, Array<String> fileList, String extension, int depth) {
        for(FileHandle file : folder.list()) {
            if(file.isDirectory() && depth < 10) {
                traverseFolder(file, fileList, extension, depth + 1);
            }
            if(file.extension().equals(extension)) {
                fileList.add(file.path());
            }
        }
    }

    public String getLocalPath() {
        try {
            return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 800;
        config.height = 600;
        config.title = "Talos";
        config.foregroundFPS = 0;
        LwjglFrame frame = new LwjglFrame(new LegacyCompareTest(), config);
    }
}
