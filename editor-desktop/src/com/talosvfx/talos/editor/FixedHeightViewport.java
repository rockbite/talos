package com.talosvfx.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class FixedHeightViewport extends Viewport {

    public static int notchSize;
    private final float worldHeight;

    public static float debugScalingFactor = 1f;

    public static Integer fakeDPI;

    public FixedHeightViewport (float worldHeight, OrthographicCamera camera) {
        this.worldHeight = worldHeight;
        setCamera(camera);
    }

    @Override
    public void update (int screenWidth, int screenHeight, boolean centerCamera) {
        int unscaledWidth = screenWidth;
        int unscaledHeight = screenHeight;


        float aspect = (float)screenWidth/screenHeight;

        float scalingHeight = getScalingHeight();

        float widthForScreen = scalingHeight * aspect;
        setWorldSize(widthForScreen, scalingHeight);

        setScreenBounds(0, 0, unscaledWidth, unscaledHeight);

        apply(centerCamera);
    }

    private static float getDeviceHeight () {
        float height = Gdx.graphics.getHeight();



        return height;
    }


    private static float getScalingHeight () {
        float diagonal = getDeviceScreenSizeInchesDiagonal();
        if (diagonal >= 7f && getDeviceHeight() > 1440) {
            int delta = (int) (getDeviceHeight() - 1440);

            // 0.555 for 7.0 inch
            // 0.600 for 7.9 inch
            // 0.850 for 12.9 inch
            float scalingPercent = 0.05f * diagonal + 0.205f;
            //Magic scaling factor <- So we dont scale 1:1 with the resolution increase. So if we double the resolution in height, the stage
            //Only gets 85% larger, not 100%;

            delta *= scalingPercent;

            return 1440 + delta;
        } else {
            return 1440;
        }
    }

    public static float getDeviceScreenSizeInchesDiagonal () {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();


        float inchesw =  width / Gdx.graphics.getPpiX();
        float inchesh =  height / Gdx.graphics.getPpiY();



        return (float) Math.sqrt(inchesh * inchesh + inchesw * inchesw);
    }

}
