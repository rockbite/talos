package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

public class ImportUtils {

    private static Color c = new Color();

    public static int[] getSplits (Pixmap raster) {

        int startX = getSplitPoint(raster, 1, 0, true, true);
        int endX = getSplitPoint(raster, startX, 0, false, true);
        int startY = getSplitPoint(raster, 0, 1, true, false);
        int endY = getSplitPoint(raster, 0, startY, false, false);

        // Ensure pixels after the end are not invalid.
        getSplitPoint(raster, endX + 1, 0, true, true);
        getSplitPoint(raster, 0, endY + 1, true, false);

        // No splits, or all splits.
        if (startX == 0 && endX == 0 && startY == 0 && endY == 0) return null;

        // Subtraction here is because the coordinates were computed before the 1px border was stripped.
        if (startX != 0) {
            startX--;
            endX = raster.getWidth() - 2 - (endX - 1);
        } else {
            // If no start point was ever found, we assume full stretch.
            endX = raster.getWidth() - 2;
        }
        if (startY != 0) {
            startY--;
            endY = raster.getHeight() - 2 - (endY - 1);
        } else {
            // If no start point was ever found, we assume full stretch.
            endY = raster.getHeight() - 2;
        }

        return new int[] {startX, endX, startY, endY};
    }

    private static int getSplitPoint (Pixmap raster, int startX, int startY, boolean startPoint, boolean xAxis) {
        int[] rgba = new int[4];

        int next = xAxis ? startX : startY;
        int end = xAxis ? raster.getWidth() : raster.getHeight();
        int breakA = startPoint ? 255 : 0;

        int x = startX;
        int y = startY;
        while (next != end) {
            if (xAxis)
                x = next;
            else
                y = next;

            int colint = raster.getPixel(x, y);
            c.set(colint);
            rgba[0] = (int)(c.r * 255);
            rgba[1] = (int)(c.g * 255);
            rgba[2] = (int)(c.b * 255);
            rgba[3] = (int)(c.a * 255);
            if (rgba[3] == breakA) return next;

            if (!startPoint && (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 255))
                System.out.println(x + "  " + y + " " + rgba + " ");

            next++;
        }

        return 0;
    }

    public static Pixmap cropImage(Pixmap pixmap, int x, int y, int width, int height) {
        Pixmap result = new Pixmap(width, height, pixmap.getFormat());

        for(int i = x; i < width; i++) {
            for(int j = y; j < height; j++) {
                int pixel = pixmap.getPixel(i, j);
                result.drawPixel(i - x, j - y, pixel);
            }
        }

        return result;
    }
}
