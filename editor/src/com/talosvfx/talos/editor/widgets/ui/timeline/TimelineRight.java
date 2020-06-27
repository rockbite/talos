package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.DynamicSlider;
import com.talosvfx.talos.editor.widgets.ui.common.FlatButton;

public class TimelineRight<U> extends AbstractList<TimeRow<U>, U> {

    private Table contentTable;
    private ScrollPane scrollPane;

    private float timeWindowSize;
    private float timeWindowPosition = 0f;
    private float timeCursor = 0f;


    private Slider zoomSlider;

    private DynamicSlider timeSlider;
    private Slider scroll;

    public TimelineRight(TimelineWidget timeline) {
        super(timeline);

        setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.BackgroundColor.PANEL_GRAY));

        Table contentPane = buildContentContainerPane();
        Table bottomPanel = buildBottomPanel();

        Table mainPart = new Table();
        Table rightPart = buildRightPart();

        add(mainPart).grow();
        add(rightPart).width(18).growY();

        mainPart.add(contentPane).grow().row();
        mainPart.add(bottomPanel).height(18).growX().padLeft(-1).row();

        initListeners();

        Table controls = buildControls();
        contentPane.addActor(controls);
    }

    private void initListeners() {
        zoomSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateTimeWindow(zoomSlider.getValue());
            }
        });
    }

    private void updateTimeWindow(float value) {
        value = value / 100f; // normalize to 0-1
        value = 1f - value;

        timeWindowSize = (float) (5f + Math.pow(value * 4f, 3.425f));

    }

    private Table buildContentContainerPane () {
        Table content = new Table();

        Image leftSeparator = new Image(getSkin().getDrawable("timeline-secondary-separator"));

        Table midPart = new Table();

        content.add(leftSeparator).width(6).growY();
        content.add(midPart).padRight(-2).grow();

        Table header = buildHeader();
        Table mainContent = buildContentPain();

        midPart.add(header).height(51).growX().row();
        midPart.add(mainContent).grow().row();

        return content;
    }

    private Table buildControls() {
        Table content = new Table();

        TimeCursor timeCursorWidget = new TimeCursor(getSkin());
        timeCursorWidget.setPosition(0, 0);

        addActor(timeCursorWidget);

        return content;
    }

    private Table buildRightPart () {
        Table content = new Table();

        Image border = new Image(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.BackgroundColor.BLACK));

        content.add(border).width(1).growY();

        Table mainTable = new Table();
        content.add(mainTable).width(17).growY();

        mainTable.add().height(33).row();

        FlatButton up = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-play"));
        up.flipVertical();
        up.getIconCell().padTop(2);

        scroll = new Slider(0, 100, 1, true, getSkin(), "timeline-vertical");
        scroll.setHeight(10);
        FlatButton down = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-play"));
        down.flipHorizontal();
        down.flipVertical();
        down.getIconCell().padTop(2).padRight(2);
        Table sliderTable = new Table();
        sliderTable.add(up).size(18).row();
        sliderTable.add(scroll).growY().width(18).padTop(-1).padBottom(-1).row();
        sliderTable.add(down).size(18).row();

        mainTable.add(sliderTable).padLeft(-1).grow().row();

        mainTable.add().height(17);

        return content;
    }

    private Table buildHeader () {
        Table header = new Table();
        header.setBackground(getSkin().getDrawable("timeline-top-bar-bg"));

        Table topPart = new Table();
        Table bottomPart = new Table();

        header.add(topPart).height(33).padBottom(1).growX().row();
        header.add(bottomPart).height(16).growX().row();

        Table timeBar = new Table();
        timeBar.setBackground(getSkin().getDrawable("timeline-time-bar"));
        topPart.add(timeBar).height(17).padTop(7).growX().expandY().top().row();

        return header;
    }

    private Table buildContentPain () {
        Table content = new Table();

        content.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.BackgroundColor.DARK_GRAY));

        contentTable = new Table();
        scrollPane = new ScrollPane(contentTable);
        scrollPane.setSmoothScrolling(false);
        scrollPane.setOverscroll(false, false);

        content.add(scrollPane).growX().expand().top().row();

        return content;
    }

    public float getScrollPos() {
        return scrollPane.getScrollY();
    }

    public void setScrollPos(float pos) {
        scrollPane.setScrollY(pos);
    }

    private Table buildBottomPanel () {
        Table contentContainer = new Table();

        timeSlider = new DynamicSlider( false, getSkin());
        timeSlider.setValue(0);
        timeSlider.updateConfig(0, timeWindowSize);

        FlatButton left = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-play"));
        left.flipHorizontal();
        left.getIconCell().padTop(2).padRight(2);
        FlatButton right = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-play"));
        right.getIconCell().padTop(2).padLeft(2);

        Table sliderTable = new Table();
        sliderTable.add(left).padTop(0).width(24).height(18);
        sliderTable.add(timeSlider).padLeft(-1).padRight(-1).grow().height(17).padTop(0);
        sliderTable.add(right).padTop(0).width(24).height(18);

        FlatButton defaultZoomBtn = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-resize"));
        defaultZoomBtn.getIconCell().padTop(1);

        zoomSlider = new Slider(0, 100, 1, false, getSkin(), "mini-slider");
        zoomSlider.setValue(50);
        updateTimeWindow(zoomSlider.getValue());

        Image border = new Image(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.BackgroundColor.BLACK));

        contentContainer.add(border).growX().height(1).expandY().top().row();
        Table content = new Table();

        content.add(sliderTable).left().grow();
        content.add(defaultZoomBtn).padLeft(-1);
        content.add(zoomSlider).pad(4);

        contentContainer.add(content).height(18).padTop(-1).growX();

        return contentContainer;
    }


    @Override
    protected TimeRow<U> createNewItem() {
        return new TimeRow<U>(timeline);
    }

    @Override
    protected void addItem(TimeRow<U> item) {
        super.addItem(item);

        addItemToTable(item);

        item.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                if(event.isCancelled()) return;

                timeline.onRowClicked(item);
            }
        });
    }

    @Override
    public void clearItems() {
        super.clearItems();

        contentTable.clearChildren();
    }

    @Override
    protected void rebuildFromData() {
        contentTable.clearChildren();

        for(TimeRow item: getItems()) {
            addItemToTable(item);
        }
    }

    private void addItemToTable(Table item) {
        contentTable.add(item).growX().top().height(21).row();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // update time related values
        for(TimeRow<U> row: getItems()) {
            row.updateTimeWindow(timeWindowPosition, timeWindowSize);
        }
        timeSlider.updateConfig(0, timeWindowSize);
    }
}
