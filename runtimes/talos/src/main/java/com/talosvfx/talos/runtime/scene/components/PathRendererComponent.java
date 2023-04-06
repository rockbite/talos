package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.IColorHolder;
import com.talosvfx.talos.runtime.scene.ISizableComponent;
import com.talosvfx.talos.runtime.scene.ValueProperty;

import java.util.UUID;


public class PathRendererComponent extends RendererComponent implements GameResourceOwner<TextureAtlas.AtlasRegion>, ISizableComponent, IColorHolder {
    public transient GameAsset<TextureAtlas.AtlasRegion> defaultGameAsset;
    public GameAsset<TextureAtlas.AtlasRegion> gameAsset;

    @ValueProperty
    public float thickness = 2f;

    public Color color = new Color(Color.WHITE);

    public transient Color finalColor = new Color();

    @ValueProperty
    public int repeatCount = 5;

    private short[] indices;
    private float[] vertices;
    private Vector2[] edgePoints;

    private Vector2[] controlPoints;

    private Vector2[] points;

    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();

    Bezier<Vector2> bezier = new Bezier<>();

    Pool<Vector2> vectorPool = new Pool<Vector2>() {
        @Override
        protected Vector2 newObject() {
            return new Vector2();
        }
    };

    transient GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate() {
            if (gameAsset.isBroken()) {
            } else {
            }
        }
    };


    @Override
    public GameAssetType getGameAssetType() {
        return GameAssetType.SPRITE;
    }

    @Override
    public GameAsset<TextureAtlas.AtlasRegion> getGameResource() {
        return gameAsset;
    }

    @Override
    public void write(Json json) {
        GameResourceOwner.writeGameAsset(json, this);

        json.writeValue("shouldInheritParentColor", shouldInheritParentColor());
        json.writeValue("thickness", thickness);
        json.writeValue("repeatCount", repeatCount);
        super.write(json);
    }

    private void loadTextureFromIdentifier(String gameResourceIdentifier) {
        GameAsset<TextureAtlas.AtlasRegion> assetForIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SPRITE);
        setGameAsset(assetForIdentifier);
    }

    private void loadTextureFromUniqueIdentifier(UUID gameResourceIdentifier) {
        GameAsset<TextureAtlas.AtlasRegion> assetForUniqueIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForUniqueIdentifier(gameResourceIdentifier, GameAssetType.SPRITE);
        setGameAsset(assetForUniqueIdentifier);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        UUID gameResourceUUID = GameResourceOwner.readGameResourceUUIDFromComponent(jsonData);
        if (gameResourceUUID == null) {
            String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);
            loadTextureFromIdentifier(gameResourceIdentifier);
        } else {
            loadTextureFromUniqueIdentifier(gameResourceUUID);
        }


        thickness = jsonData.getFloat("thickness", 3f);
        repeatCount = jsonData.getInt("repeatCount", 5);

        super.read(json, jsonData);
    }

    @Override
    public void setGameAsset(GameAsset<TextureAtlas.AtlasRegion> gameAsset) {
        if (this.gameAsset != null) {
            //Remove from old game asset, it might be the same, but it may also have changed
            this.gameAsset.listeners.removeValue(gameAssetUpdateListener, true);
        }

        if (defaultGameAsset == null && !gameAsset.isBroken()) {
            defaultGameAsset = gameAsset;
        }

        this.gameAsset = gameAsset;
        this.gameAsset.listeners.add(gameAssetUpdateListener);

        gameAssetUpdateListener.onUpdate();
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public void setWidth(float width) {

    }

    @Override
    public void setHeight(float height) {

    }

    @Override
    public void minMaxBounds(GameObject parentEntity, BoundingBox rectangle) {

    }

    public void setPoints(Array<Vector2> controlPoints) {
        this.controlPoints = controlPoints.toArray(Vector2.class);
        points = new Vector2[21 * getNumSegments()];
        int index = 0;
        for (int i = 0; i < getNumSegments(); i++) {
            Vector2[] pointsInSegment = getPointsInSegment(i);

            bezier.set(pointsInSegment);

            float step = 1f / 20f;

            for (float t = 0; t <= 1f; t += step) {
                Vector2 curr = bezier.valueAt(tmp2, t);
                points[index++] = vectorPool.obtain().set(curr);
            }
            Vector2 curr = bezier.valueAt(tmp2, 1f);
            points[index++] = vectorPool.obtain().set(curr);
        }

        edgePoints = computeEdgePoints(points, thickness);


        int ATTRIBUTE_COUNT = 5;
        vertices = new float[edgePoints.length * ATTRIBUTE_COUNT + (repeatCount) * 2 * ATTRIBUTE_COUNT];

        float length = 0;
        Vector2 prev = new Vector2(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i++) {
            length += points[i].dst(prev);
            prev.set(points[i]);
        }

        float pixelSize = repeatCount / length;

        int idx = 0;
        this.prev = null;
        progress = 0;
        for (int i = 0; i < points.length; i++) {
            idx = setData(idx, edgePoints[i * 2].x, edgePoints[i * 2].y, edgePoints[i * 2 + 1].x, edgePoints[i * 2 + 1].y, points[i].x, points[i].y, pixelSize);

        }

        int indicesLength = (vertices.length / ATTRIBUTE_COUNT - 2) * 3;
        indices = new short[indicesLength];

        int tri = 0;
        for (int i = 0; i < idx / ATTRIBUTE_COUNT - 2; i += 2) {
            indices[tri++] = (short) i;
            indices[tri++] = (short) (i + 1);
            indices[tri++] = (short) (i + 2);
            indices[tri++] = (short) (i + 1);
            indices[tri++] = (short) (i + 2);
            indices[tri++] = (short) (i + 3);
        }
    }

    public int getNumSegments() {
        return controlPoints.length / 3;
    }

    Vector2[] computeEdgePoints(Vector2[] curvePoints, float offsetDistance) {
        Vector2[] offsetPoints = new Vector2[curvePoints.length * 2];

        Vector2 firstTangent = getTangent(curvePoints[1], curvePoints[0]);
        Vector2 firstNormal = vectorPool.obtain().set(-firstTangent.y, firstTangent.x).nor();
        offsetPoints[0] = vectorPool.obtain().set(curvePoints[0]).add(firstNormal.scl(offsetDistance));
        offsetPoints[1] = vectorPool.obtain().set(curvePoints[0]).add(firstNormal.scl(-1));

        int length = curvePoints.length;
        Vector2 lastTangent = getTangent(curvePoints[length - 1], curvePoints[length - 2]);
        Vector2 lastNormal = vectorPool.obtain().set(-lastTangent.y, lastTangent.x).nor();
        offsetPoints[offsetPoints.length - 2] = vectorPool.obtain().set(curvePoints[length - 1]).add(lastNormal.scl(offsetDistance));
        offsetPoints[offsetPoints.length - 1] = vectorPool.obtain().set(curvePoints[length - 1]).add(lastNormal.scl(-1));

        int idx = 2;
        for (int i = 1; i < curvePoints.length - 1; i++) {
            Vector2 p = curvePoints[i];
            Vector2 t = getTangent(curvePoints[i + 1], curvePoints[i - 1]);
            Vector2 normal = vectorPool.obtain().set(-t.y, t.x).nor();
            offsetPoints[idx++] = vectorPool.obtain().set(p).add(normal.scl(offsetDistance));
            offsetPoints[idx++] = vectorPool.obtain().set(p).add(normal.scl(-1));
        }
        return offsetPoints;
    }


    Vector2 getTangent(Vector2 p1, Vector2 p2) {
        Vector2 tangent = vectorPool.obtain().set(p2);
        tangent.sub(p1);
        tangent.nor();
        return tangent;
    }

    float progress = 0f;
    private Vector2 prev;

    public int setData(int idx, float x1, float y1, float x2, float y2, float centerX, float centerY, float pixelSize) {
        TextureRegion region = gameAsset.getResource();
        float u = MathUtils.lerp(region.getU(), region.getU2(), progress);

        if (prev != null) {
            progress += prev.dst(centerX, centerY) * pixelSize;
        } else {
            prev = new Vector2();
        }

        prev.set(centerX, centerY);

        float v1 = region.getV();
        float v2 = region.getV2();

        vertices[idx++] = x1; // x
        vertices[idx++] = y1; // y
        vertices[idx++] = getColor().toFloatBits(); // color
        vertices[idx++] = u; // u
        vertices[idx++] = v1; // v


        vertices[idx++] = x2; // x
        vertices[idx++] = y2; // y
        vertices[idx++] = getColor().toFloatBits(); // color
        vertices[idx++] = u; // u
        vertices[idx++] = v2; // v

        if (progress >= 1f) {
            progress = 0f;

            vertices[idx++] = x1; // x
            vertices[idx++] = y1; // y
            vertices[idx++] = getColor().toFloatBits(); // color
            vertices[idx++] = region.getU(); // u
            vertices[idx++] = v1; // v


            vertices[idx++] = x2; // x
            vertices[idx++] = y2; // y
            vertices[idx++] = getColor().toFloatBits(); // color
            vertices[idx++] = region.getU(); // u
            vertices[idx++] = v2; // v
        }
        return idx;
    }

    public transient Vector2[] tmpArr = new Vector2[]{new Vector2(), new Vector2(), new Vector2(), new Vector2()};

    public Vector2[] getPointsInSegment(int index) {
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                tmpArr[i].set(controlPoints[loopIndex(index * 3 + i)]);
            } else {
                tmpArr[i].set(controlPoints[index * 3 + i]);
            }
        }
        return tmpArr;
    }

    public int loopIndex(int index) {
        return (index + controlPoints.length) % controlPoints.length;
    }

    @Override
    public boolean shouldInheritParentColor() {
        return false;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Color getFinalColor() {
        return finalColor;
    }


    public void drawMap(Batch batch) {
        if (batch instanceof PolygonBatch) {
            PolygonBatch polygonBatch = (PolygonBatch) batch;

            polygonBatch.draw(gameAsset.getResource().getTexture(), vertices, 0, vertices.length, indices, 0, indices.length);
        }
    }

    @Override
    public void remove() {
        super.remove();
        vectorPool.clear();
    }
}
