package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public class UIUtils {

    public static void invalidateForDepth(Group group, int depth) {
        if (depth <= 0) return;
        if(group.getParent() == null || !(group.getParent() instanceof Layout)) return;
        Layout parent = (Layout) group.getParent();
        if (parent != null) {
            parent.invalidate();
            invalidateForDepth(group.getParent(), depth - 1);
        }
    }
}
