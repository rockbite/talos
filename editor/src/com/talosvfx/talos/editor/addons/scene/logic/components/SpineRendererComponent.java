package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.function.Supplier;

public class SpineRendererComponent extends RendererComponent implements Json.Serializable, GameResourceOwner<SkeletonData> {

    private GameAsset<SkeletonData> gameAsset;

    public Skeleton skeleton;
    public AnimationState animationState;

    public float scale = 1/128f;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget<SkeletonData> atlasWidget = new AssetSelectWidget<>("Skeleton", GameAssetType.SKELETON, new Supplier<GameAsset<SkeletonData>>() {
            @Override
            public GameAsset<SkeletonData> get () {
                return gameAsset;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<SkeletonData>>() {
            @Override
            public void report (GameAsset<SkeletonData> value) {
                setGameAsset(value);
            }
        });

        properties.add(atlasWidget);
        properties.add(new FloatPropertyWidget("scale", new Supplier<Float>() {
            @Override
            public Float get () {
                return scale;
            }
        }, new PropertyWidget.ValueChanged<Float>() {
            @Override
            public void report (Float value) {
                scale = value;
                skeleton.setScale(scale, scale);
            }
        }));

        Array<PropertyWidget> superList = super.getListOfProperties();
        properties.addAll(superList);

        return properties;
    }


    @Override
    public String getPropertyBoxTitle () {
        return "Spine Renderer";
    }

    @Override
    public int getPriority () {
        return 3;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SKELETON;
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);

        json.writeValue("scale", scale);
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        scale = jsonData.getFloat("scale", 1/128f);
        loadSkeletonFromIdentifier(gameResourceIdentifier);


        super.read(json, jsonData);
    }

    private void loadSkeletonFromIdentifier (String gameResourceIdentifier) {
        GameAsset<SkeletonData> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SKELETON);
        setGameAsset(assetForIdentifier);
    }

    @Override
    public GameAsset<SkeletonData> getGameResource () {
        return gameAsset;
    }


    private void createSkeletonFromGameAsset () {
        if (!gameAsset.isBroken()) {
            skeleton = new Skeleton(gameAsset.getResource());
            animationState = new AnimationState(new AnimationStateData(skeleton.getData()));
            skeleton.setScale(this.scale, this.scale);

            Array<Animation> animations = skeleton.getData().getAnimations();
            if (animations.size > 0) {
                animationState.setAnimation(0, animations.peek(), true);
            }

        }
    }

    @Override
    public void setGameAsset (GameAsset<SkeletonData> gameAsset) {
        this.gameAsset = gameAsset;
        this.gameAsset.listeners.add(new GameAsset.GameAssetUpdateListener() {
            @Override
            public void onUpdate () {
                createSkeletonFromGameAsset();
            }
        });
        createSkeletonFromGameAsset();
    }

    Vector2 vec = new Vector2();
    @Override
    public void minMaxBounds (GameObject ownerEntity, BoundingBox boundingBox) {
        TransformComponent transformComponent = ownerEntity.getComponent(TransformComponent.class);
        if (transformComponent != null) {
            vec.set(0, 0);
            transformComponent.localToWorld(ownerEntity, vec);

            //todo. do it properly
            float width = transformComponent.scale.x * 1;
            float height = transformComponent.scale.y * 1;

            boundingBox.ext(vec.x - width/2, vec.y - height/2, 0);
            boundingBox.ext(vec.x + width/2, vec.y + height/2, 0);
        }
    }

}
