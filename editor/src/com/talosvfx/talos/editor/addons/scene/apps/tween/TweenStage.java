package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorProject;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.AbstractGenericTweenNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.AbstractTweenNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.DelayNode;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;
import com.talosvfx.talos.editor.project.IProject;

public class TweenStage extends DynamicNodeStage {

    public final TweenEditor tweenEditor;

    private Vector2 tmp = new Vector2();
    private String state;

    private float timeScale = 1f;

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

    @Override
    protected void onConnectionClicked(NodeBoard.NodeConnection connection) {

        // create delay widget
        connection.fromNode.getOutputSlotPos(connection.fromId, tmp);
        float x = tmp.x;
        float y = tmp.y;
        connection.toNode.getInputSlotPos(connection.toId, tmp);
        float toX = tmp.x;
        float toY = tmp.y;

        String toType = connection.toNode.getType(connection.toId);
        String fromType = connection.fromNode.getType(connection.fromId);

        if(toType.equals("signal") && fromType.equals("signal")) {

            tmp.set((x + toX) / 2f, (y + toY) / 2f); // midpoint

            DelayNode delayNode = (DelayNode) createNode("DelayNode", tmp.x, tmp.y);
            if (delayNode != null) {
                delayNode.constructNode(getNodeListPopup().getModuleByName("DelayNode"));
                Notifications.fireEvent(Notifications.obtainEvent(NodeCreatedEvent.class).set(delayNode));

                nodeBoard.tryAndConnectLasCC(delayNode);
            }

            delayNode.setY(delayNode.getY() - delayNode.getHeight() / 2f + 40);

            delayNode.setMini();

            nodeBoard.removeConnection(connection);

            nodeBoard.makeConnection(connection.fromNode, delayNode, connection.fromId, "startSignal");
            nodeBoard.makeConnection(delayNode, connection.toNode, "onComplete", connection.toId);
        }
    }

    /**
     * reset data to normal if needed
     * keep data defaults for next time
     */
    public void playInitiated() {
        if(state != null) loadFromSnapshot();
        makeSnapshot();

        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for(NodeWidget node : nodes) {
            if (node instanceof AbstractTweenNode) {
                AbstractTweenNode tweenNode = (AbstractTweenNode) node;

                tweenNode.reset();
            }
        }
    }

    /**
     * Time to reset everything to normal and reset any defaults
     */
    private void playFinished() {
        loadFromSnapshot();
        resetSnapshot();
    }

    /**
     * is reported when any node is completed, even though other oe can be started
     */
    public void nodeReportedComplete() {

        boolean isRunning = false;
        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for(NodeWidget node : nodes) {
            if (node instanceof AbstractGenericTweenNode) {
                AbstractGenericTweenNode tweenNode = (AbstractGenericTweenNode) node;

                if(tweenNode.isRunning()) {
                    isRunning = true;
                    break;
                }
            }
        }

        if(!isRunning) {
            // seems like all is Complete
            playFinished();
        }
    }

    private void resetSnapshot() {
        state = null;
    }

    private void loadFromSnapshot() {
        SceneEditorProject project = (SceneEditorProject) TalosMain.Instance().ProjectController().getProject();
        SceneEditorAddon sceneEditorAddon = project.sceneEditorAddon;

        if(state != null) {
            sceneEditorAddon.workspace.getCurrentContainer().load(state);
        }
    }

    private void makeSnapshot() {
        SceneEditorProject project = (SceneEditorProject) TalosMain.Instance().ProjectController().getProject();
        SceneEditorAddon sceneEditorAddon = project.sceneEditorAddon;
        state = sceneEditorAddon.workspace.getCurrentContainer().getAsString();
    }

    public float getDelta() {
        return Gdx.graphics.getDeltaTime() * timeScale;
    }
}
