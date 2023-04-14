package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpriteTransformGizmo extends SmartTransformGizmo {

    private static final Logger logger = LoggerFactory.getLogger(SpriteTransformGizmo.class);

    private Vector2 tempVec2 = new Vector2();

    public SpriteTransformGizmo() {
        super();
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (isSelected()) {
            Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());

            viewport.screenToLocalCoordinates(vec);
            vec = viewport.getWorldFromLocal(vec.x, vec.y);

            if (isOnTouchedPoint(vec.x, vec.y)) {
                CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.RESIZE);
            } else if (isOnTouchedRotationArea(vec.x, vec.y)) {
                CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.ROTATE);
            }
        }

    }
    private float[] verts = new float[2 * 4];

    public void getBounds (Polygon boundingPolygon) {

        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

        // patch for negative width and height cases
        float signWidth = Math.signum(spriteRendererComponent.size.x);
        float signHeight = Math.signum(spriteRendererComponent.size.y);


        float width = signWidth * spriteRendererComponent.size.x;
        float height = signHeight * spriteRendererComponent.size.y;

        verts[0] = -width/2f;
        verts[1] = -height/2f;

        verts[2] = -width/2f;
        verts[3] = height/2f;

        verts[4] = width/2f;
        verts[5] = height/2f;

        verts[6] = width/2f;
        verts[7] = -height/2f;

        boundingPolygon.setPosition(transformComponent.worldPosition.x, transformComponent.worldPosition.y);
        boundingPolygon.setVertices(verts);
        boundingPolygon.setOrigin(0, 0);
        boundingPolygon.setRotation(transformComponent.worldRotation);

    }

    @Override
    protected void updatePointsFromComponent () {
        getWorldLocAround(tmp, 0, 0); // this is center position of camera

        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

        points[LB].set(tmp.x - spriteRendererComponent.size.x/2f, tmp.y - spriteRendererComponent.size.y/2f);
        points[LT].set(tmp.x - spriteRendererComponent.size.x/2f, tmp.y + spriteRendererComponent.size.y/2f);
        points[RT].set(tmp.x + spriteRendererComponent.size.x/2f, tmp.y + spriteRendererComponent.size.y/2f);
        points[RB].set(tmp.x + spriteRendererComponent.size.x/2f, tmp.y - spriteRendererComponent.size.y/2f);

        points[LB].rotateAroundDeg(tmp, transformComponent.worldRotation);
        points[LT].rotateAroundDeg(tmp, transformComponent.worldRotation);
        points[RT].rotateAroundDeg(tmp, transformComponent.worldRotation);
        points[RB].rotateAroundDeg(tmp, transformComponent.worldRotation);

        tmp.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
        updateRotationAreas(tmp.x, tmp.y);
    }

    @Override
    protected void transformOldToNew () {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);


        int howMany90Rots = MathUtils.floor(transform.worldRotation / 90);
        int howMany180Rots = MathUtils.floor(transform.worldRotation / 180f);
        int sig = (int) Math.pow(-1, howMany90Rots + howMany180Rots);

        spriteRendererComponent.size.set(nextPoints[RB].dst(nextPoints[LB]) * Math.signum(nextPoints[RB].x - nextPoints[LB].x) * sig, nextPoints[LB].dst(nextPoints[LT]) * Math.signum(nextPoints[LT].y - nextPoints[LB].y) * sig);
        spriteRendererComponent.size = lowerPrecision(spriteRendererComponent.size);


        // if aspect ratio is fixed set height by width
        if (spriteRendererComponent.shouldFixAspectRatio(false)) {
            AtlasSprite texture = spriteRendererComponent.getGameResource().getResource();

            if (texture != null) {
                final float aspect = texture.getRegionHeight() * 1f / texture.getRegionWidth();
                spriteRendererComponent.size.y = spriteRendererComponent.size.x * aspect;
            }
        }


        if (touchedPoint == RT) {
            tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(transform.worldRotation);

            transform.position.set(nextPoints[LB]).add(tempVec2.x, tempVec2.y);
        } else if (touchedPoint == LT) {
            tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(-transform.worldRotation);

            transform.position.set(nextPoints[RB]).add(-tempVec2.x, tempVec2.y);
        } else if (touchedPoint == LB) {
            tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(transform.worldRotation);

            transform.position.set(nextPoints[RT]).add(-tempVec2.x, -tempVec2.y);
        } else if (touchedPoint == RB) {
            tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(-transform.worldRotation);

            transform.position.set(nextPoints[LT]).add(tempVec2.x, -tempVec2.y);
        }

        transform.position.sub(gameObject.getTransformSettings().offsetX, gameObject.getTransformSettings().offsetY);


        TransformComponent.worldToLocal(gameObject.parent, transform.position);
        transform.position.sub(gameObject.getTransformSettings().offsetX, gameObject.getTransformSettings().offsetY);
        transform.position = lowerPrecision(transform.position);
    }

    @Override
    protected void reportResizeUpdated (boolean isRapid) {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        SceneUtils.componentUpdated(gameObjectContainer, gameObject, transform, isRapid);

        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);
        SceneUtils.componentUpdated(gameObjectContainer, gameObject, spriteRendererComponent, isRapid);
    }


}
