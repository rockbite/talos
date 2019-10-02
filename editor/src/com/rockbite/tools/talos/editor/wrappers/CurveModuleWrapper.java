package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.widgets.CurveDataProvider;
import com.rockbite.tools.talos.editor.widgets.CurveWidget;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;

public class CurveModuleWrapper extends ModuleWrapper<CurveModule> implements CurveDataProvider {

    private CurveWidget curveWidget;

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        curveWidget = new CurveWidget(getSkin());
        contentWrapper.add(curveWidget).expandX().fillX().growX().height(100).padTop(23).padRight(3).padBottom(3);
        curveWidget.setDataProvider(this);

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();
    }

    @Override
    public Class<? extends Module> getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == CurveModule.ALPHA) return InputModule.class;

        return null;
    }

    @Override
    public Array<Vector2> getPoints() {
        if(module == null) return null;

        return module.getPoints();
    }

    @Override
    public void removePoint(int index) {
        if(module == null) return;
        module.removePoint(index);
    }

    @Override
    public int createPoint(float x, float y) {
        if(module == null) return 0;
        return module.createPoint(x, y);
    }
}
