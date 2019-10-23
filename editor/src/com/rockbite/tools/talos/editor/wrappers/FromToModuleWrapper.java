package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;

public class FromToModuleWrapper extends ModuleWrapper<FromToModule> implements IDragPointProvider {

    private Vector2 dragFrom;
    private Vector2 dragTo;

    private Label fromLabel;
    private Label toLabel;

    @Override
    protected void wrapperSelected() {
        PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
        previewWidget.registerForDragPoints(this);
    }

    @Override
    protected void wrapperDeselected() {
        PreviewWidget previewWidget = TalosMain.Instance().UIStage().PreviewWidget();
        previewWidget.unregisterDragPoints(this);
    }

    @Override
    protected void configureSlots() {
        Cell fromCell = addInputSlot("from", FromToModule.FROM);
        Cell toCell = addInputSlot("to", FromToModule.TO);
        fromLabel = getLabelFromCell(fromCell);
        toLabel = getLabelFromCell(toCell);

        addOutputSlot("rotation", FromToModule.ROTATION);
        addOutputSlot("size", FromToModule.LENGTH);
        addOutputSlot("position", FromToModule.POSITION);

        dragFrom = new Vector2(-1, 0);
        dragTo = new Vector2(1, 0);
        if(module != null) {
            module.setDefaults(dragFrom, dragTo);
        }
    }

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);
        getTitleLabel().setText("Beam Position");
    }

    @Override
    public void setModule(FromToModule module) {
        super.setModule(module);
        module.setDefaults(dragFrom, dragTo);
    }

    @Override
    protected float reportPrefWidth () {
        return 150;
    }

    @Override
    public Vector2[] fetchDragPoints() {
        return new Vector2[]{dragFrom, dragTo};
    }

    @Override
    public void dragPointChanged(Vector2 point) {
        if(point == dragFrom) {
            module.setDefaults(dragFrom, dragTo);
            markLabelAsHilighted(fromLabel);
        }

        if(point == dragTo) {
            module.setDefaults(dragFrom, dragTo);
            markLabelAsHilighted(toLabel);
        }
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("fromX", dragFrom.x);
        json.writeValue("fromY", dragFrom.y);
        json.writeValue("toX", dragTo.x);
        json.writeValue("toY", dragTo.y);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        dragFrom.set(jsonData.getFloat("fromX", 0), jsonData.getFloat("fromY", 0));
        dragTo.set(jsonData.getFloat("toX", 0), jsonData.getFloat("toY", 0));
    }

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == FromToModule.FROM) return Vector2Module.class;
        if(slot.getIndex() == FromToModule.TO) return Vector2Module.class;

        return null;
    }
}
