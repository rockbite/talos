package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.ATypeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.PropertyTypeWidgetMapper;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.widgets.CustomVarWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ImageButton;
import com.talosvfx.talos.editor.widgets.ui.menu.BasicPopup;
import com.talosvfx.talos.runtime.routine.misc.PropertyTypeWrapperMapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

import java.util.Locale;

public class EventNodeWidget extends RoutineNodeWidget {
    private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();
    private Table fieldTable;

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        Table container = getCustomContainer("customParams");
        fieldTable = new Table();
        container.add(fieldTable).grow().row();

        ImageButton plusButton = new ImageButton(
                SharedResources.skin.newDrawable("mini-btn-bg", Color.WHITE),
                SharedResources.skin.newDrawable("ic-plus", Color.WHITE));

        container.add(plusButton).growX().row();

        plusButton.addListener(new ClickListener() {
            private BasicPopup<PropertyType> popup;
            private Vector2 temp = new Vector2();

            @Override
            public void clicked(InputEvent event, float x, float y) {
                temp.set(x, y);
                plusButton.localToScreenCoordinates(temp);
                popup = BasicPopup.build(PropertyType.class)
                        .addItem("Float", PropertyType.FLOAT)
                        .addItem("Vector2", PropertyType.VECTOR2)
                        .addItem("Boolean", PropertyType.BOOLEAN)
                        .addItem("Color", PropertyType.COLOR)
                        .addItem("Asset", PropertyType.ASSET)
                        .onClick(type -> {
                            PropertyWrapper<?> newPropertyWrapper = createNewPropertyWrapper(type);
                            newPropertyWrapper.isCollapsed = false; // make open by default
                            giveEmptyNameTo(newPropertyWrapper);
                            propertyWrappers.add(newPropertyWrapper);

                            reportNodeDataModified(false);
                        })
                        .show(plusButton, temp.x, temp.y);
            }
        });
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        super.read(json, jsonValue);

        propertyWrappers.clear();
        JsonValue propertiesJson = jsonValue.get("properties");
        JsonValue propertyWrappersJson = propertiesJson.get("customParams");
        if (propertyWrappersJson != null) {
            for (JsonValue propertyWrapperJson : propertyWrappersJson) {
                String className = propertyWrapperJson.getString("className", "");
                JsonValue property = propertyWrapperJson.get("property");
                if (property != null) {
                    try {
                        Class clazz = ClassReflection.forName(className);
                        PropertyWrapper propertyWrapper = (PropertyWrapper) ClassReflection.newInstance(clazz);
                        propertyWrapper.read(json, property);
                        propertyWrappers.add(propertyWrapper);
                    } catch (ReflectionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        fillFields();
    }

    @Override
    protected void writeProperties(Json json) {
        super.writeProperties(json);
        json.writeArrayStart("customParams");
        for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
            json.writeObjectStart();
            json.writeValue("className", propertyWrapper.getClass().getName());
            json.writeValue("property", propertyWrapper);
            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

    private PropertyWrapper<?> createNewPropertyWrapper(PropertyType propertyType) {
        PropertyWrapper<?> propertyWrapper = createPropertyInstanceOfType(propertyType);
        return propertyWrapper;
    }

    private PropertyWrapper<?> createPropertyInstanceOfType(PropertyType type) {
        try {
            PropertyWrapper<?> propertyWrapper = PropertyTypeWrapperMapper.getWrapperForPropertyType(type).getConstructor().newInstance();
            return propertyWrapper;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void giveEmptyNameTo(PropertyWrapper<?> wrapper) {
        Array<String> names = new Array<>();
        for (PropertyWrapper tmp : propertyWrappers) {
            if (tmp != wrapper) {
                names.add(tmp.propertyName);
            }
        }

        String suggestion = "value";
        int iterator = 0;
        while (names.contains(suggestion + iterator, false)) {
            iterator++;
        }

        wrapper.propertyName = suggestion + iterator;
    }

    private void fillFields() {
        fieldTable.clearChildren();

        for (int i = 0; i < propertyWrappers.size; i++) {
            PropertyWrapper<?> propertyWrapper = propertyWrappers.get(i);

            PropertyType type = propertyWrapper.getType();
            ATypeWidget innerWidget = obtainTypeWidget(type);
            CustomVarWidget widget = new CustomVarWidget(propertyWrapper, innerWidget);

            // init maps
            widgetMap.put(propertyWrapper.propertyName, widget);
            typeMap.put(propertyWrapper.propertyName, type.toString().toLowerCase(Locale.ROOT));
            defaultsMap.put(propertyWrapper.propertyName, "");

            addConnection(widget, propertyWrapper.propertyName, true);
            widget.setFieldName(propertyWrapper.propertyName);
            fieldTable.add(widget).padTop(2).growX();
            fieldTable.row();

            widget.addListener(new CustomVarWidget.CustomVarWidgetChangeListener() {

                @Override
                public void nameChanged(CustomVarChangeEvent event, Actor actor, String oldName, String newName, boolean isFastChange) {
                    if (newName.equals(oldName)) {
                        return;
                    }
                    NodeBoard.NodeConnection connection = nodeBoard.findConnection(EventNodeWidget.this, true, oldName);
                    nodeBoard.removeConnection(connection, false);
                    nodeBoard.addConnectionCurve(connection.fromNode, connection.toNode, connection.fromId, newName);
                    reportNodeDataModified(isFastChange);
                }

                @Override
                public void valueChanged(CustomVarChangeEvent event, Actor actor, boolean isFastChange) {
                    reportNodeDataModified(isFastChange);
                }

                @Override
                public void delete(CustomVarChangeEvent event, Actor actor) {
                    propertyWrappers.removeValue(propertyWrapper, true);
                    reportNodeDataModified(false);
                }

                @Override
                public void collapse(CustomVarChangeEvent event, Actor actor) {
                                                                            reportNodeDataModified(false);
                                                                                                           }
            });

            innerWidget.updateFromPropertyWrapper(propertyWrapper);
        }
    }

    private ATypeWidget obtainTypeWidget(PropertyType type) {
        try {
            ATypeWidget innerWidget = ClassReflection.newInstance(PropertyTypeWidgetMapper.getWidgetForPropertyTYpe(type));
            return innerWidget;
        } catch (ReflectionException e) {
            throw new RuntimeException(e);
        }
    }
}
