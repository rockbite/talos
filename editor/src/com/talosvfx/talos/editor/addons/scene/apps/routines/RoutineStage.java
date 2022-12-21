package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.*;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineConfigMap;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.RoutineUpdated;
import com.talosvfx.talos.editor.addons.scene.events.TweenFinishedEvent;
import com.talosvfx.talos.editor.addons.scene.events.TweenPlayedEvent;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.RoutineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.*;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.*;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;

public class RoutineStage extends DynamicNodeStage implements Observer {

    public final RoutineEditorApp routineEditorApp;

    public RoutineConfigMap routineConfigMap;

    private Vector2 tmp = new Vector2();

    private float timeScale = 1f;

    public RoutineInstance routineInstance; // runtime

    public RoutineStage (RoutineEditorApp routineEditorApp, Skin skin) {
        super(skin);
        this.routineEditorApp = routineEditorApp;

        routineInstance = new RoutineInstance();

        Notifications.registerObserver(this);
    }

    @Override
    protected void initActors () {
        super.initActors();
        nodeBoard.setTouchable(Touchable.enabled);
    }

    private void updatePropertiesForGOs (GameObject gameObject, Array<RoutineRendererComponent> updatedComponents) {
        if (gameObject.hasComponent(RoutineRendererComponent.class)) {
            RoutineRendererComponent component = gameObject.getComponent(RoutineRendererComponent.class);
            if (component.routineInstance != null) {
                if (routineInstance.uuid.equals(component.routineInstance.uuid)) {
                    component.updatePropertyWrappers(true);
                    updatedComponents.add(component);
                }
            }
        }

        Array<GameObject> children = gameObject.getGameObjects();
        if (children != null) {
            for (int i = 0; i < children.size; i++) {
                GameObject child = children.get(i);
                updatePropertiesForGOs(child, updatedComponents);
            }
        }
    }

    public void writeData (FileHandle target) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(this);
        target.writeString(data, false);
    }

    public void loadFrom (GameAsset<RoutineData> asset) {
        if (asset == null) return;

        Json json = new Json();
        JsonReader jsonReader = new JsonReader();

        GameAsset<?> assetForPath = AssetRepository.getInstance().getAssetForPath(asset.getRootRawAsset().handle, true);
        AMetadata metaData = assetForPath.getRootRawAsset().metaData;
        routineInstance.loadFrom(metaData.uuid, asset.getResource().jsonString, routineConfigMap);

        read(json, jsonReader.parse(asset.getResource().jsonString));
    }

    @Override
    public void read (Json json, JsonValue root) {
        super.read(json, root);
        for (NodeWidget node : nodeBoard.nodes) {
            if (node instanceof RoutineExposedVariableNodeWidget) {
                ((RoutineExposedVariableNodeWidget) node).update(routineInstance.getPropertyWrapperWithIndex(((RoutineExposedVariableNodeWidget) node).index));
            }
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("propertyWrapperIndex", routineInstance.getExposedPropertyIndex());

        json.writeObjectStart("propertyWrappers");
        Array<PropertyWrapper<?>> propertyWrappers = routineInstance.getPropertyWrappers();
        for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
            json.writeObjectStart("property");
            json.writeValue("className", propertyWrapper.getClass().getName());
            json.writeValue("property", propertyWrapper);
            json.writeObjectEnd();
        }
        json.writeObjectEnd();

    }

    @Override
    protected XmlReader.Element loadData () {
        FileHandle list = Gdx.files.internal("addons/scene/tween-nodes.xml");
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

    public void routineUpdated () {
        Notifications.fireEvent(Notifications.obtainEvent(RoutineUpdated.class).set(routineInstance));


//        SharedResources.appManager.getAppInstances()
//        AppManager.BaseApp
//        GameObject rootGO = SceneEditorWorkspace.getInstance().getRootGO();
//        Array<RoutineRendererComponent> updatedComponents = new Array<>();
//        updatePropertiesForGOs(rootGO, updatedComponents);
//
//        Gdx.app.postRunnable(new Runnable() {
//            @Override
//            public void run () {
//                for (RoutineRendererComponent updatedComponent : updatedComponents) {
//                    Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(updatedComponent, false));
//                }
//            }
//        });
    }

    @EventHandler
    public void onNodeCreatedEvent (NodeCreatedEvent event) {
        routineUpdated();
    }

    @EventHandler
    public void onNodeRemovedEvent (NodeRemovedEvent event) {
        routineUpdated();
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
        updateRoutineInstanceDataFromWidget(routineInstance, node);
    }

    private void updateRoutineInstanceDataFromWidget (RoutineInstance routineInstance, NodeWidget nodeWidget) {
        RoutineNode logicNode = routineInstance.getNodeById(nodeWidget.getUniqueId());

        if (logicNode == null) return;

        boolean setRoutineDirty = false;

        // update input properties
        for (ObjectMap.Entry<String, AbstractWidget> stringAbstractWidgetEntry : nodeWidget.widgetMap) {
            String key = stringAbstractWidgetEntry.key;
            AbstractWidget value = stringAbstractWidgetEntry.value;

            if (value instanceof SelectWidget || value instanceof ValueWidget || value instanceof GameAssetWidget || value instanceof ColorWidget || value instanceof CheckBoxWidget || value instanceof ProbabilityChoiceWidget.ProbabilityWidget) {
                logicNode.setProperty(key, value.getValue());
                setRoutineDirty = true;
            }
        }

        if (setRoutineDirty) {
            routineInstance.isDirty = true;
            routineUpdated();
        }
    }

    /**
     * reset data to normal if needed
     * keep data defaults for next time
     */
    public void playInitiated () {
        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for (NodeWidget node : nodes) {
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
    private void playFinished () {
        Notifications.fireEvent(Notifications.obtainEvent(TweenFinishedEvent.class));
    }

    /**
     * is reported when any node is completed, even though other oe can be started
     */
    public void nodeReportedComplete () {

        boolean isRunning = false;
        Array<NodeWidget> nodes = getNodeBoard().getNodes();
        for (NodeWidget node : nodes) {
            if (node instanceof AbstractGenericRoutineNode) {
                AbstractGenericRoutineNode tweenNode = (AbstractGenericRoutineNode) node;

                if (tweenNode.isRunning()) {
                    isRunning = true;
                    break;
                }
            }
        }

        if (!isRunning) {
            // seems like all is Complete
            playFinished();
        }
    }

    public float getDelta () {
        return Gdx.graphics.getDeltaTime() * timeScale;
    }


}