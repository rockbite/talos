package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.talosvfx.talos.editor.project2.SharedResources;

public class KeymapRowWidget extends CollapsableWidget {
    private CheckBox checkBox;
    private SelectionBoxWidget selectionBoxWidget;
    private KeymapBox keymapBox;

    public KeymapRowWidget (String name) {
        super(name);

        background((Drawable) null);

        // TODO: 26.12.22 DONE FOR TEST LATER REMOVE
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 1"));
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 2"));
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 3"));
        selectionBoxWidget.addSelection(new SelectionBoxWidget.SelectionBox("selection 4"));
    }

    @Override
    protected void addListeners() {
        arrowButton.setTouchable(Touchable.enabled);
        arrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                isCollapsed = !isCollapsed;

                if (isCollapsed) contentCell.setActor(null).pad(0);
                else contentCell.setActor(content).padLeft(topSegment.getPadLeft()).padRight(topSegment.getPadRight()).padBottom(8);

                arrowButton.toggle();
            }
        });
    }

    @Override
    public Table constructTopSegment(String title) {
        // init components
        this.arrowButton = new ArrowButton(false);
        this.arrowButton.getCell(arrowButton.getArrowIcon()).pad(0);

        this.checkBox = new CheckBox("checkboxtext", SharedResources.skin);
        this.selectionBoxWidget = new SelectionBoxWidget();
        this.keymapBox = new KeymapBox();

        final Table topSegment = new Table();
        // NOTE: pads are added to top segment not the entire panel so the click listener also registered paddings
        topSegment.pad(5, 10, 5, 8);

        // assemble top segment
        topSegment.defaults().space(6);
        topSegment.add(arrowButton);
        topSegment.add(checkBox);
        topSegment.add(selectionBoxWidget);
        topSegment.add(keymapBox);
        topSegment.add().expand();
        return topSegment;
    }


    // TODO: 26.12.22 implement
    public class KeymapBox extends Table {
        public KeymapBox () {

        }
    }
}
