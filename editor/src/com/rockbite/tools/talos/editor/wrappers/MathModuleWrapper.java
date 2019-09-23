package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.rockbite.tools.talos.runtime.Expression;
import com.rockbite.tools.talos.runtime.modules.MathModule;
import com.rockbite.tools.talos.runtime.utils.MathExpressionMappings;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class MathModuleWrapper extends ModuleWrapper<MathModule> {

    VisSelectBox<String> selectBox;

    public MathModuleWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        module.a.setFlavour(module.output.getFlavour());
        module.b.setFlavour(module.output.getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            module.a.setFlavour(NumericalValue.Flavour.REGULAR);
            module.b.setFlavour(NumericalValue.Flavour.REGULAR);
        }
    }


    @Override
    protected void configureSlots() {
        Array<String> mathsExpressions = new Array<>();
        MathExpressionMappings.getAvailableMathExpressions(mathsExpressions);

        addInputSlot("A", MathModule.A);
        addInputSlot("B", MathModule.B);

        addOutputSlot("result", 0);


        selectBox = new VisSelectBox();
        selectBox.setItems(mathsExpressions);

        contentWrapper.add(selectBox).width(120).padRight(3);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedString = selectBox.getSelected();
                Expression expression = MathExpressionMappings.getMathExpressionForName(selectedString);

                module.setExpression(expression);
            }
        });
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        selectBox.setSelected(MathExpressionMappings.getNameForMathExpression(module.getExpression()));
    }
}
