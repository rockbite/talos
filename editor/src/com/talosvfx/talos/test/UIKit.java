package com.talosvfx.talos.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.widgets.ui.EmitterList;

public class UIKit extends ApplicationAdapter {

    Stage stage;

    Skin skin;

    Table table;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1200, 700);
        config.setTitle("UIKit Testing Suite");
        config.useVsync(false);

        UIKit uiKit = new UIKit();

        new Lwjgl3Application(uiKit, config);
    }

    @Override
    public void create() {
        super.create();

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.addRegions(atlas);

        table = new Table();

        EmitterList emitterList = new EmitterList(skin);
        table.add().grow().row();
        table.add(emitterList).height(250).growX().bottom();

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        table.setFillParent(true);
        stage.addActor(table);

    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
