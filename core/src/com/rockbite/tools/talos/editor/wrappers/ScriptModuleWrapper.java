package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.modules.ScriptModule;

public class ScriptModuleWrapper extends ModuleWrapper<ScriptModule> {

    private VisTextArea script;

    @Override
    protected void configureSlots() {

    	addInputSlot("i1", 0);
    	addInputSlot("i2", 1);
    	addInputSlot("i3", 2);
    	addInputSlot("i4", 3);
    	addInputSlot("i5", 4);

		addOutputSlot("o1", 0);
		addOutputSlot("o2", 1);
		addOutputSlot("o3", 2);
		addOutputSlot("o4", 3);
		addOutputSlot("o5", 4);

        script = new VisTextArea();
        contentWrapper.add(script).width(220).height(100);


        script.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setScript(script.getText());
            }
        });
    }

    @Override
    protected float reportPrefWidth() {
        return 320;
    }

    @Override
    public void write(JsonValue value) {
        value.addChild("script", new JsonValue(script.getText()));
    }

    @Override
    public void read(JsonValue value) {
        if(value.has("script")) {
            String text = value.getString("script");
            script.setText(text);
            module.setScript(text);
        }
    }
}
