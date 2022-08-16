package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.widgets.ClippedNinePatchDrawable;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class ValueWidget extends AbstractWidget<Float> {

    private final Table editing;
    private final Table main;
    private final Table progressContainer;
    private final Table progress;

    private Label label;
    private Label valueLabel;
    private TextField textField;
    private ClippedNinePatchDrawable progressDrawable;
    private Actor focusedKeyboard;

    public enum Type {
        NORMAL, TOP, MID, BOTTOM
    }

    private Type type = Type.MID;
    private boolean isSelected;
    private boolean isHover;

    private float minValue;
    private float maxValue;
    private float step = 0.01f;

    private float value;

    private boolean showProgress;

    private Vector2 tmpVec = new Vector2();

    private boolean isDragging = false;

    public ValueWidget() {
        editing = new Table();
        main = new Table();
        progressContainer = new Table();
        progress = new Table();

    }

    @Override
    public void init(Skin skin) {
        super.init(skin);

        type = Type.NORMAL;
        isSelected = false;

        label = new Label("", skin);
        valueLabel = new Label("", skin);
        textField = new TextField("0", getSkin(), "no-bg");
        progressDrawable = ColorLibrary.createClippedPatch(skin, getShape(), ColorLibrary.BackgroundColor.LIGHT_BLUE);

        Stack mainStack = new Stack();

        editing.add(textField).growX().padLeft(12);

        main.add(label).padLeft(12).left().expandX();
        main.add(valueLabel).padRight(12).right().expandX();

        progress.setBackground(progressDrawable);
        updateProgress();

        mainStack.add(editing);
        mainStack.add(progressContainer);
        mainStack.add(main);

        progressContainer.add(progress).grow().left();

        if(!showProgress) {
            progressContainer.setVisible(false);
        }

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
                if(pointer == -1 && !isDragging) {
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
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                dragged = true;
                isDragging = true;
                super.touchDragged(event, x, y, pointer);

                float diff = x - lastPos;

                float localStep = step;
                // if we are showing progress, step is auto calculated
                if(showProgress) {
                    localStep = (maxValue - minValue)/getWidth();
                }

                float change = diff * localStep;

                setValue(value + change);

                lastPos = x;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (isDragging) {
                    isDragging = false;
                    setValue(value, true);
                }
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
    }

    private void showEditMode() {
        if(editing.isVisible()) return;

        focusedKeyboard = getStage().getKeyboardFocus();
        getStage().setKeyboardFocus(textField);
        textField.selectAll();

        editing.setVisible(true);
        main.setVisible(false);
        progressContainer.setVisible(false);

        isSelected = true;
        setBackgrounds();
    }

    private void hideEditMode() {
        try {
            setValue(Float.parseFloat(textField.getText()));
        } catch (NumberFormatException exception) {
            // keep prev value
        }

        textField.clearSelection();

        editing.setVisible(false);
        main.setVisible(true);
        if(showProgress) {
            progressContainer.setVisible(true);
        }

        isSelected = false;
        setBackgrounds();
    }

    private String getShape() {
        String shape = ColorLibrary.SHAPE_SQUIRCLE;
        if (type == Type.TOP) {
            shape = ColorLibrary.SHAPE_SQUIRCLE_TOP;
        } else if (type == Type.BOTTOM) {
            shape = ColorLibrary.SHAPE_SQUIRCLE_BOTTOM;
        } else if (type == Type.MID) {
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
        progressDrawable = ColorLibrary.createClippedPatch(getSkin(), shape, ColorLibrary.BackgroundColor.LIGHT_BLUE);
        progress.setBackground(progressDrawable);
        updateProgress();
    }

    public void setLabel(String text) {
        label.setText(text);
    }

    public void setValue(float value) {
        setValue(value, isChanged(value));
    }

    public void setValue(float value, boolean notify) {
        if(value > maxValue) value = maxValue;
        if(value < minValue) value = minValue;

        this.value = value;

        String text = "";
        if (step < 1) {
            text = String.format("%.3f", value);
        } else if (step == 1) {
            text = Math.round(value) + "";
        }

        valueLabel.setText(text);
        textField.setText(text);

        updateProgress();

        if (notify) {
            fireChangedEvent();
        }
    }

    private void updateProgress() {
        float alpha = (value - minValue)/(maxValue - minValue);
        progressDrawable.setMaskScale(alpha, 1f);
    }

    public void setRange(float min, float max) {
        this.minValue = min;
        this.maxValue = max;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
        if(showProgress) {
            progressContainer.setVisible(true);
        }
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
        String text = element.getText();
        float defaultValue = element.getFloatAttribute("default", 0);
        float min = element.getFloatAttribute("min", 0);
        float max = element.getFloatAttribute("max", 9999);
        float step = element.getFloatAttribute("step", 0.01f);
        boolean progress = element.getBooleanAttribute("progress", false);

        setRange(min, max);
        setStep(step);
        setShowProgress(progress);

        setValue(defaultValue);

        setLabel(text);
    }

    public boolean isFastChange () {
        return isDragging;
    }

    @Override
    public Float getValue () {
        return value;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        setValue(jsonValue.asFloat());
    }

    @Override
    public void write (Json json, String name) {
        json.writeValue(name, getValue());
    }

    public void setType(Type type) {
        this.type = type;
        setBackgrounds();
    }

    public void setNone() {
        valueLabel.setText("-");
    }
}
