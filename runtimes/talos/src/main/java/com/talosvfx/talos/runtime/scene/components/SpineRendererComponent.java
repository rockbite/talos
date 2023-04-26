package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.spine.*;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.IColorHolder;
import com.talosvfx.talos.runtime.scene.ValueProperty;
import lombok.Getter;
import lombok.Setter;

public class SpineRendererComponent extends RendererComponent implements Json.Serializable, GameResourceOwner<SkeletonData>, IColorHolder {

    private transient GameAsset<SkeletonData> defaultGameAsset;
    private GameAsset<SkeletonData> gameAsset;

    public Skeleton skeleton;
    public AnimationState animationState;

    public Color color = new Color(Color.WHITE);
    public transient Color finalColor = new Color(Color.WHITE);
    public boolean shouldInheritParentColor = true;

    public transient String currAnimation;

    @ValueProperty(prefix = {"scale"})
    public float scale = 1f;

    @Getter@Setter
    private String skin;

    public boolean applyAnimation = true;

    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SKELETON;
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);

        if(currAnimation == null || currAnimation.isEmpty()) {
            if(animationState != null && animationState.getCurrent(0) != null && animationState.getCurrent(0).getAnimation() != null) {
                currAnimation = animationState.getCurrent(0).getAnimation().getName();
            }
        }

        json.writeValue("shouldInheritParentColor", shouldInheritParentColor);
        json.writeValue("scale", scale);
        json.writeValue("animation", currAnimation);
        json.writeValue("applyAnimation", applyAnimation);
        json.writeValue("skin", skin);
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        scale = jsonData.getFloat("scale", 1/128f);
        currAnimation = jsonData.getString("animation", "");
        applyAnimation = jsonData.getBoolean("applyAnimation", true);
        shouldInheritParentColor = jsonData.getBoolean("shouldInheritParentColor", true);
        skin = jsonData.getString("skin", null);

        loadSkeletonFromIdentifier(gameResourceIdentifier);

        if (skeleton != null) {
            if (skin != null) {
                Skin skinToApply = skeleton.getData().findSkin(skin);
                if (skinToApply != null) {
                    setAndUpdateSkin(skinToApply.getName());
                }
            } else {
                Skin skinToApply = skeleton.getData().getDefaultSkin();
                setAndUpdateSkin(skinToApply.getName());
            }


            if (applyAnimation) {
                if (!currAnimation.isEmpty()) {
                    Animation animation = skeleton.getData().findAnimation(currAnimation);
                    if (animation != null) {
                        animationState.setAnimation(0, animation, true);
                    }
                }
            }
        }

        super.read(json, jsonData);
    }

    private void loadSkeletonFromIdentifier (String gameResourceIdentifier) {
        GameAsset<SkeletonData> assetForIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SKELETON);
        setGameAsset(assetForIdentifier);
    }

    @Override
    public GameAsset<SkeletonData> getGameResource () {
        return gameAsset;
    }


    private void createSkeletonFromGameAsset () {
        if (!gameAsset.isBroken()) {
            skeleton = new Skeleton(gameAsset.getResource());
            AnimationStateData data = new AnimationStateData(skeleton.getData());
            animationState = new AnimationState(data);
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

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Color getFinalColor() {
        return finalColor;
    }

    @Override
    public boolean shouldInheritParentColor() {
        return shouldInheritParentColor;
    }

    public void setAndUpdateSkin (String value) {
        setSkin(value);
        skeleton.setSkin(value);
        skeleton.setSlotsToSetupPose();
        if (animationState != null) {
            animationState.apply(skeleton);
        }
    }
}
