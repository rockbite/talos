package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.FloatInputWidget;
import com.talosvfx.talos.editor.widgets.IntegerInputWidget;
import com.talosvfx.talos.runtime.vfx.modules.RibbonModule;

public class RibbonModuleWrapper extends ModuleWrapper<RibbonModule> {

    private FloatInputWidget memoryDuration;
    private IntegerInputWidget detailCount;

    @Override
    protected void configureSlots() {
        addInputSlot("main texture",  RibbonModule.MAIN_REGION);
        addInputSlot("ribbon texture",  RibbonModule.RIBBON_REGION);

        addInputSlot("thickness",  RibbonModule.THICKNESS);
        addInputSlot("transparency",  RibbonModule.TRANSPARENCY);
        addInputSlot("color",  RibbonModule.COLOR);

        addOutputSlot("output", RibbonModule.OUTPUT);



        detailCount = new IntegerInputWidget("detail count:", getSkin());
        detailCount.setValue(20);
        leftWrapper.add(detailCount).left().expandX().row();

        detailCount.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setDetailCount(detailCount.getValue());
            }
        });

        memoryDuration = new FloatInputWidget("memory:", getSkin());
        memoryDuration.setValue(1);
        leftWrapper.add(memoryDuration).left().expandX().padLeft(3);

        memoryDuration.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setMemoryDuration(memoryDuration.getValue());
            }
        });
    }


    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        detailCount.setValue(module.getDetailCount());
        memoryDuration.setValue(module.getMemoryDuration());
    }

    @Override
    public void write (Json json) {
        super.write(json);
    }


    @Override
    protected float reportPrefWidth() {
        return 180;
    }
}
