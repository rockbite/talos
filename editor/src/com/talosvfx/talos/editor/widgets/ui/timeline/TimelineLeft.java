package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class TimelineLeft<U> extends AbstractList<ActionRow<U>, U> {

    private Cell topActionCell;
    private Cell separatorCell;
    private int DEFAULT_ACTION_PAD = 16;

    private Table contentTable;
    private Label typeLabel;
    private ScrollPane scrollPane;

    private SquareButton repeatBtn;
    private SquareButton newBtn;
    private SquareButton playBack;
    private SquareButton play;

    private SquareButton upBtn;
    private SquareButton downBtn;

    public TimelineLeft(TimelineWidget timeline) {
        super(timeline);

        Table header = buildHeader();
        ScrollPane contentPane = buildContentPane();

        Table mainTable = new Table();
        Stack mainStack = new Stack();
        add(mainStack).expand().grow();
        mainStack.add(mainTable);
        mainStack.add(mainTable);
        Table separatorTable = new Table();
        Image separator = new Image(getSkin().getDrawable("timeline-panel-overlay-separator"));
        separatorCell = separatorTable.add(separator).expandX().right().width(2).growY();
        mainStack.add(separatorTable);

        mainTable.add(header).height(51).growX().padRight(-1).row();
        mainTable.add(contentPane).expand().growX().top().row();
        mainTable.add().height(17).row();
        setActionPad(DEFAULT_ACTION_PAD);

        setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.BackgroundColor.LIGHT_GRAY));
    }

    private Table buildHeader() {
        Skin skin = getSkin();
        SquareButton ffBack = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-ff"), "ff back");
        ffBack.flipHorizontal();
        playBack = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-play"), true, "play back");
        playBack.flipHorizontal();
        play = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-play"), true, "play");
        SquareButton ffForward = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-ff"), "ff forward");
        repeatBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-repeat"), true, "repeat");

        newBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"), "New emitter");
        newBtn.getIconCell().padTop(2).padLeft(1);
        SquareButton deleteBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-delete"), "Delete emitter");

        upBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-play"), "Move up");
        upBtn.flipVertical();
        downBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-play"), "Move down");
        downBtn.flipVertical(); downBtn.flipHorizontal();

        Table header = new Table();
        header.setBackground(skin.getDrawable("timeline-top-bar-bg"));

        Table topPart = new Table();
        Table bottomPart = new Table();

        topPart.add(ffBack).padLeft(6).left();
        //topPart.add(playBack).padLeft(6).left(); // TODO: add this back when we can support
        topPart.add(play).padLeft(6).left();
        topPart.add(ffForward).padLeft(6).left();
        topPart.add(repeatBtn).padLeft(6).left();
        topPart.add().growX().minWidth(20);
        topPart.add(upBtn).padRight(6).right();
        topPart.add(downBtn).padRight(10).right();
        topPart.add(newBtn).right().padRight(6);
        topPart.add(deleteBtn).right().padRight(6);

        topActionCell = topPart.add().right();

        typeLabel = new Label("Items", skin);
        typeLabel.setColor(ColorLibrary.FONT_GRAY);
        bottomPart.add(typeLabel).padBottom(2).padLeft(5).left().expandX();

        header.add(topPart).height(33).padBottom(1).growX().row();
        header.add(bottomPart).height(16).growX().row();

        /**
         * Build header actions
         */
        ffBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.skipToStart);
            }
        });
        ffForward.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.skipToEnd);
            }
        });
        playBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.rewind);
            }
        });
        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.play);
            }
        });
        repeatBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.toggleLoop);
            }
        });
        deleteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.deleteSelection);
            }
        });
        newBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.newItem);
            }
        });
        upBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.up);
            }
        });
        downBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                timeline.onActionButtonClicked(TimelineListener.Type.down);
            }
        });

        return header;
    }

    private ScrollPane buildContentPane () {
        contentTable = new Table();
        scrollPane = new ScrollPane(contentTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setSmoothScrolling(false);
        scrollPane.setOverscroll(false, false);

        return scrollPane;
    }

    public void setActionPad(float padding) {
        topActionCell.padRight(padding + 2);
        separatorCell.padRight(padding);
    }

    @Override
    protected ActionRow<U> createNewItem() {
        return new ActionRow(timeline);
    }

    @Override
    protected void addItem(ActionRow item) {
        super.addItem(item);

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
    public void addItem(TimelineItemDataProvider<U> data) {
        super.addItem(data);

        recalculateMaxPad();
    }

    @Override
    public void setData(Array<? extends  TimelineItemDataProvider<U>> dataArray) {
        super.setData(dataArray);

        recalculateMaxPad();
    }

    private void recalculateMaxPad() {
        if(getItems().size == 0) return;

        float maxPad = getItems().first().getActionCellWidth();
        for(ActionRow row: getItems()) {
            if(maxPad < row.getActionCellWidth()) {
                maxPad = row.getActionCellWidth();
            }
        }

        setActionPad(maxPad);
    }

    public void setTypeName(String itemTypeName) {
        if (itemTypeName == null) return;

        typeLabel.setText(itemTypeName);
    }

    public boolean isLoopEnabled() {
        return repeatBtn.isChecked();
    }

    @Override
    public void act(float delta) {
        if(rebuildFlag) {
            rebuildFromData();
        }
        super.act(delta);
    }

    @Override
    protected void rebuildFromData() {
        contentTable.clearChildren();

        for(ActionRow item: getItems()) {
            addItemToTable(item);
        }
    }

    @Override
    public void clearItems() {
        super.clearItems();

        contentTable.clearChildren();
    }

    public float getScrollPos() {
        return scrollPane.getScrollY();
    }

    public void setScrollPos(float pos) {
        scrollPane.setScrollY(pos);
    }

    private void addItemToTable(Table item) {
        contentTable.add(item).growX().top().height(21).row();
    }

    public SquareButton getNewButton() {
        return newBtn;
    }

    public SquareButton getPlayButton() {
        return play;
    }

    public SquareButton getPlayBackButton() {
        return playBack;
    }

    public SquareButton getRepeatButton() {
        return repeatBtn;
    }
}
