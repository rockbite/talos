package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class NumberPanel extends Table {

    private float value;
    private float minValue;
    private float maxValue;
    private float step = 0.01f;
    private Vector2 current = new Vector2();
    private Vector2 last = new Vector2();
    private Vector2 delta = new Vector2();
    private Vector2 tmp = new Vector2();

    private final Table editing;
    private final Table main;

    private Label valueLabel;
    private TextField textField;

    private boolean isSelected;
    private boolean isHover;
    private boolean isDragging = false;

    private Table content = new Table();

    private NumberPanel.NumberPanelListener numberPanelListener;

    public NumberPanel() {
        Stack mainStack = new Stack();
        mainStack.add(content);
        add(mainStack).grow();

        editing = new Table();
        main = new Table();
    }

    public void init() {
        Skin skin = TalosMain.Instance().getSkin();
        setSkin(skin);

        isSelected = false;

        valueLabel = new Label("", skin);
        textField = new TextField("0", getSkin(), "no-bg");

        Stack mainStack = new Stack();

        main.add(valueLabel).padRight(12).right().expandX();
        editing.add(textField).growX().padLeft(12);
        mainStack.add(editing);
        mainStack.add(main);

        hideEditMode();

        content.add(mainStack).height(32).growX();

        setTouchable(Touchable.enabled);

        setBackgrounds();

        addListener(new ClickListener() {

            private boolean dragged = false;

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
                last.set(x, y);

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                dragged = true;
                isDragging = true;
                super.touchDragged(event, x, y, pointer);

                current.set(x, y);
                delta.set(current.x, current.y);
                delta.sub(last);
                delta.scl(step);

                setValue(value + delta.x);
                if (numberPanelListener != null) {
                    numberPanelListener.dragged(value - delta.x, value);
                }

                last.set(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (numberPanelListener != null && isDragging) {
                    numberPanelListener.dragStop();
                }
                isDragging = false;
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
    }

    public void setListener (NumberPanelListener listener) {
        this.numberPanelListener = listener;
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
        try {
            float before = getValue();
            float after = Float.parseFloat(textField.getText());
            setValue(after);
            if (numberPanelListener != null) {
                numberPanelListener.typed(before, after);
            }
        } catch (NumberFormatException exception) {
            // keep prev value
        }

        textField.clearSelection();

        editing.setVisible(false);
        main.setVisible(true);

        isSelected = false;
        setBackgrounds();
    }

    private void setBackgrounds () {
        String shape = ColorLibrary.SHAPE_SQUIRCLE;

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

    public void setValue(float value) {
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
    }

    public void setRange(float min, float max) {
        this.minValue = min;
        this.maxValue = max;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public Float getValue () {
        return value;
    }

    public abstract static class NumberPanelListener {
        public abstract void typed(float before, float after);

        public abstract void dragged(float before, float after);

        public abstract void dragStop();
    }
}
