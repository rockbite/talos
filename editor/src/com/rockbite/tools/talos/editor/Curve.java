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

package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.math.Vector2;

public class Curve {

    private Vector2 from = new Vector2();
    private Vector2 to = new Vector2();

    boolean reverse = false;

    public Curve(float x, float y, float toX, float toY, boolean reverse) {
        this.reverse = reverse;
        if(reverse) {
            to.set(x, y);
            from.set(toX, toY);
        } else {
            from.set(x, y);
            to.set(toX, toY);
        }
    }

    public void setTo(float toX, float toY) {
        if(reverse) {
            from.set(toX, toY);
        } else {
            to.set(toX, toY);
        }
    }

    public void setFrom(float toX, float toY) {
        if(reverse) {
            to.set(toX, toY);
        } else {
            from.set(toX, toY);
        }
    }

    public Vector2 getFrom() {
        return from;
    }

    public Vector2 getTo() {
        return to;
    }
}
