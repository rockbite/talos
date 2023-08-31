package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;
import com.talosvfx.talos.editor.project2.SharedResources;

import com.talosvfx.talos.runtime.utils.Supplier;

public class AttachmentPointWidget extends PropertyWidget<AttachmentPoint> {

    AttachmentPointBox attachmentPointBox;

    public Supplier<Array<Bone>> boneListSuppler;


    protected AttachmentPointWidget () {}


    @Override
    public Actor getSubWidget() {
        attachmentPointBox = new AttachmentPointBox(SharedResources.skin, "position");

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
    @Override
    public PropertyWidget clone() {
        AttachmentPointWidget clone = (AttachmentPointWidget) super.clone();
        clone.boneListSuppler = this.boneListSuppler;

        return clone;
    }


}
