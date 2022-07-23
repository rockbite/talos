package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;

public class TweenStage extends DynamicNodeStage {

    public final TweenEditor tweenEditor;

    public TweenStage(TweenEditor tweenEditor, Skin skin) {
        super(skin);
        this.tweenEditor = tweenEditor;

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.S && SceneEditorWorkspace.ctrlPressed()) {
                    writeData(tweenEditor.targetFileHandle);
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    public void writeData(FileHandle target) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(this);
        target.writeString(data, false);
    }

    @Override
    protected XmlReader.Element loadData() {
        FileHandle list = Gdx.files.internal("addons/scene/tween-nodes.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        return root;
    }
}
