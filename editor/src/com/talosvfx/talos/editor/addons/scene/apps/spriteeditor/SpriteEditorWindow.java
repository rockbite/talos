package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.runtime.assets.GameAsset;

public class SpriteEditorWindow extends Table {
    protected SpriteEditor spriteEditor;
    protected GameAsset<AtlasSprite> gameAsset;

    public SpriteEditorWindow (SpriteEditor spriteEditor) {
        this.spriteEditor = spriteEditor;
    }

    public void setScrollFocus() {

    }

    public void updateForGameAsset (GameAsset<AtlasSprite> gameAsset) {
        this.gameAsset = gameAsset;
    }
}
