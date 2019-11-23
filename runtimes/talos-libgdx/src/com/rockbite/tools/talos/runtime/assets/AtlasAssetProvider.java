package com.rockbite.tools.talos.runtime.assets;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AtlasAssetProvider extends BaseAssetProvider {
    private final TextureAtlas atlas;

    public AtlasAssetProvider (final TextureAtlas atlas) {
        this.atlas = atlas;

        setAssetHandler(TextureRegion.class, new AssetHandler<TextureRegion>() {
            @Override
            public TextureRegion findAsset (String assetName) {
                return atlas.findRegion(assetName);
            }
        });

        setAssetHandler(Sprite.class, new AssetHandler<Sprite>() {
            @Override
            public Sprite findAsset (String assetName) {
                return atlas.createSprite(assetName);
            }
        });
    }
}
