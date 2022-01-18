package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

public class BackgroundImageController implements IPropertyProvider {

    public float imageWidth;
    public float xOffset;
    public float yOffset;

    @Override
    public Array<PropertyWidget> getListOfProperties() {
        Array<PropertyWidget> propertyWidgetArrayList = new Array<>();
        PropertyWidget scaleWidget = WidgetFactory.generate(this, "imageWidth", "Image Width");
        PropertyWidget xOffsetWidget = WidgetFactory.generate(this, "xOffset", "Center Position X");
        PropertyWidget yOffsetWidget = WidgetFactory.generate(this, "yOffset", "Center Position Y");

        ButtonPropertyWidget<String> deleteWidget = new ButtonPropertyWidget<String>("Delete Background Image", "Delete", new ButtonPropertyWidget.ButtonListener<String>() {
            @Override
            public void clicked (ButtonPropertyWidget<String> widget) {
                BvBWorkspace.getInstance().removePreviewImage();
            }
        });


        propertyWidgetArrayList.add(scaleWidget, xOffsetWidget, yOffsetWidget, deleteWidget);
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

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }
}
