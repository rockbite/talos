package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

import java.util.function.Supplier;

public class ButtonPropertyWidget<T> extends PropertyWidget<T> {

    private T payload;
    private Label buttonLabel;
    private SquareButton button;
    private ButtonListener btnListener;

    public interface ButtonListener<T> {
        void clicked(ButtonPropertyWidget<T> widget);
    }

    public ButtonPropertyWidget() {
        super();
    }

    @Override
    public PropertyWidget clone() {
        ButtonPropertyWidget clone = (ButtonPropertyWidget) super.clone();
        clone.btnListener = this.btnListener;
        clone.buttonLabel.setText(buttonLabel.getText());

        return clone;
    }

    public ButtonPropertyWidget(String text, ButtonListener btnListener) {
        this(null, text, btnListener);
    }

    public ButtonPropertyWidget(String name, String text, ButtonListener btnListener) {
        this(name, text, btnListener, new Supplier<T>() {
            @Override
            public T get () {
                return null;
            }
        }, new ValueChanged<T>() {
            @Override
            public void report (T value) {

            }
        });
    }

    public ButtonPropertyWidget(String name, String text, ButtonListener btnListener, Supplier<T> supplier, ValueChanged<T> valueChanged) {
        super(name, supplier, valueChanged);
        setButtonText(text);
        this.btnListener = btnListener;
    }

    @Override
    public T getValue () {
        return payload;
    }

    @Override
    public void updateWidget (T value) {
        payload = value;
    }

    @Override
    public Actor getSubWidget () {
        Skin skin = SharedResources.skin;
        Table table = new Table();

        buttonLabel = new LabelWithZoom("Edit", skin);
        button = new SquareButton(skin, buttonLabel, "Edit");

        table.add(button).expand().right().growX();

        button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if(btnListener != null) {
                    btnListener.clicked(ButtonPropertyWidget.this);
                }
            }
        });

        return table;
    }

    public void externalDataChange(T payload) {
        callValueChanged(payload);
    }

    public void setButtonText(String text) {
        buttonLabel.setText(text);
    }
}
