package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.ImportUtils;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class SpriteImporter extends AbstractImporter<Texture> {

    @Override
    public void makeInstance (GameAsset<Texture> asset, GameObject parent) {

        SpriteMetadata metaData = (SpriteMetadata)asset.getRootRawAsset().metaData;

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createSpriteObject(asset, sceneCords, parent);

        if(metaData.borderData != null) {
            SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
            component.renderMode = SpriteRendererComponent.RenderMode.sliced;
        } else {
            if(gameObject.hasComponent(TransformComponent.class)) {
                TransformComponent component = gameObject.getComponent(TransformComponent.class);
                Texture texture = asset.getResource();
                component.scale.x = texture.getWidth() / metaData.pixelsPerUnit;
                component.scale.y = texture.getHeight() / metaData.pixelsPerUnit;
            }
        }
    }
}
