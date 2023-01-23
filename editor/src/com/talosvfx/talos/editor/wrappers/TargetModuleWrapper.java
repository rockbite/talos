package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.DragPoint;
import com.talosvfx.talos.editor.widgets.ui.PreviewWidget;
import com.talosvfx.talos.runtime.vfx.Slot;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.modules.TargetModule;

public class TargetModuleWrapper extends ModuleWrapper<TargetModule> implements IDragPointProvider {

    private DragPoint dragPointFrom;
    private DragPoint dragPointTo;

    private Label fromLabel;
    private Label toLabel;

    private boolean lock = false;

    @Override
    public void setModule(TargetModule module) {
        super.setModule(module);
        if(!lock) {
            module.setDefaultPositions(dragPointFrom.position, dragPointTo.position);
        }
    }

    @Override
    protected void configureSlots() {
        addInputSlot("alpha", TargetModule.ALPHA_INPUT);
        Cell fromCell = addInputSlot("from", TargetModule.FROM);
        Cell toCell = addInputSlot("to", TargetModule.TO);
        fromLabel = getLabelFromCell(fromCell);
        toLabel = getLabelFromCell(toCell);

        dragPointFrom = new DragPoint(0, 0);
        dragPointTo = new DragPoint(0, 0);

        addOutputSlot("position", TargetModule.POSITION);
    }

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
    protected float reportPrefWidth () {
        return 210;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        lock = true;
        super.read(json, jsonData);
        lock = false;
        dragPointFrom.position.set(module.defaultFrom);
        dragPointTo.position.set(module.defaultTo);
    }

    @Override
    public DragPoint[] fetchDragPoints() {
        return new DragPoint[]{dragPointFrom, dragPointTo};
    }

    @Override
    public void dragPointChanged(DragPoint point) {
        if(point == dragPointFrom) {
            module.setDefaultPositions(dragPointFrom.position, dragPointTo.position);
            markLabelAsHilighted(fromLabel);
        }

        if(point == dragPointTo) {
            module.setDefaultPositions(dragPointFrom.position, dragPointTo.position);
            markLabelAsHilighted(toLabel);
        }
    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == TargetModule.FROM) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();;
        if(slot.getIndex() == TargetModule.TO) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();;

        return null;
    }
}
