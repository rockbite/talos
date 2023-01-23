package com.talosvfx.talos.editor.addons.scene.apps.routines.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.ATypeWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.editor.utils.UIUtils;
import com.talosvfx.talos.editor.widgets.ui.common.ArrowButton;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.TextFieldWithZoom;
import lombok.Getter;

public class CustomVarWidget extends Table {

    private final Table editing;
    private final Table main;
    @Getter
    private final int index;

    private Label label;
    private Label valueLabel;
    private TextField textField;

    private Stage stageRef;

    private boolean isSelected;
    private boolean isHover;

    private String value;

    private EventListener stageListener;
    private ArrowButton arrowButton;

    private Table top;
    private Table bottom;

    @Getter
    private Table fieldContainer;
    private Cell<Table> contentCell;

    private ATypeWidget innerWidget;
    private Label typeLabel;

    private final RoutineStage routineStage;

    public CustomVarWidget(RoutineStage routineStage, ATypeWidget innerWidget, int index) {
        this.index = index;
        this.routineStage = routineStage;
        editing = new Table();
        main = new Table();

        this.innerWidget = innerWidget;

        init(SharedResources.skin);
    }

    private void init(Skin skin) {
        setSkin(skin);
        isSelected = false;

        top = new Table();
        bottom = new Table();
        fieldContainer = new Table();

        label = new LabelWithZoom("", skin);
        valueLabel = new LabelWithZoom("", skin);
        textField = new TextFieldWithZoom("0", getSkin(), "no-bg");

        typeLabel = new LabelWithZoom(innerWidget.getTypeName(), skin);
        typeLabel.setColor(Color.GRAY);

        Stack mainStack = new Stack();

        editing.add(textField).growX().padLeft(12);

        arrowButton = new ArrowButton();
        top.add(arrowButton).growY().left();

        arrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                arrowButton.toggle();

                if(arrowButton.isCollapsed()) {
                    bottom.clearChildren();
                    bottom.pack();
                    UIUtils.invalidateForDepth(bottom, 4);
                } else {
                    bottom.clearChildren();
                    bottom.add(innerWidget);
                    bottom.pack();
                    UIUtils.invalidateForDepth(bottom, 4);
                }
            }
        });

        main.add(valueLabel).padLeft(12).left().width(0).growX().expandX();
        valueLabel.setEllipsis(true);
        valueLabel.setAlignment(Align.left);

        main.add(typeLabel).padRight(6).right().expandX();

        mainStack.add(editing);
        mainStack.add(main);

        hideEditMode();

        fieldContainer.add(mainStack).grow();
        fieldContainer.setTouchable(Touchable.enabled);
        top.add(fieldContainer).height(32).growX();


        DeleteButton deleteButton = new DeleteButton();
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                routineStage.data.removeExposedVariablesWithIndex(index);
                routineStage.routineUpdated();
                routineStage.routineEditorApp.variableCreationWindow.reloadWidgets();
            }
        });
        top.add(deleteButton).growY();

        setTouchable(Touchable.enabled);

        setBackgrounds();

        innerWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                typeLabel.setText(innerWidget.getTypeName());
                fireChangedEvent();
            }
        });

        fieldContainer.addListener(new ClickListener() {

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

        textField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                routineStage.data.changeExposedVariableKey(index, textField.getText());
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
                Actor touchTarget = fieldContainer.hit(tmpVec.x, tmpVec.y, false);
                if (touchTarget == null && getStage() != null) {
                    getStage().setKeyboardFocus(null);
                }

                return false;
            }
        };

        add(top).growX();
        row();
        contentCell = add(bottom).growX();
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

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isHover) {
            CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.MOVE_ALL_DIRECTIONS);
        }
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

        fieldContainer.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.SHAPE_SQUARE, color));
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

    public String getPropertyName() {
        return value;
    }

    public void applyValueToWrapper(PropertyWrapper<?> propertyWrapper) {
        innerWidget.applyValueToWrapper(propertyWrapper);
    }

}
