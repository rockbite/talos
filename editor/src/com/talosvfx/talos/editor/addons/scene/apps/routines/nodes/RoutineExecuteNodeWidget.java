package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.routine.nodes.RoutineExecutorNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.editor.nodes.widgets.ButtonWidget;
import com.talosvfx.talos.editor.nodes.widgets.GameAssetWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;
import com.talosvfx.talos.editor.project2.apps.ScenePreviewApp;
import com.talosvfx.talos.runtime.scene.SavableContainer;
import com.talosvfx.talos.runtime.scene.components.CameraComponent;

public class RoutineExecuteNodeWidget extends AbstractRoutineNodeWidget {

    @Override
    protected void addAdditionalContent(Table contentTable) {

    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        ButtonWidget playButton = getButton("playButton");

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
               startPlay();
            }
        });

    }

    public String getTweenTitle() {
        TextValueWidget titleText = (TextValueWidget) getWidget("title");
        return titleText.getValue();
    }

    public boolean startPlay() {
        RoutineStage nodeStage = (RoutineStage) nodeBoard.getNodeStage();
        nodeStage.resetNodes();

        GameAssetWidget assetWidget = (GameAssetWidget)getWidget("scene");
        GameAsset sceneAsset = assetWidget.getValue();

        GameObject cameraGO = null;

        SavableContainer container = null;
        if(sceneAsset != null && sceneAsset.type == GameAssetType.SCENE) {
            ScenePreviewApp scenePreviewApp = nodeStage.openPreviewWindow(sceneAsset);
            scenePreviewApp.reload();
        } else {
            return false;
        }

        RoutineInstance routineInstance = nodeStage.data.getRoutineInstance();
        routineInstance.reset();
        int uniqueId = getUniqueId();
        RoutineExecutorNode node = (RoutineExecutorNode)routineInstance.getNodeById(uniqueId);
        routineInstance.setContainer(container);
        routineInstance.setCameraGO(cameraGO);
        node.receiveSignal("startSignal");

        return true;
    }
}
