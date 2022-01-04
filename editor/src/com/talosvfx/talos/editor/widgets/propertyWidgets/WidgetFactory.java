package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

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
            } else if(field.getType().equals(Vector2.class)) {
                return generateForVector2(parent, field, object, title);
            } else if(field.getType().equals(String.class)) {
                ValueProperty annotation = field.getAnnotation(ValueProperty.class);
                if(annotation.readOnly()) {
                    return generateForStaticString(parent, field, object, title);
                } else {
                    return generateForString(parent, field, object, title);
                }
            }


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector2PropertyWidget generateForVector2 (Object parent, Field field, Object object, String title) {
        Vector2PropertyWidget widget = new Vector2PropertyWidget(title, new Supplier<Vector2>() {
            @Override
            public Vector2 get() {
                try {
                    Vector2 val = (Vector2) field.get(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return new Vector2(0, 0);
                }
            }
        }, new PropertyWidget.ValueChanged<Vector2>() {
            @Override
            public void report(Vector2 value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        });

        widget.configureFromAnnotation(field.getAnnotation(ValueProperty.class));

        return widget;
    }

    private static ColorPropertyWidget generateForColor (Object parent, Field field, Object object, String title) {
        ColorPropertyWidget widget = new ColorPropertyWidget(title, new Supplier<Color>() {
            @Override
            public Color get() {
                try {
                    Color val = (Color) field.get(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return Color.WHITE;
                }
            }
        }, new PropertyWidget.ValueChanged<Color>() {
            @Override
            public void report(Color value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        });

        return widget;
    }

    private static CheckboxWidget generateForBoolean (Object parent, Field field, Object object, String title) {

        CheckboxWidget widget = new CheckboxWidget(title, new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                try {
                    Boolean val = field.getBoolean(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return false;
                }
            }
        }, new PropertyWidget.ValueChanged<Boolean>() {
            @Override
            public void report(Boolean value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        });

        return widget;
    }

    private static LabelWidget generateForStaticString (Object parent, Field field, Object object, String title) {
        LabelWidget widget = new LabelWidget(title, new Supplier<String>() {
            @Override
            public String get() {
                try {
                    String val = field.get(parent).toString();
                    return val;
                } catch (IllegalAccessException e) {
                    return "";
                }
            }
        });

        return widget;
    }

    private static EditableLabelWidget generateForString (Object parent, Field field, Object object, String title) {
        EditableLabelWidget widget = new EditableLabelWidget(title, new Supplier<String>() {
            @Override
            public String get() {
                try {
                    String val = field.get(parent).toString();
                    return val;
                } catch (IllegalAccessException e) {
                    return "";
                }
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        });

        return widget;
    }

    private static IntPropertyWidget generateForInt (Object parent, Field field, Object object, String title) {
        IntPropertyWidget widget = new IntPropertyWidget(title, new Supplier<Integer>() {
            @Override
            public Integer get() {
                try {
                    Integer val = field.getInt(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return 0;
                }
            }
        }, new PropertyWidget.ValueChanged<Integer>() {
            @Override
            public void report(Integer value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        });

        return widget;
    }

    private static FloatPropertyWidget generateForFloat (Object parent, Field field, Object object, String title) {
        FloatPropertyWidget widget = new FloatPropertyWidget(title, new Supplier<Float>() {
            @Override
            public Float get() {
                try {
                    Float val = field.getFloat(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    return 0f;
                }
            }
        }, new PropertyWidget.ValueChanged<Float>() {
            @Override
            public void report(Float value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {

                }
            }
        });

        widget.configureFromAnnotation(field.getAnnotation(ValueProperty.class));

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

        SelectBoxWidget widget = new SelectBoxWidget(title, new Supplier<String>() {
            @Override
            public String get() {
                try {
                    return field.get(parent).toString();
                } catch (IllegalAccessException e) {

                }

                return list.first();
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                for(String name: list) {
                    if(name.equals(value)) {
                        try {
                            field.set(parent, map.get(value));
                        } catch (IllegalAccessException e) {

                        }
                    }
                }
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                return list;
            }
        });

        return  widget;
    }
}
