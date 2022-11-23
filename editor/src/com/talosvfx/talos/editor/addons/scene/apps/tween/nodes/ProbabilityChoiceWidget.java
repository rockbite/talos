package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.LabelWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;

public class ProbabilityChoiceWidget extends RoutineNodeWidget {


    private Table container;

    private ObjectMap<String, ProbabilityWidget> map = new ObjectMap<>();
    private ObjectMap<ProbabilityWidget, Cell<ProbabilityWidget>> cellMap = new ObjectMap<>();

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        container = getCustomContainer("container");

        addProbabilityRow();
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
                return prefix+"i";
            }
        }

        return prefix+(max+1);
    }

    private void addProbabilityRow() {
        String name = getFeeName("input");

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
                reportNodeDataModified();
            }
        });


        addConnection(widget, name, true);

        map.put(name, widget);
        cellMap.put(widget, probabilityWidgetCell);
    }

    @Override
    public void attachNodeToMyInput(NodeWidget node, String mySlot, String targetSlot) {
        super.attachNodeToMyInput(node, mySlot, targetSlot);

        addProbabilityRow();
        // lower position
        setY(getY() - 42);
        layout();
    }

    @Override
    public void setSlotConnectionInactive(NodeBoard.NodeConnection nodeConnection, boolean isInput) {
        String toId = nodeConnection.toId;
        super.setSlotConnectionInactive(nodeConnection, isInput);

        ProbabilityWidget probabilityChoiceWidget = map.get(toId);

        probabilityChoiceWidget.remove();
        Cell<ProbabilityWidget> probabilityWidgetCell = cellMap.get(probabilityChoiceWidget);
        probabilityWidgetCell.height(0).pad(0);
        probabilityWidgetCell.getTable().invalidateHierarchy();
        container.getCells().removeValue(probabilityWidgetCell, true);
        setY(getY() + 42);
    }

    public static class ProbabilityWidget extends AbstractWidget<Float>  {

        public ProbabilityWidget() {

        }

        @Override
        public void init(Skin skin) {
            super.init(skin);

            Label label = new Label("weight", getSkin());
            ValueWidget widget = new ValueWidget();
            widget.init(getSkin());
            widget.setRange(0, 100);
            widget.setStep(1f);
            widget.setShowProgress(true);

            add(label).left().expand();
            add(widget).right();
        }

        @Override
        public void loadFromXML(XmlReader.Element element) {

        }

        @Override
        public Float getValue() {
            return null;
        }

        @Override
        public void read(Json json, JsonValue jsonValue) {

        }

        @Override
        public void write(Json json, String name) {

        }
    }

}
