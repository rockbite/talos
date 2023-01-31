package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class Styles {
    public static SelectBox.SelectBoxStyle keyInputWidgetSelectBoxStyle = new SelectBox.SelectBoxStyle(SharedResources.skin.get(SelectBox.SelectBoxStyle.class)) {{
        font = SharedResources.skin.getFont("small-font");
        background = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY);
        backgroundOver = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY);
        backgroundOpen = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_TOP_2, ColorLibrary.BackgroundColor.LIGHT_BLUE);

        listStyle.font = SharedResources.skin.getFont("small-font");
        listStyle.selection = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.LIGHT_BLUE);
        listStyle.background = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY);

        scrollStyle.background = ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE_BOTTOM_2, ColorLibrary.BackgroundColor.ULTRA_DARK_GRAY);
    }};
}
