package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyFloatWrapper;
import com.talosvfx.talos.editor.nodes.widgets.SelectWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.UIUtils;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class CustomFloatWidget extends ATypeWidget<Float> {

    private final Cell<Table> contentCell;
    private final Table content;

    private final Table rangeTable;
    private final ValueWidget valueWidget;
    private final SelectWidget rangeWidget;
    private ValueWidget minWidget;
    private ValueWidget maxWidget;
    private ValueWidget stepWidget;

    public CustomFloatWidget() {
        valueWidget = new ValueWidget();
        valueWidget.init(SharedResources.skin);
        valueWidget.setMainColor(ColorLibrary.BackgroundColor.BLACK_TRANSPARENT);
        valueWidget.setLabel("Value");

        valueWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        add(valueWidget).padLeft(4).padRight(4).width(220).padTop(9);
        row();

        rangeWidget = new SelectWidget();
        rangeWidget.init(SharedResources.skin);
        String[] options = new String[]{"NORMAL", "RANGE"};
        rangeWidget.setOptions(options);
        add(rangeWidget).padLeft(4).padRight(4).width(220).padTop(5);
        row();

        rangeWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(isRanged()) {
                    valueWidget.setShowProgress(true);
                    expand();
                } else {
                    valueWidget.setShowProgress(false);
                    collapse();
                }
            }
        });

        content = new Table();
        contentCell = add(content);

        rangeTable = buildRangeTable();

        row();
        add().padBottom(10);
    }

    private Table buildRangeTable() {
        Table table = new Table();

        minWidget = new ValueWidget();
        minWidget.init(SharedResources.skin);
        minWidget.setMainColor(ColorLibrary.BackgroundColor.BLACK_TRANSPARENT);
        minWidget.setLabel("Min");
        minWidget.setType(ValueWidget.Type.TOP);
        table.add(minWidget).padLeft(4).padRight(4).width(220).padTop(5);
        table.row();
        maxWidget = new ValueWidget();
        maxWidget.init(SharedResources.skin);
        maxWidget.setMainColor(ColorLibrary.BackgroundColor.BLACK_TRANSPARENT);
        maxWidget.setLabel("Max");
        maxWidget.setType(ValueWidget.Type.BOTTOM);
        table.add(maxWidget).padLeft(4).padRight(4).width(220);
        table.row();

        stepWidget = new ValueWidget();
        stepWidget.init(SharedResources.skin);
        stepWidget.setMainColor(ColorLibrary.BackgroundColor.BLACK_TRANSPARENT);
        stepWidget.setLabel("Step");
        stepWidget.setType(ValueWidget.Type.NORMAL);
        table.add(stepWidget).padTop(5).padLeft(4).padRight(4).width(220);
        table.row();

        table.pack();

        minWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                valueWidget.setRange(minWidget.getValue(), maxWidget.getValue());
                valueWidget.setStep(stepWidget.getValue());
                fireChangedEvent();
            }
        });

        maxWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                valueWidget.setRange(minWidget.getValue(), maxWidget.getValue());
                valueWidget.setStep(stepWidget.getValue());
                fireChangedEvent();
            }
        });

        stepWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                valueWidget.setRange(minWidget.getValue(), maxWidget.getValue());
                valueWidget.setStep(stepWidget.getValue());
                fireChangedEvent();
            }
        });

        return table;
    }

    private void expand() {
        content.clearChildren();
        content.add(rangeTable);
        content.pack();
        UIUtils.invalidateForDepth(content, 6);
    }

    private void collapse() {
        content.clearChildren();
        UIUtils.invalidateForDepth(content, 6);
    }

    private boolean isRanged() {
        String value = rangeWidget.getValue();
        return value.equalsIgnoreCase("range");
    }

    @Override
    public String getTypeName() {
        return "float";
    }

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<Float> propertyWrapper) {
        PropertyFloatWrapper floatWrapper = (PropertyFloatWrapper) propertyWrapper;
        if (floatWrapper.isRanged) {
            rangeWidget.setValue("RANGE");
            valueWidget.setValue(floatWrapper.defaultValue);
            stepWidget.setValue(floatWrapper.step);
            minWidget.setValue(floatWrapper.minValue);
            maxWidget.setValue(floatWrapper.maxValue);
            expand();
        } else {
            rangeWidget.setValue("NORMAL");
        }
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<Float> propertyWrapper) {
        PropertyFloatWrapper floatPropertyWrapper = (PropertyFloatWrapper) propertyWrapper;
        boolean ranged = isRanged();
        floatPropertyWrapper.isRanged = ranged;
        if (ranged) {
            floatPropertyWrapper.minValue = minWidget.getValue();
            floatPropertyWrapper.maxValue = maxWidget.getValue();
            floatPropertyWrapper.step = stepWidget.getValue();
        }
        floatPropertyWrapper.defaultValue = valueWidget.getValue();
    }
}
