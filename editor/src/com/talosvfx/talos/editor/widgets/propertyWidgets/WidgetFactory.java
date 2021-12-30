package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WidgetFactory {

    public static PropertyWidget generate(Object parent, String fieldName, String title) {
        try {
            Field field = parent.getClass().getField(fieldName);
            Object object = field.get(parent);

            if(field.getType().equals(boolean.class)) {
                return generateForBoolean(parent, field, object, title);
            } else if (field.getType().isEnum()) {
                return generateForEnum(parent, field, object, title);
            } else if(field.getType().equals(int.class)) {
                return generateForInt(parent, field, object, title);
            } else if(field.getType().equals(float.class)) {
                return generateForFloat(parent, field, object, title);
            } else if(field.getType().equals(Color.class)) {
                return generateForColor(parent, field, object, title);
            }
            else if(field.getType().equals(Vector2.class)) {
                return generateForVector2(parent, field, object, title);
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector2PropertyWidget generateForVector2 (Object parent, Field field, Object object, String title) {
        Vector2PropertyWidget widget = new Vector2PropertyWidget(title) {
            @Override
            public Vector2 getValue () {
                try {
                    Vector2 val = (Vector2) field.get(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return new Vector2(0, 0);
                }
            }

            @Override
            public void valueChanged (Vector2 value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        };

        return widget;
    }

    private static ColorPropertyWidget generateForColor (Object parent, Field field, Object object, String title) {
        ColorPropertyWidget widget = new ColorPropertyWidget(title) {
            @Override
            public Color getValue () {
                try {
                    Color val = (Color) field.get(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return Color.WHITE;
                }
            }

            @Override
            public void valueChanged (Color value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        };

        return widget;
    }

    private static CheckboxWidget generateForBoolean (Object parent, Field field, Object object, String title) {
        CheckboxWidget widget = new CheckboxWidget(title) {
            @Override
            public Boolean getValue () {
                try {
                    Boolean val = field.getBoolean(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return false;
                }
            }

            @Override
            public void valueChanged (Boolean value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        };

        return widget;
    }

    private static IntPropertyWidget generateForInt (Object parent, Field field, Object object, String title) {
        IntPropertyWidget widget = new IntPropertyWidget(title) {
            @Override
            public Integer getValue () {
                try {
                    Integer val = field.getInt(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return 0;
                }
            }

            @Override
            public void valueChanged (Integer value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        };

        return widget;
    }

    private static FloatPropertyWidget generateForFloat (Object parent, Field field, Object object, String title) {
        FloatPropertyWidget widget = new FloatPropertyWidget(title) {
            @Override
            public Float getValue () {
                try {
                    Float val = field.getFloat(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return 0f;
                }
            }

            @Override
            public void valueChanged (Float value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        };

        return widget;
    }

    private static PropertyWidget generateForEnum (Object parent, Field field, Object object, String title) {
        final Array<String> list = new Array<>();
        final ObjectMap<String, Object> map = new ObjectMap<>();
        try {
            Method method = object.getClass().getDeclaredMethod("values");
            Object[] obj = (Object[]) method.invoke(null);

            for(Object enumVal: obj) {
                list.add(enumVal.toString());
                map.put(enumVal.toString(), enumVal);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

        }

        SelectBoxWidget widget = new SelectBoxWidget("Render Mode") {
            @Override
            public Array<String> getOptionsList() {
                return list;
            }

            @Override
            public String getValue() {
                try {
                    field.get(parent);
                } catch (IllegalAccessException e) {

                }

                return list.first();
            }

            @Override
            public void valueChanged(String value) {
                for(String name: list) {
                    if(name.equals(value)) {
                        try {
                            field.set(parent, map.get(value));
                        } catch (IllegalAccessException e) {

                        }
                    }
                }
            }
        };

        return  widget;
    }
}
