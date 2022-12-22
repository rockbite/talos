package com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers;

import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.*;
import lombok.Getter;

public enum PropertyType {

    FLOAT(PropertyFloatWrapper.class, CustomFloatWidget.class),
    VECTOR2(PropertyVec2Wrapper.class, CustomVector2Widget.class),
    COLOR(PropertyColorWrapper.class, CustomColorWidget.class),
    ASSET(PropertyGameAssetWrapper.class, CustomAssetWidget.class);
    //BOOLEAN(),
    //STRING(),
    //GAME_OBJECT();

    @Getter
    private final Class<? extends PropertyWrapper<?>> wrapperClass;
    @Getter
    private final Class<? extends ATypeWidget> widgetClass;

    PropertyType(Class<? extends PropertyWrapper<?>> wrapperClass, Class<? extends ATypeWidget> widgetClass) {
        this.wrapperClass = wrapperClass;
        this.widgetClass = widgetClass;
    }
}
