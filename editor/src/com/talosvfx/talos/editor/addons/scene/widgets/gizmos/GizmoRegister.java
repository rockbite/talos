package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class GizmoRegister {

    public static ObjectMap<Class<? extends IComponent>, Class<? extends Gizmo>> map = new ObjectMap<>();

    public static void init() {
        map.put(TransformComponent.class, TransformGizmo.class);
    }

    public static Gizmo makeGizmoFor (IComponent component) {
        if(map.containsKey(component.getClass())) {
            Class clazz = map.get(component.getClass());
            try {
                Gizmo instance = (Gizmo) ClassReflection.newInstance(clazz);
                instance.setComponent(component);
                return instance;
            } catch (ReflectionException e) {
                return null;
            }
        }

        return null;
    }
}
