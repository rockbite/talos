package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.editor.widgets.CurveWidget;
import com.rockbite.tools.talos.runtime.modules.CurveModule;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;

public class CurveModuleWrapper extends ModuleWrapper<CurveModule> {

    private CurveWidget curveWidget;

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        curveWidget = new CurveWidget(getSkin());
        contentWrapper.add(curveWidget).expandX().fillX().growX().height(100).padTop(23).padRight(3).padBottom(3);

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();
    }

    @Override
    public void setModule(CurveModule module) {
        super.setModule(module);
        curveWidget.setModule(module);
    }

    @Override
    public void write(JsonValue value) {
        Array<Vector2> points = module.getPoints();
        JsonValue arr = new JsonValue(JsonValue.ValueType.array);
        value.addChild("points", arr);
        for(Vector2 point: points) {
            JsonValue vec = new JsonValue(JsonValue.ValueType.array);
            vec.addChild(new JsonValue(point.x));
            vec.addChild(new JsonValue(point.y));
            arr.addChild(vec);
        }
    }

    @Override
    public void read(JsonValue value) {
        JsonValue points = value.get("points");
        module.getPoints().clear();
        for(JsonValue point: points) {
            module.createPoint(point.get(0).asFloat(), point.get(1).asFloat());
        }
    }
}
