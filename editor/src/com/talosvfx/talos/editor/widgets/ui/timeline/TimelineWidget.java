package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class TimelineWidget extends Table {

    TimelineItems itemWidget;
    TimelineMain mainWidget;

    public TimelineWidget(Skin skin) {
        setSkin(skin);

        itemWidget = new TimelineItems(skin);
        mainWidget = new TimelineMain(skin);

        SplitPane splitPane = new SplitPane(itemWidget, mainWidget, false, skin, "timeline");
        splitPane.setSplitAmount(0.3f);

        add(splitPane).grow();

        itemWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                mainWidget.setSelected(itemWidget.getSelectedIndex());
            }
        });

        mainWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                itemWidget.setSelected(mainWidget.getSelectedIndex());
            }
        });
    }

    public void setData (Array<ItemDataProvider> items) {
        itemWidget.setData(items);
        mainWidget.setData(items);
    }
}
