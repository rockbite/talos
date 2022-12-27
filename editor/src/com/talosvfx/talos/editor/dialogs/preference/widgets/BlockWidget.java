package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;
import lombok.Getter;

public class BlockWidget extends CollapsableWidget {


    private final XmlReader.Element block;

    @Getter
    private final Array<APrefWidget> widgetArray;

    public BlockWidget(String id, XmlReader.Element block) {
        super(block.getAttribute("title"));

        this.block = block;

        widgetArray = new Array<>();

        content = new Table();
        int childCount = block.getChildCount();
        for(int i = 0; i < childCount; i++) {
            XmlReader.Element item = block.getChild(i);
            APrefWidget widget = PrefWidgetFactory.generateWidget(id, item);

            widgetArray.add(widget);

            content.add(widget).growX();
            content.row();
        }
    }
}
