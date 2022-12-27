package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.dialogs.preference.widgets.APrefWidget;
import com.talosvfx.talos.editor.dialogs.preference.widgets.PrefWidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.common.CollapsableWidget;
import lombok.Getter;
import lombok.Setter;

public class BlockWidget extends CollapsableWidget {

    private XmlReader.Element block;
    @Setter
    protected String id;

    @Getter
    protected Array<APrefWidget> widgetArray;

    public BlockWidget(String id, XmlReader.Element block) {
        super(block.getAttribute("title"));

        this.id = id;
        this.block = block;

        build();
    }

    public BlockWidget() {
        super("");
    }

    public void build() {
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
