package com.talosvfx.talos.editor.addons.scene.apps.routines.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class CustomVarWidget extends Table {

    private final Table editing;
    private final Table main;

    private Label label;
    private Label valueLabel;
    private TextField textField;

    private Stage stageRef;

    private boolean isSelected;
    private boolean isHover;

    private String value;

    private EventListener stageListener;

    public CustomVarWidget() {
        editing = new Table();
        main = new Table();
        init(SharedResources.skin);
    }

    private void init(Skin skin) {
        setSkin(skin);
        isSelected = false;

        Table top = new Table();
        Table bottom = new Table();

        label = new Label("", skin);
        valueLabel = new Label("", skin);
        textField = new TextField("0", getSkin(), "no-bg");

        Stack mainStack = new Stack();

        editing.add(textField).growX().padLeft(12);

        main.add(valueLabel).padLeft(12).left().width(0).growX().expandX();
        valueLabel.setEllipsis(true);
        valueLabel.setAlignment(Align.left);

        mainStack.add(editing);
        mainStack.add(main);

        hideEditMode();

        top.add(mainStack).height(32).growX();

        setTouchable(Touchable.enabled);

        setBackgrounds();

        addListener(new ClickListener() {

            private boolean dragged = false;

            private float lastPos = 0;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                isHover = true;
                setBackgrounds();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                isHover = false;
                if(pointer == -1) {
                    setBackgrounds();
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dragged = false;
                lastPos = x;
                return super.touchDown(event, x, y, pointer, button);
            }


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                setBackgrounds();
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(!dragged) {
                    showEditMode();
                }
            }
        });

        textField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (SceneEditorWorkspace.isEnterPressed(keycode)) {
                    hideEditMode();
                }

                return super.keyDown(event, keycode);
            }
        });

        textField.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                super.keyboardFocusChanged(event, actor, focused);
                if(!focused) {
                    hideEditMode();
                }
            }
        });

        stageListener = new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                Vector2 tmpVec = new Vector2();
                tmpVec.set(x, y);
                CustomVarWidget.this.stageToLocalCoordinates(tmpVec);
                Actor touchTarget = CustomVarWidget.this.hit(tmpVec.x, tmpVec.y, false);
                if (touchTarget == null) {
                    getStage().setKeyboardFocus(null);
                }

                return false;
            }
        };

        add(top).growX();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            getStage().getRoot().addCaptureListener(stageListener);
            stageRef = getStage();
        } else {
            if(stageRef != null) {
                stageRef.getRoot().removeCaptureListener(stageListener);
                stageRef = null;
            }
        }
    }

    private void showEditMode() {
        if(editing.isVisible()) return;

        getStage().setKeyboardFocus(textField);
        textField.selectAll();

        editing.setVisible(true);
        main.setVisible(false);

        isSelected = true;
        setBackgrounds();
    }

    private void hideEditMode() {
        setValue(textField.getText());

        textField.clearSelection();

        editing.setVisible(false);
        main.setVisible(true);

        isSelected = false;
        setBackgrounds();
    }

    private void setBackgrounds () {

        ColorLibrary.BackgroundColor color = ColorLibrary.BackgroundColor.LIGHT_GRAY;

        if(isSelected) {
            color = ColorLibrary.BackgroundColor.MID_GRAY;
        } else {
            if (isHover) {
                color = ColorLibrary.BackgroundColor.BRIGHT_GRAY;
            }
        }

        setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.SHAPE_SQUIRCLE, color));
    }

    public void setLabel(String text) {
        label.setText(text);
    }

    public void setValue(String text) {
        valueLabel.setText(text);
        textField.setText(text);

        value = text;

        fireChangedEvent();
    }

    public String getValue () {
        return value;
    }

    protected boolean fireChangedEvent() {
        ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);

        boolean var2 = false;
        try {
            var2 = fire(changeEvent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Pools.free(changeEvent);
        }

        return var2;
    }

}
