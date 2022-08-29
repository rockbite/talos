package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

public class CameraComponent extends AComponent {

    public Color backgroundColor = new Color(Color.valueOf("1e3357ff"));

    @ValueProperty(min=0.01f, max=10f, step=0.01f)
    public float zoom = 1f;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 size = new Vector2(6, 4);

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        PropertyWidget colorWidget = WidgetFactory.generate(this, "backgroundColor", "Background Color");
        PropertyWidget zoomWidget = WidgetFactory.generate(this, "zoom", "Zoom");
        PropertyWidget sizeWidget = WidgetFactory.generate(this, "size", "Size");

        properties.add(colorWidget);
        properties.add(zoomWidget);
        properties.add(sizeWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Camera";
    }

    @Override
    public int getPriority () {
        return 2;
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    @Override
    public PropertyOptionType[] getOptions() {
        return PropertyOptionType.RESET_REMOVE_OPTION;
    }

    @Override
    public void reset() {
        super.reset();
        zoom = 1;
        size.set(6,6);
        backgroundColor.set(Color.valueOf("1e3357ff"));
    }
}
