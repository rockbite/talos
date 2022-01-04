package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;

import java.util.function.Supplier;

public class AttachmentPointWidget extends PropertyWidget<AttachmentPoint> {

    AttachmentPointBox attachmentPointBox;

    Supplier<Array<Bone>> boneListSuppler;

    public AttachmentPointWidget(Supplier<AttachmentPoint> supplier, Supplier<Array<Bone>> boneListSuppler) {
        super(supplier, null);
        this.boneListSuppler = boneListSuppler;
    }

    @Override
    public Actor getSubWidget() {
        attachmentPointBox = new AttachmentPointBox(TalosMain.Instance().UIStage().getSkin(), "position");

        Array<String> boneNameList = new Array<>();
        boneNameList.clear();
        for(Bone bone: boneListSuppler.get()) {
            boneNameList.add(bone.getData().getName());
        }

        attachmentPointBox.setBoneList(boneNameList);
        return attachmentPointBox;
    }

    @Override
    public void updateWidget(AttachmentPoint value) {
        attachmentPointBox.setData(value);
    }


}
