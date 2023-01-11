package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.talosvfx.talos.editor.WorkplaceStage;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.data.DynamicNodeStageData;
import com.talosvfx.talos.editor.notifications.EventContextProvider;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeCreatedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DynamicNodeStage<T extends DynamicNodeStageData> extends WorkplaceStage implements EventContextProvider<DynamicNodeStage<?>>, GameAsset.GameAssetUpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(DynamicNodeStage.class);

    protected XmlReader.Element nodeData;
    public Skin skin;
    protected NodeBoard<T> nodeBoard;
    private Image selectionRect;

    private NodeListPopup nodeListPopup;
    private Stage stageSentIn;

    public GameAsset<T> gameAsset;
    public T data;
    private GameAsset.GameAssetUpdateListener gameAssetUpdateListener;

    public DynamicNodeStage (Skin skin) {
        super();
        this.skin = skin;
        nodeData = loadData();
    }

    protected abstract XmlReader.Element loadData();

    public void setFromData (GameAsset<T> data) {
        this.gameAsset = data;
        this.data = data.getResource();


        if (!gameAsset.listeners.contains(this, true)) {
            gameAsset.listeners.add(this);
        }
    }

    public void markAssetChanged () {
        AssetRepository.getInstance().assetChanged(gameAsset);
    }

    @Override
    public void init () {
//        bgColor.set(0.15f, 0.15f, 0.15f, 1f);

        nodeListPopup = new NodeListPopup(nodeData);
        nodeListPopup.setListener(new NodeListPopup.NodeListListener() {
            @Override
            public void chosen (Class clazz, XmlReader.Element module, float screenX, float screenY) {
                if(ClassReflection.isAssignableFrom(NodeWidget.class, clazz)) {
                    NodeWidget node = createNode(module.getAttribute("name"), screenX, screenY);
                    if(node != null) {
                        node.constructNode(module);
                        Notifications.fireEvent(Notifications.obtainEvent(NodeCreatedEvent.class).set(DynamicNodeStage.this, node));

                        nodeBoard.tryAndConnectLasCC(node);

                        node.finishedCreatingFresh();
                    }
                }
            }
        });

        initActors();
        initListeners();
    }

    public NodeListPopup getNodeListPopup() {
        return nodeListPopup;
    }

    public XmlReader.Element getConfigForNodeClass(Class clazz) {
        return nodeListPopup.getConfigFor(clazz);
    }

    public void showPopup() {
        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        final Vector2 vec2 = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        Stage uiStage = SharedResources.stage;
        uiStage.screenToStageCoordinates(vec);

        nodeListPopup.showPopup(uiStage, vec, vec2);
    }
    public NodeWidget createNode (String nodeName, float screenX, float screenY) {
        Class clazz = nodeListPopup.getNodeClassByName(nodeName);
        NodeWidget node = nodeBoard.createNode(clazz, nodeListPopup.getConfigFor(nodeName), screenX, screenY);

        nodeBoard.registerNodeId(node);

        return node;
    }
    public void sendInStage (Stage stage) {
        stageSentIn = stage;

        stageSentIn.addListener(new InputListener() {

            boolean dragged = false;
            Vector2 startPos = new Vector2();
            Vector2 tmp = new Vector2();
            Rectangle rectangle = new Rectangle();

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (stageSentIn != event.getStage()) {
                    event.cancel();
                }
//                getCameraController().scrolled(amountX, amountY);
                return super.scrolled(event, x, y, amountX, amountY);
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (stageSentIn != event.getStage()) {
                    event.cancel();
                    return super.touchDown(event, x, y, pointer, button);
                }

                dragged = false;

                boolean shouldHandle = false;

                if(button == 2 || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    selectionRect.setVisible(true);
                    selectionRect.setSize(0, 0);
                    startPos.set(x, y);
                    shouldHandle = true;
                }

                NodeBoard.NodeConnection hoveredConnection = nodeBoard.getHoveredConnection();

                if(hoveredConnection != null && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && button == 0) {
                    onConnectionClicked(hoveredConnection);
                    shouldHandle = true;
                }

                if (button == 1) {
                    shouldHandle = true;
                }

                if (shouldHandle) {
                    return true;
                } else {
                    // unselect
                    if(!event.isHandled()) {
                        nodeBoard.clearSelection();
                    }

                    return super.touchDown(event, x, y, pointer, button);
                }
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (stageSentIn != event.getStage()) {
                    event.cancel();
                    return;
                }
                super.touchDragged(event, x, y, pointer);

                dragged = true;

                if(selectionRect.isVisible()) {
                    tmp.set(x, y);
                    tmp.sub(startPos);
                    if(tmp.x < 0) {
                        rectangle.setX(x);
                    } else {
                        rectangle.setX(startPos.x);
                    }
                    if(tmp.y < 0) {
                        rectangle.setY(y);
                    } else {
                        rectangle.setY(startPos.y);
                    }
                    rectangle.setWidth(Math.abs(tmp.x));
                    rectangle.setHeight(Math.abs(tmp.y));

                    selectionRect.setPosition(rectangle.x, rectangle.y);
                    selectionRect.setSize(rectangle.getWidth(), rectangle.getHeight());
                }
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if (stageSentIn != event.getStage()) {
                    event.cancel();
                    return;
                }
                if(button == 0 && (!event.isCancelled())) { // previously there was event handled, dunno why
//                    FocusManager.resetFocus(getStage());
                    nodeBoard.clearSelection();
                }

                if(button == 1 && !event.isCancelled()) {
                    showPopup();
                }

                if(selectionRect.isVisible()) {
                    nodeBoard.userSelectionApply(rectangle);
                }

                selectionRect.setVisible(false);
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (stageSentIn != event.getStage()) {
                    event.cancel();
                    return super.keyDown(event, keycode);
                }
//
//                if(keycode == Input.Keys.F5) {
//                    stage.getCamera().position.set(0, 0, 0);
//                    ((OrthographicCamera)stage.getCamera()).zoom = 1.0f;
//                }

                return super.keyDown(event, keycode);
            }
        });

    }
    protected void initActors() {
//        GridRendererWrapper gridRenderer = new GridRendererWrapper(stage);
//        stage.addActor(gridRenderer);

        nodeBoard = new NodeBoard<T>(skin, this);

        getRootActor().addActor(nodeBoard);

        selectionRect = new Image(skin.getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        getRootActor().addActor(selectionRect);
    }

    @Override
    protected void initListeners () {
        super.initListeners();

    }

    protected void onConnectionClicked(NodeBoard.NodeConnection hoveredConnection) {

    }

    public void reset () {
        nodeBoard.reset();
    }

    public NodeBoard<T> getNodeBoard () {
        return nodeBoard;
    }


    @Override
    public void fileDrop (String[] paths, float x, float y) {

    }

    @Override
    public DynamicNodeStage<?> getContext () {
       return this;
    }
}
