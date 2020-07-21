package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.WorkplaceStage;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;
import com.talosvfx.talos.editor.utils.GridRenderer;

public abstract class DynamicNodeStage extends WorkplaceStage {

    protected XmlReader.Element nodeData;
    public Skin skin;
    protected NodeBoard nodeBoard;
    private Image selectionRect;

    private NodeListPopup nodeListPopup;

    public DynamicNodeStage (Skin skin) {
        super();
        this.skin = skin;
        nodeData = loadData();
    }

    protected abstract XmlReader.Element loadData();

    @Override
    public void init () {
        bgColor.set(0.15f, 0.15f, 0.15f, 1f);

        nodeListPopup = new NodeListPopup(nodeData);
        nodeListPopup.setListener(new NodeListPopup.NodeListListener() {
            @Override
            public void chosen (Class clazz, float x, float y) {
                if(NodeWidget.class.isAssignableFrom(clazz)) {
                    NodeWidget node = createNode(clazz, x, y);

                    Notifications.fireEvent(Notifications.obtainEvent(NodeCreatedEvent.class).set(node));
                }
            }
        });

        initActors();
        initListeners();
    }

    public void showPopup() {
        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);

        nodeListPopup.showPopup(TalosMain.Instance().UIStage().getStage(), vec);
    }
    public NodeWidget createNode (Class<? extends NodeWidget> clazz, float x, float y) {
        return nodeBoard.createNode(clazz, nodeListPopup.getConfigFor(clazz), x, y);
    }

    protected void initActors() {
        GridRenderer gridRenderer = new GridRenderer(stage);
        stage.addActor(gridRenderer);

        nodeBoard = new NodeBoard(skin, this);

        stage.addActor(nodeBoard);

        selectionRect = new Image(skin.getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        stage.addActor(selectionRect);
    }

    @Override
    protected void initListeners () {
        super.initListeners();

        stage.addListener(new InputListener() {

            boolean dragged = false;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                dragged = false;
                return true;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                dragged = true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {

                if(button == 0 && !event.isCancelled()) {
                    FocusManager.resetFocus(getStage());
                    nodeBoard.clearSelection();
                }

                if(button == 1 && !event.isCancelled()) {
                    showPopup();
                }
            }
        });
    }
}
