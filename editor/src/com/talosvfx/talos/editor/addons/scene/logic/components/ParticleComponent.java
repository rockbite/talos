package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.project.ProjectController;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;

import java.util.function.Supplier;

public class ParticleComponent extends RendererComponent implements GameResourceOwner<ParticleEffectDescriptor> {

    public GameAsset<ParticleEffectDescriptor> gameAsset;
    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget<ParticleEffectDescriptor> descriptorWidget = new AssetSelectWidget<>("Effect", GameAssetType.VFX, new Supplier<GameAsset<ParticleEffectDescriptor>>() {
            @Override
            public GameAsset<ParticleEffectDescriptor> get() {
                return gameAsset;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<ParticleEffectDescriptor>>() {
            @Override
            public void report(GameAsset<ParticleEffectDescriptor> value) {
                setGameAsset(value);
            }
        });

        ButtonPropertyWidget<String> linkedToWidget = new ButtonPropertyWidget<String>("Effect Project", "Edit", new ButtonPropertyWidget.ButtonListener<String>() {
            @Override
            public void clicked(ButtonPropertyWidget<String> widget) {
                //Edit this tls
                RawAsset rootRawAsset = getGameResource().getRootRawAsset();
                TalosMain.Instance().ProjectController().setProject(ProjectController.TLS);
                TalosMain.Instance().ProjectController().loadProject(rootRawAsset.handle);
            }
        }, new Supplier<String>() {
            @Override
            public String get() {
//                return linkedTo;
                return "";
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
//                linkedTo = value;
            }
        });

        properties.add(descriptorWidget);
        properties.add(linkedToWidget);
        properties.addAll(super.getListOfProperties());

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Particle Effect";
    }

    @Override
    public int getPriority () {
        return 2;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {

        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        loadDescriptorFromIdentifier(gameResourceIdentifier);

        super.read(json, jsonData);
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);
        super.write(json);
    }

    GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate () {
            if (gameAsset.isBroken()) {

            } else {
                //Its ok
            }
        }
    };

    private void loadDescriptorFromIdentifier (String gameResourceIdentifier) {
        GameAsset<ParticleEffectDescriptor> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.VFX);
        setGameAsset(assetForIdentifier);
    }



    @Override
    public GameAsset<ParticleEffectDescriptor> getGameResource () {
        return this.gameAsset;
    }

    @Override
    public void setGameAsset (GameAsset<ParticleEffectDescriptor> newGameAsset) {
        if (this.gameAsset != null) {
            //Remove from old game asset, it might be the same, but it may also have changed
            this.gameAsset.listeners.removeValue(gameAssetUpdateListener, true);
        }

        this.gameAsset = newGameAsset;
        this.gameAsset.listeners.add(gameAssetUpdateListener);

        gameAssetUpdateListener.onUpdate();
    }
}
