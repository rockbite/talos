package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.TalosInputProcessor;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.AbstractGenericRoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.AbstractRoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.DelayNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineConfigMap;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.events.TweenFinishedEvent;
import com.talosvfx.talos.editor.addons.scene.events.TweenPlayedEvent;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;

public class RoutineStage extends DynamicNodeStage {

    public final RoutineEditor routineEditor;

    public RoutineConfigMap routineConfigMap;

    private Vector2 tmp = new Vector2();

    private float timeScale = 1f;

    private RoutineInstance routineInstance; // runtime

    public RoutineStage(RoutineEditor routineEditor, Skin skin) {
        super(skin);
        this.routineEditor = routineEditor;

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.S && TalosInputProcessor.ctrlPressed()) {
                    writeData(routineEditor.targetFileHandle);
                }
                return super.keyDown(event, keycode);
            }
        });

        routineInstance = new RoutineInstance();
    }

    public void writeData(FileHandle target) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(this);
        target.writeString(data, false);
    }

    public void loadFrom(FileHandle targetFileHandle) {
        Json json = new Json();
        JsonReader jsonReader = new JsonReader();
        read(json, jsonReader.parse(targetFileHandle));
        routineInstance.loadFrom(targetFileHandle.readString(), routineConfigMap);
    }

    @Override
    public void read(Json json, JsonValue root) {
        super.read(json, root);
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
        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for(NodeWidget node : nodes) {
            if (node instanceof AbstractRoutineNode) {
                AbstractRoutineNode tweenNode = (AbstractRoutineNode) node;

                tweenNode.reset();
            }
        }

        Notifications.fireEvent(Notifications.obtainEvent(TweenPlayedEvent.class));

    }

    /**
     * Time to reset everything to normal and reset any defaults
     */
    private void playFinished() {
        Notifications.fireEvent(Notifications.obtainEvent(TweenFinishedEvent.class));
    }

    /**
     * is reported when any node is completed, even though other oe can be started
     */
    public void nodeReportedComplete() {

        boolean isRunning = false;
        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for(NodeWidget node : nodes) {
            if (node instanceof AbstractGenericRoutineNode) {
                AbstractGenericRoutineNode tweenNode = (AbstractGenericRoutineNode) node;

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

    public float getDelta() {
        return Gdx.graphics.getDeltaTime() * timeScale;
    }
}
