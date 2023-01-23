package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.IntegerInputWidget;
import com.talosvfx.talos.runtime.vfx.modules.NinePatchModule;

public class NinePatchModuleWrapper extends ModuleWrapper<NinePatchModule> {

    IntegerInputWidget leftSplit;
    IntegerInputWidget rightSplit;
    IntegerInputWidget topSplit;
    IntegerInputWidget bottomSplit;

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots () {
        addInputSlot("input",  NinePatchModule.INPUT);
        addOutputSlot("output", NinePatchModule.OUTPUT);

        leftSplit = new IntegerInputWidget("left split", getSkin());
        leftWrapper.add(leftSplit).left().expandX().padLeft(3).row();

        rightSplit = new IntegerInputWidget("right split", getSkin(), Align.right);
        rightWrapper.add(rightSplit).right().expandX().padRight(3).row();

        topSplit = new IntegerInputWidget("top split", getSkin());
        leftWrapper.add(topSplit).left().expandX().padLeft(3);

        bottomSplit = new IntegerInputWidget("bottom split", getSkin(), Align.right);
        rightWrapper.add(bottomSplit).right().expandX().padRight(3);

        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                updateValues();
            }
        };

        leftSplit.addListener(changeListener);
        rightSplit.addListener(changeListener);
        topSplit.addListener(changeListener);
        rightWrapper.addListener(changeListener);
    }

    private void updateValues() {
        module.setSplits(leftSplit.getValue(), rightSplit.getValue(), topSplit.getValue(), bottomSplit.getValue());
        module.resetPatch();
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setData(module.getSplits());
    }

    public void setData(int[] splits) {
        leftSplit.setValue(splits[0]);
        rightSplit.setValue(splits[1]);
        topSplit.setValue(splits[2]);
        bottomSplit.setValue(splits[3]);
        updateValues();
    }
}
