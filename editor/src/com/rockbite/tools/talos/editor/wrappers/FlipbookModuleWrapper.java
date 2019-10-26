package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.editor.widgets.FloatInputWidget;
import com.rockbite.tools.talos.editor.widgets.IntegerInputWidget;
import com.rockbite.tools.talos.editor.widgets.TextureDropWidget;
import com.rockbite.tools.talos.runtime.modules.FlipbookModule;
import com.rockbite.tools.talos.runtime.modules.Module;

public class FlipbookModuleWrapper extends TextureDropModuleWrapper<FlipbookModule> {

    IntegerInputWidget rows;
    IntegerInputWidget cols;

    FloatInputWidget duration;

    @Override
    protected void configureSlots() {
        super.configureSlots();
        dropWidget = new TextureDropWidget<Module>(defaultRegion, getSkin(), 100f);

        addInputSlot("phase",  FlipbookModule.PHASE);

        rows =  new IntegerInputWidget("Rows", getSkin());
        cols =  new IntegerInputWidget("Cols", getSkin());
        duration =  new FloatInputWidget("Duration", getSkin());
        rows.setValue(1);
        cols.setValue(1);
        duration.setValue(1f);

        leftWrapper.add(rows).padTop(5f).left().expandX().row();
        leftWrapper.add(cols).left().expandX().row();
        leftWrapper.add(duration).padLeft(5).left().expandX().row();

        rightWrapper.add(dropWidget).size(100).right().row();

        rightWrapper.add().growY().row();
        addOutputSlot("output", FlipbookModule.OUTPUT);

        rows.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setRows(rows.getValue());
            }
        });


        duration.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.duration = duration.getValue();
            }
        });

        cols.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setCols(cols.getValue());
            }
        });
    }

    @Override
    public void setModuleRegion(String name, TextureRegion region) {
        module.setRegion(name, region);
    }

    @Override
    protected float reportPrefWidth() {
        return 200;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        rows.setValue(module.getRows());
        cols.setValue(module.getCols());
        duration.setValue(module.duration);
    }
}
