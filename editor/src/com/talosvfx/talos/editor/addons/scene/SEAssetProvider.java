package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.runtime.assets.BaseAssetProvider;

public class SEAssetProvider extends BaseAssetProvider {

    Sprite defaultSprite;

    public SEAssetProvider() {
        defaultSprite = new Sprite();
        defaultSprite.setRegion(TalosMain.Instance().getSkin().getRegion("white"));
    }

    @Override
    public <T> T findAsset (String assetName, Class<T> clazz) {
        if(clazz.equals(Sprite.class) && assetName.equals("white")) {
            return (T) defaultSprite;
        }
        return super.findAsset(assetName, clazz);
    }
}
