package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;
import com.talosvfx.talos.editor.project2.SharedResources;

import java.util.function.Supplier;

public class GlobalValuePointsWidget extends PropertyWidget<Array<AttachmentPoint>> {

    GlobalValueListContainer listContainer;

    public Supplier<Array<Bone>> boneListSuppler;
    protected GlobalValuePointsWidget () {}

    public GlobalValuePointsWidget (String name, Supplier<Array<AttachmentPoint>> supplier, ValueChanged<Array<AttachmentPoint>> valueChanged, Object parent) {
        super(name, supplier, valueChanged, parent);
    }


    @Override
    public PropertyWidget clone() {
        GlobalValuePointsWidget clone = (GlobalValuePointsWidget) super.clone();
        clone.boneListSuppler = this.boneListSuppler;

        return clone;
    }


    public Actor getSubWidget() {
        listContainer = new GlobalValueListContainer(SharedResources.skin);
        listContainer.setBoneList(boneListSuppler.get());
        return listContainer;
    }

    @Override
    public void updateWidget(Array<AttachmentPoint> value) {
        listContainer.setData(value);
    }
}
