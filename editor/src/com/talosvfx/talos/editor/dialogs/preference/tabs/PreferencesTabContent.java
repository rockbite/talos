package com.talosvfx.talos.editor.dialogs.preference.tabs;


import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.dialogs.preference.widgets.APrefWidget;
import com.talosvfx.talos.editor.dialogs.preference.widgets.blocks.BlockWidget;
import lombok.Getter;

public class PreferencesTabContent extends Table {

    @Getter
    Array<APrefWidget> widgetArray;

    Array<BlockWidget> blocks;

    public PreferencesTabContent(XmlReader.Element content) {
        widgetArray = new Array<>();
        blocks = new Array<>();
        padTop(3).defaults().space(2).top().growX();

        String tabName = content.getAttribute("name");

        int childCount = content.getChildCount();
        for(int i = 0; i < childCount; i++) {
            XmlReader.Element item = content.getChild(i);

            String id = tabName + "." + item.getAttribute("name");

            BlockWidget blockWidget = null;
            if(item.getName().equals("block")) {
                blockWidget = buildBlock(id, item);
            } else if(item.getName().equals("injectable")) {
                blockWidget = buildInjectable(id, item);
            }

            if(blockWidget != null) {
                widgetArray.addAll(blockWidget.getWidgetArray());
                add(blockWidget).padTop(2);
                row();

                blocks.add(blockWidget);
            }
        }

        row();
        add().expandY();
    }

    private BlockWidget buildInjectable(String id, XmlReader.Element item) {
        String className = item.getText();

        if(className == null) return null;

        String packageName = "com.talosvfx.talos.editor.dialogs.preference.widgets.blocks." + className;

        Class clazz = null;
        try {
            clazz = ClassReflection.forName(packageName);
            BlockWidget block = (BlockWidget) ClassReflection.newInstance(clazz);
            block.setId(id);
            block.build();
            return block;
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private BlockWidget buildBlock(String id, XmlReader.Element block) {
        BlockWidget blockWidget = new BlockWidget(id, block);
        return blockWidget;
    }

    public void expandFirstBlock() {
        if(blocks.size > 0) {
            BlockWidget first = blocks.first();
            first.expand();
        }
    }
}
