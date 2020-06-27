package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.FlatButton;

public class TimelineRight<U> extends AbstractList<TimeRow<U>, U> {

    private Table contentTable;
    private ScrollPane scrollPane;

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
        Slider slider = new Slider(0, 10, 1, true, getSkin(), "timeline-vertical");
        slider.setHeight(10);
        FlatButton down = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-play"));
        down.flipHorizontal();
        down.flipVertical();
        down.getIconCell().padTop(2);
        Table sliderTable = new Table();
        sliderTable.add(up).size(18).row();
        sliderTable.add(slider).growY().width(18).padTop(-1).padBottom(-1).row();
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
        topPart.add(timeBar).height(17).padTop(8).growX().expandY().top().row();

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

        Slider slider = new Slider(0, 10, 1, false, getSkin(), "timeline-horizontal");

        FlatButton left = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-play"));
        left.flipHorizontal();
        left.getIconCell().padTop(2).padRight(2);
        FlatButton right = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-play"));
        right.getIconCell().padTop(2).padLeft(2);

        Table sliderTable = new Table();
        sliderTable.add(left).padTop(-1).width(24).height(18);
        sliderTable.add(slider).padLeft(-1).padRight(-1).grow().height(18).padTop(1);
        sliderTable.add(right).padTop(-1).width(24).height(18);

        FlatButton defaultZoomBtn = new FlatButton(getSkin(), getSkin().getDrawable("timeline-btn-icon-resize"));
        defaultZoomBtn.getIconCell().padTop(1);

        Slider zoomSlider = new Slider(0, 10, 1, false, getSkin(), "mini-slider");

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
        contentTable.add(item).growX().top().height(25).row();
    }
}
