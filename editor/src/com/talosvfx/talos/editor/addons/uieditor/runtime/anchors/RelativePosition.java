package com.talosvfx.talos.editor.addons.uieditor.runtime.anchors;

public class RelativePosition {

    /**
     * is this anchored from outside of the anchoring actor or form inside.
     */
    public boolean isOuter = false;

    /**
     * @Align based integer for alignment
     */
    public int anchorAlign;

    /**
     * hard set float offsets for positioning on top of anchoring
     */
    public float offsetX;
    public float offsetY;


}
