package com.talosvfx.talos.runtime.routine.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;

public class DrawableQuad implements Pool.Poolable {

    public Vector2 position = new Vector2();
    public Vector2 size = new Vector2();
    public GameAsset<AtlasSprite> gameAsset;
    public float rotation;
    public Color color = new Color();
    public boolean aspect;
    public float z;
    public SpriteMetadata metadata;
    public SpriteRendererComponent.RenderMode renderMode;

    @Override
    public void reset() {
        color.set(Color.WHITE);
    }
}
