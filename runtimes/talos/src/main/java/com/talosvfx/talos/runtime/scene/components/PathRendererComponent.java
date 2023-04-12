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
    public float repeatCount = 5f;

    private short[] indices;
    private float[] vertices;
    private Array<Vector2> edgePoints = new Array<>();

    private Array<Vector2> controlPoints = new Array<>();

    private Array<Vector2> points = new Array<>();

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
    private float THRESHOLD = 0.0001f;


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
        repeatCount = jsonData.getFloat("repeatCount", 5f);

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
        this.controlPoints = controlPoints;
        points.clear();
        edgePoints.clear();
        vectorPool.freeAll(points);
        vectorPool.freeAll(edgePoints);
        progress = 0;
        if (this.prev != null) {
            vectorPool.free(this.prev);
        }
        for (int i = 0; i < getNumSegments(); i++) {
            Vector2[] pointsInSegment = getPointsInSegment(i);

            bezier.set(pointsInSegment);

            float step = 1f / 20f;

            for (float t = 0; t <= 1f; t += step) {
                Vector2 curr = bezier.valueAt(tmp2, t);
                points.add(vectorPool.obtain().set(curr));
            }
            Vector2 curr = bezier.valueAt(tmp2, 1f);
            points.add(vectorPool.obtain().set(curr));
        }

        float length = 0;
        Vector2 prev = vectorPool.obtain().set(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size; i++) {
            length += points.get(i).dst(prev);
            prev.set(points.get(i));
        }
        vectorPool.free(prev);

        float pixelSize = repeatCount / length;

        addCriticalPoints(pixelSize);

        computeEdgePoints(points, thickness);

        int ATTRIBUTE_COUNT = 5;
        int verticesLength = (int) (edgePoints.size * ATTRIBUTE_COUNT + (Math.ceil(repeatCount)) * 2 * ATTRIBUTE_COUNT);
        if (vertices == null || verticesLength != vertices.length) {
            vertices = new float[verticesLength];

            int indicesLength = (vertices.length / ATTRIBUTE_COUNT - 2) * 3;
            indices = new short[indicesLength];
        }

        int idx = 0;
        this.prev = null;
        progress = 0;
        for (int i = 0; i < points.size; i++) {
            idx = setData(idx, edgePoints.get(i * 2).x, edgePoints.get(i * 2).y, edgePoints.get(i * 2 + 1).x,
                    edgePoints.get(i * 2 + 1).y, points.get(i).x, points.get(i).y, pixelSize);
        }

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

    private void addCriticalPoints(float pixelSize) {
        Vector2 prev = vectorPool.obtain().set(points.get(0));
        float progress = 0f;
        int size = points.size;
        for (int i = 1; i < size; i++) {
            Vector2 point = points.get(i);
            float length = point.dst(prev) * pixelSize;
            if (progress + length > 1f) {
                float x = (1f - progress) / pixelSize;
                float distance = prev.dst(point);
                float newX = prev.x + (x / distance) * (point.x - prev.x);
                float newY = prev.y + (x / distance) * (point.y - prev.y);
                Vector2 newPoint = vectorPool.obtain().set(newX, newY);
                float v = progress + prev.dst(newPoint) * pixelSize;
                points.insert(i, newPoint);
                size++;
                progress = 0;
                prev.set(newX, newY);
                continue;
            } else {
                progress += length;
            }
            prev.set(point);
        }
        vectorPool.free(prev);
    }

    public int getNumSegments() {
        return controlPoints.size / 3;
    }

    void computeEdgePoints(Array<Vector2> curvePoints, float offsetDistance) {
        Vector2 firstTangent = getTangent(curvePoints.get(1), curvePoints.get(0));
        Vector2 firstNormal = vectorPool.obtain().set(-firstTangent.y, firstTangent.x).nor();
        edgePoints.add(vectorPool.obtain().set(curvePoints.get(0)).add(firstNormal.scl(offsetDistance)));
        edgePoints.add(vectorPool.obtain().set(curvePoints.get(0)).add(firstNormal.scl(-1)));
        vectorPool.free(firstNormal);
        vectorPool.free(firstTangent);

        for (int i = 1; i < curvePoints.size - 1; i++) {
            Vector2 p = curvePoints.get(i);
            Vector2 t = getTangent(curvePoints.get(i + 1), curvePoints.get(i - 1));
            Vector2 normal = vectorPool.obtain().set(-t.y, t.x).nor();
            edgePoints.add(vectorPool.obtain().set(p).add(normal.scl(offsetDistance)));
            edgePoints.add(vectorPool.obtain().set(p).add(normal.scl(-1)));

            vectorPool.free(normal);
            vectorPool.free(t);
        }

        int length = curvePoints.size;
        Vector2 lastTangent = getTangent(curvePoints.get(length - 1), curvePoints.get(length - 2));
        Vector2 lastNormal = vectorPool.obtain().set(-lastTangent.y, lastTangent.x).nor();
        edgePoints.add(vectorPool.obtain().set(curvePoints.get(length - 1)).add(lastNormal.scl(offsetDistance)));
        edgePoints.add(vectorPool.obtain().set(curvePoints.get(length - 1)).add(lastNormal.scl(-1)));
        vectorPool.free(lastNormal);
        vectorPool.free(lastTangent);
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
        TextureAtlas.AtlasRegion region = gameAsset.getResource();

        if (prev != null) {
            progress += prev.dst(centerX, centerY) * pixelSize;
        } else {
            prev = vectorPool.obtain();
        }


        float u = MathUtils.lerp(region.getU(), region.getU2(), progress);

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

        if (Math.abs(progress - 1f) < THRESHOLD) {
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
                tmpArr[i].set(controlPoints.get(loopIndex(index * 3 + i)));
            } else {
                tmpArr[i].set(controlPoints.get(index * 3 + i));
            }
        }
        return tmpArr;
    }

    public int loopIndex(int index) {
        return (index + controlPoints.size) % controlPoints.size;
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
