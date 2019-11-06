/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.DragPoint;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;

public class FromToModuleWrapper extends ModuleWrapper<FromToModule> implements IDragPointProvider {

    private DragPoint dragFrom;
    private DragPoint dragTo;

    private Label fromLabel;
    private Label toLabel;

    private boolean lock = false;

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

        dragFrom = new DragPoint(-1, 0);
        dragTo = new DragPoint(1, 0);
        if(module != null) {
            module.setDefaults(dragFrom.position, dragTo.position);
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
        if(!lock) {
            module.setDefaults(dragFrom.position, dragTo.position);
        }
    }

    @Override
    protected float reportPrefWidth () {
        return 150;
    }

    @Override
    public DragPoint[] fetchDragPoints() {
        return new DragPoint[]{dragFrom, dragTo};
    }

    @Override
    public void dragPointChanged(DragPoint point) {
        if(point == dragFrom) {
            module.setDefaults(dragFrom.position, dragTo.position);
            markLabelAsHilighted(fromLabel);
        }

        if(point == dragTo) {
            module.setDefaults(dragFrom.position, dragTo.position);
            markLabelAsHilighted(toLabel);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        lock = true;
        super.read(json, jsonData);
        lock = false;
        dragFrom.position.set(module.defaultFrom);
        dragTo.position.set(module.defaultTo);
    }

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == FromToModule.FROM) return Vector2Module.class;
        if(slot.getIndex() == FromToModule.TO) return Vector2Module.class;

        return null;
    }
}
