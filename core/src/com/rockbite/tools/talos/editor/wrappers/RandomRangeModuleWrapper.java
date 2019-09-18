package com.rockbite.tools.talos.editor.wrappers;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.modules.RandomRangeModule;

public class RandomRangeModuleWrapper extends ModuleWrapper<RandomRangeModule> {

    VisTextField minLabel;
    VisTextField maxLabel;

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addOutputSlot("result", 0);

        VisTable table = new VisTable();

        // let's create our fields
        VisLabel label = new VisLabel(" RNG Range");
        minLabel = new VisTextField("0");
        maxLabel = new VisTextField("1");

        table.add(label).left();
        table.row().padTop(4);

        Table eWrap = new Table();
        eWrap.add(minLabel).width(120).padRight(10);
        eWrap.add(maxLabel).width(120);
        table.add(eWrap).expandX().left();

        table.row();

        contentWrapper.add(table).left().padTop(0).expandX();

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        minLabel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues();
            }
        });
        maxLabel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues();
            }
        });
    }

    private void updateValues() {
        float min = floatFromText(minLabel);
        float max = floatFromText(maxLabel);

        module.setMinMax(min, max);
    }

    @Override
    public void write(JsonValue value) {
        float min = module.getMin();
        float max = module.getMax();
        value.addChild("min", new JsonValue(min));
        value.addChild("max", new JsonValue(max));
    }

    @Override
    public void read(JsonValue value) {
        String min = value.getString("min");
        String max = value.getString("max");
        setData(floatFromText(min), floatFromText(max));
    }

    public void setData(float min, float max) {
        minLabel.setText(min+"");
        maxLabel.setText(max+"");
        updateValues();
    }
}
