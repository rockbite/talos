package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;

import java.util.function.Supplier;

public class GlobalValuePointsWidget extends PropertyWidget<Array<AttachmentPoint>> {

    GlobalValueListContainer listContainer;

    Supplier<Array<Bone>> boneListSuppler;

    public GlobalValuePointsWidget(Supplier<Array<AttachmentPoint>> supplier, Supplier<Array<Bone>> boneListSuppler) {
        super(supplier, null);
        this.boneListSuppler = boneListSuppler;
    }

    @Override
    public Actor getSubWidget() {
        listContainer = new GlobalValueListContainer(TalosMain.Instance().getSkin());
        listContainer.setBoneList(boneListSuppler.get());
        return listContainer;
    }

    @Override
    public void updateWidget(Array<AttachmentPoint> value) {
        listContainer.setData(value);
    }
}
