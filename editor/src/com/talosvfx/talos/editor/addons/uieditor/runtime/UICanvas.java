package com.talosvfx.talos.editor.addons.uieditor.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.talosvfx.talos.editor.addons.uieditor.runtime.anchors.Anchor;

public class UICanvas extends DynamicGroup {

    private Color canvasBg = new Color(0x282728ff);

    public UICanvas (Skin skin) {
        Drawable canvasBackground = skin.newDrawable("white", canvasBg);
        setBackground(canvasBackground);
    }

    public boolean isMovable (Actor target) {
        if(target == this) return false;

        Anchor anchor = getAnchor(target);
        if (anchor == null) return true;

        return anchor.isMovable();
    }
}
