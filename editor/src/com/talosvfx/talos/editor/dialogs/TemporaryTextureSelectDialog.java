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

package com.talosvfx.talos.editor.dialogs;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Getter;
import lombok.Setter;

public class TemporaryTextureSelectDialog extends VisWindow {

    public interface OnTextureSelected {
        void onSelected (TextureSelection textureSelection);
    }

    private TextureSelection selected;

    @Setter
    private OnTextureSelected listener;

    public TemporaryTextureSelectDialog () {
        super("Select Texture");

        setCenterOnAdd(true);
        setModal(true);
        setResizable(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();

        initContent();

        pack();
        invalidate();

        centerWindow();
    }

    public static class TextureSelection extends Table {

        private final Image selected;

        @Getter
        private String internalAssetPath;
        @Getter
        private final Texture texture;

        public TextureSelection (String internalAssetPath) {
            this.internalAssetPath = internalAssetPath;
            setBackground(SharedResources.skin.newDrawable("white", 0, 0, 0, 0.9f));

            texture = new Texture(Gdx.files.internal(internalAssetPath));
            final Image image = new Image(texture);
            selected = new Image(SharedResources.skin.newDrawable("white", 1f, 1f, 1f, 1f));
            this.selected.setColor(1f, 1f, 1f, 0);

            add(new Stack(image, selected)).grow();
        }

        public void setSelected (boolean selected) {
            this.selected.setColor(1f, 1f, 1f, selected ? 0.2f : 0);
        }
    }

    private void initContent() {

        Array<TextureSelection> selection = new Array<>();

        Table subTable = new Table();

        String[] inbuilt = new String[] {
            "fire.png",
            "spot.png",
            "smoke.png"
        };

        int elementsPerRow = 4;
        int counter = 0;
        for (int i = 0; i < inbuilt.length; i++) {
            final TextureSelection textureSelection = new TextureSelection(inbuilt[i]);
            selection.add(textureSelection);
            textureSelection.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    selected = textureSelection;

                    for (TextureSelection tex : selection) {
                        if (tex == textureSelection) {
                            tex.setSelected(true);
                        } else {
                            tex.setSelected(false);
                        }
                    }
                }
            });

            subTable.add(textureSelection).size(100);

            counter++;
            if (counter > elementsPerRow) {
                counter = 0;
                subTable.row();
            }
        }

        add(subTable);
        row();

        TextButton saveButton = new TextButton("Select", getSkin());
        add(saveButton).right().padRight(5);
        row();

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (selected != null) {
                    listener.onSelected(selected);
                    selected.setSelected(false);
                    selected = null;
                }
                close();
            }
        });
    }

}
