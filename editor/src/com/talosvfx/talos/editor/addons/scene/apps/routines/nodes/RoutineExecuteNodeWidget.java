package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.RoutineExecutorNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.components.CameraComponent;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.ButtonWidget;
import com.talosvfx.talos.editor.nodes.widgets.GameAssetWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;
import com.talosvfx.talos.editor.project2.apps.ScenePreviewApp;

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
            container = scenePreviewApp.getWorkspaceWidget().currentScene;
            Array<GameObject> cameraGoList = container.root.getChildrenByComponent(CameraComponent.class, new Array<>());
            if(cameraGoList != null && !cameraGoList.isEmpty()) {
                cameraGO = cameraGoList.first();
            } else {
                cameraGO = null;
            }
            scenePreviewApp.getWorkspaceWidget().setCameraGO(cameraGO);
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
