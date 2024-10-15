package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.graphics.NineSlice;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.*;

public class SpriteComponentRenderer extends ComponentRenderer<SpriteRendererComponent> {


    private Vector2 vector2 = new Vector2();


    public SpriteComponentRenderer (GameObjectRenderer gameObjectRenderer) {
        super(gameObjectRenderer);
    }

    @Override
    public void render (Batch batch, Camera camera, GameObject gameObject, SpriteRendererComponent rendererComponent) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        TransformComponent transformComponent = gameObject.getTransformComponent();

        SpriteRendererComponent spriteRenderer = gameObject.getSpriteComponent();
        GameAsset<AtlasSprite> gameResource = spriteRenderer.getGameResource();

        if (gameResource.isBroken()) {
            GameObjectRenderer.renderBrokenComponent(batch, gameObject, transformComponent);
            return;
        }


        RawAsset rootRawAsset = gameResource.getRootRawAsset();
        AMetadata metaData = rootRawAsset.metaData;

        AtlasSprite textureRegion = gameResource.getResource();

        if (metaData instanceof SpriteMetadata) {
            //It should be
            SpriteMetadata metadata = (SpriteMetadata) metaData;

            AtlasSprite resource = spriteRenderer.getGameResource().getResource();
            Texture texture = resource.getTexture();
            if (texture.getMagFilter() != metadata.magFilter || texture.getMinFilter() != metadata.minFilter) {
                texture.setFilter(metadata.minFilter, metadata.magFilter);
            }


            if (textureRegion != null) {
                textureRegion.flip(textureRegion.isFlipX() != rendererComponent.flipX, textureRegion.isFlipY() != rendererComponent.flipY);
                textureRegion.setColor(spriteRenderer.finalColor);

                final float width = spriteRenderer.size.x;
                final float height = spriteRenderer.size.y;

                if (metadata != null && metadata.borderData != null && spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.sliced) {
                    RuntimeContext.TalosContext talosContext = RuntimeContext.getInstance().getTalosContext(gameObject.getTalosIdentifier());
                    BaseAssetRepository baseAssetRepository = talosContext.getBaseAssetRepository();
                    NineSlice patch = baseAssetRepository.obtainNinePatch(gameResource);// todo: this has to be done better
                    float tileWidth = spriteRenderer.tileSize.x;
                    float tileHeight = spriteRenderer.tileSize.y;
                    patch.setTileWidth(tileWidth);
                    patch.setTileHeight(tileHeight);
                    patch.setRenderMode(spriteRenderer.sliceMode);
                    //todo: and this renders wrong so this needs fixing too
                    float xSign = width < 0 ? -1 : 1;
                    float ySign = height < 0 ? -1 : 1;

                    float pivotX = transformComponent.pivot.x;
                    float pivotY = transformComponent.pivot.y;

                    batch.setColor(rendererComponent.finalColor);
                    patch.draw(batch,
                            transformComponent.worldPosition.x - pivotX * width * xSign, transformComponent.worldPosition.y - pivotY * height * ySign,
                            pivotX * width * xSign, pivotY * height * ySign,
                            width, height,
                            xSign * transformComponent.worldScale.x, ySign * transformComponent.worldScale.y,
                            transformComponent.worldRotation);
                } else if (spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.tiled) {


                    //Tiled mode, we draw from bottom left and fill it based on tile size

                    float tileWidth = spriteRenderer.tileSize.x * transformComponent.worldScale.x;
                    float tileHeight = spriteRenderer.tileSize.y * transformComponent.worldScale.y;

                    float totalWidth = width * transformComponent.worldScale.x;
                    float totalHeight = height * transformComponent.worldScale.y;

                    float startX = transformComponent.worldPosition.x - totalWidth / 2;
                    float startY = transformComponent.worldPosition.y - totalHeight / 2;

                    float xCoord = 0;
                    float yCoord = 0;

                    float halfTileWidth = tileWidth / 2f;
                    float halfTileHeight = tileHeight / 2f;

                    float endX = startX + totalWidth;
                    float endY = startY + totalHeight;

                    xCoord = startX;
                    yCoord = startY;

                    for (xCoord = startX; xCoord < endX - tileWidth; xCoord += tileWidth) {
                        for (yCoord = startY; yCoord < endY - tileHeight; yCoord += tileHeight) {

                            //Coord needs to be rotated from cneter

                            vector2.set(xCoord + halfTileWidth, yCoord + halfTileHeight);
                            vector2.sub(transformComponent.worldPosition);
                            vector2.rotateDeg(transformComponent.worldRotation);
                            vector2.add(transformComponent.worldPosition);

                            //Tiny scale for artifacting, better to do with a mesh really

                            textureRegion.setPosition(vector2.x - halfTileWidth, vector2.y - halfTileHeight);
                            textureRegion.setOrigin(halfTileWidth, halfTileHeight);
                            textureRegion.setSize(tileWidth, tileHeight);
                            textureRegion.setScale(1.002f, 1.002f);
                            textureRegion.setRotation(transformComponent.worldRotation);
                            textureRegion.draw(batch);
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
                        float uScale = uWidth * remainderX / tileWidth;
                        float cachedU2 = textureRegion.getU2();
                        textureRegion.setU2(textureRegion.getU() + uScale);

                        textureRegion.setPosition(vector2.x - halfTileWidth, vector2.y - halfTileHeight);
                        textureRegion.setOrigin(halfTileWidth, halfTileHeight);
                        textureRegion.setSize(remainderX, tileHeight);
                        textureRegion.setScale(1.002f, 1.002f);
                        textureRegion.setRotation(transformComponent.worldRotation);
                        textureRegion.draw(batch);

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
                        float vScale = vWidth * remainderY / tileHeight;
                        float cachedV = textureRegion.getV();
                        textureRegion.setV(textureRegion.getV2() - vScale);

                        textureRegion.setPosition(vector2.x - halfTileWidth, vector2.y - halfTileHeight);
                        textureRegion.setOrigin(halfTileWidth, halfTileHeight);
                        textureRegion.setSize(tileWidth, remainderY);
                        textureRegion.setScale(1.002f, 1.002f);
                        textureRegion.setRotation(transformComponent.worldRotation);
                        textureRegion.draw(batch);

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
                        float uScale = uWidth * remainderX / tileWidth;
                        float cachedU2 = textureRegion.getU2();
                        textureRegion.setU2(textureRegion.getU() + uScale);


                        float vWidth = textureRegion.getV2() - textureRegion.getV();
                        float vScale = vWidth * remainderY / tileHeight;
                        float cachedV = textureRegion.getV();
                        textureRegion.setV(textureRegion.getV2() - vScale);

                        textureRegion.setPosition(vector2.x - halfTileWidth, vector2.y - halfTileHeight);
                        textureRegion.setOrigin(halfTileWidth, halfTileHeight);
                        textureRegion.setSize(remainderX, remainderY);
                        textureRegion.setScale(1.002f, 1.002f);
                        textureRegion.setRotation(transformComponent.worldRotation);
                        textureRegion.draw(batch);


                        textureRegion.setV(cachedV);
                        textureRegion.setU2(cachedU2);

                    }


                } else if (spriteRenderer.renderMode == SpriteRendererComponent.RenderMode.simple) {

                    float pivotX = transformComponent.pivot.x;
                    float pivotY = transformComponent.pivot.y;

                    textureRegion.setPosition(transformComponent.worldPosition.x - width / 2f, transformComponent.worldPosition.y - height / 2f);
                    textureRegion.setOrigin(pivotX * width, pivotY * height);
                    textureRegion.setSize(width, height);
                    textureRegion.setScale(transformComponent.worldScale.x, transformComponent.worldScale.y);
                    textureRegion.setFlip(rendererComponent.flipX, rendererComponent.flipY);
                    textureRegion.setRotation(transformComponent.worldRotation);
                    textureRegion.draw(batch);
                }

                batch.setColor(Color.WHITE);
            }
        }

    }

}
