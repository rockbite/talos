package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.*;
import com.talosvfx.talos.editor.addons.scene.utils.SkeletonAttachmentLoader;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class SkeletonComponent implements IComponent {

    public transient SkeletonBinary json;
    public transient Skeleton skeleton;
    public transient SkeletonData skeletonData;
    public transient AnimationStateData stateData;
    public transient AnimationState state;
    public transient SkeletonAttachmentLoader attachmentLoader = new SkeletonAttachmentLoader();

    public String path = "";

    public String skin;
    public String animation;
    public boolean loop = true;
    @ValueProperty(min=0.1f, max=2, step=0.01f,progress = true)
    public float timeScale = 1f;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        SpineMetadata spineMetadata = AssetImporter.readMetadataFor(Gdx.files.absolute(path), SpineMetadata.class);
        reloadData(spineMetadata.scale);

        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget skelWidget = new AssetSelectWidget("Skeleton Data", "skel", new Supplier<String>() {
            @Override
            public String get() {
                FileHandle fileHandle = Gdx.files.absolute(path);
                return fileHandle.path();
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                path = value;
                SpineMetadata spineMetadata = AssetImporter.readMetadataFor(Gdx.files.absolute(path), SpineMetadata.class);
                reloadData(spineMetadata.scale);
            }
        });

        SelectBoxWidget skinWidget = new SelectBoxWidget("Animation", new Supplier<String>() {
            @Override
            public String get () {
                return skin;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report (String value) {
                skin = value;
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get () {
                Array<Skin> skins = skeletonData.getSkins();
                Array<String> list = new Array<>();
                for(Skin skin : skins) {
                    list.add(skin.getName());
                }
                return list;
            }
        });

        SelectBoxWidget animationWidget = new SelectBoxWidget("Animation", new Supplier<String>() {
            @Override
            public String get () {
                return animation;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report (String value) {
                animation = value;
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get () {
                Array<Animation> animations = skeletonData.getAnimations();
                Array<String> list = new Array<>();
                for(Animation animation : animations) {
                    list.add(animation.getName());
                }
                return list;
            }
        });

        PropertyWidget loopWidget = WidgetFactory.generate(this, "loop", "Loop");
        PropertyWidget timeScaleWidget = WidgetFactory.generate(this, "timeScale", "Time Scl");

        properties.add(skelWidget);
        if(skeletonData != null) {
            properties.add(skinWidget);
            properties.add(animationWidget);
        }
        properties.add(loopWidget);
        properties.add(timeScaleWidget);

        return properties;
    }

    public void reloadData (float scale) {
        if(path != null) {
            FileHandle fileHandle = Gdx.files.absolute(path);
            if(fileHandle.exists()) {
                json = new SkeletonBinary(attachmentLoader);
                json.setScale(scale);
                skeletonData = json.readSkeletonData(fileHandle);
                skeleton = new Skeleton(skeletonData);
                stateData = new AnimationStateData(skeletonData);
                state = new AnimationState(stateData);
                state.setAnimation(0, skeletonData.getAnimations().first(), true); // todo: change this
            }
        }
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Skeleton Data";
    }

    @Override
    public int getPriority () {
        return 2;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    public void setAtlas (TextureAtlas textureAtlas) {
        if(attachmentLoader.atlas != textureAtlas) {
            attachmentLoader.setAtlas(textureAtlas);

            SpineMetadata spineMetadata = AssetImporter.readMetadataFor(Gdx.files.absolute(path), SpineMetadata.class);
            reloadData(spineMetadata.scale);
        }
    }
}
