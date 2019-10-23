package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.runtime.modules.GlobalScopeModule;
import com.rockbite.tools.talos.runtime.utils.InterpolationMappings;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class GlobalScopeModuleWrapper extends ModuleWrapper<GlobalScopeModule> implements IDragPointProvider {

    VisSelectBox<String> selectBox;

    Vector2 dragPoint;

    @Override
    protected void configureSlots() {
        dragPoint = new Vector2(0, 0);

        Array<String> array = new Array<>();

        for(int i = 0; i < 10; i++) {
            array.add(i+"");
        }

        selectBox = addSelectBox(array);

        addOutputSlot("output", GlobalScopeModule.OUTPUT);
    }

    @Override
    public void setModule(GlobalScopeModule module) {
        super.setModule(module);
        NumericalValue value = TalosMain.Instance().globalScope.getDynamicValue(module.getKey());
        dragPoint.set(value.get(0), value.get(1));
    }

    @Override
    protected void wrapperSelected() {
        PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
        previewWidget.registerForDragPoints(this);
        updateFromSelectBox();
    }

    @Override
    protected void wrapperDeselected() {
        PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
        previewWidget.unregisterDragPoints(this);
    }

    protected VisSelectBox addSelectBox(Array<String> values) {
        Table slotRow = new Table();
        final VisSelectBox selectBox = new VisSelectBox();

        selectBox.setItems(values);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateFromSelectBox();
            }
        });

        slotRow.add(selectBox).width(50).left().padBottom(4).padLeft(5).padRight(10);

        leftWrapper.add(slotRow).left().expandX();
        leftWrapper.row();

        return selectBox;
    }

    private void updateFromSelectBox() {
        String selected = selectBox.getSelected();
        int key = Integer.parseInt(selected);
        module.setKey(key);
        NumericalValue value = TalosMain.Instance().globalScope.getDynamicValue(key);
        dragPoint.set(value.get(0), value.get(1));
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    public Vector2[] fetchDragPoints() {
        return new Vector2[]{dragPoint};
    }

    @Override
    public void dragPointChanged(Vector2 point) {
        TalosMain.Instance().globalScope.setDynamicValue(module.getKey(), dragPoint);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        selectBox.setSelected(module.getKey()+"");
    }
}
