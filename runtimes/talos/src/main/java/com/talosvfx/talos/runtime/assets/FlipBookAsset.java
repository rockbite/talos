package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import lombok.Getter;

public class FlipBookAsset extends TextureAtlas.AtlasSprite {

    @Getter
    private Array<TextureAtlas.AtlasSprite> frames;

    public FlipBookAsset (TextureAtlas.AtlasRegion region) {
        super(region);
    }

    public FlipBookAsset (Array<TextureAtlas.AtlasSprite> frames) {
        super(frames.first());

        this.frames = frames;
    }
}
