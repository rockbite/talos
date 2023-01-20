package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.*;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.RoutineUpdated;
import com.talosvfx.talos.editor.addons.scene.events.TweenFinishedEvent;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.*;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.*;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.ScenePreviewApp;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.nodes.RoutineExecutorNode;
import com.talosvfx.talos.runtime.scene.Scene;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutineStage extends DynamicNodeStage<RoutineStageData> implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(RoutineStage.class);

    public final RoutineEditorApp routineEditorApp;

    private Vector2 tmp = new Vector2();

    @Getter
    private float timeScale = 1f;
    private boolean loading = false;

    @Getter
    private boolean paused = false;
    @Getter
    private boolean playing;
    private ScenePreviewApp scenePreviewApp;

    private boolean cameraLocked = false;

    public RoutineStage (RoutineEditorApp routineEditorApp, Skin skin) {
        super(skin);
        this.routineEditorApp = routineEditorApp;
        Notifications.registerObserver(this);
    }

    @Override
    protected void initActors () {
        super.initActors();
        nodeBoard.setTouchable(Touchable.enabled);
    }

    public void loadFrom (GameAsset<RoutineStageData> asset) {
        loading = true;
        if (asset == null || asset.getResource() == null) return;

        setFromData(asset);

        reset();

        asset.getResource().constructForUI(this);

        setInstanceListeners();
        loading = false;
    }

    @Override
    public void markAssetChanged () {
        if(!loading) {
            super.markAssetChanged();
        }
    }

    private void setInstanceListeners() {
        data.getRoutineInstance().setListener(new RoutineInstance.RoutineListenerAdapter() {
            @Override
            public void onSignalSent(int nodeId, String port) {
                AbstractRoutineNodeWidget nodeWidget = (AbstractRoutineNodeWidget)nodeBoard.getNodeById(nodeId);
                nodeWidget.animateSignal(port);
            }

            @Override
            public void onInputFetched(int nodeId, String port) {
                AbstractRoutineNodeWidget nodeWidget = (AbstractRoutineNodeWidget)nodeBoard.getNodeById(nodeId);
                nodeWidget.animateInput(port);
            }

            @Override
            public void onComplete() {

            }
        });
    }


    @Override
    protected XmlReader.Element loadData () {
        FileHandle list = Gdx.files.internal("addons/scene/routine-nodes.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        return root;
    }

    @Override
    protected void onConnectionClicked (NodeBoard.NodeConnection connection) {

        // create delay widget
        connection.fromNode.getOutputSlotPos(connection.fromId, tmp);
        float x = tmp.x;
        float y = tmp.y;
        connection.toNode.getInputSlotPos(connection.toId, tmp);
        float toX = tmp.x;
        float toY = tmp.y;

        String toType = connection.toNode.getType(connection.toId);
        String fromType = connection.fromNode.getType(connection.fromId);

        if (toType.equals("signal") && fromType.equals("signal")) {

            tmp.set((x + toX) / 2f, (y + toY) / 2f); // midpoint

            /*
            DelayNodeWidget delayNode = (DelayNodeWidget) createNode("DelayNode", tmp.x, tmp.y);
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

          */
            // do this with any async
        }
    }

    public void routineUpdated() {
        routineUpdated(false);
    }

    public void routineUpdated (boolean isFastChange) {
        if(!loading) {
            //todo: this isn't right
            if (!isFastChange) {
                markAssetChanged();
            }

            data.setRoutineInstance(data.createInstance(true));

            gameAsset.setUpdated();

            Notifications.fireEvent(Notifications.obtainEvent(RoutineUpdated.class).set(gameAsset));
            // we need to remove this listener to avoid reloading, but we need this to be listener for undo/redo functionality

            setInstanceListeners();
        }
    }

    @EventHandler
    public void onNodeCreatedEvent (NodeCreatedEvent event) {
        routineUpdated();
        routineEditorApp.controlWindow.update();
    }

    @EventHandler
    public void onNodeRemovedEvent (NodeRemovedEvent event) {
        routineUpdated();
        routineEditorApp.controlWindow.update();
    }

    @EventHandler
    public void onNodeConnectionCreatedEvent (NodeConnectionCreatedEvent event) {
        routineUpdated();
    }

    @EventHandler
    public void onNodeConnectionRemovedEvent (NodeConnectionRemovedEvent event) {
        routineUpdated();
    }

    @EventHandler
    public void onNodeDataModifiedEvent (NodeDataModifiedEvent event) {
        NodeWidget node = event.getNode();
        updateRoutineInstanceDataFromWidget(data.getRoutineInstance(), node, event.isFastChange);
        routineEditorApp.controlWindow.update();
    }

    private void updateRoutineInstanceDataFromWidget (RoutineInstance routineInstance, NodeWidget nodeWidget, boolean isFastChange) {
        RoutineNode logicNode = routineInstance.getNodeById(nodeWidget.getUniqueId());

        if (logicNode == null) return;

        boolean setRoutineDirty = false;

        // update input properties
        for (ObjectMap.Entry<String, AbstractWidget> stringAbstractWidgetEntry : nodeWidget.widgetMap) {
            String key = stringAbstractWidgetEntry.key;
            AbstractWidget value = stringAbstractWidgetEntry.value;

            if (value instanceof TextValueWidget || value instanceof SelectWidget || value instanceof ValueWidget || value instanceof GameAssetWidget || value instanceof ColorWidget || value instanceof CheckBoxWidget || value instanceof ProbabilityChoiceWidget.ProbabilityWidget) {
                logicNode.setProperty(key, value.getValue());
                setRoutineDirty = true;
            }
        }

        if (setRoutineDirty) {
            routineInstance.setDirty();
            routineUpdated(isFastChange);
        }
    }

    /**
     * reset data to normal if needed
     * keep data defaults for next time
     */
    public void playInitiated () {
        /*
        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for (NodeWidget node : nodes) {
            if (node instanceof AbstractRoutineNodeWidget) {
                AbstractRoutineNodeWidget tweenNode = (AbstractRoutineNodeWidget) node;

                tweenNode.reset();
            }
        }

        Notifications.fireEvent(Notifications.obtainEvent(TweenPlayedEvent.class));
         */

    }

    /**
     * Time to reset everything to normal and reset any defaults
     */
    private void playFinished () {
        Notifications.fireEvent(Notifications.obtainEvent(TweenFinishedEvent.class));
    }

    /**
     * is reported when any node is completed, even though other oe can be started
     */
    public void nodeReportedComplete () {

        /*
        boolean isRunning = false;
        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for (NodeWidget node : nodes) {
            if (node instanceof AsyncRoutineNodeWidget) {
                AsyncRoutineNodeWidget tweenNode = (AsyncRoutineNodeWidget) node;

                if (tweenNode.isRunning()) {
                    isRunning = true;
                    break;
                }
            }
        }

        if (!isRunning) {
            // seems like all is Complete
            playFinished();
        }*/
    }

    public float getDelta () {
        return Gdx.graphics.getDeltaTime() * timeScale;
    }

    @Override
    public void act() {
        if(data == null) return;
        data.getRoutineInstance().tick(getDelta());
        if(scenePreviewApp != null) {
            if (data.getRoutineInstance() != null) {
                scenePreviewApp.setSpeed(timeScale * data.getRoutineInstance().getTimeScale());
            } else {
                scenePreviewApp.setSpeed(timeScale);
            }
        }
    }

    public ScenePreviewApp openPreviewWindow(GameAsset<Scene> gameAsset) {
        scenePreviewApp = SharedResources.appManager.openAppIfNotOpened(gameAsset, ScenePreviewApp.class);

        return scenePreviewApp;
    }

    public void resetNodes() {
        nodeBoard.resetNodes();
    }

    public void play(String executorName) {
        RoutineInstance routineInstance = data.getRoutineInstance();
        RoutineExecutorNode node = (RoutineExecutorNode) routineInstance.getCustomLookup().get(executorName);
        if(node != null) {
            RoutineExecuteNodeWidget executorWidget = (RoutineExecuteNodeWidget) nodeBoard.findNode(node.uniqueId);
            boolean result = executorWidget.startPlay();

            if(result) {
                playing = true;
                lockCamera(cameraLocked);
            }
        }
    }

    public void stop() {
        RoutineInstance routineInstance = data.getRoutineInstance();
        routineInstance.stop();
        if (scenePreviewApp != null) {
            scenePreviewApp.reload();
        }
        playing = false;
        //timeScale = 1f;
    }

    public void resume() {
        paused = false;
        data.getRoutineInstance().setPaused(paused);
        if (scenePreviewApp != null) {
            scenePreviewApp.setPaused(paused);
        }
    }

    public void pause() {
        paused = true;
        data.getRoutineInstance().setPaused(paused);
        if (scenePreviewApp != null) {
            scenePreviewApp.setPaused(paused);
        }
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
        if(scenePreviewApp != null) {
            scenePreviewApp.setSpeed(timeScale);
        }
    }

    public void lockCamera(boolean checked) {
        cameraLocked = checked;
        if(scenePreviewApp != null) {
            scenePreviewApp.setLockCamera(checked);
        }
    }
}
