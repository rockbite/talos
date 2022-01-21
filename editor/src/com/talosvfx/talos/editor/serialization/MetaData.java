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

package com.talosvfx.talos.editor.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class MetaData implements Json.Serializable {

    private Array<String> resourcePathStrings = new Array<>();

    @Override
    public void write(Json json) {
        json.writeArrayStart("scopeDefaults");
        for(int i = 0; i < 10; i++) {
            NumericalValue val = TalosMain.Instance().globalScope.getDynamicValue(i);
            float[] arr = new float[4];
            for(int j = 0; j < 4; j++) {
                arr[j] = val.get(j);
            }
            json.writeValue(arr);
        }
        json.writeArrayEnd();

        // now sync preview widget stuff
        float camX = TalosMain.Instance().UIStage().PreviewWidget().getCameraPosX();
        float camY = TalosMain.Instance().UIStage().PreviewWidget().getCameraPosY();

        json.writeValue("previewCamPos", new Vector2(camX, camY));
        json.writeValue("previewCamZoom", TalosMain.Instance().UIStage().PreviewWidget().getCameraZoom());

        json.writeValue("bgImagePath", TalosMain.Instance().UIStage().PreviewWidget().getBackgroundImagePath());
        json.writeValue("isGridVisible", TalosMain.Instance().UIStage().PreviewWidget().isGridVisible());
        json.writeValue("bgImageIsInBack", TalosMain.Instance().UIStage().PreviewWidget().isBackgroundImageInBack());
        json.writeValue("bgImageSize", TalosMain.Instance().UIStage().PreviewWidget().getBgImageSize());
        json.writeValue("gridSize", TalosMain.Instance().UIStage().PreviewWidget().getGridSize());

        // particle position
        ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
        if(particleEffect != null) {
            json.writeValue("particlePositionX", particleEffect.getPosition().x);
            json.writeValue("particlePositionY", particleEffect.getPosition().y);
        }

        final ObjectMap<FileHandle, FileTracker.FileEntry> currentTabFiles = TalosMain.Instance().FileTracker().getCurrentTabFiles();

        if(currentTabFiles != null) {
            json.writeArrayStart("resourcePaths");
            for (ObjectMap.Entry<FileHandle, FileTracker.FileEntry> currentTabFile : currentTabFiles) {
                json.writeValue(currentTabFile.key.path());
            }
            json.writeArrayEnd();
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        JsonValue arr = jsonData.get("scopeDefaults");
        int iter = 0;
        for(JsonValue item: arr) {
            NumericalValue val = new NumericalValue();
            val.set(item.get(0).asFloat(), item.get(1).asFloat(), item.get(2).asFloat(), item.get(3).asFloat());
            TalosMain.Instance().globalScope.setDynamicValue(iter++, val);
        }

        // preview widget stuff
        if(jsonData.has("previewCamPos")) {
            JsonValue camPos = jsonData.get("previewCamPos");
            TalosMain.Instance().UIStage().PreviewWidget().setCameraPos(camPos.getFloat("x", 0), camPos.getFloat("y", 0));
        } else {
            TalosMain.Instance().UIStage().PreviewWidget().setCameraPos(0, 0);
        }

        TalosMain.Instance().UIStage().PreviewWidget().setBackgroundImage(jsonData.getString("bgImagePath", null));
        TalosMain.Instance().UIStage().PreviewWidget().setGridVisible(jsonData.getBoolean("isGridVisible", true));
        TalosMain.Instance().UIStage().PreviewWidget().setImageIsBackground(jsonData.getBoolean("bgImageIsInBack", true));
        TalosMain.Instance().UIStage().PreviewWidget().setBgImageSize(jsonData.getFloat("bgImageSize", 10));
        TalosMain.Instance().UIStage().PreviewWidget().setGridSize(jsonData.getFloat("gridSize", 1));
        TalosMain.Instance().UIStage().PreviewWidget().setCameraZoom(jsonData.getFloat("previewCamZoom", 1.4285715f));

        // particle position
        ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
        if(particleEffect != null) {
            float pPosX = jsonData.getFloat("particlePositionX", 0);
            float pPosY = jsonData.getFloat("particlePositionY", 0);
            float pPosZ = jsonData.getFloat("particlePositionZ", 0);
            particleEffect.setPosition(pPosX, pPosY, pPosZ);
        }

        JsonValue resourcePaths = jsonData.get("resourcePaths");
        if (resourcePaths != null) {
            iter = 0;
            for (JsonValue item : resourcePaths) {
                final String filePath = item.asString();
                resourcePathStrings.add(filePath);
            }
        }
    }

    public Array<String> getResourcePathStrings () {
        return resourcePathStrings;
    }
}
