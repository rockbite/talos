package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.runtime.utils.SimplexNoise;

public class PerlinNoiseNodeWidget extends RoutineNodeWidget {


    private Image image;

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        Table previewTable = getCustomContainer("preview");



        image = new Image(makeTexture());
        image.setScaling(Scaling.fit);
        previewTable.add(image).growX();
    }

    @Override
    protected void reportNodeDataModified() {
        image.setDrawable(new TextureRegionDrawable(new TextureRegion(makeTexture())));
        super.reportNodeDataModified();
    }

    private Texture makeTexture() {
        Color color = new Color(Color.WHITE);

        SimplexNoise noise = new SimplexNoise();

        float scale = getWidgetFloatValue("scale");

        Pixmap pixmap = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        for(int x = 0; x < pixmap.getWidth(); x++) {
            for(int y = 0; y < pixmap.getHeight(); y++) {
                float val = (noise.query(x/256f, y/256f, scale) + 1f)/2f;

                color.set(val, val, val, 1f);
                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);

        return texture;
    }


}
