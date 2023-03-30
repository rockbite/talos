package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class CustomVector2Widget extends ATypeWidget<Vector2> {

    private final ValueWidget xWidget;
    private final ValueWidget yWidget;

    private Vector2 vec = new Vector2();

    @Override
    public boolean isFastChange() {
        return xWidget.isFastChange() || yWidget.isFastChange();
    }

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

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<Vector2> propertyWrapper) {
        xWidget.setValue(propertyWrapper.value.x);
        yWidget.setValue(propertyWrapper.value.y);
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<Vector2> propertyWrapper) {
        vec.set(xWidget.getValue(), yWidget.getValue());
        propertyWrapper.value.set(vec);
    }
}
