package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.math.MathUtils;
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
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.TextFieldWithZoom;

public class ValueWidget extends AbstractWidget<Float> {

    private Table editing;
    private Table main;
    private Table progressContainer;
    private Table progress;
    private LabelWithZoom label;
    private LabelWithZoom valueLabel;
    private TextField textField;
    private ClippedNinePatchDrawable progressDrawable;
    private ColorLibrary.BackgroundColor mainBgColor = ColorLibrary.BackgroundColor.LIGHT_GRAY;

    public void setMainColor(ColorLibrary.BackgroundColor color) {
        mainBgColor = color;
        setBackgrounds();
    }

    public enum Type {
        NORMAL, TOP, MID, BOTTOM
    }

    private Type type = Type.MID;
    private boolean isSelected;
    private boolean isHover;

    private float minValue = -9999;
    private float maxValue = 9999;
    private float step = 0.01f;

    private float value;

    private boolean showProgress;

    private Vector2 tmpVec = new Vector2();

    private boolean isDragging = false;

    private boolean isDisabled;

    public ValueWidget() {
        editing = new Table();
        main = new Table();
        progressContainer = new Table();
        progress = new Table();
    }

    public ValueWidget(Skin skin) {
        this();
        init(skin);
    }

    @Override
    public void init(Skin skin) {
        super.init(skin);

        type = Type.NORMAL;
        isSelected = false;

        label = new LabelWithZoom("", skin);
        valueLabel = new LabelWithZoom("", skin);
        textField = new TextFieldWithZoom("0", getSkin(), "no-bg");
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
                if(event.getTarget() == portBorder) return false;

                if(isDisabled) return false;
                dragged = false;
                lastPos = x;

                event.stop();

                super.touchDown(event, x, y, pointer, button);
                return true;
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

                setValue(value + change, true);

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
                    getStage().setKeyboardFocus(null);
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
        if(isDisabled) return;
        if(editing.isVisible()) return;

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
            setValue(Float.parseFloat(textField.getText()), true);
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

        ColorLibrary.BackgroundColor color = mainBgColor;

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
        setValue(value, false);
    }

    public void setValue(float value, boolean notify) {
        if(value > maxValue) value = maxValue;
        if(value < minValue) value = minValue;

        float precision = 1 / step;
        value = MathUtils.round((value) * precision) / precision;

        String text = value + "";
        this.value = value;

        if (MathUtils.round(step) == step) {
            text = MathUtils.round(value) + "";
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

        setValue(defaultValue, false);

        setLabel(text);
    }

    public boolean isFastChange () {
        return isDragging || isSelected;
    }

    @Override
    public Float getValue () {
        return value;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        setValue(jsonValue.asFloat(), false);
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

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public boolean isDisabled() {
        return isDisabled;
    }
}
