package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.bvb.AttachmentPoint;

public abstract class AttachmentPointWidget extends PropertyWidget<AttachmentPoint> {

    AttachmentPointBox attachmentPointBox;

    public AttachmentPointWidget(String name) {
        super(name);
    }

    @Override
    public Actor getSubWidget() {
        attachmentPointBox = new AttachmentPointBox(TalosMain.Instance().UIStage().getSkin(), "position");
        attachmentPointBox.setBoneList(getBoneList());
        return attachmentPointBox;
    }

    @Override
    public void updateWidget(AttachmentPoint value) {
        attachmentPointBox.setData(value);
    }

    public abstract Array<Bone> getBoneList();
}
