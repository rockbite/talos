package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.editor.widgets.CurveWidget;
import com.rockbite.tools.talos.runtime.modules.CurveModule;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;

public class CurveModuleWrapper extends ModuleWrapper<CurveModule> {

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

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();
    }

    @Override
    public void setModule(CurveModule module) {
        super.setModule(module);
        curveWidget.setModule(module);
    }

}
