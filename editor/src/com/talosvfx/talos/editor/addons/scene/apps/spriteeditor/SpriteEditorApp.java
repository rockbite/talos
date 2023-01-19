package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.runtime.assets.GameAsset;
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
        DummyLayoutApp<Texture> spriteEditorApp = new DummyLayoutApp<Texture>(SharedResources.skin, this, getAppName()) {
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
    public void updateForGameAsset (GameAsset<Texture> gameAsset) {
        super.updateForGameAsset(gameAsset);
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
