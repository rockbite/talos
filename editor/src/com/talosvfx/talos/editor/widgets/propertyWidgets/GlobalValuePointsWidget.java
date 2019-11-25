package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.AttachmentPoint;

public abstract class GlobalValuePointsWidget extends PropertyWidget<Array<AttachmentPoint>> {

    GlobalValueListContainer listContainer;

    @Override
    public Actor getSubWidget() {
        listContainer = new GlobalValueListContainer(TalosMain.Instance().getSkin());
        listContainer.setBoneList(getBoneList());
        return listContainer;
    }

    @Override
    public void updateWidget(Array<AttachmentPoint> value) {
        listContainer.setData(value);
    }

    public abstract Array<Bone> getBoneList();
}
