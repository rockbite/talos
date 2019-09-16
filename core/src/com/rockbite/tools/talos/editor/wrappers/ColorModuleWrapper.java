package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.rockbite.tools.talos.runtime.modules.ColorModule;
import com.rockbite.tools.talos.runtime.modules.Module;

public class ColorModuleWrapper extends ModuleWrapper<ColorModule> {

    private Image colorBtn;

    private ColorPicker picker;

    @Override
    protected void configureSlots() {
        final VisTextField rField = addInputSlotWithTextField("R: ", 0, 40);
        final VisTextField gField = addInputSlotWithTextField("G: ", 1, 40);
        final VisTextField bField = addInputSlotWithTextField("B: ", 2, 40);

        rField.setText("255");
        gField.setText("0");
        bField.setText("0");

        rField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float r = floatFromText(rField);
                module.setR(r/255f);
                update();
            }
        });

        gField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float g = floatFromText(gField);
                module.setG(g/255f);
                update();
            }
        });

        bField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float b = floatFromText(bField);
                module.setB(b/255f);
                update();
            }
        });

        addOutputSlot("position", 0);

        picker = new ColorPicker(new ColorPickerAdapter() {
            @Override
            public void changed (Color newColor) {
                if(colorBtn != null) {
                    colorBtn.setColor(newColor);
                    rField.setText(""+(int)(newColor.r * 255f));
                    gField.setText(""+(int)(newColor.g * 255f));
                    bField.setText(""+(int)(newColor.b * 255f));

                    module.setR(newColor.r);
                    module.setG(newColor.g);
                    module.setB(newColor.b);
                }
            }
        });

        // create color picker Btn
        colorBtn = new Image(getSkin().getDrawable("white"));
        contentWrapper.add(colorBtn).width(50).height(50).right().padLeft(26);

        colorBtn.setColor(1f, 0, 0, 1f);

        colorBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().addActor(picker.fadeIn());
            }
        });

        picker.padTop(32);
        picker.padLeft(16);
        picker.setHeight(330);
        picker.setWidth(430);
        picker.padRight(26);
    }

    private void update() {
        colorBtn.setColor(module.getColor());
    }

    @Override
    protected float reportPrefWidth() {
        return 230;
    }
}
