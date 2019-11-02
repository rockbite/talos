package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class AttachmentPoint {

    private Type type;
    private AttachmentType attachmentType;
    private int attachedToSlot = 0;

    /**
     * Attached to bone
     */
    private String boneName;
    private Vector2 offset = new Vector2();

    /**
     * attached to static numeric value
     */
    private NumericalValue numericalValue = new NumericalValue();

    public boolean isStatic() {
        return type == Type.STATIC;
    }

    public boolean seAttached() {
        return type == Type.ATTACHED;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public NumericalValue getStaticValue() {
        return numericalValue;
    }

    enum Type {
        STATIC,
        ATTACHED
    }

    enum AttachmentType {
        POSITION,
        ROTATION
    }

    public AttachmentPoint() {
        setTypeStatic(numericalValue, 0);
    }

    public void setTypeAttached(String bone, int toSlot) {
        type = Type.ATTACHED;
        this.attachedToSlot = toSlot;
        attachmentType = AttachmentType.POSITION;
        boneName = bone;
    }

    public void setTypeStatic(NumericalValue value, int toSlot) {
        type = Type.STATIC;
        this.attachedToSlot = toSlot;
        numericalValue.set(value);
    }

    public void setTypeAttached(AttachmentType attachmentType) {
        if(type == Type.ATTACHED) {
            this.attachmentType = attachmentType;
        }
    }

    @Override
    public int hashCode() {
        return attachedToSlot;
    }

    public void setOffset(float offsetX, float offsetY) {
        offset.set(offsetX, offsetY);
    }

    public String getBoneName() {
        return boneName;
    }

    public int getSlotId() {
        return attachedToSlot;
    }

    public float getOffsetX() {
        return offset.x;
    }

    public float getOffsetY() {
        return offset.y;
    }

    public void setOffsetX (float offsetX) {
        offset.x = offsetX;
    }

    public void setOffsetY (float offsetY) {
        offset.y = offsetY;
    }
}
