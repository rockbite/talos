package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.nodes.widgets.ButtonWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;

public class RoutineExecuteNodeWidget extends AbstractRoutineNodeWidget {

    @Override
    protected void addAdditionalContent(Table contentTable) {

    }

    private void playTween() {
        /*
        String target = (String) (getWidget("target").getValue());
        ObjectMap payload = new ObjectMap<String, Object>();
        payload.put("target", target);
        sendSignal("startSignal", "execute", payload);*/
    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        ButtonWidget playButton = getButton("playButton");

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                RoutineStage nodeStage = (RoutineStage) nodeBoard.getNodeStage();
                RoutineInstance routineInstance = nodeStage.data.getRoutineInstance();
                int uniqueId = getUniqueId();
                RoutineNode node = routineInstance.getNodeById(uniqueId);
                node.receiveSignal("startSignal");
            }
        });

    }

    public String getTweenTitle() {
        TextValueWidget titleText = (TextValueWidget) getWidget("title");
        return titleText.getValue();
    }
}
