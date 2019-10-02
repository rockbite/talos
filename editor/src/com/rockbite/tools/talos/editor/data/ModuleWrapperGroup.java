package com.rockbite.tools.talos.editor.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.EditableLabel;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;

public class ModuleWrapperGroup extends Group{

    private ObjectSet<ModuleWrapper> wrappers = new ObjectSet();

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

    public ModuleWrapperGroup(Skin skin) {
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
                TalosMain.Instance().NodeStage().moduleBoardWidget.removeGroup(ModuleWrapperGroup.this);
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
                TalosMain.Instance().NodeStage().moduleBoardWidget.setSelectedWrappers(wrappers);
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
        });

        setTouchable(Touchable.childrenOnly);
        frameImage.setTouchable(Touchable.disabled);
    }

    public void setWrappers(ObjectSet<ModuleWrapper> wrappers) {
        this.wrappers.addAll(wrappers);
    }

    private void recalculateTransform() {
        posMin.set(wrappers.first().getX(), wrappers.first().getY());
        posMax.set(wrappers.first().getX() + wrappers.first().getWidth(), wrappers.first().getY() + wrappers.first().getHeight());
        for (ModuleWrapper wrapper : wrappers) {
            if (wrapper.getX() < posMin.x)
                posMin.x = wrapper.getX();
            if (wrapper.getY() < posMin.y)
                posMin.y = wrapper.getY();
            if (wrapper.getX() + wrapper.getWidth() > posMax.x)
                posMax.x = wrapper.getX() + wrapper.getWidth();
            if (wrapper.getY() + wrapper.getHeight() > posMax.y)
                posMax.y = wrapper.getY() + wrapper.getHeight();

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
        for(ModuleWrapper wrapper: wrappers) {
            wrapper.moveBy(x, y);
        }
    }

    public void removeWrappers(ObjectSet<ModuleWrapper> wrappersToRemove) {
        for(ModuleWrapper wrapper: wrappersToRemove) {
            if(wrappers.contains(wrapper)) {
                wrappers.remove(wrapper);
            }
        }

        if(wrappers.size == 0) {
            TalosMain.Instance().NodeStage().moduleBoardWidget.removeGroup(this);
        }
    }

    public String getText() {
        return title.getText();
    }

    public ObjectSet<ModuleWrapper> getModuleWrappers() {
        return wrappers;
    }

    public Color getFrameColor() {
        return frameImage.getColor();
    }

    public void setData(String text, Color color) {
        title.setText(text);
        frameImage.setColor(color);
    }

    public void removeWrapper(ModuleWrapper wrapper) {
        wrappers.remove(wrapper);
        if(wrappers.size == 0) {
            TalosMain.Instance().NodeStage().moduleBoardWidget.removeGroup(this);
        }
    }
}
