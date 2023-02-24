package com.talosvfx.talos.editor.addons.scene.apps.routines.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.editor.project2.apps.ScenePreviewApp;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.RoundedFlatButton;
import com.talosvfx.talos.runtime.routine.nodes.RoutineExecutorNode;

public class RoutineControlWindow extends Table {

    private final Table content;
    private final RoutineStage routineStage;
    private final SelectBox<String> selectBox;
    private final RoundedFlatButton playButton;
    private final ValueWidget speedValueWidget;
    private final RoundedFlatButton pauseButton;
    private final RoundedFlatButton cameraLockBtn;
    private Array<Integer> executorIndexToIdMap;

    public RoutineControlWindow(RoutineStage routineStage) {
        executorIndexToIdMap = new Array<>();
        setTouchable(Touchable.enabled);
        this.routineStage = routineStage;

        setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.DARK_GRAY));

        content = new Table();

        Skin skin = SharedResources.skin;

        Table topBar = new Table();
        topBar.setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE_TOP, ColorLibrary.BackgroundColor.LIGHT_GRAY));
        Label title = new Label("Controls", skin);
        topBar.add(title).left().pad(5).expandX().padLeft(7);

        add(topBar).growX();
        row();
        add(content).grow().pad(5);

        selectBox = new SelectBox<>(SharedResources.skin);
        content.add(selectBox).width(100);

        playButton = new RoundedFlatButton();
        playButton.make("Play");
        content.add(playButton).padLeft(5);

        pauseButton = new RoundedFlatButton();
        pauseButton.make("Pause");
        content.add(pauseButton).padLeft(5);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                // TODO: 23.02.23 dummy refactor
                if (routineStage.data == null) {
                    return;
                }

                if(routineStage.isPlaying()) {
                    routineStage.stop(getSelected());
                    updatePlayState();
                } else {
                    Object selected = selectBox.getSelected();
                    if (selected != null) {
                        routineStage.play(getSelected());
                        updatePlayState();
                    }
                }
                if (routineStage.isPaused()) {
                    routineStage.resume(getSelected());
                    updatePauseState();
                }
            }
        });

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                // TODO: 23.02.23 dummy refactor
                if (routineStage.data == null) {
                    return;
                }

                if(routineStage.isPaused()) {
                    routineStage.resume(getSelected());
                    updatePauseState();
                } else {
                    routineStage.pause(getSelected());
                    updatePauseState();
                }
            }
        });

        speedValueWidget = new ValueWidget(SharedResources.skin);
        speedValueWidget.setLabel("speed");
        content.add(speedValueWidget).padLeft(5);
        speedValueWidget.setValue(1f);
        speedValueWidget.setStep(0.1f);
        speedValueWidget.setRange(0.1f, 5f);
        speedValueWidget.setShowProgress(true);

        speedValueWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (routineStage.data == null) {
                    return;
                }

                routineStage.setTimeScale(speedValueWidget.getValue(), getSelected());
            }
        });

        cameraLockBtn = new RoundedFlatButton();
        cameraLockBtn.make("Camera Lock");
        cameraLockBtn.getStyle().checked = ColorLibrary.createClippedPatch(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.LIGHT_BLUE);
        content.add(cameraLockBtn).padLeft(5);
        cameraLockBtn.setChecked(false);

        cameraLockBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO: 23.02.23 dummy refactor
                if (routineStage.data == null) {
                    return;
                }

                routineStage.lockCamera(cameraLockBtn.isChecked(), getSelected());
            }
        });

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScenePreviewApp currentScenePreviewApp = routineStage.getPreviewAppIfOpened(getSelected());
                if (currentScenePreviewApp != null) {
                    cameraLockBtn.setChecked(currentScenePreviewApp.getWorkspaceWidget().isLockCamera());

                    if (routineStage.isPlaying()) {
                        routineStage.stop(getSelected());
                        updatePlayState();
                    }

                    if (currentScenePreviewApp.getWorkspaceWidget().isPaused()) {
                        routineStage.pause(getSelected());
                    } else {
                        routineStage.resume(getSelected());
                    }
                    updatePauseState();
                } else {
                    reset();
                }
            }
        });

    }

    public void reset(){
        cameraLockBtn.setChecked(false);
        if (routineStage.isPlaying()) {
            routineStage.stop(getSelected());
            updatePlayState();
        }
        routineStage.resume(getSelected());
        updatePauseState();
    }

    private void updatePauseState() {
        if(routineStage.isPaused()) {
            pauseButton.setText("Resume");
        } else {
            pauseButton.setText("Pause");
        }
    }
    public void update() {
        RoutineStageData data = routineStage.data;
        RoutineInstance instance = routineStage.data.getRoutineInstance();

        Array<RoutineExecutorNode> executors = instance.getNodesByClass(RoutineExecutorNode.class);
        Array<String> titles = new Array<>();
        ObjectMap<String, Integer> nameCountMap = new ObjectMap<>();
        executorIndexToIdMap.clear();
        for (RoutineExecutorNode executor : executors) {
            String title = executor.getTitle();
            nameCountMap.put(title, nameCountMap.get(title, 0) + 1);
            int id = executor.uniqueId;
            executorIndexToIdMap.add(id);
            if (nameCountMap.get(title) > 1) {
                title += "(" + (nameCountMap.get(title) - 1) + ")";
            }
            titles.add(title);
        }
        selectBox.setItems(titles);

        speedValueWidget.setValue(routineStage.getTimeScale());
    }

    private int getSelected(){
        return executorIndexToIdMap.get(selectBox.getSelectedIndex());
    }


    private void updatePlayState() {
        if(routineStage.isPlaying()) {
            playButton.setText("Stop");
        } else {
            playButton.setText("Play");
        }
    }
}
