package com.talosvfx.talos.editor.addons.uieditor.runtime.anchors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;

/**
 * Describes the strategy of positioning a ui element
 */
public class Anchor implements Pool.Poolable {

    /**
     * Actor to anchor, can only be either parent or a same level neighbour
     */
    private Actor anchorActor;

    private RelativePosition position = new RelativePosition();

    private RelativeValue relativeWidth = new RelativeValue();
    private RelativeValue relativeHeight = new RelativeValue();

    public boolean isMovable = true;

    public float getX(float boxWidth) {
        return calcX(boxWidth) + position.offsetX;
    }

    public float getY(float boxHeight) {
        return calcY(boxHeight) + position.offsetY;
    }


    public float calcX (float boxWidth) {
        if(anchorActor == null) return position.offsetX;

        float anchorPos = 0;
        if(position.isOuter) anchorPos = anchorActor.getX();

        float pos = anchorPos + anchorActor.getWidth()/2f - boxWidth/2f;

        if (Align.isLeft(position.anchorAlign)) {
            pos = anchorPos - (position.isOuter ? boxWidth : 0);
        } else if(Align.isRight(position.anchorAlign)) {
            pos = anchorPos + anchorActor.getWidth() + (position.isOuter ? 0 : -boxWidth);
        }

        return pos;
    }

    public float calcY (float boxHeight) {
        if(anchorActor == null) return position.offsetY;

        float anchorPos = 0;
        if(position.isOuter) anchorPos = anchorActor.getY();

        float pos = anchorPos + anchorActor.getHeight()/2f - boxHeight/2f;

        if (Align.isBottom(position.anchorAlign)) {
            pos = anchorPos - (position.isOuter ? boxHeight : 0);
        } else if(Align.isTop(position.anchorAlign)) {
            pos = anchorPos + anchorActor.getHeight() + (position.isOuter ? 0 : -boxHeight);
        }

        return pos;
    }

    @Override
    public void reset () {
        anchorActor = null;
        position.isOuter = false;
    }

    public boolean isAnchored () {
        return anchorActor != null;
    }

    public float computeWidth () {
        if(isAnchored()) {
            relativeWidth.setAttached(true);
            relativeWidth.updateSourceValue(anchorActor.getWidth());
        }

        return relativeWidth.getValue();
    }

    public float computeHeight () {
        if(isAnchored()) {
            relativeHeight.setAttached(true);
            relativeHeight.updateSourceValue(anchorActor.getHeight());
        }

        return relativeHeight.getValue();
    }

    public void setAlign (int align) {
        position.anchorAlign = align;
    }

    public void setOffset (float x, float y) {
        position.offsetX = x;
        position.offsetY = y;
    }

    public void setSize (float widthPercent, float widthOffset, float heightPercent, float heightOffset) {
        relativeWidth.setPercent(widthPercent);
        relativeHeight.setPercent(heightPercent);
        relativeWidth.setOffset(widthOffset);
        relativeHeight.setOffset(heightOffset);
    }

    public void anchorTo (Actor actor) {
        anchorActor = actor;
    }

    public boolean isMovable () {
        return isMovable;
    }

    public void setOffsetFromPosition (float width, float height, float x, float y) {
        float alignedX = calcX(width);
        float alignedY = calcY(height);

        position.offsetX = x - alignedX;
        position.offsetY = y - alignedY;
    }

    public void updateAnchorFromSize (float width, float height) {
        relativeWidth.offset = width - relativeWidth.percent * relativeWidth.sourceValue;
        relativeHeight.offset = height - relativeHeight.percent * relativeHeight.sourceValue;
    }
}
