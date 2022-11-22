package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class SpriteImporter extends AbstractImporter<Texture> {

    @Override
    public GameObject makeInstance (GameAsset<Texture> asset, GameObject parent) {

        SpriteMetadata metaData = (SpriteMetadata)asset.getRootRawAsset().metaData;

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createSpriteObject(asset, sceneCords, parent);

        SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);

        boolean isSlice = metaData.isSlice();
        component.renderMode = isSlice ? SpriteRendererComponent.RenderMode.sliced : SpriteRendererComponent.RenderMode.simple;

        Texture texture = asset.getResource();
        component.size.x = texture.getWidth() / metaData.pixelsPerUnit;
        component.size.y = texture.getHeight() / metaData.pixelsPerUnit;

        return gameObject;
    }
}
