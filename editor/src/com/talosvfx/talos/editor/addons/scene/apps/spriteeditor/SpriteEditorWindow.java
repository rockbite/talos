package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.runtime.assets.GameAsset;

public class SpriteEditorWindow extends Table {
    protected SpriteEditor spriteEditor;
    protected GameAsset<AtlasRegion> gameAsset;

    public SpriteEditorWindow (SpriteEditor spriteEditor) {
        this.spriteEditor = spriteEditor;
    }

    public void setScrollFocus() {

    }

    public void updateForGameAsset (GameAsset<AtlasRegion> gameAsset) {
        this.gameAsset = gameAsset;
    }
}
