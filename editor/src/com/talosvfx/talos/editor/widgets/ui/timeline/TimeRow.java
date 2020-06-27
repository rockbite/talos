package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class TimeRow<U> extends BasicRow<U> {

    private AreaWidget areaWidget;

    public TimeRow(TimelineWidget timeline) {
        super(timeline);

        areaWidget = new AreaWidget(getSkin());
        areaWidget.setWidth(100);
        areaWidget.setHeight(18);
        areaWidget.setPosition(0,2);

        addActor(areaWidget);
    }

    public void updateTimeWindow(float timeWindowPosition, float timeWindowSize) {
        float widgetTimeSize = 2f;
        float widgetWidth = (widgetTimeSize/timeWindowSize) * getWidth();

        areaWidget.setWidth(widgetWidth);
    }

    private class AreaWidget extends Table {

        public AreaWidget(Skin skin) {
            setSkin(skin);
            Image left = new Image(getSkin().getDrawable("timeline-timeline-item-left"));
            Image right = new Image(getSkin().getDrawable("timeline-timeline-item-right"));

            add(left).grow();
            add(right).grow();
        }

    }
}
