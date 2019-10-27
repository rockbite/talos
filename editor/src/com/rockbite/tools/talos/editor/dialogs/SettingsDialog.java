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

package com.rockbite.tools.talos.editor.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.rockbite.tools.talos.TalosMain;

public class SettingsDialog extends VisWindow {

    FileChooser fileChooser;
    TextField inputPathField;

    public static final String ASSET_PATH = "assetPath";

    public SettingsDialog() {
        super("Talos Preferences");

        setCenterOnAdd(true);
        setModal(true);
        setResizable(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();


        setSize(500, 150);
        centerWindow();

        initContent();

        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setBackground(getSkin().getDrawable("window-noborder"));

        inputPathField.setText(TalosMain.Instance().Prefs().getString(ASSET_PATH));
    }

    private void initContent() {
        Table inputTable = new Table();

        inputTable.add(new Label("Default Assets Path: ", getSkin()));
        inputPathField = new TextField("", getSkin());
        inputTable.add(inputPathField).padLeft(13).width(270);
        TextButton browseInputBtn = new TextButton("Browse", getSkin());
        inputTable.add(browseInputBtn).padLeft(3);
        add(inputTable).pad(5).left().expandX().padTop(10);
        row();

        TextButton saveButton = new TextButton("Save", getSkin());
        add(saveButton).right().padRight(5);
        row();

        browseInputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                showFolderSelect(inputPathField);
            }
        });

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                save();
            }
        });
    }

    private void showFolderSelect(final TextField pathField) {
        fileChooser.setMode(FileChooser.Mode.OPEN);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                pathField.setText(file.get(0).path());
            }
        });

        getStage().addActor(fileChooser.fadeIn());
    }

    private void save() {
        TalosMain.Instance().Prefs().putString(ASSET_PATH, inputPathField.getText());
        TalosMain.Instance().Prefs().flush();
        close();
    }
}
