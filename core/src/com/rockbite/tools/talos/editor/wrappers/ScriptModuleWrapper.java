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

    	addInputSlot("i1", 1);
    	addInputSlot("i2", 2);
    	addInputSlot("i3", 3);
    	addInputSlot("i4", 4);
    	addInputSlot("i5", 5);

        script = new VisTextArea();
        contentWrapper.add(script).width(220).height(100);


        script.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setScript(script.getText());
            }
        });

        addOutputSlot("out", 0);
    }

    @Override
    protected float reportPrefWidth() {
        return 320;
    }

    @Override
    public void write(JsonValue value) {

    }

    @Override
    public void read(JsonValue value) {

    }
}
