package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.widgets.ui.common.LabelWithZoom;

public class ProbabilityChoiceWidget extends RoutineNodeWidget {


    private Table container;

    private ObjectMap<String, ProbabilityWidget> map = new ObjectMap<>();
    private ObjectMap<ProbabilityWidget, Cell<ProbabilityWidget>> cellMap = new ObjectMap<>();
    private boolean reading = false;

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        container = getCustomContainer("container");
    }

    private String getFeeName(String prefix) {
        if(map.size == 0) return prefix+ "0";
        int max = Integer.parseInt(map.keys().next().substring(prefix.length()));
        for(String name: map.keys()) {
            int num = Integer.parseInt(name.substring(prefix.length()));
            if(num > max) {
                max = num;
            }
        }

        // try to find holes
        for(int i = 0; i <= max; i++) {
            if(!map.containsKey(prefix+i)) {
                return prefix + i;
            }
        }

        return prefix+(max+1);
    }

    @Override
    protected void readProperties(JsonValue properties) {
        reading = true;
        for(int i = 0; i < properties.size; i++) {
            String name = properties.get(i).name;
            float value = properties.get(i).asFloat();
            if(!widgetMap.containsKey(name)) {
                addProbabilityRow(name);
                ProbabilityWidget widget = (ProbabilityWidget) widgetMap.get(name);
                widget.setValue(value);
            }
        }

        ProbabilityWidget widget = addProbabilityRow(getFeeName("input"));
        widget.setDisabled(true);

        if(properties.size == 0) {
            widget.setValue(100);
        }

        reading = false;
    }

    private void adjustOthersExcept(ProbabilityWidget widget) {
        if(reading) return;

        float sum = 0;
        Array<ProbabilityWidget> list = new Array<>();
        for(String name: map.keys()) {
            ProbabilityWidget item = map.get(name);
            if(!item.isDisabled()) {
                list.add(item);
                sum += item.getValue();
            }
        }

        float target = 100;
        float magnitude = target/sum; // this is how much everything in list has to grow
        if(widget != null) {
            if(list.size > 1) {
                list.removeValue(widget, true);
                target = target - widget.getValue();
                sum = sum - widget.getValue();
                if(sum > 0) {
                    magnitude = target / sum;
                } else {
                    magnitude = 0;
                }
            }
        }

        reading = true; // lock this event
        for (ProbabilityWidget probabilityWidget : list) {
            float newVal = target/list.size;
            if(sum != 0) {
                newVal = probabilityWidget.getValue() * magnitude;
            }
            probabilityWidget.setValue(newVal);
        }
        reading = false;
    }

    private ProbabilityWidget addProbabilityRow(String name) {
        ProbabilityWidget widget = new ProbabilityWidget();
        widget.init(getSkin());

        Cell<ProbabilityWidget> probabilityWidgetCell = container.add(widget).padTop(5).padBottom(5).growX();
        probabilityWidgetCell.row();

        widgetMap.put(name, widget);
        typeMap.put(name, "float");
        defaultsMap.put(name, "0.0");
        addConnection(widget, name, true);
        widget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent changeEvent, Actor actor) {
                adjustOthersExcept(widget);
                reportNodeDataModified();
            }
        });


        addConnection(widget, name, true);

        map.put(name, widget);
        cellMap.put(widget, probabilityWidgetCell);

        return widget;
    }

    @Override
    public void attachNodeToMyInput(NodeWidget node, String mySlot, String targetSlot) {
        super.attachNodeToMyInput(node, mySlot, targetSlot);

        ProbabilityWidget curr = (ProbabilityWidget) widgetMap.get(mySlot);
        curr.setDisabled(false);

        // adding latest one
        if(widgetMap.size - 1 == inputs.size) {
            ProbabilityWidget widget = addProbabilityRow(getFeeName("input"));
            // lower position
            setY(getY() - 42);
            layout();

            widget.setDisabled(true);
        }
    }

    @Override
    public void setSlotConnectionInactive(NodeBoard.NodeConnection nodeConnection, boolean isInput) {
        String toId = nodeConnection.toId;
        super.setSlotConnectionInactive(nodeConnection, isInput);

        if(isInput) {
            ProbabilityWidget probabilityChoiceWidget = map.get(toId);

            probabilityChoiceWidget.remove();
            Cell<ProbabilityWidget> probabilityWidgetCell = cellMap.get(probabilityChoiceWidget);
            probabilityWidgetCell.height(0).pad(0);
            probabilityWidgetCell.getTable().invalidateHierarchy();
            //container.getCells().removeValue(probabilityWidgetCell, true);
            setY(getY() + 42);

            widgetMap.remove(toId);
            typeMap.remove(toId);
            defaultsMap.remove(toId);
            map.remove(toId);
            cellMap.remove(probabilityChoiceWidget);
            inputs.remove(toId);

            adjustOthersExcept(null);
        }
    }

    public static class ProbabilityWidget extends AbstractWidget<Float>  {

        private ValueWidget widget;

        public ProbabilityWidget() {

        }

        @Override
        public void init(Skin skin) {
            super.init(skin);

            Label label = new LabelWithZoom("weight", getSkin());
            widget = new ValueWidget();
            widget.init(getSkin());
            widget.setRange(0, 100);
            widget.setStep(1f);
            widget.setShowProgress(true);

            add(label).left().expand();
            add(widget).right();
        }

        @Override
        public boolean isFastChange() {
            return widget.isFastChange();
        }

        public void setValue(float val) {
            widget.setValue(val);
        }

        @Override
        public void loadFromXML(XmlReader.Element element) {

        }

        @Override
        public Float getValue() {
            return widget.getValue();
        }

        @Override
        public void read(Json json, JsonValue jsonValue) {
           widget.read(json, jsonValue);
        }

        @Override
        public void write(Json json, String name) {
            widget.write(json, name);
        }

        public void setDisabled(boolean isDisabled) {
            widget.setDisabled(isDisabled);
        }

        public boolean isDisabled() {
            return widget.isDisabled();
        }
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        super.read(json, jsonValue);
    }

    @Override
    public void write (Json json) {
        json.writeValue("name", getNodeName());
        json.writeValue("id", getUniqueId());
        json.writeValue("title", title.getText());
        json.writeObjectStart("position");
        json.writeValue("x", getX() + "");
        json.writeValue("y", getY() + "");
        json.writeObjectEnd();

        json.writeObjectStart("properties");

        for(String name: widgetMap.keys()) {
            if(inputs.get(name) != null) {
                AbstractWidget widget = widgetMap.get(name);
                widget.write(json, name);
            }
        }

        writeProperties(json);

        json.writeObjectEnd();
    }

    @Override
    public void finishedCreatingFresh() {
        if(inputs.size == 0) {
            ProbabilityWidget widget = addProbabilityRow(getFeeName("input"));
            widget.setDisabled(true);
            widget.setValue(100);
        }
    }
}
