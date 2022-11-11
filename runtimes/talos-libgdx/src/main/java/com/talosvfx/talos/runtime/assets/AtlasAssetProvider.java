package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.utils.ShaderDescriptor;

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
        
        setAssetHandler(ShaderDescriptor.class, new AssetHandler<ShaderDescriptor>() {

            private final ObjectMap<String, ShaderDescriptor> shaderDescriptorMap = new ObjectMap<>();
            /**
            * assetName: filename of the Shader (*.shdr file)
            **/
            @Override
            public ShaderDescriptor findAsset(String assetName) {
                ShaderDescriptor asset = shaderDescriptorMap.get(assetName);
                if (asset == null) {
                    final FileHandle file = Gdx.files.internal(assetName);

                    asset = new ShaderDescriptor();
                    if (file.exists()) {
                        asset.setData(file.readString());
                        shaderDescriptorMap.put(assetName, asset);
                    }
                }
                return asset;
            }
        });
    }
}
