package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.function.Supplier;

public class ScriptComponent extends AComponent {

    String path;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget widget = new AssetSelectWidget("Script", "js", new Supplier<String>() {
            @Override
            public String get() {
                return path;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                path = value;
            }
        });

        properties.add(widget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Script Component";
    }

    @Override
    public int getPriority () {
        return 4;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    public void setScript (FileHandle handle) {
        path = AssetImporter.relative(handle);
    }

    @Override
    public boolean notifyAssetPathChanged (String oldPath, String newPath) {
        if(path.equals(oldPath)) {
            path = newPath;
            return true;
        }

        return false;
    }
}
