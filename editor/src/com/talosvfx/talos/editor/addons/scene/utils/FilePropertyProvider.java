package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.Locale;

public class FilePropertyProvider implements IPropertyProvider {

    private FileHandle fileHandle;

    public FilePropertyProvider(FileHandle fileHandle) {
        this.fileHandle = fileHandle;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        ButtonPropertyWidget<String> linkedToWidget = new ButtonPropertyWidget<String>(fileHandle.name(), "Open", new ButtonPropertyWidget.ButtonListener<String>() {
            @Override
            public void clicked (ButtonPropertyWidget<String> widget) {
                AssetImporter.fileOpen(fileHandle);
            }
        });
        properties.add(linkedToWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return fileHandle.nameWithoutExtension();
    }

    @Override
    public int getPriority () {
        return -2;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }
}
