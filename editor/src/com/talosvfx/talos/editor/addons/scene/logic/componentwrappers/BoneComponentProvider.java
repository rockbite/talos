package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.scene.components.BoneComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class BoneComponentProvider extends AComponentProvider<BoneComponent> {

    public BoneComponentProvider (BoneComponent component) {
        super(component);
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        if (component.getGameObject().hasComponent(TransformComponent.class)) {

            TransformComponent transformComponent = component.getGameObject().getComponent(TransformComponent.class);

            PropertyWidget positionWidget = WidgetFactory.generate(transformComponent, "worldPosition", "World Position");
            PropertyWidget rotationWidget = WidgetFactory.generate(transformComponent, "worldRotation", "World Rotation");
            PropertyWidget scaleWidget = WidgetFactory.generate(transformComponent, "worldScale", "World Scale");

            positionWidget.setReadOnly();
            rotationWidget.setReadOnly();
            scaleWidget.setReadOnly();

            properties.add(positionWidget);
            properties.add(rotationWidget);
            properties.add(scaleWidget);

        }


        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Bone";
    }

    @Override
    public int getPriority () {
        return 10;
    }

}
