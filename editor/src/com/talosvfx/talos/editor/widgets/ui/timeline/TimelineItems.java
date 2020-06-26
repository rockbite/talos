package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class TimelineItems extends Table {

    private Cell topActionCell;
    private Cell separatorCell;
    private int DEFAULT_ACTION_PAD = 16;

    private Pool<TimelineItemRow> rowPool;
    private Array<TimelineItemRow> rows = new Array<>();

    private Table contentTable;
    private TimelineItemRow selectedRow = null;

    public TimelineItems(Skin skin) {
        setSkin(skin);
        Table header = buildHeader();
        ScrollPane contentPane = buildContentPane();

        Table mainTable = new Table();
        Stack mainStack = new Stack();
        add(mainStack).expand().grow();
        mainStack.add(mainTable);
        mainStack.add(mainTable);
        Table separatorTable = new Table();
        Image separator = new Image(skin.getDrawable("timeline-panel-overlay-separator"));
        separatorCell = separatorTable.add(separator).expandX().right().width(2).growY();
        mainStack.add(separatorTable);

        mainTable.add(header).height(51).growX().padRight(-1).row();
        mainTable.add(contentPane).expand().growX().top().row();
        setActionPad(DEFAULT_ACTION_PAD);

        setBackground(ColorLibrary.obtainBackground(skin, ColorLibrary.BackgroundColor.LIGHT_GRAY));

        rowPool = new Pool<TimelineItemRow>(20) {
            @Override
            protected TimelineItemRow newObject () {
                return new TimelineItemRow(skin);
            }
        };
    }

    private Table buildHeader() {
        Skin skin = getSkin();
        SquareButton ffBack = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-ff"));
        ffBack.flipHorizontal();
        SquareButton playBack = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-play"));
        playBack.flipHorizontal();
        SquareButton play = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-play"));
        SquareButton ffForward = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-ff"));
        SquareButton repeat = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-repeat"), true);

        SquareButton newBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"));
        newBtn.getIconCell().padTop(2).padLeft(1);
        SquareButton deleteBtn = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-delete"));

        Table header = new Table();
        header.setBackground(skin.getDrawable("timeline-top-bar-bg"));

        Table topPart = new Table();
        Table bottomPart = new Table();

        topPart.add(ffBack).padLeft(6).left();
        topPart.add(playBack).padLeft(6).left();
        topPart.add(play).padLeft(6).left();
        topPart.add(ffForward).padLeft(6).left();
        topPart.add(repeat).padLeft(6).left();
        topPart.add().growX().minWidth(20);
        topPart.add(newBtn).right().padRight(6);
        topPart.add(deleteBtn).right().padRight(6);

        topActionCell = topPart.add().right();

        Label label = new Label("Emitters", skin);
        label.setColor(ColorLibrary.FONT_GRAY);
        bottomPart.add(label).padBottom(2).padLeft(5).left().expandX();

        header.add(topPart).height(33).padBottom(1).growX().row();
        header.add(bottomPart).height(16).growX().row();

        return header;
    }

    private ScrollPane buildContentPane () {
        contentTable = new Table();
        ScrollPane scrollPane = new ScrollPane(contentTable);
        scrollPane.setScrollingDisabled(true, false);

        return scrollPane;
    }

    public void setActionPad(float padding) {
        topActionCell.padRight(padding + 2);
        separatorCell.padRight(padding);
    }

    public void setData (Array<ItemDataProvider> items) {
        clearRows();

        int index = 0;
        for(ItemDataProvider item: items) {
            TimelineItemRow row = rowPool.obtain();
            row.setIndex(index++);
            row.setFrom(item);
            rows.add(row);

            contentTable.add(row).growX().top().row();

            row.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    if(event.isCancelled()) return;
                    selectRow(row);

                    ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);
                    try {
                        fire(changeEvent);
                    } finally {
                        Pools.free(changeEvent);
                    }
                }
            });
        }

        recalculateMaxPad();
    }

    public void setSelected(int index) {
        if(index < 0) return;
        selectRow(rows.get(index));
    }

    private void selectRow(TimelineItemRow newRow) {
        if(selectedRow != null) {
            selectedRow.setSelected(false);
        }
        newRow.setSelected(true);
        selectedRow = newRow;
    }

    public void clearSelection() {
        if(selectedRow != null) {
            selectedRow.setSelected(false);
        }

        selectedRow = null;
    }

    public void clearRows() {
        for(TimelineItemRow row: rows) {
            rowPool.free(row);
        }
        rows.clear();
    }

    private void recalculateMaxPad() {
        if(rows.size == 0) return;

        float maxPad = rows.first().getActionCellWidth();
        for(TimelineItemRow row: rows) {
            if(maxPad < row.getActionCellWidth()) {
                maxPad = row.getActionCellWidth();
            }
        }

        setActionPad(maxPad);
    }

    public int getSelectedIndex () {
        return rows.indexOf(selectedRow, true);
    }

    public class TimelineItemRow extends Table implements Pool.Poolable {

        private final Image eye;
        private boolean isItemVisible = true;

        private final EditableLabel label;
        private int index =  -1;
        private final Drawable selectedBackground;
        private final Cell actionCell;
        private final Button selectorBox;
        private Drawable[] backgrounds;

        private boolean isSelected = false;

        public TimelineItemRow(Skin skin) {
            setHeight(20);

            Drawable lightBackground = skin.getDrawable("timeline-row-light");
            Drawable darkBackground = skin.getDrawable("timeline-row-dark");

            backgrounds = new Drawable[2];
            backgrounds[0] = lightBackground;
            backgrounds[1] = darkBackground;

            selectedBackground = skin.getDrawable("timeline-row-selected");

            setBackground(lightBackground); // this will be set as zebra pattern later by parent

            eye = new Image(skin.getDrawable("timeline-icon-eye"));
            eye.setColor(new Color(Color.WHITE));
            label = new EditableLabel("Default Emitter", skin);
            label.setColor(ColorLibrary.FONT_WHITE);
            selectorBox = new Button(skin, "miniSelector");

            add(eye).left().pad(3);
            add(label).padLeft(7).padBottom(4).left().expandX();
            actionCell = add(selectorBox).right().pad(4);

            setTouchable(Touchable.enabled);

            eye.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    event.cancel();

                    isItemVisible = !isItemVisible;

                    if (isItemVisible) {
                        eye.getColor().a = 1f;
                    } else {
                        eye.getColor().a = 0.3f;
                    }
                }
            });
        }

        public float getActionCellWidth () {
            return actionCell.getPrefWidth() + actionCell.getPadLeft() + actionCell.getPadRight();
        }

        @Override
        public void reset () {
            super.reset();

            clearActions();
        }

        public void setIndex (int index) {
            if(index == -1) {
                setBackground(backgrounds[0]);
            } else {
                int backgroundIndex = index % backgrounds.length;
                Drawable background = backgrounds[backgroundIndex];

                this.index = index;

                setBackground(background);
            }
        }

        public boolean isItemVisible() {
            return isItemVisible;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;

            if(isSelected) {
                setBackground(selectedBackground);
            } else {
                setIndex(index);
            }
        }

        public boolean isChecked() {
            return selectorBox.isChecked();
        }

        public void setFrom (ItemDataProvider item) {
            label.setText(item.getItemName());
        }
    }



}
