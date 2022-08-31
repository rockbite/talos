package com.talosvfx.talos.editor.utils.grid;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import static com.kotcrab.vis.ui.VisUI.getSkin;

public class RulerRenderer extends Group {

    private GridPropertyProvider gridPropertyProvider;

    private Table yRulerTable;
    private Table xRulerTable;

    private ViewportWidget viewportWidget;

    public RulerRenderer (GridPropertyProvider gridRenderer, ViewportWidget widget) {
        this.gridPropertyProvider = gridRenderer;
        this.viewportWidget = widget;
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

    public void configureRulers () {
        xRulerTable.clearChildren();
        xRulerTable.setWidth(viewportWidget.getWidth());
        float rulerHeight = 20f;
        xRulerTable.setY(gridPropertyProvider.rulerOnBottom() ? 0 : viewportWidget.getHeight() - rulerHeight);
        xRulerTable.setHeight(rulerHeight);

        float xStart = gridPropertyProvider.getGridStartX();
        while (xStart <= gridPropertyProvider.getGridEndX()) {
            String coordText;
            int testInt = (int)xStart;
            float tmp = xStart - testInt;
            coordText = tmp > 0 ? "" + xStart : "" + testInt;
            Label coordinateLabel = new Label(coordText, getSkin());
            coordinateLabel.setX(viewportWidget.getLocalFromWorld(xStart, 0).x - coordinateLabel.getWidth() / 2f);
            xRulerTable.addActor(coordinateLabel);
            xStart += gridPropertyProvider.getUnitX();
        }

        yRulerTable.clearChildren();
        float yStart = gridPropertyProvider.getGridStartY();

        float maxWidth = 0;
        yRulerTable.clearChildren();
        yRulerTable.setHeight(viewportWidget.getHeight());
        while (yStart <= gridPropertyProvider.getGridEndY()) {

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
            yStart += gridPropertyProvider.getUnitY();

        }
        yRulerTable.setWidth(maxWidth);
    }

}
