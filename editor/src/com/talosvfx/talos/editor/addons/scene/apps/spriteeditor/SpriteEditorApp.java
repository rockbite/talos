package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.SingletonApp;

@SingletonApp
public class SpriteEditorApp extends AppManager.BaseApp<Texture> {
    private final SpriteEditor spriteEditor;

    public SpriteEditorApp () {
        this.singleton = true;

        spriteEditor = new SpriteEditor();
        spriteEditor.setListener(new SpriteEditor.SpriteMetadataListener() {
            @Override
            public void changed(int left, int right, int top, int bottom) {
                SpriteMetadata metaData = (SpriteMetadata) gameAsset.getRootRawAsset().metaData;
                metaData.borderData[0] = left;
                metaData.borderData[1] = right;
                metaData.borderData[2] = top;
                metaData.borderData[3] = bottom;

                // TODO: 05.12.22 Save to medatada file 
            }
        });
        DummyLayoutApp spriteEditorApp = new DummyLayoutApp(SharedResources.skin, getAppName()) {
            @Override
            public void onInputProcessorAdded() {
                super.onInputProcessorAdded();
                SharedResources.stage.setScrollFocus(spriteEditor.editPanel);
            }

            @Override
            public void onInputProcessorRemoved() {
                super.onInputProcessorRemoved();
            }

            @Override
            public Actor getMainContent () {
                return spriteEditor;
            }
        };

        this.gridAppReference = spriteEditorApp;
    }

    @Override
    public void updateForGameAsset (GameAsset<Texture> gameAsset) {
        super.updateForGameAsset(gameAsset);
        spriteEditor.show(gameAsset);
    }

    @Override
    public String getAppName () {
        if (gameAsset != null) {
            return "Sprite - " + gameAsset.nameIdentifier;
        } else {
            return "Sprite - ";
        }
    }

    @Override
    public void onRemove () {

    }
}
