package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.TalosSkeletonRenderer;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.BoneComponent;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class SkeletonComponentRenderer extends ComponentRenderer<SpineRendererComponent> {

    private final TalosSkeletonRenderer skeletonRenderer;


    public SkeletonComponentRenderer (GameObjectRenderer gameObjectRenderer) {
        super(gameObjectRenderer);
        skeletonRenderer = new TalosSkeletonRenderer();
    }

    @Override
    public void update (GameObject gameObject, SpineRendererComponent spineRendererComponent, float delta) {
        TransformComponent parentTransform = gameObject.getTransformComponent();

        spineRendererComponent.skeleton.setPosition(parentTransform.worldPosition.x, parentTransform.worldPosition.y);
        spineRendererComponent.skeleton.setScale(parentTransform.worldScale.x * spineRendererComponent.scale, parentTransform.worldScale.y * spineRendererComponent.scale);
        spineRendererComponent.skeleton.getRootBone().setRotation(parentTransform.rotation);

        if (!gameObjectRenderer.isSkipUpdates()) {
            spineRendererComponent.animationState.update(Gdx.graphics.getDeltaTime());
            spineRendererComponent.animationState.apply(spineRendererComponent.skeleton);
        }

        spineRendererComponent.skeleton.updateWorldTransform();
    }

    @Override
    public void render (Batch batch, Camera camera, GameObject gameObject, SpineRendererComponent rendererComponent) {
        TransformComponent parentTransform = gameObject.getTransformComponent();
        SpineRendererComponent spineRendererComponent = gameObject.getSpineComponent();

        GameAsset<SkeletonData> gameResource = rendererComponent.getGameResource();
        if (gameResource.isBroken()) {
            GameObjectRenderer.renderBrokenComponent(batch, gameObject, parentTransform);
            return;
        }


        spineRendererComponent.skeleton.getColor().set(spineRendererComponent.finalColor);
        skeletonRenderer.draw(batch, spineRendererComponent.skeleton);

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);




    }
}
