package com.talosvfx.talos.runtime.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class NineSlice extends NinePatch {
    static private final Color tmpDrawColor = new Color();

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float tileWidth = 1;
    private float tileHeight = 1;
    private int tiled_idx;
    private RenderMode renderMode = RenderMode.Simple;

    // re-exposed for subclasses
    protected final TextureRegion[] patches = new TextureRegion[9];

    private static final float[] VERTICES_BUFF = new float[15000];


    /**
     * NineSlice can have two render modes.
     * Simple mode - corners are preserved, center chunk is scaled in both axis,
     *  top/bottom-middle chunks are scaled along horizontal axis,
     *  left/right-middle chunks are scaled along vertical axis.
     * Tiled mode - corners are preserved, center chunk is repeated in both axis,
     *  top/bottom-middle chunks are repeated along horizontal axis,
     *  left/right-middle chunks are repeated along vertical axis.
     */
    public enum RenderMode {
        Simple,
        Tiled
    }

    public NineSlice(Texture texture, int left, int right, int top, int bottom) {
        this(new TextureRegion(texture), left, right, top, bottom);
    }

    public NineSlice(TextureRegion region, int left, int right, int top, int bottom) {
        super(region, left, right, top, bottom);

        final int middleWidth = region.getRegionWidth() - left - right;
        final int middleHeight = region.getRegionHeight() - top - bottom;

        if (top > 0) {
            if (left > 0) patches[TOP_LEFT] = new TextureRegion(region, 0, 0, left, top);
            if (middleWidth > 0) patches[TOP_CENTER] = new TextureRegion(region, left, 0, middleWidth, top);
            if (right > 0) patches[TOP_RIGHT] = new TextureRegion(region, left + middleWidth, 0, right, top);
        }
        if (middleHeight > 0) {
            if (left > 0) patches[MIDDLE_LEFT] = new TextureRegion(region, 0, top, left, middleHeight);
            if (middleWidth > 0) patches[MIDDLE_CENTER] = new TextureRegion(region, left, top, middleWidth, middleHeight);
            if (right > 0) patches[MIDDLE_RIGHT] = new TextureRegion(region, left + middleWidth, top, right, middleHeight);
        }
        if (bottom > 0) {
            if (left > 0) patches[BOTTOM_LEFT] = new TextureRegion(region, 0, top + middleHeight, left, bottom);
            if (middleWidth > 0) patches[BOTTOM_CENTER] = new TextureRegion(region, left, top + middleHeight, middleWidth, bottom);
            if (right > 0) patches[BOTTOM_RIGHT] = new TextureRegion(region, left + middleWidth, top + middleHeight, right, bottom);
        }

        // If split only vertical, move splits from right to center.
        if (left == 0 && middleWidth == 0) {
            patches[TOP_CENTER] = patches[TOP_RIGHT];
            patches[MIDDLE_CENTER] = patches[MIDDLE_RIGHT];
            patches[BOTTOM_CENTER] = patches[BOTTOM_RIGHT];
            patches[TOP_RIGHT] = null;
            patches[MIDDLE_RIGHT] = null;
            patches[BOTTOM_RIGHT] = null;
        }
        // If split only horizontal, move splits from bottom to center.
        if (top == 0 && middleHeight == 0) {
            patches[MIDDLE_LEFT] = patches[BOTTOM_LEFT];
            patches[MIDDLE_CENTER] = patches[BOTTOM_CENTER];
            patches[MIDDLE_RIGHT] = patches[BOTTOM_RIGHT];
            patches[BOTTOM_LEFT] = null;
            patches[BOTTOM_CENTER] = null;
            patches[BOTTOM_RIGHT] = null;
        }
    }

    /** Construct a degenerate "nine" patch with only a center component. */
    public NineSlice(Texture texture, Color color) {
        this(texture);
        setColor(color);
    }

    public NineSlice(Texture texture) {
        this(new TextureRegion(texture));
    }

    /** Construct a degenerate "nine" patch with only a center component. */
    public NineSlice(TextureRegion region) {
        super(new TextureRegion[]{
                null, null, null,
                null, region, null,
                null, null, null
        });

        this.patches[BOTTOM_LEFT] = null;
        this.patches[BOTTOM_CENTER] = null;
        this.patches[BOTTOM_RIGHT] = null;
        this.patches[MIDDLE_LEFT] = null;
        this.patches[MIDDLE_CENTER] = region;
        this.patches[MIDDLE_RIGHT] = null;
        this.patches[TOP_LEFT] = null;
        this.patches[TOP_CENTER] = null;
        this.patches[TOP_RIGHT] = null;
    }


    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        if (renderMode == RenderMode.Simple) {
            super.draw(batch, x, y, width, height);
        } else if (renderMode == RenderMode.Tiled) {
            prepareVertices(batch, x, y, width, height);
            batch.draw(getTexture(), VERTICES_BUFF, 0, tiled_idx);
        }
    }

    @Override
    public void draw (Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX,
                      float scaleY, float rotation) {
        if (renderMode == RenderMode.Simple) {
            super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
        } else if (renderMode == RenderMode.Tiled) {
            prepareVertices(batch, x, y, width, height);
            float worldOriginX = x + originX, worldOriginY = y + originY;
            int n = this.tiled_idx;
            if (rotation != 0) {
                for (int i = 0; i < n; i += 5) {
                    float vx = (VERTICES_BUFF[i] - worldOriginX) * scaleX, vy = (VERTICES_BUFF[i + 1] - worldOriginY) * scaleY;
                    float cos = MathUtils.cosDeg(rotation), sin = MathUtils.sinDeg(rotation);
                    VERTICES_BUFF[i] = cos * vx - sin * vy + worldOriginX;
                    VERTICES_BUFF[i + 1] = sin * vx + cos * vy + worldOriginY;
                }
            } else if (scaleX != 1 || scaleY != 1) {
                for (int i = 0; i < n; i += 5) {
                    VERTICES_BUFF[i] = (VERTICES_BUFF[i] - worldOriginX) * scaleX + worldOriginX;
                    VERTICES_BUFF[i + 1] = (VERTICES_BUFF[i + 1] - worldOriginY) * scaleY + worldOriginY;
                }
            }
            batch.draw(getTexture(), VERTICES_BUFF, 0, n);
        }
    }
    private void prepareVertices(Batch batch, float x, float y, float width, float height) {
        float xSign = Math.signum(width);
        float ySign = Math.signum(height);

        if (xSign < 0) {
            x += width * xSign;
        }

        if (ySign < 0) {
            y += height * ySign;
        }

        final float rightWidth = getRightWidth();
        final float leftWidth = getLeftWidth();
        final float topHeight = getTopHeight();
        final float bottomHeight = getBottomHeight();
        final float centerWidth = Math.max(Math.abs(width) - rightWidth - leftWidth, 0);
        final float centerHeight = Math.max(Math.abs(height) - topHeight - bottomHeight, 0);
        final float centerX = x + xSign * leftWidth;
        final float centerY = y + ySign * bottomHeight;
        final float rightX = centerX + xSign * centerWidth;
        final float topY = centerY + ySign * centerHeight;

        int tiledVerticesSize = getVerticesSize(centerWidth, centerHeight);
        if (tiledVerticesSize > VERTICES_BUFF.length) {
            return;
        }

        tiled_idx = 0;

        final float c = tmpDrawColor.set(getColor()).mul(batch.getColor()).toFloatBits();

        // bottom-left corner
        if (patches[BOTTOM_LEFT] != null) {
            add(patches[BOTTOM_LEFT], false, false, c, x, y, leftWidth, bottomHeight, xSign, ySign);
        }

        // bottom-center side
        if (patches[BOTTOM_CENTER] != null) {
            add(patches[BOTTOM_CENTER], true, false, c, centerX, y, centerWidth, bottomHeight, xSign, ySign);
        }

        // bottom-right corner
        if (patches[BOTTOM_RIGHT] != null) {
            add(patches[BOTTOM_RIGHT], false, false, c, rightX, y, rightWidth, bottomHeight, xSign, ySign);
        }

        // middle-left side
        if (patches[MIDDLE_LEFT] != null) {
            add(patches[MIDDLE_LEFT], false, true, c, x, centerY, leftWidth, centerHeight, xSign, ySign);
        }

        // middle-center chunk
        if (patches[MIDDLE_CENTER] != null) {
            add(patches[MIDDLE_CENTER], true, true, c, centerX, centerY, centerWidth, centerHeight, xSign, ySign);
        }

        // middle-right side
        if (patches[MIDDLE_RIGHT] != null) {
            add(patches[MIDDLE_RIGHT], false, true, c, rightX, centerY, rightWidth, centerHeight, xSign, ySign);
        }

        // top-left corner
        if (patches[TOP_LEFT] != null) {
            add(patches[TOP_LEFT], false, false, c, x, topY, leftWidth, topHeight, xSign, ySign);
        }

        // top-center side
        if (patches[TOP_CENTER] != null) {
            add(patches[TOP_CENTER], true, false, c, centerX, topY, centerWidth, topHeight, xSign, ySign);
        }

        // top-right corner
        if (patches[TOP_RIGHT] != null) {
            add(patches[TOP_RIGHT], false, false, c, rightX, topY, rightWidth, topHeight, xSign, ySign);
        }
    }

    private int getVerticesSize(float centerWidth, float centerHeight) {
        int blCnt = patches[BOTTOM_LEFT] == null ? 0 : 1;
        int bcCnt = patches[BOTTOM_CENTER] == null ? 0 : (int) Math.ceil(centerWidth / (patches[BOTTOM_CENTER].getRegionWidth() * scaleX * tileWidth));
        int brCnt = patches[BOTTOM_RIGHT] == null ? 0 : 1;
        int mlCnt = patches[MIDDLE_LEFT] == null ? 0 : (int) Math.ceil(centerHeight / (patches[MIDDLE_LEFT].getRegionHeight() * scaleY * tileHeight));
        int mcCnt = patches[MIDDLE_CENTER] == null ? 0: (int)
                (Math.ceil(centerWidth / (patches[MIDDLE_CENTER].getRegionWidth() * scaleX * tileWidth))
                        * Math.ceil(centerHeight / (patches[MIDDLE_CENTER].getRegionHeight() * scaleY * tileHeight)));
        int mrCnt = patches[MIDDLE_RIGHT] == null ? 0 : (int) Math.ceil(centerHeight / (patches[MIDDLE_RIGHT].getRegionHeight() * scaleY * tileHeight));
        int tlCnt = patches[TOP_LEFT] == null ? 0 : 1;
        int tcCnt = patches[TOP_CENTER] == null ? 0 : (int) Math.ceil(centerWidth / (patches[TOP_CENTER].getRegionWidth() * scaleX * tileWidth));
        int trCnt = patches[TOP_RIGHT] == null ? 0 : 1;

        return  (blCnt + bcCnt + brCnt + mlCnt + mcCnt + mrCnt + tlCnt + tcCnt + trCnt) * 4 * 5;
    }

    private void add (TextureRegion region, boolean isTiledX, boolean isTiledY, float color, float x, float y, float width, float height, float signX, float signY) {

        float[] vertices = VERTICES_BUFF;

        float tileXModifier = isTiledX ? tileWidth : 1.0f;
        float tileYModifier = isTiledY ? tileHeight : 1.0f;

        final int horzCnt = (int) Math.ceil(width / (region.getRegionWidth() * scaleX * tileXModifier));
        final int vertCnt = (int) Math.ceil(height / (region.getRegionHeight() * scaleY * tileYModifier));

        for (int row = 0; row < vertCnt; row++) {
            for (int col = 0; col < horzCnt; col++) {
                float u = region.getU(), v = region.getV2(), u2 = region.getU2(), v2 = region.getV();

                int i = tiled_idx;

                final float regUnitWidth = region.getRegionWidth() * scaleX * tileXModifier;
                final float regUnitHeight = region.getRegionHeight() * scaleY * tileYModifier;

                float stepWidth = isTiledX ? regUnitWidth : width;
                stepWidth *= signX;
                float stepHeight = isTiledY ? regUnitHeight : height;
                stepHeight *= signY;

                float stepX = x + stepWidth * col;
                float stepY = y + stepHeight * row;
                float fx2, fy2;

                if (signX * stepWidth * (col + 1) > width) { // partial
                    float remaning = width - signX * stepWidth * col;
                    float percent = remaning / regUnitWidth;
                    fx2 = x + signX * width;
                    u2 = u + (u2 - u) * percent;
                } else {
                    fx2 = stepX + stepWidth;
                }

                if (signY * stepHeight * (row + 1) > height) { // partial
                    float remaning = height - signY * stepHeight * row;
                    float percent = remaning / regUnitHeight;

                    fy2 = y + signY * height;
                    v2 = v + (v2 - v) * percent;
                } else {
                    fy2 = stepY + stepHeight;
                }

                vertices[i] = stepX;
                vertices[i + 1] = stepY;
                vertices[i + 2] = color;
                vertices[i + 3] = u;
                vertices[i + 4] = v;


                vertices[i + 5] = stepX;
                vertices[i + 6] = fy2;
                vertices[i + 7] = color;
                vertices[i + 8] = u;
                vertices[i + 9] = v2;

                vertices[i + 10] = fx2;
                vertices[i + 11] = fy2;
                vertices[i + 12] = color;
                vertices[i + 13] = u2;
                vertices[i + 14] = v2;

                vertices[i + 15] = fx2;
                vertices[i + 16] = stepY;
                vertices[i + 17] = color;
                vertices[i + 18] = u2;
                vertices[i + 19] = v;

                tiled_idx += 20;
            }
        }
    }

    public float getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(float tileWidth) {
        this.tileWidth = Math.max(0.00001f, tileWidth);
    }

    public float getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(float tileHeight) {
        this.tileHeight = Math.max(0.00001f, tileHeight);
    }

    public void setTileSize (float tileWidth, float tileHeight) {
        setTileWidth(tileWidth);
        setTileHeight(tileHeight);
    }

    public void setRenderMode(RenderMode mode) {
        this.renderMode = mode;
    }


    public RenderMode getRenderMode() {
        return renderMode;
    }

    @Override
    public void scale(float scaleX, float scaleY) {
        super.scale(scaleX, scaleY);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
}
