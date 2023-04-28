package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.utils.ReflectionUtilities;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.property.GameObjectSelectWidget;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.ValueProperty;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyBooleanWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyColorWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyFloatWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyGameAssetWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyGameObjectWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyIntegerWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyStringWrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyVec2Wrapper;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class WidgetFactory {

    public static PropertyWidget generateForPropertyWrapper (PropertyWrapper wrapper) {
        try {
            Field value = wrapper.getClass().getField("value");
            if (wrapper instanceof PropertyBooleanWrapper) {
                return generateForBoolean(wrapper, value, null, wrapper.propertyName, false);
            } else if (wrapper instanceof PropertyIntegerWrapper) {
                return generateForInt(wrapper, value, null, wrapper.propertyName, false);
            } else if(wrapper instanceof PropertyFloatWrapper) {
                return generateForFloat(wrapper, value, null, wrapper.propertyName, false);
            }  else if(wrapper instanceof PropertyStringWrapper) {
                return generateForString(wrapper, value, null, wrapper.propertyName);
            } else if (wrapper instanceof PropertyGameObjectWrapper) {
                return generateForGameObject((PropertyGameObjectWrapper) wrapper);
            } else if (wrapper instanceof PropertyVec2Wrapper) {
                return generateForVector2(wrapper, value, null, wrapper.propertyName);
            } else if (wrapper instanceof PropertyColorWrapper) {
                return generateForColor(wrapper, value, null, wrapper.propertyName);
            } else if (wrapper instanceof PropertyGameAssetWrapper) {
                return generateForGameAsset(wrapper, value, null, wrapper.propertyName, GameAssetType.SPRITE);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PropertyWidget generate (Object parent, Field field, String title) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Object object = field.get(parent);

            PropertyWidget generatedWidget = null;
            if(field.getType().equals(boolean.class)) {
                generatedWidget = generateForBoolean(parent, field, object, title, true);
            } else if (field.getType().isEnum()) {
                generatedWidget = generateForEnum(parent, field, object, title);
            } else if(field.getType().equals(int.class)) {
                generatedWidget = generateForInt(parent, field, object, title, true);
            } else if(field.getType().equals(float.class)) {
                generatedWidget = generateForFloat(parent, field, object, title, true);
            } else if(field.getType().equals(Color.class)) {
                generatedWidget = generateForColor(parent, field, object, title);
            } else if(field.getType().equals(Vector2.class)) {
                generatedWidget = generateForVector2(parent, field, object, title);
            } else if(field.getType().equals(String.class)) {
                ValueProperty annotation = field.getAnnotation(ValueProperty.class);
                if(annotation != null && annotation.readOnly()) {
                    generatedWidget = generateForStaticString(parent, field, object, title);
                } else {
                    generatedWidget = generateForString(parent, field, object, title);
                }
            } else if (field.getType().equals(GameObject.class)) {
                return generateForGameObject(parent, field, object, title);
            }
            if (generatedWidget == null) {
                return null;
            }

//            generatedWidget.setParent(parent);
            return generatedWidget;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static PropertyWidget generate(Object parent, String fieldName, String title) {
        try {

            Field field = ReflectionUtilities.getFieldWithName(fieldName, parent.getClass(), null);
            return generate(parent, field, title);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return null;
    }



    private static PropertyWidget generateForGameObject (Object parent, Field field, Object object, String title) {
       GameObjectSelectWidget gameObjectSelectWidget = new GameObjectSelectWidget(title, new Supplier<GameObject>() {
           @Override
           public GameObject get () {
               try {
                   GameObject gameObject = (GameObject)field.get(parent);
                   return gameObject;
               } catch (IllegalAccessException e) {
                   throw new RuntimeException(e);
               }
           }
       }, new PropertyWidget.ValueChanged<GameObject>() {
           @Override
           public void report (GameObject value) {
               try {
                   field.set(parent, value);
               } catch (IllegalAccessException e) {
                   throw new RuntimeException(e);
               }
           }
       }, parent);

       return gameObjectSelectWidget;
    }

    public static <T> PropertyWidget generateForGameAsset (Object parent, String field, Object object, String title, GameAssetType assetType) {
        try {

            Field declaredField = ReflectionUtilities.getFieldWithName(field, parent.getClass(), null);
            declaredField.setAccessible(true);
            return generateForGameAsset(parent, declaredField, object, title, assetType);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> PropertyWidget generateForGameAsset (Object parent, Field field, Object object, String title, GameAssetType assetType) {
        PropertyPanelAssetSelectionWidget<T> textureWidget = new PropertyPanelAssetSelectionWidget<>(title, assetType, new Supplier<GameAsset<T>>() {
            @Override
            public GameAsset<T> get() {
                try {
                    return ((GameAsset<T>) field.get(parent));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, new PropertyWidget.ValueChanged<GameAsset<T>>() {
            @Override
            public void report(GameAsset<T> value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, parent);

        return textureWidget;
    }

    private static Vector2PropertyWidget generateForVector2 (Object parent, Field field, Object object, String title) {
        Vector2PropertyWidget widget = new Vector2PropertyWidget(title, new Supplier<Vector2>() {
            @Override
            public Vector2 get() {
                try {
                    Vector2 val = (Vector2) field.get(parent);
                    return val;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return new Vector2(0, 0);
                }
            }
        }, new PropertyWidget.ValueChanged<Vector2>() {
            @Override
            public void report(Vector2 value) {
                try {
                    Vector2 vec = (Vector2) field.get(parent);

                    if(!Float.isNaN(value.x)) {
                        vec.x = value.x;
                    }
                    if(!Float.isNaN(value.y)) {
                        vec.y = value.y;
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, parent);

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
                    e.printStackTrace();
                    return Color.WHITE;
                }
            }
        }, new PropertyWidget.ValueChanged<Color>() {
            @Override
            public void report(Color value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, parent);

        return widget;
    }

    private static CheckboxWidget generateForBoolean (Object parent, Field field, Object object, String title, boolean primitive) {

        CheckboxWidget widget = new CheckboxWidget(title, new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                try {
                    return primitive ? field.getBoolean(parent) : (Boolean) field.get(parent);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }, new PropertyWidget.ValueChanged<Boolean>() {
            @Override
            public void report(Boolean value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, parent);

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
                    e.printStackTrace();
                    return "";
                }
            }
        }, parent);

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
                    e.printStackTrace();
                    return "";
                }
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, parent);

        return widget;
    }

    private static IntPropertyWidget generateForInt (Object parent, Field field, Object object, String title, boolean primitive) {
        IntPropertyWidget widget = new IntPropertyWidget(title, new Supplier<Integer>() {
            @Override
            public Integer get() {
                try {
                    return primitive ? field.getInt(parent) : (Integer) field.get(parent);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        }, new PropertyWidget.ValueChanged<Integer>() {
            @Override
            public void report(Integer value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, parent);

        return widget;
    }

    private static GameObjectSelectWidget generateForGameObject (PropertyGameObjectWrapper wrapper) {
        GameObjectSelectWidget widget = new GameObjectSelectWidget(wrapper.propertyName, new Supplier<GameObject>() {
            @Override
            public GameObject get() {
                return wrapper.getValue();
            }
        }, new PropertyWidget.ValueChanged<GameObject>() {
            @Override
            public void report(GameObject value) {
                wrapper.setValue(value);
            }
        }, wrapper);

        return widget;
    }


    private static FloatPropertyWidget generateForFloat (Object parent, Field field, Object object, String title, boolean primitive) {
        FloatPropertyWidget widget = new FloatPropertyWidget(title, new Supplier<Float>() {
            @Override
            public Float get () {
                try {
                    return primitive ? field.getFloat(parent) : (Float) field.get(parent);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return 0f;
                }
            }
        }, new PropertyWidget.ValueChanged<Float>() {
            @Override
            public void report(Float value) {
                try {
                    field.set(parent, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, parent);

        if (parent instanceof PropertyFloatWrapper) {
            PropertyFloatWrapper numberWrapper = (PropertyFloatWrapper) parent;
            if (numberWrapper.isRanged) {
                widget.configureFromValues(numberWrapper.minValue, numberWrapper.maxValue, numberWrapper.step);
            }
        } else {
            widget.configureFromAnnotation(field.getAnnotation(ValueProperty.class));
        }

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
            e.printStackTrace();
        }

        SelectBoxWidget widget = new SelectBoxWidget(title, new Supplier<String>() {
            @Override
            public String get() {
                try {
                    return field.get(parent).toString();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
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
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                return list;
            }
        }, parent);

        return  widget;
    }


}
