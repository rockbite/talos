package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
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
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class SpineRendererComponent extends RendererComponent implements Json.Serializable, GameResourceOwner<SkeletonData> {

    private transient GameAsset<SkeletonData> defaultGameAsset;
    private GameAsset<SkeletonData> gameAsset;

    public Skeleton skeleton;
    public AnimationState animationState;

    public Color color = new Color(Color.WHITE);


    @ValueProperty(prefix = {"scale"})
    public float scale = 1f;


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

        properties.add(WidgetFactory.generate(this, "scale", "Scale"));

        PropertyWidget colorWidget = WidgetFactory.generate(this, "color", "Color");
        properties.add(colorWidget);

        SelectBoxWidget animSelectWidget = new SelectBoxWidget("Animation", new Supplier<String>() {
            @Override
            public String get() {
                if(animationState != null && animationState.getCurrent(0) != null && animationState.getCurrent(0).getAnimation() != null) {
                    return animationState.getCurrent(0).getAnimation().getName();
                } else {
                    return "";
                }
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                Animation animation = skeleton.getData().findAnimation(value);
                animationState.setAnimation(0, animation, true);
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                Array<String> names = new Array<>();
                if(skeleton == null || skeleton.getData() == null) {
                    return names;
                }
                Array<Animation> animations = skeleton.getData().getAnimations();
                for(Animation animation: animations) {
                    names.add(animation.getName());
                }
                return names;
            }
        });
        properties.add(animSelectWidget);


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

        if(defaultGameAsset == null && !gameAsset.isBroken()){
            defaultGameAsset = gameAsset;
        }

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

            skeleton.setSlotsToSetupPose();
            skeleton.setBonesToSetupPose();
            skeleton.updateWorldTransform();


            Vector2 offset = new Vector2();
            Vector2 size = offset;
            FloatArray floatArray = new FloatArray();
            skeleton.getBounds(offset, size, floatArray);

            //todo. do it properly
            float width = transformComponent.scale.x * size.x;
            float height = transformComponent.scale.y * size.y;

            boundingBox.ext(-width/2, - height/2, 0);
            boundingBox.ext(width/2, + height/2, 0);
        }
    }

    @Override
    public void reset() {
        super.reset();
        scale = 1f;
        if(this.defaultGameAsset != null){
            setGameAsset(gameAsset);
        }
    }

}
