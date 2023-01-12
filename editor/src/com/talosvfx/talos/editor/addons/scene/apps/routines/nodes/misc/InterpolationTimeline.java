package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.AsyncRoutineNodeWidget;

public class InterpolationTimeline extends Table {
    private final MicroNodeView microNodeView;
    private final AsyncRoutineNodeWidget asyncRoutineNodeWidget;
    private Group container = new Group();
    private ObjectMap<Object, Image> progressMap = new ObjectMap<>();
    private Interpolation interpolation = Interpolation.linear;

    private static final Color bgColor = Color.valueOf("#37574a");
    private static final Color lineColor = Color.valueOf("18db66ff");

    private Image chartImage;

    public InterpolationTimeline(AsyncRoutineNodeWidget asyncRoutineNodeWidget, Skin skin) {
        super(skin);

        this.asyncRoutineNodeWidget = asyncRoutineNodeWidget;
        this.microNodeView = asyncRoutineNodeWidget.getMicroNodeView();

        setBackground(getSkin().getDrawable("timelinebg"));

        chartImage = new Image();
        chartImage.setFillParent(true);
        chartImage.setPosition(1, 0);

        addActor(chartImage);
        addActor(container);

        setInterpolation(interpolation);
    }

    public void clearMap() {
        for (ObjectMap.Entry<Object, Image> objectImageEntry : progressMap) {
            objectImageEntry.value.remove();
            Pools.free(objectImageEntry.value);

        }
        progressMap.clear();
    }

    public void setProgress(Object target, float value) {
        if(!progressMap.containsKey(target)) {
            Image image = Pools.obtain(Image.class);
            image.setDrawable(getSkin().getDrawable("white"));
            image.setColor(Color.valueOf("#37574a"));
            progressMap.put(target, image);
            container.addActor(image);
        }

        Image image = progressMap.get(target);

        image.getColor().a = 0.4f;

        float alpha = value;

        image.setPosition(1, 1);

        if(alpha * (getWidth() - 1) > 0) {
            float width = alpha * (getWidth() - 1);
            if(width > getWidth() - 2) width = getWidth() - 2;
            image.setSize(width, getHeight() - 2);
            image.setVisible(true);
        } else {
            image.setVisible(false);
        }

        if(alpha == 0 || alpha == 1) {
            image.setVisible(false);
        } else {
            image.setVisible(true);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public float getPrefHeight() {
        return 58;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;

        buildChart();
    }

    private void buildChart() {
        float width = 720;
        float height = 480;
        float margin = height * 0.2f;
        float bottomLeftX = 0;
        float bottomLeftY = margin;
        float steps = width * 0.5f;

        Pixmap pixmap = new Pixmap((int) width, (int) height, Pixmap.Format.RGBA8888);

        float lastX = bottomLeftX, lastY = bottomLeftY;
        for (float step = 0; step < steps - 3; step++) {
            float percent = step / steps;
            float x = bottomLeftX + width * percent;
            float y = bottomLeftY + (height - 2.0f * margin) * interpolation.apply(percent);
            plotLineWidth(pixmap, (int)lastX, (int)lastY, (int)x, (int)y, (long) (6 * 256.0));
            lastX = x;
            lastY = y;
        }

        Texture texture = new Texture(pixmap);
        TextureRegion region = new TextureRegion(texture);
        region.flip(false, true);
        chartImage.setDrawable(new TextureRegionDrawable(region));
    }

    void plotLineWidth(Pixmap pixmap, int x0, int y0, int x1, int y1, long th) { /* plot an anti-aliased line of thickness th := 256 == 1 pixel */
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        long dx = Math.abs(x1-x0), dy = Math.abs(y1-y0);
        long err = dx < dy ? dx : dy, e2 = dx < dy ? dy : dx; /* min / max */
        final long BKGD = (255L<<16); /* max pixel value = background */
        if (th <= 256 || e2 == 0) {
            plotLineAA(pixmap, x0, y0, x1, y1);
            return; /* assert */
        }
        e2 = BKGD/(e2+2*err*err*e2/(4*e2*e2+err*err)); /* sqrt approximation */
        th = (th-256)<<16; dx *= e2; dy *= e2; /* scale values */
        if (dx < dy) { /* steep line */
            x1 = (int) ((BKGD+th/2)/dy); /* start offset */
            err = x1*dy-th/2; /* shift error value to offset width */
            for (x0 -= x1*sx; ; y0 += sy) {
                pixmap.drawPixel(x1 = x0, y0, color((int) (err>>16))); /* aliasing pre-pixel */
                for (e2 = dy-err-th; e2+dy < BKGD; e2 += dy)
                    pixmap.drawPixel(x1 += sx, y0, Color.rgba8888(lineColor)); /* pixel on thick line */
                pixmap.drawPixel(x1+sx, y0, color((int) (e2>>16))); /* aliasing post-pixel */
                if (y0 == y1) break;
                err += dx; /* y-step */
                if (err > BKGD) { err -= dy; x0 += sx; } /* x-step */
            }
        } else { /* flat line */
            y1 = (int) ((BKGD+th/2)/dx); /* start offset */
            err = y1*dx-th/2; /* shift error value to offset width */
            for (y0 -= y1*sy; ; x0 += sx) {
                pixmap.drawPixel(x0, y1 = y0, color((int) (err>>16))); /* aliasing pre-pixel */
                for (e2 = dx-err-th; e2+dx < BKGD; e2 += dx)
                    pixmap.drawPixel(x0, y1 += sy, Color.rgba8888(lineColor)); /* pixel on thick line */
                pixmap.drawPixel(x0, y1+sy, color((int) (e2>>16))); /* aliasing post-pixel */
                if (x0 == x1) break;
                err += dy; /* x-step */
                if (err > BKGD) { err -= dx; y0 += sy; } /* y-step */
            }
        }
    }

    void plotLineAA(Pixmap pixmap, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1-x0), sx = x0 < x1 ? 1 : -1;
        int dy = Math.abs(y1-y0), sy = y0 < y1 ? 1 : -1;
        int x2, e2, err = dx-dy; /* error value e_xy */
        int ed = dx+dy == 0 ? 1 : (int) Math.sqrt((float) dx * dx + (float) dy * dy);
        for ( ; ; ){ /* pixel loop */
            pixmap.drawPixel(x0,y0,color(255 * Math.abs(err-dx+dy)/ed));
            e2 = err; x2 = x0;
            if (2*e2 >= -dx) { /* x step */
                if (x0 == x1) break;
                if (e2+dy < ed) pixmap.drawPixel(x0,y0+sy,color(255*(e2+dy)/ed));
                err -= dy; x0 += sx;
            }
            if (2*e2 <= dy) { /* y step */
                if (y0 == y1) break;
                if (dx-e2 < ed) pixmap.drawPixel(x2+sx,y0,color(255*(dx-e2)/ed));
                err += dx; y0 += sy;
            }
        }
    }

    private static int color (int intensity) {
        float r = (intensity*bgColor.r + (255-intensity)*lineColor.r)/255.0f;
        float g = (intensity*bgColor.g + (255-intensity)*lineColor.g)/255.0f;
        float b = (intensity*bgColor.b + (255-intensity)*lineColor.b)/255.0f;
        float a = (intensity*bgColor.a + (255-intensity)*lineColor.a)/255.0f;
        return Color.rgba8888(r, g, b, a);
    }
}
