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

package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;

public class DynamicTable extends VisTable {

    Array<Actor> rows = new Array<>();
    Array<Boolean> align = new Array<>();

    public Cell addRow(Actor actor, boolean left) {
        Cell cell;
        if(left) {
            cell = add(actor).left().expandX();
        } else {
            cell = add(actor).right().expandX();
        }
        row();

        if(!rows.contains(actor, true)) {
            rows.add(actor);
            align.add(left);
        }

        return cell;
    }

    public void removeRow(int index) {
        clearChildren();

        rows.removeIndex(index);
        align.removeIndex(index);

        for(int i = 0; i < rows.size; i++) {
            addRow(rows.get(i), align.get(i));
        }
    }

}
