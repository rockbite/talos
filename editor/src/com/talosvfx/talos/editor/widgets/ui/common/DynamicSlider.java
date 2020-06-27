package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import org.lwjgl.system.MathUtil;

public class DynamicSlider extends Slider {

    private Skin skin;

    private final Drawable knobDrawable;

    private float dataSize;
    private float windowSize;

    public DynamicSlider(boolean vertical, Skin skin) {
        super(0, 100, 1, vertical, skin);
        setSkin(skin);

        String drawableName = "timeline-slider-" + (vertical? "vertical" : "horizontal");
        knobDrawable = getSkin().newDrawable(drawableName);
        Drawable staticDrawable = getStyle().knobAfter = getSkin().getDrawable(drawableName);
        getStyle().knob = knobDrawable;
        getStyle().knobOver = knobDrawable;
        getStyle().knobDown = knobDrawable;

        getStyle().background = getSkin().getDrawable("timeline-slider-bg");
        getStyle().knobAfter = getSkin().getDrawable("timeline-slider-bg");

        knobDrawable.setMinWidth(0);

    }

    public void updateConfig(float dataSize, float windowSize) {
        this.dataSize = dataSize;
        this.windowSize = windowSize;

        float sliderSizePercent = windowSize/dataSize;
        sliderSizePercent = MathUtils.clamp(sliderSizePercent, 0, 1);

        knobDrawable.setMinWidth(sliderSizePercent * getWidth());
    }

    private void setSkin(Skin skin) {
        this.skin = skin;
    }

    private Skin getSkin() {
        return skin;
    }
}
