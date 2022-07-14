package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.UUID;

public abstract class AMetadata implements IPropertyProvider, IPropertyHolder, Json.Serializable {

    public transient FileHandle currentFile;

    public UUID uuid;

    public AMetadata() {
        uuid = UUID.randomUUID();
    }

    public void setFile(FileHandle currentFile) {
        this.currentFile = currentFile;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        return null;
    }

    @Override
    public String getPropertyBoxTitle () {
        return null;
    }

    @Override
    public int getPriority () {
        return 1;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    @Override
    public Iterable<IPropertyProvider> getPropertyProviders () {
        Array<IPropertyProvider> propertyProviders = new Array<>();

        propertyProviders.add(new FilePropertyProvider(currentFile));

        if(getPropertyBoxTitle() != null) {
            propertyProviders.add(this);
        }

        return propertyProviders;
    }

    @Override
    public void write (Json json) {
        json.writeValue("uuid", uuid.toString());
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        uuid = UUID.fromString(jsonData.getString("uuid"));
    }
}