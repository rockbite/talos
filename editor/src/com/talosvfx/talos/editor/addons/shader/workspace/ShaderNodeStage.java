package com.talosvfx.talos.editor.addons.shader.workspace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.shader.nodes.ColorOutput;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;

public class ShaderNodeStage extends DynamicNodeStage {

    private ColorOutput colorOutput;

    public ShaderNodeStage (Skin skin) {
        super(skin);
    }

    @Override
    protected XmlReader.Element loadData () {
        FileHandle list = Gdx.files.internal("addons/shader/nodes.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        return root;
    }

    @Override
    public NodeWidget createNode (Class<? extends NodeWidget> clazz, float x, float y) {
        if(!ColorOutput.class.isAssignableFrom(clazz)) {
            return super.createNode(clazz, x, y);
        } else {
            if(colorOutput == null) {
                NodeWidget node = super.createNode(clazz, x, y);
                colorOutput = (ColorOutput) node;
                return node;
            }
        }

        return null;
    }
}
