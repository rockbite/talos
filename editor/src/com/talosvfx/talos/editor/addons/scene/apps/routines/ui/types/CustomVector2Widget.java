package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class CustomVector2Widget extends ATypeWidget {

    private final ValueWidget xWidget;
    private final ValueWidget yWidget;

    public CustomVector2Widget() {
        xWidget = new ValueWidget();
        xWidget.init(SharedResources.skin);
        xWidget.setMainColor(ColorLibrary.BackgroundColor.BLACK_TRANSPARENT);
        xWidget.setLabel("X:");
        xWidget.setType(ValueWidget.Type.TOP);
        add(xWidget).padLeft(4).padRight(4).width(220).padTop(9);
        row();
        yWidget = new ValueWidget();
        yWidget.init(SharedResources.skin);
        yWidget.setMainColor(ColorLibrary.BackgroundColor.BLACK_TRANSPARENT);
        yWidget.setLabel("Y:");
        yWidget.setType(ValueWidget.Type.BOTTOM);
        add(yWidget).padLeft(4).padRight(4).width(220);
        row();

        add().padBottom(10);
    }

    @Override
    public String getTypeName() {
        return "vector2";
    }
}
