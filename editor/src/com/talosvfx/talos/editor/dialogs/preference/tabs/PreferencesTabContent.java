package com.talosvfx.talos.editor.dialogs.preference.tabs;


import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.dialogs.preference.widgets.APrefWidget;
import com.talosvfx.talos.editor.dialogs.preference.widgets.BlockWidget;
import lombok.Getter;

public class PreferencesTabContent extends Table {

    @Getter
    Array<APrefWidget> widgetArray;

    public PreferencesTabContent(XmlReader.Element content) {
        widgetArray = new Array<>();
        padTop(1).defaults().space(2).top().growX();

        String tabName = content.getAttribute("name");

        int childCount = content.getChildCount();
        for(int i = 0; i < childCount; i++) {
            XmlReader.Element item = content.getChild(i);

            if(item.getName().equals("block")) {
                buildBlock(tabName, item);
            }
        }

        row();
        add().expandY();
    }

    private void buildBlock(String parent, XmlReader.Element block) {
        String id = parent + "." + block.getAttribute("name");
        BlockWidget blockWidget = new BlockWidget(id, block);

        widgetArray.addAll(blockWidget.getWidgetArray());

        add(blockWidget);
        row();
    }
}
