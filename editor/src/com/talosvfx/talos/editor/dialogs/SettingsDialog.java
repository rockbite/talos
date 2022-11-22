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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.filesystem.FileSystemInteraction;

public class SettingsDialog extends VisWindow {

    public static final String ASSET_PATH = "assetPath";

    private ObjectMap<String, TextField> textFieldMap;

    public SettingsDialog() {
        super("Talos Preferences");

        textFieldMap = new ObjectMap<>();

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

        for(String key: textFieldMap.keys()) {
            textFieldMap.get(key).setText(TalosMain.Instance().Prefs().getString(key));
        }

    }

    public void addPathSetting(String name, final String id) {
        Table inputTable = new Table();

        inputTable.add(new Label(name, getSkin())).width(220);
        final TextField inputPathField = new TextField("", getSkin());
        inputPathField.setDisabled(true);
        inputTable.add(inputPathField).padLeft(13).width(270);
        TextButton browseInputBtn = new TextButton("Browse", getSkin());
        inputTable.add(browseInputBtn).padLeft(3);

        add(inputTable).pad(5).left().expandX().padTop(10);
        row();

        textFieldMap.put(id, inputPathField);

        browseInputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                showFolderSelect(id);
            }
        });
    }

    private void initContent() {
        addPathSetting("Particle Assets Default Path", ASSET_PATH);
        TalosMain.Instance().Addons().announceLocalSettings(this);

        TextButton saveButton = new TextButton("Save", getSkin());
        add(saveButton).right().padRight(5);
        row();

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                save();
            }
        });
    }

    private void showFolderSelect(final String id) {
        FileSystemInteraction.instance().showFolderChooser(new FileChooserListener() {
            @Override
            public void selected (Array<FileHandle> files) {
                textFieldMap.get(id).setText(files.get(0).path());
            }
        });

    }

    private void save() {
        for(String key: textFieldMap.keys()) {
            TalosMain.Instance().Prefs().putString(key, textFieldMap.get(key).getText());
        }
        TalosMain.Instance().Prefs().flush();
        close();
    }
}
