package com.talosvfx.talos.editor.addons.scene.widgets.property;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Predicate;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.widgets.DataComponentFieldPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.GenericListPopup;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.runtime.scene.components.DataComponent;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.DataPropertiesUtil;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class PropertyPanelFieldWidget extends ButtonPropertyWidget<String> {
    private DataComponentFieldPopup<String> typeListPopUp;

    public PropertyPanelFieldWidget() {
        super("Add field");
        typeListPopUp = new DataComponentFieldPopup<>("Create new field");

        btnListener = new ButtonListener() {
            @Override
            public void clicked(ButtonPropertyWidget widget) {
                Vector2 pos = new Vector2(getWidth() / 2f, getHeight() / 2f);
                localToStageCoordinates(pos);

                Predicate<FilteredTree.Node<String>> filter = new Predicate<FilteredTree.Node<String>>() {
                    @Override
                    public boolean evaluate(FilteredTree.Node<String> arg0) {
                        return true;
                    }
                };

                typeListPopUp.showPopup(getStage(), DataPropertiesUtil.getListOfPrimitives(), pos, filter, new FilteredTree.ItemListener<String>() {
                    @Override
                    public void selected(FilteredTree.Node<String> node) {
                        super.selected(node);

                        DataComponent component = (DataComponent) getParentObject();
                        PropertyWrapper wrapper = DataPropertiesUtil.makeWrapper(node.getObject(), typeListPopUp.getFieldName());
                        component.getProperties().add(wrapper);
                        SceneUtils.componentUpdated(component.getGameObject().getGameObjectContainerRoot(), component.getGameObject(), component, false);

                        typeListPopUp.remove();
                    }
                });
            }
        };
    }
}
