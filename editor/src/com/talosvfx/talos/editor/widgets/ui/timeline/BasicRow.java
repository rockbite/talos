package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public abstract class BasicRow<U> extends ListItem<U> {

    private final Drawable selectedBackground;
    private final Drawable[] backgrounds;

    protected TimelineWidget timeline;

    public BasicRow(TimelineWidget timeline) {
        setSkin(timeline.getSkin());

        this.timeline = timeline;

        Drawable lightBackground = getSkin().getDrawable("timeline-row-light");
        Drawable darkBackground = getSkin().getDrawable("timeline-row-dark");

        backgrounds = new Drawable[2];
        backgrounds[0] = lightBackground;
        backgrounds[1] = darkBackground;

        selectedBackground = getSkin().getDrawable("timeline-row-selected");

        // default behaviour is not zebra pattern
        setBackground(lightBackground);

        setTouchable(Touchable.enabled);
    }

    @Override
    public void setSelected(boolean isSelected) {
        super.setSelected(isSelected);

        if(isSelected) {
            setBackground(selectedBackground);
        } else {
            setBackgroundByIndex(getIndex());
        }
    }

    private void setBackgroundByIndex(int index) {
        if(index == -1) {
            setBackground(backgrounds[0]);
        } else {
            int backgroundIndex = index % backgrounds.length;
            Drawable background = backgrounds[backgroundIndex];
            setBackground(background);
        }
    }

    public void setIndex (int index) {
        super.setIndex(index);
        setBackgroundByIndex(index);
    }
}
