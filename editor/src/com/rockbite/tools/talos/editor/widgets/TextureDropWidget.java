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

package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.rockbite.tools.talos.runtime.modules.Module;

public class TextureDropWidget<F extends Module> extends Table {

    Image image;

    public TextureDropWidget(TextureRegion region, Skin skin) {
        init(region, skin, 50f);
    }

    public TextureDropWidget(TextureRegion region, Skin skin, float width) {
        init(region, skin, width);
    }

    public void init(TextureRegion region, Skin skin, float width) {

        Stack stack = new Stack();

        image = new Image(region);
        Image border = new Image(skin.getDrawable("border"));

        stack.add(image);
        stack.add(border);

        add(stack).size(width);

    }

    public void setDrawable(TextureRegionDrawable textureRegionDrawable) {
        image.setDrawable(textureRegionDrawable);
    }
}
