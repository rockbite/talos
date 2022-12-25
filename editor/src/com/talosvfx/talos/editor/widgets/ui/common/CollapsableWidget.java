package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.CustomVarWidget;
import com.talosvfx.talos.editor.project2.SharedResources;

public class CollapsableWidget extends Table {
    private final Table topSegment;
    private final Table content;
    private final Cell contentCell;

    private boolean isCollapsed = true;

    public CollapsableWidget (String title) {
        setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.DARK_GRAY));

        // init components
        topSegment = constructTopSegment(title);
        content = constructContent();

        // assemble widget
        add(topSegment).growX();
        row();
        contentCell = add().grow();
    }

    public Table constructTopSegment (String title) {
        // init components
        final ArrowButton arrowButton = new ArrowButton(false);
        arrowButton.getCell(arrowButton.getArrowIcon()).pad(0);
        final Label widgetLabel = new Label(title, SharedResources.skin, "small");

        final Table topSegment = new Table();
        // NOTE: pads are added to top segment not the entire panel so the click listener also registered paddings
        topSegment.pad(5, 10, 5, 8);
        // make top segment collapse and open instead of icon, so it was more comfortable to click
        topSegment.setTouchable(Touchable.enabled);
        topSegment.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                isCollapsed = !isCollapsed;

                if (isCollapsed) contentCell.setActor(null).pad(0);
                else contentCell.setActor(content).padLeft(topSegment.getPadLeft()).padRight(topSegment.getPadRight()).padBottom(8);

                arrowButton.toggle();
            }
        });

        // assemble top segment
        topSegment.defaults().space(6);
        topSegment.add(arrowButton);
        topSegment.add(widgetLabel).expand().left();
        return topSegment;
    }

    public Table constructContent () {
        final Table content = new Table();
        content.add().height(300);
        return content;
    }
}
