package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.rockbite.tools.talos.runtime.modules.EmConfigModule;

public class EmConfigModuleWrapper extends ModuleWrapper<EmConfigModule> {

    VisCheckBox additiveBox;
    VisCheckBox attachedBox;
    VisCheckBox continuousBox;
    VisCheckBox alignedBox;

    boolean lockListeners = false;

    @Override
    protected void configureSlots() {
        addOutputSlot("config", EmConfigModule.OUTPUT);

        additiveBox = new VisCheckBox("additive");
        attachedBox = new VisCheckBox("attached");
        continuousBox = new VisCheckBox("continuous");
        alignedBox = new VisCheckBox("aligned");

        Table form = new Table();

        form.add(additiveBox).left().padLeft(3);
        form.row();
        form.add(attachedBox).left().padLeft(3);
        form.row();
        form.add(continuousBox).left().padLeft(3);
        form.row();
        form.add(alignedBox).left().padLeft(3);

        contentWrapper.add(form).left();
        contentWrapper.add().expandX();

        rightWrapper.add().expandY();

        additiveBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        attachedBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        continuousBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        alignedBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
    }

    @Override
    public void setModule(EmConfigModule module) {
        super.setModule(module);
        fromDataToUI();
    }

    public void fromUIToData() {
        if(!lockListeners) {
            module.getUserValue().additive = additiveBox.isChecked();
            module.getUserValue().attached = attachedBox.isChecked();
            module.getUserValue().continuous = continuousBox.isChecked();
            module.getUserValue().aligned = alignedBox.isChecked();
        }
    }

    public void fromDataToUI() {
        lockListeners = true;
        additiveBox.setChecked(module.getUserValue().additive);
        attachedBox.setChecked(module.getUserValue().attached);
        continuousBox.setChecked(module.getUserValue().continuous);
        alignedBox.setChecked(module.getUserValue().aligned);
        lockListeners = false;
    }

    @Override
    protected float reportPrefWidth() {
        return 170;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        fromDataToUI();
    }

    public void setAttached(boolean attached) {
        attachedBox.setChecked(attached);
    }

    public void setContinuous(boolean attached) {
        continuousBox.setChecked(attached);
    }

    public void setAdditive(boolean attached) {
        additiveBox.setChecked(attached);
    }

    public void setAligned(boolean attached) {
        alignedBox.setChecked(attached);
    }
}
