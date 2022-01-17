package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.assets.TalosAssetProvider;

public class ButtonWidget extends PropertyWidget<ClickListener>{

    private TextButton button;
    private final String buttonName;

    public ButtonWidget(String text) {
        super(null);
        buttonName = text;
    }

    @Override
    public ClickListener getValue() {
        return button.getClickListener();
    }

    @Override
    public Actor getSubWidget() {
        button = new TextButton(buttonName, TalosMain.Instance().getSkin());
        return button;
    }

    @Override
    public void updateWidget(ClickListener value) {
        button.clearListeners();
        button.addListener(value);
    }
}
