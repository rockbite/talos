package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class TextValueWidget extends AbstractWidget<String> {

    private final Table editing;
    private final Table main;

    private Label label;
    private Label valueLabel;
    private TextField textField;

    private Stage stageRef;

    public enum Type {
        NORMAL, TOP, MID, BOTTOM
    }

    private ValueWidget.Type type = ValueWidget.Type.MID;
    private boolean isSelected;
    private boolean isHover;

    private String value;

    private EventListener stageListener;

    public TextValueWidget() {
        editing = new Table();
        main = new Table();

    }

    @Override
    public void init(Skin skin) {
        super.init(skin);

        type = ValueWidget.Type.NORMAL;
        isSelected = false;

        label = new Label("", skin);
        valueLabel = new Label("", skin);
        textField = new TextField("0", getSkin(), "no-bg");

        Stack mainStack = new Stack();

        editing.add(textField).growX().padLeft(12);

        main.add(label).padLeft(12).left().expandX();
        main.add(valueLabel).padRight(12).right().width(0).growX();
        valueLabel.setEllipsis(true);
        valueLabel.setAlignment(Align.right);

        mainStack.add(editing);
        mainStack.add(main);

        hideEditMode();

        content.add(mainStack).height(32).growX();

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
                if(keycode == Input.Keys.ENTER) {
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
                TextValueWidget.this.stageToLocalCoordinates(tmpVec);
                Actor touchTarget = TextValueWidget.this.hit(tmpVec.x, tmpVec.y, false);
                if (touchTarget == null) {
                    getStage().setKeyboardFocus(null);
                }

                return false;
            }
        };
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

    private String getShape() {
        String shape = ColorLibrary.SHAPE_SQUIRCLE;
        if (type == ValueWidget.Type.TOP) {
            shape = ColorLibrary.SHAPE_SQUIRCLE_TOP;
        } else if (type == ValueWidget.Type.BOTTOM) {
            shape = ColorLibrary.SHAPE_SQUIRCLE_BOTTOM;
        } else if (type == ValueWidget.Type.MID) {
            shape = ColorLibrary.SHAPE_SQUARE;
        }

        return shape;
    }

    private void setBackgrounds () {
        String shape = getShape();

        ColorLibrary.BackgroundColor color = ColorLibrary.BackgroundColor.LIGHT_GRAY;

        if(isSelected) {
            color = ColorLibrary.BackgroundColor.MID_GRAY;
        } else {
            if (isHover) {
                color = ColorLibrary.BackgroundColor.BRIGHT_GRAY;
            }
        }

        setBackground(ColorLibrary.obtainBackground(getSkin(), shape, color));
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


    @Override
    public void loadFromXML(XmlReader.Element element) {
        String text = element.getText();
        String defaultValue = element.getAttribute("default", "");

        setValue(defaultValue);

        setLabel(text);
    }

    @Override
    public String getValue () {
        return value;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        setValue(jsonValue.asString());
    }

    @Override
    public void write (Json json, String name) {
        json.writeValue(name, getValue());
    }

    public void setType(ValueWidget.Type type) {
        this.type = type;
        setBackgrounds();
    }

    public void setNone() {
        valueLabel.setText("-");
    }
}
