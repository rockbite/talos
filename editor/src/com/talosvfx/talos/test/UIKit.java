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
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.nodes.AbstractShaderNode;

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

        AbstractShaderNode shaderNode = new AbstractShaderNode() {
            @Override
            public void prepareDeclarations(ShaderBuilder shaderBuilder) {

            }

            @Override
            public String writeOutputCode(String slotId) {
                return null;
            }
        };
        shaderNode.init(skin, null);
        XmlReader reader = new XmlReader();
        XmlReader.Element root = reader.parse(Gdx.files.internal("addons/shader/nodes.xml"));
        shaderNode.constructNode(root.getChild(0).getChild(0));

        table.add(shaderNode);

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
