package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.math.Vector2;

public class AttachmentPoint {

    public static final int POSITION_TYPE = 0;
    public static final int GLOBAL_VALUE_TYPE = 1;

    private Vector2 offset = new Vector2();
    private int type = POSITION_TYPE;

    private int value = 0; // used when type is global value only

    private String boneName;

    public AttachmentPoint() {
        value = 0;
        type = POSITION_TYPE;
    }

    public AttachmentPoint(int value) {
        this.value = value;
        type = GLOBAL_VALUE_TYPE;
    }

    @Override
    public int hashCode() {
        return type * 10 + value;
    }

    public void setData(String boneName, float offsetX, float offsetY) {
        this.boneName = boneName;
        offset.set(offsetX, offsetY);
    }
}
