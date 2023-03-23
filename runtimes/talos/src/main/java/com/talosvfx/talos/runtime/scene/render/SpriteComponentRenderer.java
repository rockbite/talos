package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class SpriteComponentRenderer extends ComponentRenderer<SpriteRendererComponent> {


	private TextureRegion textureRegion = new TextureRegion();
	private Vector2 vector2 = new Vector2();

	public SpriteComponentRenderer (GameObjectRenderer gameObjectRenderer) {
		super(gameObjectRenderer);
	}

	@Override
	public void render (Batch batch, Camera camera, GameObject gameObject, SpriteRendererComponent rendererComponent) {
		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

		SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
		GameAsset<Texture> gameResource = spriteRenderer.getGameResource();
		RawAsset rootRawAsset = gameResource.getRootRawAsset();
		AMetadata metaData = rootRawAsset.metaData;
		if (metaData instanceof SpriteMetadata) {
			//It should be
			SpriteMetadata metadata = (SpriteMetadata)metaData;

			Texture resource = spriteRenderer.getGameResource().getResource();
			if (resource.getMagFilter() != metadata.magFilter || resource.getMinFilter() != metadata.minFilter) {
				resource.setFilter(metadata.minFilter, metadata.magFilter);
			}
			textureRegion.setRegion(resource);
			if(textureRegion != null) {
				batch.setColor(spriteRenderer.finalColor);

				final float width = spriteRenderer.size.x;
				final float height = spriteRenderer.size.y;

				if(metadata != null && metadata.borderData != null && spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.sliced) {
					NinePatch patch = RuntimeContext.getInstance().AssetRepository.obtainNinePatch(gameResource);// todo: this has to be done better
					//todo: and this renders wrong so this needs fixing too
					float xSign = width < 0 ? -1 : 1;
					float ySign = height < 0 ? -1 : 1;

					float pivotX = transformComponent.pivot.x;
					float pivotY = transformComponent.pivot.y;

					patch.draw(batch,
						transformComponent.worldPosition.x - pivotX * width * xSign, transformComponent.worldPosition.y - pivotY * height * ySign,
						pivotX * width * xSign, pivotY * height * ySign,
						Math.abs(width), Math.abs(height),
						xSign * transformComponent.worldScale.x, ySign * transformComponent.worldScale.y,
						transformComponent.worldRotation);
				} else if(spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.tiled) {


					//Tiled mode, we draw from bottom left and fill it based on tile size

					float tileWidth = spriteRenderer.tileSize.x * transformComponent.worldScale.x;
					float tileHeight = spriteRenderer.tileSize.y * transformComponent.worldScale.y;

					float totalWidth = width * transformComponent.worldScale.x;
					float totalHeight = height * transformComponent.worldScale.y;

					float startX = transformComponent.worldPosition.x - totalWidth/2;
					float startY = transformComponent.worldPosition.y - totalHeight/2;

					float xCoord = 0;
					float yCoord = 0;

					float halfTileWidth = tileWidth / 2f;
					float halfTileHeight = tileHeight / 2f;

					float endX = startX + totalWidth;
					float endY = startY + totalHeight;

					xCoord = startX;
					yCoord = startY;

					for (xCoord = startX; xCoord < endX - tileWidth; xCoord += tileWidth){
						for (yCoord = startY; yCoord < endY - tileHeight; yCoord += tileHeight) {

							//Coord needs to be rotated from cneter

							vector2.set(xCoord + halfTileWidth, yCoord + halfTileHeight);
							vector2.sub(transformComponent.worldPosition);
							vector2.rotateDeg(transformComponent.worldRotation);
							vector2.add(transformComponent.worldPosition);

							//Tiny scale for artifacting, better to do with a mesh really
							batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, tileWidth, tileHeight, 1.0002f, 1.002f, transformComponent.worldRotation);
						}
					}

					// clip remainder if a tile is bigger than the sprite
					final float remainderX, remainderY;
					if (totalWidth < tileWidth) remainderX = totalWidth;
					else remainderX = endX - xCoord;
					if (totalHeight < tileHeight) remainderY = totalHeight;
					else remainderY = endY - yCoord;

					//Draw the remainders in x
					for (float yCoordRemainder = startY; yCoordRemainder < endY - tileHeight; yCoordRemainder += tileHeight) {

						//Coord needs to be rotated from cneter

						vector2.set(xCoord + halfTileWidth, yCoordRemainder + halfTileHeight);
						vector2.sub(transformComponent.worldPosition);
						vector2.rotateDeg(transformComponent.worldRotation);
						vector2.add(transformComponent.worldPosition);

						//clip it

						float uWidth = textureRegion.getU2() - textureRegion.getU();
						float uScale = uWidth * remainderX/tileWidth;
						float cachedU2 = textureRegion.getU2();
						textureRegion.setU2(textureRegion.getU() + uScale);
						batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, remainderX, tileHeight, 1.002f, 1.002f, transformComponent.worldRotation);
						textureRegion.setU2(cachedU2);
					}

					//Draw the remainders in y
					for (float xCoordRemainder = startX; xCoordRemainder < endX - tileWidth; xCoordRemainder += tileWidth) {

						//Coord needs to be rotated from cneter

						vector2.set(xCoordRemainder + halfTileWidth, yCoord + halfTileHeight);
						vector2.sub(transformComponent.worldPosition);
						vector2.rotateDeg(transformComponent.worldRotation);
						vector2.add(transformComponent.worldPosition);

						//clip it

						float vWidth = textureRegion.getV2() - textureRegion.getV();
						float vScale = vWidth * remainderY/tileHeight;
						float cachedV = textureRegion.getV();
						textureRegion.setV(textureRegion.getV2() - vScale);
						batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, tileWidth, remainderY, 1.002f, 1.002f, transformComponent.worldRotation);
						textureRegion.setV(cachedV);
					}

					//Last one

					{
						vector2.set(xCoord + halfTileWidth, yCoord + halfTileHeight);
						vector2.sub(transformComponent.worldPosition);
						vector2.rotateDeg(transformComponent.worldRotation);
						vector2.add(transformComponent.worldPosition);

						//clip it

						float uWidth = textureRegion.getU2() - textureRegion.getU();
						float uScale = uWidth * remainderX/tileWidth;
						float cachedU2 = textureRegion.getU2();
						textureRegion.setU2(textureRegion.getU() + uScale);


						float vWidth = textureRegion.getV2() - textureRegion.getV();
						float vScale = vWidth * remainderY/tileHeight;
						float cachedV = textureRegion.getV();
						textureRegion.setV(textureRegion.getV2() - vScale);

						batch.draw(textureRegion, vector2.x - halfTileWidth, vector2.y - halfTileHeight, halfTileWidth, halfTileHeight, remainderX, remainderY, 1.002f, 1.002f, transformComponent.worldRotation);
						textureRegion.setV(cachedV);
						textureRegion.setU2(cachedU2);

					}

//
//                    batch.draw(textureRegion,
//                        transformComponent.worldPosition.x - 0.5f, transformComponent.worldPosition.y - 0.5f,
//                            0.5f, 0.5f,
//                            1f, 1f,
//                            width * transformComponent.worldScale.x, height * transformComponent.worldScale.y,
//                            transformComponent.worldRotation);
				} else if(spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.simple) {

					float pivotX = transformComponent.pivot.x;
					float pivotY = transformComponent.pivot.y;

					batch.draw(textureRegion,
							transformComponent.worldPosition.x - width/2f, transformComponent.worldPosition.y - height/2f,
							pivotX * width, pivotY * height,
							width,
							height,
							transformComponent.worldScale.x, transformComponent.worldScale.y,
							transformComponent.worldRotation
					);
				}

				batch.setColor(Color.WHITE);
			}
		}

	}
}
