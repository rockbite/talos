package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.modules.StaticValueModule;

public class StaticValueModuleWrapper extends ModuleWrapper<StaticValueModule> {

    private VisTextField textField;

    public StaticValueModuleWrapper() {
        super();
    }

    @Override
    public void setModule(StaticValueModule module) {
        super.setModule(module);
        module.setStaticValue(1f);
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {
        textField = addTextField("1");
        addOutputSlot("output", 0);


        textField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = floatFromText(textField);

                module.setStaticValue(value);
            }
        });
    }

    @Override
    public void write(JsonValue value) {
        value.addChild("value", new JsonValue(module.getStaticValue()));
    }

    @Override
    public void read(JsonValue value) {
        float val = value.getFloat("value");
        textField.setText(val+"");
        module.setStaticValue(val);
    }

}
