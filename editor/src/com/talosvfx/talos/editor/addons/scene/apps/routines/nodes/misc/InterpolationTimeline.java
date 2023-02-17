package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rockbite.bongo.engine.EngineBuilder;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.AsyncRoutineNodeWidget;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.graphics.GL20.*;

public class InterpolationTimeline extends Table {
    private static Logger logger = LoggerFactory.getLogger(InterpolationTimeline.class);

    @Setter
    private float progress;

    @Setter
    private Interpolation interpolation = Interpolation.linear;
    private static final Color bgColor = Color.valueOf("#37574aff");
    private static final Color lineColor = Color.valueOf("18db66ff");

    private ShapeRenderer shapeRenderer;


    private static ObjectFloatMap<Interpolation> interpolationMaxValues;
    private static ObjectFloatMap<Interpolation> interpolationMinValues;

    static {
        // beforehand calculate min/max for all interpolation functions
        interpolationMaxValues = new ObjectFloatMap<>();
        interpolationMinValues = new ObjectFloatMap<>();
        Field[] interpolationFields = ClassReflection.getFields(Interpolation.class);
        for (int i = 0; i < interpolationFields.length; i++) {
            if (ClassReflection.isAssignableFrom(Interpolation.class, interpolationFields[i].getDeclaringClass()) && interpolationFields[i].isStatic()) {
                try {
                    Interpolation interpolation = (Interpolation) interpolationFields[i].get(null);
                    for (float alpha = 0.0f; alpha <= 1.0f; alpha += 0.01f) {
                        float val = interpolation.apply(alpha);
                        if (val > interpolationMaxValues.get(interpolation, -69)) {
                            interpolationMaxValues.put(interpolation, val);
                        }
                        if (val < interpolationMinValues.get(interpolation, 69)) {
                            interpolationMinValues.put(interpolation, val);
                        }
                    }
                } catch (ReflectionException e) {
                    logger.warn(e.getMessage());
                }
            }
        }
    }

    public InterpolationTimeline(AsyncRoutineNodeWidget asyncRoutineNodeWidget, Skin skin) {
        super(skin);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.end();

        Camera camera = getStage().getCamera();
        final float baseThickness = 1.0f;
        float thickness = baseThickness;
        if (camera instanceof OrthographicCamera) {
            OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
            final float zoom = orthographicCamera.zoom;
            final float zoomRatio = MathUtils.clamp(zoom, 1.0f, 10.0f) / 10.0f;
            float extraThickness = 10.0f;
            thickness = baseThickness + (extraThickness * Interpolation.sineOut.apply(zoomRatio));
        }

        Gdx.gl.glEnable(GL_BLEND);
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(bgColor.r, bgColor.g, bgColor.b, 0.4f);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.setColor(bgColor.r, bgColor.g, bgColor.b, 1.0f);
        shapeRenderer.rect(getX(), getY(), getWidth() * progress, getHeight());

        shapeRenderer.setColor(lineColor);
        final float steps = getWidth() / 2f;
        final float graphWidth = getWidth();
        final float graphHeight = getHeight() - 15; // with tiny top padding
        final float bottomLeftX = getX();
        final float bottomLeftY = getY() + 5; // with tiny bottom padding
        float max = interpolationMaxValues.get(interpolation, 1.0f);
        float min = interpolationMinValues.get(interpolation, 0.0f);
        float range = Math.abs(max - min);
        float lastX = bottomLeftX, lastY = bottomLeftY + graphHeight * (-min + (interpolation.apply(0) / range));
        for (float step = 0; step <= steps; step++) {
            float percent = step / steps;
            float x = bottomLeftX + graphWidth * percent;
            float y = bottomLeftY + graphHeight * (-min + (interpolation.apply(percent) / range));
            shapeRenderer.rectLine(lastX, lastY, x, y, thickness);
            lastX = x;
            lastY = y;
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL_BLEND);
        batch.begin();
    }

    @Override
    public float getPrefHeight() {
        return 80;
    }

    // Goodbye, cruel world. Goodbye, cruel lamp. Goodbye, cruel velvet drapes, lined with what would appear to be some
    // sort of cruel muslin and the cute little pom-pom curtain pull cords. Cruel though they may be...
//    void plotLineWidth(Pixmap pixmap, int x0, int y0, int x1, int y1, long th) { /* plot an anti-aliased line of thickness th := 256 == 1 pixel */
//        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
//        long dx = Math.abs(x1-x0), dy = Math.abs(y1-y0);
//        long err = dx < dy ? dx : dy, e2 = dx < dy ? dy : dx; /* min / max */
//        final long BKGD = (255L<<16); /* max pixel value = background */
//        if (th <= 256 || e2 == 0) {
//            plotLineAA(pixmap, x0, y0, x1, y1);
//            return; /* assert */
//        }
//        e2 = BKGD/(e2+2*err*err*e2/(4*e2*e2+err*err)); /* sqrt approximation */
//        th = (th-256)<<16; dx *= e2; dy *= e2; /* scale values */
//        if (dx < dy) { /* steep line */
//            x1 = (int) ((BKGD+th/2)/dy); /* start offset */
//            err = x1*dy-th/2; /* shift error value to offset width */
//            for (x0 -= x1*sx; ; y0 += sy) {
//                pixmap.drawPixel(x1 = x0, y0, color((int) (err>>16))); /* aliasing pre-pixel */
//                for (e2 = dy-err-th; e2+dy < BKGD; e2 += dy)
//                    pixmap.drawPixel(x1 += sx, y0, Color.rgba8888(lineColor)); /* pixel on thick line */
//                pixmap.drawPixel(x1+sx, y0, color((int) (e2>>16))); /* aliasing post-pixel */
//                if (y0 == y1) break;
//                err += dx; /* y-step */
//                if (err > BKGD) { err -= dy; x0 += sx; } /* x-step */
//            }
//        } else { /* flat line */
//            y1 = (int) ((BKGD+th/2)/dx); /* start offset */
//            err = y1*dx-th/2; /* shift error value to offset width */
//            for (y0 -= y1*sy; ; x0 += sx) {
//                pixmap.drawPixel(x0, y1 = y0, color((int) (err>>16))); /* aliasing pre-pixel */
//                for (e2 = dx-err-th; e2+dx < BKGD; e2 += dx)
//                    pixmap.drawPixel(x0, y1 += sy, Color.rgba8888(lineColor)); /* pixel on thick line */
//                pixmap.drawPixel(x0, y1+sy, color((int) (e2>>16))); /* aliasing post-pixel */
//                if (x0 == x1) break;
//                err += dy; /* x-step */
//                if (err > BKGD) { err -= dx; y0 += sy; } /* y-step */
//            }
//        }
//    }
//
//    void plotLineAA(Pixmap pixmap, int x0, int y0, int x1, int y1) {
//        int dx = Math.abs(x1-x0), sx = x0 < x1 ? 1 : -1;
//        int dy = Math.abs(y1-y0), sy = y0 < y1 ? 1 : -1;
//        int x2, e2, err = dx-dy; /* error value e_xy */
//        int ed = dx+dy == 0 ? 1 : (int) Math.sqrt((float) dx * dx + (float) dy * dy);
//        for ( ; ; ){ /* pixel loop */
//            pixmap.drawPixel(x0,y0,color(255 * Math.abs(err-dx+dy)/ed));
//            e2 = err; x2 = x0;
//            if (2*e2 >= -dx) { /* x step */
//                if (x0 == x1) break;
//                if (e2+dy < ed) pixmap.drawPixel(x0,y0+sy,color(255*(e2+dy)/ed));
//                err -= dy; x0 += sx;
//            }
//            if (2*e2 <= dy) { /* y step */
//                if (y0 == y1) break;
//                if (dx-e2 < ed) pixmap.drawPixel(x2+sx,y0,color(255*(dx-e2)/ed));
//                err += dx; y0 += sy;
//            }
//        }
//    }
//
//    private static int color (int intensity) {
//        float r = (intensity*bgColor.r + (255-intensity)*lineColor.r)/255.0f;
//        float g = (intensity*bgColor.g + (255-intensity)*lineColor.g)/255.0f;
//        float b = (intensity*bgColor.b + (255-intensity)*lineColor.b)/255.0f;
//        float a = (intensity*bgColor.a + (255-intensity)*lineColor.a)/255.0f;
//        return Color.rgba8888(r, g, b, a);
//    }
}
