package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class SearchWidget extends Table {
    private static final float HEIGHT = 20f;
    private TextField textField;

    public SearchWidget () {
        Skin skin = TalosMain.Instance().getSkin();
        // add search icon
        Container<Image> searchIcon = new Container<>();
        searchIcon.setBackground(ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_LEFT, ColorLibrary.BackgroundColor.BLACK));
        Image icon = new Image(TalosMain.Instance().getSkin().newDrawable("search"), Scaling.fit);
        icon.setOrigin(icon.getWidth() / 2f, icon.getHeight() / 2f);
        icon.setScale(0.6f, 0.6f);
        searchIcon.setActor(icon);
        add(searchIcon).size(HEIGHT);

        textField = new TextField("", skin);
        TextField.TextFieldStyle style = textField.getStyle();
        TextField.TextFieldStyle newStyle = new TextField.TextFieldStyle();
        newStyle.font = style.font;
        newStyle.fontColor = style.fontColor;
        newStyle.disabledFontColor = style.disabledFontColor;
        newStyle.selection = style.selection;
        newStyle.background = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_RIGHT, ColorLibrary.BackgroundColor.BLACK);
        newStyle.cursor = style.cursor;
        newStyle.messageFont = style.messageFont;
        newStyle.messageFontColor = style.messageFontColor;
        textField.setStyle(newStyle);
        add(textField).height(HEIGHT);

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    textField.setProgrammaticChangeEvents(true);
                    textField.setText("");
                    textField.setProgrammaticChangeEvents(false);
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    public TextField getTextField() {
        return textField;
    }
}
