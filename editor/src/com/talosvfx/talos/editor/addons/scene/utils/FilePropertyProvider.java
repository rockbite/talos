package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;
import java.util.regex.Pattern;

public class FilePropertyProvider implements IPropertyProvider {

    private static final Pattern imageRegExp = Pattern.compile("(?i)(jpe?g|png|gif|bmp)");

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
        properties.add(nameWidget);

        LabelWidget sizeWidget = new LabelWidget("Size", new Supplier<String>() {
            @Override
            public String get() {
                int kb = 1024;
                int mb = 1024 * 1024;
                int gb = 1024 * 1024 * 1024;
                if (fileHandle.length() >= gb) {
                    return (fileHandle.length() / gb) + " gb";
                } else if (fileHandle.length() >= mb) {
                    return (fileHandle.length() / mb) + " mb";
                } else if (fileHandle.length() >= kb) {
                    return (fileHandle.length() / kb) + " kb";
                }
                return fileHandle.length() + " bytes";
            }
        });
        properties.add(sizeWidget);

        // show dimensions, if we're working with an image
        String ext = fileHandle.extension();
        boolean isImage = imageRegExp.matcher(ext).matches();
        if (isImage) {
             LabelWidget dimensionsWidget = new LabelWidget("Dimensions", new Supplier<String>() {
                @Override
                public String get() {
                    Texture texture = new Texture(fileHandle);
                    return texture.getWidth() + " x " + texture.getHeight();
                }
            });
            properties.add(dimensionsWidget);
        }

        ButtonPropertyWidget<String> actionWidget = new ButtonPropertyWidget<String>("action", "Open", new ButtonPropertyWidget.ButtonListener<String>() {
            @Override
            public void clicked (ButtonPropertyWidget<String> widget) {
                AssetImporter.fileOpen(fileHandle);
            }
        });
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

    @Override
    public Array<PropertyOption> getOptionsList() {
        return null;
    }
}
