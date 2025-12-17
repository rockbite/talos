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

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.widgets.ui.common.GenericAssetSelectionWidget;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.TextFieldWithZoom;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.vfx.Slot;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.modules.CurveModule;
import com.talosvfx.talos.runtime.vfx.modules.FlipBookMaterialModule;
import com.talosvfx.talos.runtime.vfx.modules.InputModule;
import com.talosvfx.talos.runtime.vfx.modules.MaterialModule;
import com.talosvfx.talos.runtime.vfx.modules.StaticValueModule;

public class FlipBookMaterialModuleWrapper extends ModuleWrapper<FlipBookMaterialModule> {

    private Label assetNameLabel;
    private GenericAssetSelectionWidget<AtlasSprite> selector;

    private GameAsset<AtlasSprite> asset;
    private Image texturePreview;
    private TextFieldWithZoom rowsField;
    private TextFieldWithZoom columnsField;
    private TextFieldWithZoom totalSplitsField;

    public FlipBookMaterialModuleWrapper () {
        super();
    }

    @Override
    public void setModuleToDefaults () {
        module.setToDefault();
    }

    @Override
    protected float reportPrefWidth () {
        return 400;
    }

    @Override
    protected void configureSlots () {


        addInputSlot("Animation phase (0 to 1)", FlipBookMaterialModule.ALPHA);
        rowsField = addInputSlotWithTextField("Rows", FlipBookMaterialModule.ROWS);
        columnsField = addInputSlotWithTextField("Columns", FlipBookMaterialModule.COLUMNS);
        totalSplitsField = addInputSlotWithTextField("Total frames", FlipBookMaterialModule.SPLIT_COUNT);

        rowsField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                float v = floatFromText(rowsField);
                int rounded = MathUtils.round(v);

                rowsField.setProgrammaticChangeEvents(false);
                rowsField.setText(rounded + "");
                rowsField.setProgrammaticChangeEvents(true);

                module.setRows(rounded);
                moduleBoardWidget.app.dataModified();;
            }
        });
        columnsField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                float v = floatFromText(columnsField);
                int rounded = MathUtils.round(v);

                columnsField.setProgrammaticChangeEvents(false);
                columnsField.setText(rounded + "");
                columnsField.setProgrammaticChangeEvents(true);

                module.setColumns(rounded);
                moduleBoardWidget.app.dataModified();

            }
        });
        totalSplitsField.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                float v = floatFromText(totalSplitsField);
                int rounded = MathUtils.round(v);
                module.setTotalSplitsField(rounded);

                totalSplitsField.setProgrammaticChangeEvents(false);
                totalSplitsField.setText(rounded + "");
                totalSplitsField.setProgrammaticChangeEvents(true);
                moduleBoardWidget.app.dataModified();


            }
        });

        addOutputSlot("", MaterialModule.MATERIAL_MODULE);

        asset = AssetRepository.getInstance().getAssetForIdentifier("white", GameAssetType.SPRITE);

        selector = new GenericAssetSelectionWidget<>(GameAssetType.SPRITE);
        selector.setValue(asset);
        contentWrapper.add(selector).growX().right().expandX();
        contentWrapper.row();

        texturePreview = new Image();
        texturePreview.setScaling(Scaling.fit);

        contentWrapper.add(texturePreview).size(50);

        if (asset.getResource() != null) {
            texturePreview.setDrawable(new TextureRegionDrawable(asset.getResource()));
        }

        selector.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                asset = selector.getValue();
                module.setGameAsset(asset);
                if (asset.getResource() != null) {
                    texturePreview.setDrawable(new TextureRegionDrawable(asset.getResource()));
                }
                moduleBoardWidget.app.dataModified();
            }
        });
    }

    @Override
    public void setModule (FlipBookMaterialModule module) {
        super.setModule(module);
        totalSplitsField.setText(module.getSplitCountDefaultValue() + "");
        rowsField.setText(module.getRowsDefaultValue() + "");
        columnsField.setText(module.getColumnsDefaultValue() + "");
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("asset", asset.nameIdentifier);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        String identifier = jsonData.getString("asset", "white");
        asset = AssetRepository.getInstance().getAssetForIdentifier(identifier, GameAssetType.SPRITE);
        selector.setValue(asset);


        if (asset.getResource() != null) {
            texturePreview.setDrawable(new TextureRegionDrawable(asset.getResource()));
        }
    }

    @Override
    public Class<? extends AbstractModule> getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == CurveModule.ALPHA) return InputModule.class;
        if (slot.getIndex() == FlipBookMaterialModule.ROWS) return StaticValueModule.class;
        if (slot.getIndex() == FlipBookMaterialModule.COLUMNS) return StaticValueModule.class;
        if (slot.getIndex() == FlipBookMaterialModule.SPLIT_COUNT) return StaticValueModule.class;
        return null;
    }
}
