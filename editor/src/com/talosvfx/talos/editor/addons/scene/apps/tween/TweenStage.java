package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;

public class TweenStage extends DynamicNodeStage {

    public final TweenEditor tweenEditor;

    public TweenStage(TweenEditor tweenEditor, Skin skin) {
        super(skin);
        this.tweenEditor = tweenEditor;
    }

    @Override
    protected XmlReader.Element loadData() {
        FileHandle list = Gdx.files.internal("addons/scene/tween-nodes.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        return root;
    }
}
