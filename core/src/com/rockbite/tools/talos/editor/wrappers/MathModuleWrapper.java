package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.rockbite.tools.talos.runtime.Expression;
import com.rockbite.tools.talos.runtime.modules.MathModule;

public class MathModuleWrapper extends ModuleWrapper<MathModule> {

    IntMap<Expression> map;
    ObjectMap<String, Integer> names;

    VisSelectBox<String> selectBox;

    public MathModuleWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {
        map = new IntMap<>();
        names = new ObjectMap<>();
        Array<String> namesArr = new Array<>();
        // get list of possible interpolations

        Field[] fields = ClassReflection.getFields(Expression.class);
        int iter = 0;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getType().isAssignableFrom(Expression.class)) {
                try {
                    Expression expression = (Expression) fields[i].get(null);
                    names.put(fields[i].getName(), iter);
                    map.put(iter++, expression);
                    namesArr.add(fields[i].getName());
                } catch (ReflectionException e) {
                    e.printStackTrace();
                }
            }
        }

        addInputSlot("A", MathModule.A);
        addInputSlot("B", MathModule.B);

        addOutputSlot("result", 0);


        selectBox = new VisSelectBox();
        selectBox.setItems(namesArr);

        contentWrapper.add(selectBox).width(120).padRight(3);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedString = selectBox.getSelected();
                Expression expression = map.get(names.get(selectedString));

                module.setExpression(expression);
            }
        });
    }

    @Override
    public void write(JsonValue value) {
        Expression expression = module.getExpression();
        int key = map.findKey(expression, true, 0);
        String name = names.findKey(key, false);
        value.addChild("scopeKey", new JsonValue(name));
    }

    @Override
    public void read(JsonValue value) {
        String name = value.getString("scopeKey");
        Expression expression = map.get(names.get(name));
        module.setExpression(expression);

        selectBox.setSelected(name);
    }
}
