package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.ui.*;

public class TimeRow<U> extends BasicRow<U> {

    private AreaWidget areaWidget;

    private TimelineItemDataProvider<U> dataProviderRef;

    public TimeRow(TimelineWidget timeline) {
        super(timeline);

        areaWidget = new AreaWidget(getSkin());
        areaWidget.setWidth(100);
        areaWidget.setHeight(18);
        areaWidget.setPosition(0,2);

        addActor(areaWidget);
    }

    public void updateTimeWindow(float timeWindowPosition, float timeWindowSize) {
        if(dataProviderRef == null) return;

        float durationOne = dataProviderRef.getDurationOne();
        float durationTwo = dataProviderRef.getDurationTwo();
        float timePosition = dataProviderRef.getTimePosition();

        areaWidget.set(durationOne, durationTwo, timePosition);

        float widgetWidth = (areaWidget.getTimeSize()/timeWindowSize) * getWidth();

        areaWidget.setWidth(widgetWidth);

        if(areaWidget.getTimeSize() == 0) {
            areaWidget.setVisible(false);
        } else {
            areaWidget.setVisible(true);
        }

        areaWidget.setPosition((timePosition/timeWindowSize) * getWidth(),2);
    }

    private class AreaWidget extends Table {

        private final Cell<Image> leftCell;
        private final Cell<Image> rightCell;
        private float leftDuration;
        private float rightDuration;
        private float timePosition;

        public AreaWidget(Skin skin) {
            setSkin(skin);
            Image left = new Image(getSkin().getDrawable("timeline-timeline-item-left"));
            Image right = new Image(getSkin().getDrawable("timeline-timeline-item-right"));

            leftCell = add(left).grow();
            rightCell = add(right).grow();
        }

        public void set (float durationOne, float durationTwo, float timePosition) {
            leftDuration = durationOne;
            rightDuration = durationTwo;
            this.timePosition = timePosition;

            leftCell.width(Value.percentWidth(leftDuration/getTimeSize(), this));
            rightCell.width(Value.percentWidth(rightDuration/getTimeSize(), this));
        }

        public float getTimeSize () {
            return leftDuration + rightDuration;
        }

        public float getTimePosition() {
            return timePosition;
        }
    }

    @Override
    public void setFrom(TimelineItemDataProvider<U> dataProvider) {
        super.setFrom(dataProvider);

        dataProviderRef = dataProvider;

        float durationOne = dataProvider.getDurationOne();
        float durationTwo = dataProvider.getDurationTwo();
        float timePosition = dataProvider.getTimePosition();

        areaWidget.set(durationOne, durationTwo, timePosition);
    }
}
