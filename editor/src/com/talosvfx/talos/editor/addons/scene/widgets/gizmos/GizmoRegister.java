package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.CameraComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class GizmoRegister {

    public static ObjectMap<Class<? extends IComponent>, Array<Class<? extends Gizmo>>> map = new ObjectMap<>();

    public static void init(XmlReader.Element root) {
        try {
            String componentClassPath = root.getAttribute("componentClassPath");
            String gizmoClassPath = root.getAttribute("gizmoClassPath");
            XmlReader.Element componentsContainer = root.getChildByName("components");
            Array<XmlReader.Element> components = componentsContainer.getChildrenByName("component");
            for (XmlReader.Element component : components) {
                String componentClassName = component.getAttribute("className");

                Class componentClazz = ClassReflection.forName(componentClassPath + "." + componentClassName);

                XmlReader.Element gizmosContainer = component.getChildByName("gizmos");
                Array<XmlReader.Element> gizmos = gizmosContainer.getChildrenByName("gizmo");

                Array<Class<? extends Gizmo>> list = new Array<>();

                for (XmlReader.Element gizmo : gizmos) {
                    String gizmoClassName = gizmo.getAttribute("className");
                    Class gizmoClazz = ClassReflection.forName(gizmoClassPath + "." + gizmoClassName);
                    list.add(gizmoClazz);
                }

                map.put(componentClazz, list);
            }
        } catch (ReflectionException e) {

        }
    }

    /**
     * todo: this needs pooling later, bot not super important
     * @param component
     * @return
     */
    public static Array<Gizmo> makeGizmosFor (IComponent component) {
        Array<Gizmo> list = new Array<>();
        if(map.containsKey(component.getClass())) {
            Array<Class<? extends Gizmo>> classes = map.get(component.getClass());

            for(Class clazz: classes) {
                try {
                    Gizmo instance = (Gizmo) ClassReflection.newInstance(clazz);
                    list.add(instance);
                } catch (ReflectionException e) {
                    return list;
                }
            }
        }

        return list;
    }
}
