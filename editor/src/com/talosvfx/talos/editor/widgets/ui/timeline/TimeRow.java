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

        float timePosition = dataProviderRef.getTimePosition();

        if(dataProviderRef.isFull()) {
            float durationOne = dataProviderRef.getDurationOne();
            areaWidget.setFull(durationOne, timePosition);
        } else {
            float durationOne = dataProviderRef.getDurationOne();
            float durationTwo = dataProviderRef.getDurationTwo();
            areaWidget.set(durationOne, durationTwo, timePosition);
        }

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

        private final Table fullTable;
        private final Table partTable;
        private final Image left;
        private final Image right;
        private float leftDuration;
        private float rightDuration;
        private float timePosition;

        public AreaWidget(Skin skin) {

            setSkin(skin);

            Stack stack = new Stack();

            partTable = new Table();
            fullTable = new Table();

            stack.add(partTable);
            stack.add(fullTable);

            fullTable.setVisible(false);

            left = new Image(getSkin().getDrawable("timeline-timeline-item-left"));
            right = new Image(getSkin().getDrawable("timeline-timeline-item-right"));
            Image full = new Image(getSkin().getDrawable("timeline-timeline-item-full"));

            partTable.addActor(left);
            partTable.addActor(right);

            fullTable.add(full).grow();

            add(stack).expand().grow();
        }

        public void set (float durationOne, float durationTwo, float timePosition) {
            leftDuration = durationOne;
            rightDuration = durationTwo;
            this.timePosition = timePosition;

            partTable.setVisible(true);
            fullTable.setVisible(false);
        }

        @Override
        public void setWidth (float width) {
            super.setWidth(width);

            if(rightDuration > 0) {
                left.setWidth((width * leftDuration) / getTimeSize());
                right.setWidth((width * rightDuration) / getTimeSize());
                right.setX(left.getWidth());
            }
        }

        public float getTimeSize () {
            return leftDuration + rightDuration;
        }

        public float getTimePosition() {
            return timePosition;
        }

        public void setFull (float durationOne, float timePosition) {
            partTable.setVisible(false);
            fullTable.setVisible(true);

            this.leftDuration = durationOne;
            this.rightDuration = 0;

            this.timePosition = timePosition;
        }
    }

    @Override
    public void setFrom(TimelineItemDataProvider<U> dataProvider) {
        super.setFrom(dataProvider);

        dataProviderRef = dataProvider;
    }
}
