package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.Locale;
import java.util.function.Supplier;

public class FilePropertyProvider implements IPropertyProvider {

    private FileHandle fileHandle;

    public FilePropertyProvider(FileHandle fileHandle) {
        this.fileHandle = fileHandle;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        LabelWidget nameWidget = new LabelWidget("Name", new Supplier<String>() {
            @Override
            public String get() {
                return fileHandle.name();
            }
        });

        ButtonPropertyWidget<String> actionWidget = new ButtonPropertyWidget<String>("action", "Open", new ButtonPropertyWidget.ButtonListener<String>() {
            @Override
            public void clicked (ButtonPropertyWidget<String> widget) {
                AssetImporter.fileOpen(fileHandle);
            }
        });

        properties.add(nameWidget);
        properties.add(actionWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "File";
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
