package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;
import com.talosvfx.talos.runtime.scene.components.EdgeCollider2DComponent;

import java.util.function.Supplier;

public class EdgeCollider2DComponentProvider extends AComponentProvider<EdgeCollider2DComponent> {


    public EdgeCollider2DComponentProvider(EdgeCollider2DComponent component) {
        super(component);
    }

    @Override
    public Array<PropertyWidget> getListOfProperties() {
        Array<PropertyWidget> properties = new Array<>();

        properties.add(new LabelWidget("points", new Supplier<String>() {
            @Override
            public String get () {
                return component.points.size + "";
            }
        }, component));

        ButtonPropertyWidget<String> cleanButton = new ButtonPropertyWidget<String>("Reset", new ButtonPropertyWidget.ButtonListener() {
            @Override
            public void clicked (ButtonPropertyWidget widget) {
                component.setToNew();
            }
        });

        properties.add(cleanButton);

        PropertyWidget isClosedWidget = WidgetFactory.generate(component, "isClosed", "Is Closed");
        PropertyWidget edgeRadiusWidget = WidgetFactory.generate(component, "edgeRadius", "Edge Radius");

        properties.add(isClosedWidget);
        properties.add(edgeRadiusWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle() {
        return "EdgeCollider2D";
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
