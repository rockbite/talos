package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelFieldWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;
import com.talosvfx.talos.runtime.scene.components.DataComponent;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class DataComponentProvider extends AComponentProvider<DataComponent> {

    public DataComponentProvider(DataComponent component) {
        super(component);
    }

    @Override
    public Array<PropertyWidget> getListOfProperties() {
        Array<PropertyWidget> properties = new Array<>();

        for (PropertyWrapper<?> property : component.getProperties()) {
            PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(property);
            generate.setParent(component);
            generate.defaults().pad(5);
            SquareButton deleteProperty = new SquareButton(SharedResources.skin, new Label("-", SharedResources.skin), "Delete property");
            deleteProperty.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                   component.getProperties().removeValue(property, true);

                    SceneUtils.componentUpdated(component.getGameObject().getGameObjectContainerRoot(), component.getGameObject(), component, false);
                }
            });
            generate.add(deleteProperty);

            properties.add(generate);
        }

		ButtonPropertyWidget buttonPropertyWidget = new PropertyPanelFieldWidget();
        buttonPropertyWidget.setParent(component);
		properties.add(buttonPropertyWidget);
		return properties;
	}

    @Override
    public String getPropertyBoxTitle() {
        return "Data Component";
    }

    @Override
    public int getPriority() {
        return 4;
    }
}
