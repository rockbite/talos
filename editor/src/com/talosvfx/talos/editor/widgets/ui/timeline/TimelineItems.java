package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class TimelineItems extends Table {

    private Cell topActionCell;
    private Cell separatorCell;
    private int DEFAULT_ACTION_PAD = 16;

    public TimelineItems(Skin skin) {
        setSkin(skin);
        Table header = buildHeader();

        add(header).size(300, 52).expandX().row();
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

        Stack mainStack = new Stack();

        Table topPart = new Table();
        Table bottomPart = new Table();

        topPart.add(ffBack).padLeft(6).left();
        topPart.add(playBack).padLeft(6).left();
        topPart.add(play).padLeft(6).left();
        topPart.add(ffForward).padLeft(6).left();
        topPart.add(repeat).padLeft(6).left();
        topPart.add().growX();
        topPart.add(newBtn).right().padRight(6);
        topPart.add(deleteBtn).right().padRight(6);

        topActionCell = topPart.add().right();

        Label label = new Label("Emitters", skin);
        label.setColor(ColorLibrary.FONT_GRAY);
        bottomPart.add(label).padBottom(2).padLeft(5).left().expandX();

        header.add(mainStack).expandX().growX();
        Table subHeader = new Table();
        subHeader.add(topPart).height(33).padBottom(1).growX().row();
        subHeader.add(bottomPart).height(16).growX().row();

        Table separatorTable = new Table();
        Image separator = new Image(skin.getDrawable("timeline-panel-overlay-separator"));
        separatorCell = separatorTable.add(separator).expandX().right().width(2).growY();

        mainStack.add(subHeader);
        mainStack.add(separatorTable);

        setActionPad(DEFAULT_ACTION_PAD);

        return header;
    }

    public void setActionPad(float padding) {
        topActionCell.padRight(padding + 2);
        separatorCell.padRight(padding);
    }

}
