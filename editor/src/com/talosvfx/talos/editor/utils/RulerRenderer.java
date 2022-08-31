package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import static com.kotcrab.vis.ui.VisUI.getSkin;

public class RulerRenderer extends Group {

    private GridRenderer gridRenderer;

    private Table yRulerTable;
    private Table xRulerTable;

    public RulerRenderer (GridRenderer gridRenderer) {
        this.gridRenderer = gridRenderer;
        addRulers();
    }

    protected void addRulers () {
        Skin skin = TalosMain.Instance().getSkin();
        xRulerTable = new Table(skin);
        xRulerTable.background("panel_input_bg");
        addActor(xRulerTable);

        yRulerTable = new Table(skin);
        yRulerTable.background("panel_input_bg");
        addActor(yRulerTable);
    }

    public void configureRulers (ViewportWidget viewportWidget) {
        xRulerTable.clearChildren();
        xRulerTable.setWidth(viewportWidget.getWidth());
        float rulerHeight = 20f;
        xRulerTable.setY(viewportWidget.getHeight() - rulerHeight);
        xRulerTable.setHeight(rulerHeight);

        float xStart = gridRenderer.gridXStart;
        while (xStart <= gridRenderer.gridXEnd) {
            xStart += gridRenderer.gridUnit;

            String coordText;
            int testInt = (int)xStart;
            float tmp = xStart - testInt;
            coordText = tmp > 0 ? "" + xStart : "" + testInt;
            Label coordinateLabel = new Label(coordText, getSkin());
            coordinateLabel.setX(viewportWidget.getLocalFromWorld(xStart, 0).x - coordinateLabel.getWidth() / 2f);
            xRulerTable.addActor(coordinateLabel);
        }

        yRulerTable.clearChildren();
        float yStart = gridRenderer.gridYStart;

        float maxWidth = 0;
        yRulerTable.clearChildren();
        yRulerTable.setHeight(viewportWidget.getHeight());
        while (yStart <= gridRenderer.gridYEnd) {
            yStart += gridRenderer.gridUnit;

            String coordText;
            int testInt = (int)yStart;
            float tmp = yStart - testInt;
            coordText = tmp > 0 ? "" + yStart : "" + testInt;
            Label coordinateLabel = new Label(coordText, getSkin());
            coordinateLabel.setY(viewportWidget.getLocalFromWorld(0, yStart).y - coordinateLabel.getHeight() / 2f);
            if (maxWidth < coordinateLabel.getWidth()) {
                maxWidth = coordinateLabel.getWidth();
            }
            yRulerTable.addActor(coordinateLabel);
        }
        yRulerTable.setWidth(maxWidth);
    }

}
