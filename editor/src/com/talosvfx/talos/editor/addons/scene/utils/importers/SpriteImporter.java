package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.ImportUtils;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class SpriteImporter extends AbstractImporter {

    @Override
    public FileHandle importAsset (FileHandle fileHandle, FileHandle destinationDir) {
        FileHandle importedAsset = importAssetFile(fileHandle, destinationDir);
        // this is now copied to our assets folder, and metadata created

        if(fileHandle.nameWithoutExtension().endsWith(".9")) {
            // it's a nine slice, and needs metadata created accordingly
            FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(importedAsset);
            metadataHandle = renameAsset(metadataHandle, metadataHandle.nameWithoutExtension().replace(".9", "") + ".meta");
            importedAsset = renameAsset(importedAsset, importedAsset.nameWithoutExtension().replace(".9", "") + ".png");
            SpriteMetadata metadata = AssetImporter.readMetadata(metadataHandle, SpriteMetadata.class);

            Pixmap pixmap = new Pixmap(importedAsset);
            int[] splits = ImportUtils.getSplits(pixmap);
            metadata.borderData = splits;

            AssetImporter.saveMetadata(metadataHandle, metadata);

            Pixmap newPixmap = ImportUtils.cropImage(pixmap, 1, 1, pixmap.getWidth() - 1, pixmap.getHeight() - 1);
            PixmapIO.writePNG(importedAsset, newPixmap);

            pixmap.dispose();
            newPixmap.dispose();
        }

        makeInstance(importedAsset, SceneEditorAddon.get().workspace.getRootGO());

        return importedAsset;
    }

    @Override
    public void makeInstance(FileHandle asset, GameObject parent) {
        if(!AssetImporter.getMetadataHandleFor(asset).exists()) {
            createMetadataFor(asset);
        }
        SpriteMetadata metadata = AssetImporter.readMetadataFor(asset, SpriteMetadata.class);

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createSpriteObject(asset, sceneCords, parent);

        if(metadata.borderData != null) {
            SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
            component.renderMode = SpriteRendererComponent.RenderMode.sliced;
        } else {
            if(gameObject.hasComponent(TransformComponent.class)) {
                TransformComponent component = gameObject.getComponent(TransformComponent.class);
                Texture texture = new Texture(asset);
                component.scale.x = texture.getWidth() / metadata.pixelsPerUnit;
                component.scale.y = texture.getHeight() / metadata.pixelsPerUnit;
            }
        }
    }

    @Override
    public FileHandle createMetadataFor (FileHandle handle) {
        FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(handle);
        SpriteMetadata metadata = new SpriteMetadata();
        AssetImporter.saveMetadata(metadataHandle, metadata);

        return metadataHandle;
    }

}
