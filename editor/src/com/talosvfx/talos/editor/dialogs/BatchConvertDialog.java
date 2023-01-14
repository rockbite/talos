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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.filesystem.FileSystemInteraction;

import java.io.File;

public class BatchConvertDialog extends VisWindow {

    TextField inputPathField;
    TextField outputPathField;
    TextField inputFilterField;
    List<Label> logArea;

    String outputPath;
    Array<String> fileList = new Array<>();
    Array<Label> logItems = new Array<>();

    ScrollPane scrollPane;

    boolean isConverting = false;

    public BatchConvertDialog() {
        super("Batch Convert Legacy Effects");

        setCenterOnAdd(true);
        setModal(true);
        setResizable(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();


        setSize(700, 400);
        centerWindow();

        initContent();

    }

    private void initContent() {
        Table inputTable = new Table();

        inputTable.add(new Label("Input Folder: ", getSkin()));
        inputPathField = new TextField("", getSkin());
        inputTable.add(inputPathField).padLeft(13).width(200);
        TextButton browseInputBtn = new TextButton("Browse", getSkin());
        inputTable.add(browseInputBtn).padLeft(3);
        inputTable.add(new Label("Ext: ", getSkin())).padLeft(10);
        inputFilterField = new TextField("p", getSkin());
        inputTable.add(inputFilterField).width(50);

        add(inputTable).pad(5).left().expandX().padTop(10);
        row();

        Table outputTable = new Table();
        outputTable.add(new Label("Output Folder: ", getSkin()));

        outputPathField = new TextField("", getSkin());
        outputTable.add(outputPathField).width(200);
        TextButton browseOutputBtn = new TextButton("Browse", getSkin());
        outputTable.add(browseOutputBtn).padLeft(3);

        add(outputTable).pad(5).left().expandX();
        row();
        logArea = new List<>(getSkin());
        scrollPane = new ScrollPane(logArea);

        add(scrollPane).pad(5).left().grow();
        row();

        TextButton startButton = new TextButton("Start", getSkin());
        add(startButton).right().padRight(5);
        row();

        browseInputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                showFolderSelect(inputPathField);
            }
        });

        browseOutputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                showFolderSelect(outputPathField);
            }
        });

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                startConversion();
            }
        });
    }

    private void showFolderSelect(final TextField pathField) {
        FileSystemInteraction.instance().showFolderChooser(new FileChooserListener() {
            @Override
            public void selected (Array<FileHandle> files) {
                pathField.setText(files.get(0).path());
            }
        });
    }

    private void startConversion() {
        String inputPath = inputPathField.getText();
        outputPath = outputPathField.getText();
        String extension = inputFilterField.getText();

        fileList.clear();

        FileHandle input = Gdx.files.absolute(inputPath);

        if(input.isDirectory() && input.exists()) {
            traverseFolder(input, fileList, extension, 0);
        }

        isConverting = true;
        logArea.clearItems();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(fileList.size == 0) isConverting = false;

        if(isConverting) {
            String path = fileList.pop();

            FileHandle fileHandle = Gdx.files.absolute(path);

            convertOne(fileHandle);
        }
    }

    private void convertOne(FileHandle fileHandle) {
        String subPath;

        if (inputPathField.getText().length() == fileHandle.parent().path().length()) {
            subPath = File.separator;
        } else {
             subPath = fileHandle.parent().path().substring(inputPathField.getText().length() + 1) + File.separator;
        }
        String projectPath = outputPath + File.separator + "projects" +  File.separator + subPath + fileHandle.nameWithoutExtension() + ".tls";
        String runtimePath = outputPath + File.separator + "runtime" +  File.separator + subPath + fileHandle.nameWithoutExtension() + ".p";

        FileHandle projectDestination = Gdx.files.absolute(projectPath);
        FileHandle exportDestination = Gdx.files.absolute(runtimePath);

        String result = "ok";
        try {
//            TalosMain.Instance().TalosProject().importFromLegacyFormat(fileHandle);
//             now that it's done save TLS file
//            TalosMain.Instance().ProjectController().saveProject(projectDestination);
//            TalosMain.Instance().TalosProject().exportProject(exportDestination);

        } catch (Exception e) {
            result = "nok";
        }

        String text = "converting: " + fileHandle.name() + "        " + result + "\n";


        logItems.add(new Label(text, getSkin()));
        logArea.setItems(logItems);
        Label lbl = logArea.getItems().get(logArea.getItems().size-1);
        logArea.setSelected(lbl);
        scrollPane.layout();
        scrollPane.scrollTo(0, 0, 0, 0);
    }

    private void traverseFolder(FileHandle folder, Array<String> fileList, String extension, int depth) {
        for(FileHandle file : folder.list()) {
            if(file.isDirectory() && depth < 10) {
                traverseFolder(file, fileList, extension, depth + 1);
            }
            if(file.extension().equals(extension)) {
                fileList.add(file.path());
            }
        }
    }
}
