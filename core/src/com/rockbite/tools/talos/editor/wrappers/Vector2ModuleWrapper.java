package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.modules.Vector2Module;

public class Vector2ModuleWrapper extends ModuleWrapper<Vector2Module> {
    @Override
    protected void configureSlots() {

        final VisTextField xField = addInputSlotWithTextField("X: ", 0);
        final VisTextField yField = addInputSlotWithTextField("Y: ", 1);

        xField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float x = floatFromText(xField);
                module.setX(x);
            }
        });

        yField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float y = floatFromText(yField);
                module.setY(y);
            }
        });

        addOutputSlot("position", 0);
    }

    @Override
    protected float reportPrefWidth() {
        return 210;
    }

    protected VisTextField addInputSlotWithTextField(String title, int key) {
        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");
        slotRow.add(icon).left();
        slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);

        VisTextField textField = new VisTextField();
        slotRow.add(textField).width(60);

        leftWrapper.add(slotRow).left().expandX().pad(3);
        leftWrapper.row();

        configureNodeActions(icon, key, true);

        return textField;
    }
}
