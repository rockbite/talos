package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;

public class NodeGroup extends Group implements Json.Serializable{

    private ObjectSet<NodeWidget> nodes = new ObjectSet();

    private Vector2 pos = new Vector2();

    private Vector2 size = new Vector2();

    private final int PADDING = 20;
    private final int TOP_BAR = 34;

    private Skin skin;

    private Vector2 posMin = new Vector2();
    private Vector2 posMax = new Vector2();

    Image frameImage;
    EditableLabel title;
    ImageButton settings;
    Actor topHit;

    PopupMenu settingsPopup;

    NodeBoard nodeBoard;

    public NodeGroup() {

    }

    public NodeGroup(NodeBoard nodeBoard) {
        this.nodeBoard = nodeBoard;
    }

    public NodeGroup(NodeBoard nodeBoard, Skin skin) {
        this.nodeBoard = nodeBoard;
        init(skin);
    }

    public void init(Skin skin) {
        this.skin = skin;

        frameImage = new Image(skin.getDrawable("group_frame"));
        frameImage.setColor(44/255f, 140/255f, 209/255f, 1f);
        addActor(frameImage);

        topHit = new Actor();
        topHit.setTouchable(Touchable.enabled);
        addActor(topHit);

        title = new EditableLabel("GROUP NAME", skin);
        addActor(title);

        settings = new ImageButton(skin, "settings");
        settings.setSize(25, 25);
        addActor(settings);

        settings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);
                settingsPopup.showMenu(TalosMain.Instance().UIStage().getStage(), vec.x, vec.y);
            }
        });

        settingsPopup = new PopupMenu();
        MenuItem changeColorMenuItem = new MenuItem("Change Color");
        MenuItem ungroupMenuItem = new MenuItem("Ungroup");
        settingsPopup.addItem(changeColorMenuItem);
        settingsPopup.addItem(ungroupMenuItem);
        changeColorMenuItem.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void changed(Color newColor) {
                        super.changed(newColor);
                        frameImage.setColor(newColor);
                    }
                });
            }
        });
        ungroupMenuItem.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                nodeBoard.removeGroup(NodeGroup.this);
            }
        });

        topHit.addListener(new ClickListener() {

            Vector2 tmp = new Vector2();
            Vector2 pos = new Vector2();
            Vector2 diff = new Vector2();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pos.set(x, y);
                topHit.localToStageCoordinates(pos);

                nodeBoard.setSelectedNodes(nodes);

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                tmp.set(x, y);
                topHit.localToStageCoordinates(tmp);

                diff.set(tmp).sub(pos);

                moveGroupBy(diff.x, diff.y);

                pos.set(tmp);

                super.touchDragged(event, x, y, pointer);
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                event.cancel();
            }
        });

        setTouchable(Touchable.childrenOnly);
        frameImage.setTouchable(Touchable.disabled);
    }

    public void setNodes(ObjectSet<NodeWidget> wrappers) {
        this.nodes.addAll(wrappers);
    }

    private void recalculateTransform() {
        posMin.set(nodes.first().getX(), nodes.first().getY());
        posMax.set(nodes.first().getX() + nodes.first().getWidth(), nodes.first().getY() + nodes.first().getHeight());
        for (NodeWidget node : nodes) {
            if (node.getX() < posMin.x)
                posMin.x = node.getX();
            if (node.getY() < posMin.y)
                posMin.y = node.getY();
            if (node.getX() + node.getWidth() > posMax.x)
                posMax.x = node.getX() + node.getWidth();
            if (node.getY() + node.getHeight() > posMax.y)
                posMax.y = node.getY() + node.getHeight();

        }

        pos.set(posMin).sub(PADDING, PADDING);
        size.set(posMax).sub(posMin).add(PADDING * 2, PADDING * 2);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        recalculateTransform(); //TODO: dirty logic

        setPosition(pos.x, pos.y);
        setSize(size.x, size.y);
        frameImage.setPosition(0, 0);
        frameImage.setSize(getWidth(), getHeight() + TOP_BAR);

        title.setPosition(7, getHeight() - title.getPrefHeight() + TOP_BAR - 5);
        settings.setPosition(getWidth() - settings.getWidth() - 3, getHeight() - settings.getHeight() + TOP_BAR - 3);

        topHit.setPosition(0, getHeight());
        topHit.setSize(getWidth(), TOP_BAR);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    private void moveGroupBy(float x, float y) {
        for(NodeWidget wrapper: nodes) {
            wrapper.moveBy(x, y);
        }
    }

    public void removeWrappers(ObjectSet<NodeWidget> wrappersToRemove) {
        for(NodeWidget node: wrappersToRemove) {
            if(nodes.contains(node)) {
                nodes.remove(node);
            }
        }

        if(nodes.size == 0) {
            nodeBoard.removeGroup(this);
        }
    }

    public String getText() {
        return title.getText();
    }

    public ObjectSet<NodeWidget> getNodes() {
        return nodes;
    }

    public Color getFrameColor() {
        return frameImage.getColor();
    }

    public void setData(String text, Color color) {
        title.setText(text);
        frameImage.setColor(color);
    }

    public void removeWrapper(NodeWidget node) {
        nodes.remove(node);
        if(nodes.size == 0) {
            nodeBoard.removeGroup(this);
        }
    }

    public void setText(String text) {
        title.setText(text);
    }

    @Override
    public Color getColor() {
        return frameImage.getColor();
    }

    @Override
    public void setColor(Color color) {
        frameImage.setColor(color);
    }

    @Override
    public void write(Json json) {
        json.writeValue("color", frameImage.getColor());
        json.writeValue("text", title.getText());
        json.writeValue("modules", nodes);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        init(VisUI.getSkin());
        Color color = json.readValue(Color.class, jsonData.get("color"));
        String text = jsonData.getString("text");
        nodes = json.readValue(ObjectSet.class, jsonData.get("modules"));
        setText(text);
        setColor(color);
    }
}