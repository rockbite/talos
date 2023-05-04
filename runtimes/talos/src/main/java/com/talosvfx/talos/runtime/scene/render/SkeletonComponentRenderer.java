package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.esotericsoftware.spine.TalosSkeletonRenderer;
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
	public void render (Batch batch, Camera camera, GameObject gameObject, SpineRendererComponent rendererComponent) {
		TransformComponent parentTransform = gameObject.getComponent(TransformComponent.class);
		SpineRendererComponent spineRendererComponent = gameObject.getComponent(SpineRendererComponent.class);

		spineRendererComponent.skeleton.setPosition(parentTransform.worldPosition.x, parentTransform.worldPosition.y);
		spineRendererComponent.skeleton.setScale(parentTransform.worldScale.x * spineRendererComponent.scale, parentTransform.worldScale.y * spineRendererComponent.scale);
		spineRendererComponent.skeleton.getRootBone().setRotation(parentTransform.rotation);

		if (!gameObjectRenderer.isSkipUpdates()) {
			spineRendererComponent.animationState.update(Gdx.graphics.getDeltaTime());
			spineRendererComponent.animationState.apply(spineRendererComponent.skeleton);
		}
		spineRendererComponent.skeleton.updateWorldTransform();

		spineRendererComponent.skeleton.getColor().set(spineRendererComponent.finalColor);
		skeletonRenderer.draw(batch, spineRendererComponent.skeleton);

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		// update bone game objects
		Array<GameObject> children = new Array<>();
		Array<GameObject> boneGOs = gameObject.getChildrenByComponent(BoneComponent.class, children);

		for (GameObject boneGO : boneGOs) {
			BoneComponent boneComponent = boneGO.getComponent(BoneComponent.class);
			Bone bone =  boneComponent.getBone();
			TransformComponent transform = boneGO.getComponent(TransformComponent.class);

//
			transform.worldScale.set(bone.getWorldScaleX(), bone.getWorldScaleY());
			transform.scale.x = bone.getWorldScaleX() / parentTransform.worldScale.x;
			transform.scale.y = bone.getWorldScaleY() / parentTransform.worldScale.y;

			transform.worldRotation = bone.localToWorldRotation(bone.getRotation());
			transform.rotation = transform.worldRotation - parentTransform.worldRotation;


			transform.worldPosition.set(bone.getWorldX(), bone.getWorldY());
//
//			transform.position.set(transform.worldPosition);
//			transform.position.sub(parentTransform.worldPosition);
//			transform.position.rotateDeg(-parentTransform.worldRotation);
//			transform.position.scl(1f/parentTransform.worldScale.x, 1f/parentTransform.worldScale.y);


			transform.position.set(bone.getX(), bone.getY());
			transform.rotation = bone.getRotation();
			transform.scale.set(bone.getScaleX(), bone.getScaleY());


		}


		ShapeRenderer shapeRenderer = new ShapeRenderer();
		SkeletonRendererDebug skeletonRendererDebug = new SkeletonRendererDebug(shapeRenderer);
		batch.end();

		skeletonRendererDebug.setScale(1/64f);
		shapeRenderer.setProjectionMatrix(camera.combined);
		skeletonRendererDebug.draw(rendererComponent.skeleton);

		shapeRenderer.dispose();

		batch.begin();

	}
}
