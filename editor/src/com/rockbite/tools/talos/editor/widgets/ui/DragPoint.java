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

package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class DragPoint {
    public Vector2 position = new Vector2();
    public Vector2 origin = new Vector2();
    public Color color = new Color();
    public boolean drawOrigin = false;

    public DragPoint() {

    }

    public DragPoint(float x, float y) {
        position.set(x, y);
        drawOrigin = false;
        color.set(Color.ORANGE);
    }

    public void set(float x, float y) {
        position.set(x, y);
    }
}
