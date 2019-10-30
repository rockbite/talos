package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.math.Vector2;

public class AttachmentPoint {

    public static final int POSITION_TYPE = 0;
    public static final int GLOBAL_VALUE_TYPE = 1;

    private Vector2 offset = new Vector2();
    private int type;

    private int index = 0; // used when type is global value only

    private String boneName;

    public AttachmentPoint() {
        index = 0;
        type = POSITION_TYPE;
    }

    public AttachmentPoint(int value) {
        this.index = value;
        type = GLOBAL_VALUE_TYPE;
    }

    public void setBoneName(String boneName) {
        this.boneName = boneName;
    }

    @Override
    public int hashCode() {
        return type * 10 + index;
    }

    public void setData(String boneName, float offsetX, float offsetY) {
        this.boneName = boneName;
        offset.set(offsetX, offsetY);
    }

    public String getBoneName() {
        return boneName;
    }

    public int getIndex() {
        return index;
    }

    public float getOffsetX() {
        return offset.x;
    }

    public float getOffsetY() {
        return offset.y;
    }
}
