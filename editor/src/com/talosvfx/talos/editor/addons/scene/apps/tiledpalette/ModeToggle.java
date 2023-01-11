package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class ModeToggle extends Table {
    private static final ColorLibrary.BackgroundColor btnBgUp = ColorLibrary.BackgroundColor.PALETTE_TOGGLE_UP;
    private static final ColorLibrary.BackgroundColor btnBgDown = ColorLibrary.BackgroundColor.PALETTE_TOGGLE_DOWN;
    private static final ColorLibrary.BackgroundColor btnBgChecked = ColorLibrary.BackgroundColor.PALETTE_TOGGLE_DOWN;
    private static final ColorLibrary.BackgroundColor btnBgOver = ColorLibrary.BackgroundColor.PALETTE_TOGGLE_HOVER;

    private TextButton tileBtn;
    private TextButton entityBtn;

    public ModeToggle () {
        Skin skin = SharedResources.skin;

        defaults().padLeft(8).padRight(8);
        setBackground(skin.newDrawable("square-bordered"));

        Label modeLabel = new Label("Import Mode", skin);

        // Left button
        tileBtn = new TextButton("Tile", skin);
        TextButton.TextButtonStyle leftBtnStyle = getButtonStyle(skin);
        tileBtn.setStyle(leftBtnStyle);

        // Right button
        TextButton.TextButtonStyle toggleBtnStyleRight = getButtonStyle(skin);
        toggleBtnStyleRight.up = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_RIGHT_2, btnBgUp);
        toggleBtnStyleRight.over = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_RIGHT_2, btnBgOver);
        entityBtn = new TextButton("Entity", skin);
        entityBtn.setStyle(toggleBtnStyleRight);

        add(modeLabel).left();
        Table grayTable = new Table(); // just for visual thing
        grayTable.setBackground(skin.newDrawable("square-patch", Color.valueOf("#333333")));
        grayTable.add(tileBtn).size(92, 24);
        grayTable.add(entityBtn).size(92, 24);
        add(grayTable).expandX().right();

        // visual toggle
        tileBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tileBtn.isChecked()) {
                    entityBtn.setChecked(false);
                }
            }
        });
        entityBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (entityBtn.isChecked()) {
                    tileBtn.setChecked(false);
                }
            }
        });
    }

    private TextButton.TextButtonStyle getButtonStyle (Skin skin) {
        BitmapFont font = skin.getFont("default-font");
        Color btnFontUp = Color.valueOf("#ffffff99");
        Color btnFontChecked = Color.valueOf("#81cdff");
        Color btnFontOver = Color.valueOf("#81cdffcc");

        TextButton.TextButtonStyle toggleBtnStyle = new TextButton.TextButtonStyle();
        toggleBtnStyle.up = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_LEFT_2, btnBgUp);
        toggleBtnStyle.down = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_2, btnBgDown);
        toggleBtnStyle.checked = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_2, btnBgChecked);
        toggleBtnStyle.over = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_LEFT_2, btnBgOver);
        toggleBtnStyle.font = font;
        toggleBtnStyle.fontColor = btnFontUp;
        toggleBtnStyle.downFontColor = btnFontChecked;
        toggleBtnStyle.checkedFontColor = btnFontChecked;
        toggleBtnStyle.overFontColor = btnFontOver;

        return toggleBtnStyle;
    }

    public TextButton getTileBtn() {
        return tileBtn;
    }

    public TextButton getEntityBtn() {
        return entityBtn;
    }
}
