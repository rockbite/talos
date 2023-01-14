package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class DrawableQuad implements Pool.Poolable {

    public Vector2 position = new Vector2();
    public Vector2 size = new Vector2();
    public GameAsset<Texture> gameAsset;
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
