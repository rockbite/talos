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

package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.assets.FlipBookAsset;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;
import lombok.Getter;

import java.util.Comparator;

public class FlipBookMaterialModule extends SpriteMaterialModule implements GameResourceOwner<AtlasSprite>, GameAsset.GameAssetUpdateListener {

    public static final int ALPHA = 0;
    public static final int ROWS = 1;
    public static final int COLUMNS = 2;
    public static final int SPLIT_COUNT = 3;
    private transient AtlasSprite region;

    public GameAsset<AtlasSprite> asset;

    @Deprecated
    private String assetIdentifier = "white";
    private ModuleValue<FlipBookMaterialModule> moduleOutput;
    private NumericalValue rowsValue;
    private NumericalValue columnsValue;
    private NumericalValue splitCountValue;

    private NumericalValue alphaInputSlot;

    @Getter
    private int rowsDefaultValue;
    @Getter
    private int columnsDefaultValue;
    @Getter
    private int splitCountDefaultValue;

    @Getter
    private int targetFrame = 0;
    private AtlasSprite[][] cachedSprites;

    private int cachedRows = -1;
    private int cachedColumns = -1;
    private AtlasSprite cachedRegion = null;

    @Override
    protected void defineSlots () {
        moduleOutput = new ModuleValue<>();
        moduleOutput.setModule(this);

        createOutputSlot(MATERIAL_MODULE, moduleOutput);

        alphaInputSlot = createAlphaInputSlot(ALPHA);
        rowsValue = createInputSlot(ROWS);
        columnsValue = createInputSlot(COLUMNS);
        splitCountValue = createInputSlot(SPLIT_COUNT);
    }

    @Override
    public void processCustomValues () {
        fetchInputSlotValue(ALPHA);
        float calcAlpha = alphaInputSlot.getFloat();
        calcAlpha = MathUtils.clamp(calcAlpha, 0, 0.99f);

        int calcRows = rowsValue.isEmpty() ? rowsDefaultValue : MathUtils.round(rowsValue.getFloat());
        int calcColumns = columnsValue.isEmpty() ? columnsDefaultValue : MathUtils.round(columnsValue.getFloat());
        int calcSplitCount = splitCountValue.isEmpty() ? splitCountDefaultValue : MathUtils.round(splitCountValue.getFloat());

        if (cachedSprites == null || cachedRows != calcRows || cachedColumns != calcColumns || cachedRegion != region) {
            calculateSplits(calcRows, calcColumns);
            cachedRows = calcRows;
            cachedColumns = calcColumns;
            cachedRegion = region;
        }

        targetFrame = MathUtils.floor(calcAlpha * calcSplitCount) % calcSplitCount;
    }

    private static int extractIndex(String name) {
        int underscore1 = name.indexOf('_');
        int underscore2 = name.indexOf('_', underscore1 + 1);

        // sampleflip_12_frame → "12"
        return Integer.parseInt(name.substring(underscore1 + 1, underscore2));
    }

    private void calculateSplits(int rows, int columns) {
        if (region == null || rows <= 0 || columns <= 0) {
            cachedSprites = null;
            return;
        }

        if (asset.getResource() instanceof FlipBookAsset) {
            Array<AtlasSprite> frames = ((FlipBookAsset)asset.getResource()).getFrames();
            frames.sort(new Comparator<AtlasSprite>() {
                @Override
                public int compare (AtlasSprite a, AtlasSprite b) {
                    String na = a.getAtlasRegion().name;
                    String nb = b.getAtlasRegion().name;

                    int ia = extractIndex(na);
                    int ib = extractIndex(nb);

                    return Integer.compare(ia, ib);
                }
            });

            cachedSprites = new AtlasSprite[rows][columns];
            for (int i = 0; i < frames.size; i++) {
                int row = i / columns;
                int col = i % columns;

                if (row < rows) {
                    cachedSprites[row][col] = frames.get(i);
                }
            }

            return;
        }

        TextureAtlas.AtlasRegion atlasRegion = region.getAtlasRegion();

        int originalWidth = atlasRegion.originalWidth;
        int originalHeight = atlasRegion.originalHeight;
        int packedWidth = atlasRegion.packedWidth;
        int packedHeight = atlasRegion.packedHeight;
        float offsetX = atlasRegion.offsetX;
        float offsetY = atlasRegion.offsetY;

        int cellOrigWidth = originalWidth / columns;
        int cellOrigHeight = originalHeight / rows;

        cachedSprites = new AtlasSprite[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int origX = col * cellOrigWidth;
                int origY = row * cellOrigHeight;

                float packedX = origX - offsetX;
                float packedY = origY - offsetY;

                float cellLeft = Math.max(0, packedX);
                float cellBottom = Math.max(0, packedY);
                float cellRight = Math.min(packedWidth, packedX + cellOrigWidth);
                float cellTop = Math.min(packedHeight, packedY + cellOrigHeight);

                int cellPackedWidth = (int)(cellRight - cellLeft);
                int cellPackedHeight = (int)(cellTop - cellBottom);

                TextureAtlas.AtlasRegion cellAtlasRegion;
                if (cellPackedWidth > 0 && cellPackedHeight > 0) {
                    cellAtlasRegion = new TextureAtlas.AtlasRegion(
                        atlasRegion.getTexture(),
                        atlasRegion.getRegionX() + (int)cellLeft,
                        atlasRegion.getRegionY() + (int)cellBottom,
                        cellPackedWidth,
                        cellPackedHeight
                    );
                    cellAtlasRegion.originalWidth = cellOrigWidth;
                    cellAtlasRegion.originalHeight = cellOrigHeight;
                    cellAtlasRegion.offsetX = Math.max(0, -packedX);
                    cellAtlasRegion.offsetY = Math.max(0, -packedY);
                    cellAtlasRegion.packedWidth = cellPackedWidth;
                    cellAtlasRegion.packedHeight = cellPackedHeight;
                } else {
                    cellAtlasRegion = new TextureAtlas.AtlasRegion(atlasRegion.getTexture(), 0, 0, 1, 1);
                    cellAtlasRegion.originalWidth = cellOrigWidth;
                    cellAtlasRegion.originalHeight = cellOrigHeight;
                }

                cachedSprites[row][col] = new AtlasSprite(cellAtlasRegion);
            }
        }
    }

    @Override
    public void write (Json json) {
        json.writeValue("index", index);

        GameResourceOwner.writeGameAsset(json, this);

        json.writeValue("rows", rowsDefaultValue);
        json.writeValue("columns", columnsDefaultValue);
        json.writeValue("splitCount", splitCountDefaultValue);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        index = jsonData.getInt("index");

        assetIdentifier = jsonData.getString("asset", "white");

        if (RuntimeContext.getInstance().getEditorContext() == null) {
            GameAsset<FlipBookAsset> asset = GameResourceOwner.readAssetForceType(json, jsonData, GameAssetType.FLIPBOOK);
            setGameAsset((GameAsset)asset);
        } else {
            GameAsset<AtlasSprite> asset = GameResourceOwner.readAsset(json, jsonData);
            setGameAsset(asset);
        }


        rowsDefaultValue = jsonData.getInt("rows", 1);
        columnsDefaultValue = jsonData.getInt("columns", 1);
        splitCountDefaultValue = jsonData.getInt("splitCount", 1);
    }

    public void setToDefault () {
        BaseAssetRepository baseAssetRepository = RuntimeContext.getInstance().getEditorContext().getBaseAssetRepository();
        GameAsset<AtlasSprite> defaultValue = baseAssetRepository.getAssetForIdentifier("white", GameAssetType.SPRITE);
        setGameAsset(defaultValue);
    }

    @Override
    public void onUpdate () {
        if (asset != null && !asset.isBroken()) {
            region = new AtlasSprite(asset.getResource());
            cachedRegion = null;
            cachedSprites = null;
        }
    }

    @Override
    public void remove () {
        super.remove();
        if (asset != null) {
            asset.listeners.removeValue(this, true);
        }
    }

    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SPRITE;
    }

    @Override
    public GameAsset<AtlasSprite> getGameResource () {
        return asset;
    }

    @Override
    public void setGameAsset (GameAsset<AtlasSprite> gameAsset) {
        if (this.asset != null) {
            this.asset.listeners.removeValue(this, true);
        }

        this.asset = gameAsset;
        asset.listeners.add(this);

        if (asset != null && !asset.isBroken()) {
            region = new AtlasSprite(asset.getResource());
            cachedRegion = null;
            cachedSprites = null;
        } else {
            System.out.println("Sprite material asset broken " + asset.nameIdentifier);
        }
    }

    @Override
    public void clearResource () {
        if (asset != null) {
            asset.listeners.removeValue(this, true);
            asset = null;
        }
        region = null;
        moduleOutput = null;
        cachedSprites = null;
        cachedRegion = null;
    }

    public void setRows (int v) {
        rowsValue.set(v);
        rowsDefaultValue = v;
    }

    public void setColumns (int v) {
        columnsValue.set(v);
        columnsDefaultValue = v;
    }

    public void setTotalSplitsField (int rounded) {
        splitCountValue.set(rounded);
        splitCountDefaultValue = rounded;
    }

    public AtlasSprite getTextureRegion () {
        if (cachedSprites != null && cachedSprites.length > 0 && cachedSprites[0].length > 0) {
            int columns = cachedSprites[0].length;
            int rows = cachedSprites.length;
            int totalFrames = rows * columns;

            if (totalFrames == 0) {
                return region;
            }

            int frameIndex = targetFrame % totalFrames;
            int row = frameIndex / columns;
            int col = frameIndex % columns;

            return cachedSprites[row][col];
        }

        return region;
    }

}
