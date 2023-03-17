package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.TextFieldWithZoom;

public class DataComponentFieldPopup<T> extends GenericListPopup<T> {

    private TextFieldWithZoom fieldNameTextField;

    public DataComponentFieldPopup(String title) {
        super(title);
    }

    @Override
    protected void build() {
        Table fieldName = new Table();
        LabelWithZoom fieldNameLabel = new LabelWithZoom("Field name: ", SharedResources.skin);
        fieldNameTextField = new TextFieldWithZoom("", SharedResources.skin);
        add(fieldName).growX().padBottom(10).row();
        fieldName.add(fieldNameLabel);
        fieldName.add(fieldNameTextField).growX();
        super.build();
    }

    public String getFieldName() {
        return fieldNameTextField.getText();
    }
}
