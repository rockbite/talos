package com.talosvfx.talos.editor.addons.shader.workspace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.nodes.ColorOutput;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;
import com.talosvfx.talos.editor.notifications.events.NodeRemovedEvent;

public class ShaderNodeStage extends DynamicNodeStage implements Notifications.Observer {

    private ColorOutput colorOutput;

    public ShaderNodeStage (Skin skin) {
        super(skin);

        Notifications.registerObserver(this);
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

    @Override
    public void reset () {
        super.reset();
        colorOutput = null;
    }

    @EventHandler
    public void onNodeRemoved(NodeRemovedEvent event) {
       if (event.getNode() == colorOutput) {
           colorOutput = null;
       }
    }

    public String getFragShader() {
        ShaderBuilder builder = new ShaderBuilder();
        colorOutput.buildFragmentShader(builder);

        return builder.getFragmentString();
    }
}
