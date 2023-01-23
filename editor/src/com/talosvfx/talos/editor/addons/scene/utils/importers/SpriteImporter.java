package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.graphics.Texture;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.scene.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpriteImporter extends AbstractImporter<Texture> {

    private static final Logger logger = LoggerFactory.getLogger(SpriteImporter.class);

    @Override
    public GameObject makeInstance (GameAsset<Texture> asset, GameObject parent) {

        SpriteMetadata metaData = (SpriteMetadata)asset.getRootRawAsset().metaData;

        logger.info("Needs redoing for context");
//
//        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
//        Vector2 sceneCords = workspace.getMouseCordsOnScene();
//        GameObject gameObject = workspace.createSpriteObject(asset, sceneCords, parent);
//
//        SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
//
//        boolean isSlice = metaData.isSlice();
//        component.renderMode = isSlice ? SpriteRendererComponent.RenderMode.sliced : SpriteRendererComponent.RenderMode.simple;
//
//        Texture texture = asset.getResource();
//        component.size.x = texture.getWidth() / metaData.pixelsPerUnit;
//        component.size.y = texture.getHeight() / metaData.pixelsPerUnit;
//
//        return gameObject;

        return null;
    }
}
