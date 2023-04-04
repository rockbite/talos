package com.talosvfx.talos.runtime.routine.nodes;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class ExposedVariableChangedNode extends RoutineNode {


    private ObjectMap<String, PropertyWrapper> cachedValues = new ObjectMap<>();


    @Override
    public void receiveSignal (String portName) {

        ObjectMap<String, PropertyWrapper> properties = routineInstanceRef.getProperties();

        boolean changes = checkAndUpdateChanges(properties, cachedValues);


        if (changes) {
            sendSignal("changed");
        }
    }

    private ObjectSet<String> temp = new ObjectSet<>();

    private boolean checkAndUpdateChanges (ObjectMap<String, PropertyWrapper> properties, ObjectMap<String, PropertyWrapper> cachedValues) {
        boolean hasChanges = false;


        temp.clear();
        String filter1 = fetchStringValue("filter1");
        String filter2 = fetchStringValue("filter2");
        String filter3 = fetchStringValue("filter3");

        if (filter1 != null && !filter1.isEmpty()) {
            temp.add(filter1);
        }
        if (filter2 != null && !filter2.isEmpty()) {
            temp.add(filter2);
        }
        if (filter3 != null && !filter3.isEmpty()) {
            temp.add(filter3);
        }

        boolean checkFilter = temp.size > 0;

        for (ObjectMap.Entry<String, PropertyWrapper> property : properties) {
            if (!cachedValues.containsKey(property.key)) {
                cachedValues.put(property.key, property.value.clone());

                if (checkFilter) {
                    if (temp.contains(property.key)) {
                        hasChanges = true;
                    }
                } else {
                    hasChanges = true;
                }
            } else {
                Object o = cachedValues.get(property.key).getValue();
                if (!property.value.getValue().equals(o)) {
                    if (checkFilter) {
                        if (temp.contains(property.key)) {
                            hasChanges = true;
                        }
                    } else {
                        hasChanges = true;
                    }
                }
            }
            cachedValues.get(property.key).setValue(property.value.getValue());
        }

        return hasChanges;

    }


}
