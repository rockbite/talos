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

package com.rockbite.tools.talos.editor.tools;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;

public class MinWidthAction<T extends Cell> extends TemporalAction {

    private float targetMinWidth;
    private float startMinWidth;
    private float minWidth;

    private T iconCell;

    public void setTarget (float targetMinWidth) {
        this.targetMinWidth = targetMinWidth;
    }

    @Override
    protected void begin () {
        this.startMinWidth = iconCell.getMinWidth();
    }


    @Override
    protected void update (float percent) {
        minWidth = startMinWidth + (targetMinWidth - startMinWidth) * percent;
        iconCell.minWidth(minWidth);
        iconCell.getTable().invalidateHierarchy();
    }

    public void setTarget (T iconCell) {
        this.iconCell = iconCell;
    }
}