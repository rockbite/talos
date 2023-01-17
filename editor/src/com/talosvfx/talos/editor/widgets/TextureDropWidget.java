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

package com.talosvfx.talos.editor.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.UIStage;
import com.talosvfx.talos.editor.dialogs.TemporaryTextureSelectDialog;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;

public class TextureDropWidget<F extends AbstractModule> extends Table {

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

        stack.setTouchable(Touchable.enabled);
        stack.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                imageClicked();
            }
        });
    }

    public void imageClicked () {
        final UIStage uistage = TalosMain.Instance().UIStage();
        final Stage stage = uistage.getStage();

        uistage.temporaryTextureDialog.setListener(new TemporaryTextureSelectDialog.OnTextureSelected() {
            @Override
            public void onSelected (TemporaryTextureSelectDialog.TextureSelection textureSelection) {
                onTextureSelected(textureSelection);
            }
        });

        stage.addActor(uistage.temporaryTextureDialog.fadeIn());

    }

    public void onTextureSelected (TemporaryTextureSelectDialog.TextureSelection textureSelection) {
        final Texture texture = textureSelection.getTexture();
        setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
    }

    public void setDrawable(TextureRegionDrawable textureRegionDrawable) {
        image.setDrawable(textureRegionDrawable);
    }
}
