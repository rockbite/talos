package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;

public abstract class AttachmentPointWidget extends PropertyWidget<AttachmentPoint> {

    AttachmentPointBox attachmentPointBox;

    @Override
    public Actor getSubWidget() {
        attachmentPointBox = new AttachmentPointBox(TalosMain.Instance().UIStage().getSkin(), "position");

        Array<String> boneNameList = new Array<>();
        boneNameList.clear();
        for(Bone bone: getBoneList()) {
            boneNameList.add(bone.getData().getName());
        }

        attachmentPointBox.setBoneList(boneNameList);
        return attachmentPointBox;
    }

    @Override
    public void updateWidget(AttachmentPoint value) {
        attachmentPointBox.setData(value);
    }

    public abstract Array<Bone> getBoneList();
}
