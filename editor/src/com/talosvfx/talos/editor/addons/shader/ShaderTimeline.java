package com.talosvfx.talos.editor.addons.shader;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineWidget;

public class ShaderTimeline extends TimelineWidget<Object> {

    public ShaderTimeline (Skin skin) {
        super(skin);
    }

    @Override
    protected String getItemTypeName () {
        return "Shader";
    }
}
