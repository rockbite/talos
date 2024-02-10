package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;
import com.talosvfx.talos.editor.addons.scene.widgets.directoryview.DirectoryViewWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;

import java.util.Stack;

public class SearchWidget extends Table {
    private static final float HEIGHT = 20f;
    private TextField textField;
    private DirectoryViewWidget directoryViewWidget;
    private Stack<FileHandle> stack = new Stack<>();
    private Array<FileHandle> similarFiles = new Array<>(false, 16);
    private final float DEBOUNCE_DELAY = 0.2f;
    private Timer.Task searchTask; // task for debounced search
    private String queryTemp;
    private String otherTemp;// to not create a new Strings every time for lowercase()
    private final StringSimilarity similar = new JaroWinkler();


    public SearchWidget (DirectoryViewWidget boundDirectoryWidget) {
        this.directoryViewWidget = boundDirectoryWidget;
        Skin skin = SharedResources.skin;
        // add search icon
        Container<Image> searchIcon = new Container<>();
        searchIcon.setBackground(ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_LEFT, ColorLibrary.BackgroundColor.BLACK));
        Image icon = new Image(SharedResources.skin.newDrawable("search"), Scaling.fit);
        icon.setOrigin(icon.getWidth() / 2f, icon.getHeight() / 2f);
        icon.setScale(0.6f, 0.6f);
        searchIcon.setActor(icon);
        add(searchIcon).size(HEIGHT);

        textField = new TextField("", skin);
        TextField.TextFieldStyle style = textField.getStyle();
        TextField.TextFieldStyle newStyle = new TextField.TextFieldStyle();
        newStyle.font = style.font;
        newStyle.fontColor = style.fontColor;
        newStyle.disabledFontColor = style.disabledFontColor;
        newStyle.selection = style.selection;
        newStyle.background = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE_RIGHT, ColorLibrary.BackgroundColor.BLACK);
        newStyle.cursor = style.cursor;
        newStyle.messageFont = style.messageFont;
        newStyle.messageFontColor = style.messageFontColor;
        textField.setStyle(newStyle);
        add(textField).height(HEIGHT);

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    textField.setProgrammaticChangeEvents(true);
                    textField.setText("");
                    textField.setProgrammaticChangeEvents(false);
                }
                return super.keyDown(event, keycode);
            }
        });

        textField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (searchTask != null) {
                    searchTask.cancel(); // using debouncing
                }

                searchTask = Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        performSearch(textField.getText());
                    }
                }, DEBOUNCE_DELAY);
            }
        });
    }


    private void performSearch (String query) {
        this.queryTemp = query.toLowerCase();
        if (queryTemp.isEmpty()) {
            FileHandle currentFolder = directoryViewWidget.getCurrentFolder();
            directoryViewWidget.openDirectory(currentFolder.path());
            return;
        }

        FileHandle root = SharedResources.currentProject.rootProjectDir();
        stack.clear();
        similarFiles.clear();

        stack.add(root);

        while (!stack.isEmpty()) {
            FileHandle pop = stack.pop();

            for (FileHandle fileHandle : pop.list()) {
                if (fileHandle.isDirectory()) {
                    stack.add(fileHandle);
                }

                otherTemp = fileHandle.name().toLowerCase();
                if (otherTemp.contains(queryTemp)) {
                    similarFiles.add(fileHandle);
                } else if (similar.similarity(queryTemp, fileHandle.nameWithoutExtension().toLowerCase()) > 0.9) {
                    similarFiles.add(fileHandle);
                }
            }
        }

        directoryViewWidget.fillItems(similarFiles);
    }

}
