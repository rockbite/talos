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

package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.deprecatedparticles.RegisterDragPoints;
import com.talosvfx.talos.editor.notifications.events.deprecatedparticles.UnRegisterDragPoints;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.widgets.ui.DragPoint;
import com.talosvfx.talos.editor.widgets.ui.PreviewWidget;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.vfx.modules.GlobalScopeModule;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.SelectBoxWithZoom;

public class GlobalScopeModuleWrapper extends ModuleWrapper<GlobalScopeModule> implements IDragPointProvider {

    SelectBox<String> selectBox;

    DragPoint dragPoint;

    private boolean loading;

    @Override
    protected void configureSlots() {
        dragPoint = new DragPoint(0, 0);

        Array<String> array = new Array<>();

        for(int i = 0; i < 10; i++) {
            array.add(i+"");
        }

        selectBox = addSelectBox(array);

        addOutputSlot("output", GlobalScopeModule.OUTPUT);
    }


    @Override
    public void onGraphSet () {
        super.onGraphSet();
        NumericalValue value = getModule().getScope().getDynamicValue(module.getKey());
        dragPoint.set(value.get(0), value.get(1));
    }

    @Override
    protected void wrapperSelected() {
        RegisterDragPoints registerDragPoints = Notifications.obtainEvent(RegisterDragPoints.class);
        registerDragPoints.setRegisterForDragPoints(this);
        Notifications.fireEvent(registerDragPoints);
    }

    @Override
    protected void wrapperDeselected() {
        UnRegisterDragPoints unregisterDragPoints = Notifications.obtainEvent(UnRegisterDragPoints.class);
        unregisterDragPoints.setUnRegisterForDragPoints(this);
        Notifications.fireEvent(unregisterDragPoints);
    }

    protected SelectBoxWithZoom addSelectBox(Array<String> values) {
        Table slotRow = new Table();
        SelectBoxWithZoom selectBox = new SelectBoxWithZoom<>(VisUI.getSkin());

        selectBox.setItems(values);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!loading) {
                    updateFromSelectBox();
                }
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
        NumericalValue value = getModule().getScope().getDynamicValue(key);
        dragPoint.set(value.get(0), value.get(1));

        GameAsset<VFXProjectData> gameAsset = moduleBoardWidget.app.getGameAsset();
        AssetRepository.getInstance().assetChanged(gameAsset);
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    public DragPoint[] fetchDragPoints() {
        return new DragPoint[]{dragPoint};
    }

    @Override
    public void dragPointChanged(DragPoint point) {
        getModule().getScope().setDynamicValue(module.getKey(), dragPoint.position);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        loading = true;
        super.read(json, jsonData);
        selectBox.setSelected(module.getKey()+"");
        loading = false;
    }
}
