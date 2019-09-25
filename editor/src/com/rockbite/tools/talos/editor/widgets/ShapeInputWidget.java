package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import static com.rockbite.tools.talos.runtime.modules.OffsetModule.*;

public class ShapeInputWidget extends Table {

    Array<String> shapeTypes = new Array<>();

    TextField scaleField;
    CheckBox edgeBox;
    ShapeWidget shapeWidget;
    SelectBox<String> shapeType;

    ChangeListener changeListener;

    public ShapeInputWidget(Skin skin) {
        setSkin(skin);

        shapeTypes.add("SQUARE");
        shapeTypes.add("ELLIPSE");
        shapeTypes.add("LINE");

        scaleField = new TextField("5", skin);
        shapeWidget = new ShapeWidget(skin);
        shapeType = new SelectBox<>(skin);

        edgeBox = new CheckBox("", skin);
        edgeBox.setChecked(true);

        shapeType.setItems(shapeTypes);

        add(edgeBox).width(50);
        add(scaleField).width(45).padLeft(5).row();
        add(shapeWidget).size(100).colspan(2).row();
        add(shapeType).width(100).colspan(2);


        shapeType.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                shapeWidget.setType(getShape());

                if(changeListener != null) changeListener.changed(new ChangeListener.ChangeEvent(), ShapeInputWidget.this);
            }
        });

        shapeWidget.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(changeListener != null) changeListener.changed(new ChangeListener.ChangeEvent(), ShapeInputWidget.this);
            }
        });

        scaleField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(changeListener != null) changeListener.changed(new ChangeListener.ChangeEvent(), ShapeInputWidget.this);
            }
        });

        edgeBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(changeListener != null) changeListener.changed(new ChangeListener.ChangeEvent(), ShapeInputWidget.this);
            }
        });
    }


    public void getShapePos(Vector2 pos) {
        try {
            float scl = Float.parseFloat(scaleField.getText());
            pos.set(shapeWidget.getPosX(), shapeWidget.getPosY()).scl(scl);
        } catch (Exception e) {

        }
    }

    public void getShapeSize(Vector2 size) {
        try {
            float scl = Float.parseFloat(scaleField.getText());
            size.set(shapeWidget.getShapeWidth(), shapeWidget.getShapeHeight()).scl(scl);
        } catch (Exception e) {

        }
    }

    public void setListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    public void setPos(Vector2 tmp) {
        try {
            float scl = Float.parseFloat(scaleField.getText());
            shapeWidget.setPos(tmp.x / scl, tmp.y / scl);
        } catch (Exception e) {

        }
    }

    public void setShapeSize(Vector2 size) {
        try {
            float scl = Float.parseFloat(scaleField.getText());
            shapeWidget.setShapeSize(size.x / scl, size.y / scl);
        } catch (Exception e) {

        }
    }

    public int getShape() {
        int res = 0;
        String type = shapeType.getSelected();

        if(type.equals("SQUARE")) res = TYPE_SQUARE;
        if(type.equals("ELLIPSE")) res =TYPE_ELLIPSE;
        if(type.equals("LINE")) res =TYPE_LINE;

        return res;
    }

    public boolean isEdge() {
        return edgeBox.isChecked();
    }
}
