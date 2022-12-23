package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
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
        pad(5, 10, 5, 8).top();
        setBackground(ColorLibrary.obtainBackground(ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.DARK_GRAY));

        topSegment = constructTopSegment(title);
        content = constructContent();

        add(topSegment).growX();
        row();
        contentCell = add().grow();
    }

    public Table constructTopSegment (String title) {
        final CustomVarWidget.ArrowButton arrowButton = new CustomVarWidget.ArrowButton(false);
        arrowButton.getCell(arrowButton.getArrowIcon()).pad(0);
        arrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                isCollapsed = !isCollapsed;
                contentCell.setActor(isCollapsed ? null : content);
                arrowButton.toggle();
            }
        });

        final Label widgetLabel = new Label(title, SharedResources.skin, "small");

        final Table topSegment = new Table();
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
