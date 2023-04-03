package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.UIUtils;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyStringWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class CustomStringWidget extends ATypeWidget<String> {

    private final Cell<Table> contentCell;
    private final Table content;

    private final TextValueWidget textValueWidget;

    public CustomStringWidget() {
        textValueWidget = new TextValueWidget();
        textValueWidget.init(SharedResources.skin);
        textValueWidget.setLabel("Value");

        textValueWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        add(textValueWidget).padLeft(4).padRight(4).width(220).padTop(9);
        row();

        content = new Table();
        contentCell = add(content);

        row();
        add().padBottom(10);
    }

    private void expand() {
        content.clearChildren();
        content.pack();
        UIUtils.invalidateForDepth(content, 6);
    }

    private void collapse() {
        content.clearChildren();
        UIUtils.invalidateForDepth(content, 6);
    }


    @Override
    public String getTypeName() {
        return "string";
    }

    @Override
    public boolean isFastChange() {
        boolean valueFastChange = textValueWidget.isFastChange();
        return valueFastChange;
    }

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<String> propertyWrapper) {
        PropertyStringWrapper stringWrapper = (PropertyStringWrapper) propertyWrapper;
        textValueWidget.setValue(stringWrapper.value);
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<String> propertyWrapper) {
        PropertyStringWrapper stringPropertyWrapper = (PropertyStringWrapper) propertyWrapper;
        stringPropertyWrapper.value = textValueWidget.getValue();
    }
}
