package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.talosvfx.talos.TalosMain;

public class ScreenshotService {

    private boolean scheduled = false;

    private ScreenshotListener listener;

    private int posX;
    private int posY;

    private Color color = new Color();
    private static Vector2 vec = new Vector2();

    public ScreenshotService() {

    }

    public void take(int x, int y, ScreenshotListener listener) {
        this.listener = listener;
        posX = x;
        posY = y;
        scheduled = true;
    }

    public interface ScreenshotListener {
        void onComplete(Color color);
    }

    /**
     * call this after all is rendered
     */
    public void postRender() {
        if(scheduled) {

            if(listener != null) {
                takeScreenshot();
                listener.onComplete(color);
            }

            scheduled = false;
        }
    }

    private void takeScreenshot() {
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);

        float scaleX = (float)Gdx.graphics.getBackBufferWidth()/Gdx.graphics.getWidth();
        float scaleY = (float)Gdx.graphics.getBackBufferHeight()/Gdx.graphics.getHeight();

        // This loop makes sure the whole screenshot is opaque and looks exactly like what the user is seeing
        for (int i = 4; i < pixels.length; i += 4) {
            pixels[i - 1] = (byte) 255;
        }

        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);

        color.set(pixmap.getPixel((int)(posX * scaleX), (int)(posY * scaleY)));

        pixmap.dispose();
    }

    public static void testForPicker(ColorPicker picker) {
        if(picker.getStage() == null) {

//            TalosMain.Instance().setCursor(null);
            return;
        }

        vec.set(Gdx.input.getX(), Gdx.input.getY());
        (picker.getStage().getViewport()).unproject(vec);
        picker.stageToLocalCoordinates(vec);
        boolean hit = vec.x > 0 && vec.y > 0 && vec.x < picker.getWidth() && vec.y < picker.getHeight();

        if(!hit) {
            // should be different icon
//            TalosMain.Instance().setCursor(TalosMain.Instance().pickerCursor);

            if (Gdx.input.justTouched()) {
                TalosMain.Instance().Screeshot().take(Gdx.input.getX(), Gdx.input.getY(), new ScreenshotService.ScreenshotListener() {
                    @Override
                    public void onComplete (Color color) {
                        picker.setColor(color);
                    }
                });
            }
        } else {
            // usual icon
//            TalosMain.Instance().setCursor(null);
        }
    }

}
