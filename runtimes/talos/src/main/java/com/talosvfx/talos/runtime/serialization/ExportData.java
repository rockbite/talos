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

package com.talosvfx.talos.runtime.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.talosvfx.talos.runtime.modules.AbstractModule;

public class ExportData {

    public Array<EmitterExportData> emitters = new Array<>();

    public ExportMetadata metadata = new ExportMetadata();

    public static class ExportMetadata {
        public Array<String> resources = new Array<>();
        public String versionString;
    }

    public static class EmitterExportData {
        public String name;
        public Array<AbstractModule> modules = new Array<>();
        public Array<ConnectionData> connections = new Array<>();

        @Override
        public String toString () {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(name);
            stringBuilder.append("\n");

            for (AbstractModule module : modules) {
                stringBuilder.append("\t");
                stringBuilder.append("ModuleID: ");
                stringBuilder.append(module.getIndex());
                stringBuilder.append("\n");
            }

            stringBuilder.append("\n");
            for (ConnectionData connection : connections) {
                stringBuilder.append("\t");
                stringBuilder.append("Connection: ");
                stringBuilder.append(connection);
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        }
    }

}
