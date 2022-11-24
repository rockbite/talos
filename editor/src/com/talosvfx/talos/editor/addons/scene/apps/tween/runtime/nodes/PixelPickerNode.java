package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

public class PixelPickerNode extends RoutineNode {
    private Pixmap pixmap;
    private String assetName;

    private Color color = new Color();

    @Override
    public Object queryValue(String targetPortName) {

        int x = (int)fetchFloatValue("x");
        int y = (int)fetchFloatValue("y");

        GameAsset<Texture> asset = fetchAssetValue("texture");
        Texture texture = asset.getResource();

        if(assetName == null || (assetName != null && !assetName.equals(asset.nameIdentifier)) || nodeDirty) {
            texture.getTextureData().prepare();
            pixmap = texture.getTextureData().consumePixmap();
            assetName = asset.nameIdentifier;
            nodeDirty = false;
        }

        if(pixmap != null) {
            if(x >= 0 && y >= 0 && x <= pixmap.getWidth() && y <= pixmap.getHeight()) {
                int bits =  pixmap.getPixel(x, y);
                color.set(bits);

                return color;
            }
        }

        return Color.BLACK;
    }
}
