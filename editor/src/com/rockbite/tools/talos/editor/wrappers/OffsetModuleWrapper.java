package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.widgets.CurveDataProvider;
import com.rockbite.tools.talos.editor.widgets.CurveWidget;
import com.rockbite.tools.talos.editor.widgets.ShapeInputWidget;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;
import com.rockbite.tools.talos.runtime.modules.OffsetModule;

public class OffsetModuleWrapper extends ModuleWrapper<OffsetModule> implements CurveDataProvider {

    private ShapeInputWidget lowShape;
    private ShapeInputWidget highShape;

    private ImageButton equalsButton;

    private CurveWidget curveWidget;

    private Vector2 pos = new Vector2();
    private Vector2 size = new Vector2();

    private boolean lockUpdate = false;

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    public void setModule(OffsetModule module) {
        super.setModule(module);
        updateModuleDataFromWidgets();
    }

    @Override
    protected void configureSlots() {

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        lowShape = new ShapeInputWidget(getSkin());
        highShape = new ShapeInputWidget(getSkin());
        curveWidget = new CurveWidget(getSkin());
        curveWidget.setDataProvider(this);

        Table midTable = new Table();
        equalsButton = new ImageButton(getSkin(), "chain");
        equalsButton.setChecked(true);
        midTable.add(equalsButton);

        contentWrapper.add(lowShape).width(100).pad(5);
        contentWrapper.add(midTable).width(30);
        contentWrapper.add(highShape).width(100).pad(5);
        contentWrapper.row();
        contentWrapper.add(curveWidget).width(240).height(100).padTop(0).colspan(3).padBottom(3);

        contentWrapper.padTop(15);

        addOutputSlot("output", OffsetModule.OUTPUT);
        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        if(module != null) {
            updateModuleDataFromWidgets();
        }

        equalsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lowShape.getShapePos(pos);
                if(equalsButton.isChecked()) highShape.setPos(pos);

                lowShape.getShapeSize(size);
                if(equalsButton.isChecked()) highShape.setShapeSize(size);
            }
        });

        lowShape.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lowShape.getShapePos(pos);
                if(equalsButton.isChecked()) highShape.setPos(pos);

                lowShape.getShapeSize(size);
                if(equalsButton.isChecked()) highShape.setShapeSize(size);

                updateModuleDataFromWidgets();
            }
        });

        highShape.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                highShape.getShapePos(pos);
                if(equalsButton.isChecked()) lowShape.setPos(pos);

                highShape.getShapeSize(size);
                if(equalsButton.isChecked()) lowShape.setShapeSize(size);

                updateModuleDataFromWidgets();
            }
        });
    }

    private void updateModuleDataFromWidgets() {
        if(!lockUpdate) {
            lowShape.getShapePos(pos);
            module.setLowPos(pos);
            lowShape.getShapeSize(size);
            module.setLowSize(size);
            highShape.getShapePos(pos);
            module.setHighPos(pos);
            highShape.getShapeSize(size);
            module.setHighSize(size);

            module.setLowShape(lowShape.getShape());
            module.setHighShape(highShape.getShape());

            module.setLowEdge(lowShape.isEdge());
            module.setHighEdge(highShape.isEdge());

            module.setLowSide(lowShape.getSide());
            module.setHighSide(highShape.getSide());
        }
    }

    public void updateWidgetsFromModuleData() {
        lockUpdate = true;
        module.getLowPos(pos);
        lowShape.setPos(pos);
        module.getHighPos(pos);
        highShape.setPos(pos);

        module.getLowSize(size);
        lowShape.setShapeSize(size);
        module.getHighSize(size);
        highShape.setShapeSize(size);

        // shape edge and side
        lowShape.setShape(module.getLowShape());
        highShape.setShape(module.getHighShape());
        lowShape.setEdge(module.getLowEdge());
        highShape.setEdge(module.getLowEdge());
        highShape.setSide(module.getLowSide());
        highShape.setSide(module.getHighSide());
        lockUpdate = false;
    }

    @Override
    public Array<Vector2> getPoints() {
        return module.getPoints();
    }

    @Override
    public void removePoint(int index) {
        module.removePoint(index);
    }

    @Override
    public int createPoint(float x, float y) {
        return module.createPoint(x, y);
    }

    public void setEquals(boolean eequals) {
        equalsButton.setChecked(eequals);
    }

    public void setPoints(Array<Vector2> points) {
        module.setPoints(points);
    }
}
