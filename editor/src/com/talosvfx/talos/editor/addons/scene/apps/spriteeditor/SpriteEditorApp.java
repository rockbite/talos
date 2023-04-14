package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.SingletonApp;

@SingletonApp
public class SpriteEditorApp extends AppManager.BaseApp<AtlasSprite> {
    private final SpriteEditor spriteEditor;

    public SpriteEditorApp () {

        spriteEditor = new SpriteEditor();
        DummyLayoutApp<AtlasSprite> spriteEditorApp = new DummyLayoutApp<AtlasSprite>(SharedResources.skin, this, getAppName()) {
            @Override
            public void onInputProcessorAdded() {
                super.onInputProcessorAdded();
                spriteEditor.setScrollFocus();
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
    public void updateForGameAsset (GameAsset<AtlasSprite> gameAsset) {
        super.updateForGameAsset(gameAsset);

        // TODO: 23.02.23 dummy refactor
        if (AppManager.dummyAsset == (GameAsset) gameAsset) {
            return;
        }

        spriteEditor.updateForGameAsset(gameAsset);
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
