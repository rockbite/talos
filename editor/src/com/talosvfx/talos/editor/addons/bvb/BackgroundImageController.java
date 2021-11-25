package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

public class BackgroundImageController implements IPropertyProvider {

    public float imageWidth;
    public float xOffset;
    public float yOffset;

    @Override
    public Array<PropertyWidget> getListOfProperties() {
        Array<PropertyWidget> propertyWidgetArrayList = new Array<>();
        FloatPropertyWidget scaleWidget = new FloatPropertyWidget("image width") {
            @Override
            public Float getValue() {
                return imageWidth;
            }

            @Override
            public void valueChanged(Float value) {
                imageWidth = value;
            }
        };

        FloatPropertyWidget xOffsetWidget = new FloatPropertyWidget("center position X") {
            @Override
            public Float getValue() {
                return xOffset;
            }

            @Override
            public void valueChanged(Float value) {
                xOffset = value;
            }
        };

        FloatPropertyWidget yOffsetWidget = new FloatPropertyWidget("center position Y") {
            @Override
            public Float getValue() {
                return yOffset;
            }

            @Override
            public void valueChanged(Float value) {
                yOffset = value;
            }
        };

        ButtonWid

        propertyWidgetArrayList.add(scaleWidget, xOffsetWidget, yOffsetWidget);
        return propertyWidgetArrayList;
    }

    @Override
    public String getPropertyBoxTitle() {
        return "Background Image";
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
