package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.AbstractRoutineNodeWidget;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

import java.util.Comparator;

public class StaggerNode extends RoutineNode {

    private Array<GameObject> orderedArray = new Array<>();

    private final PositionComparator positionComparator;

    public StaggerNode() {
        positionComparator = new PositionComparator();
    }

    @Override
    public Object queryValue(String targetPortName) {

        String strategy = fetchStringValue("strategy");
        String sorting = fetchStringValue("sorting");
        float min = fetchFloatValue("min");
        float max = fetchFloatValue("max");

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        Array<GameObject> executedTargets = (Array<GameObject>) routineInstanceRef.fetchGlobal("executedTargets");

        float interpolation = 0;

        if(executedTargets != null && !executedTargets.isEmpty()) {
            if(strategy.equals("INDEX")) {
                interpolation = (float)executedTargets.indexOf(target, true)/(executedTargets.size-1);
            } else if(strategy.equals("YPOS") || strategy.equals("XPOS")) {
                orderedArray.clear();
                orderedArray.addAll(executedTargets);

                PositionComparator.Dimension dimension = PositionComparator.Dimension.X;
                PositionComparator.Order order = PositionComparator.Order.ASC;

                if(strategy.equals("YPOS")) dimension = PositionComparator.Dimension.Y;
                if(sorting.equals("DESC")) order = PositionComparator.Order.DESC;

                positionComparator.configure(dimension, order);

                orderedArray.sort(positionComparator);

                interpolation = (float)orderedArray.indexOf(target, true)/(orderedArray.size-1);
            }


            return min + (max - min) * interpolation;
        }

        return super.queryValue(targetPortName);
    }

    static class PositionComparator implements Comparator<GameObject> {

        public enum Dimension {
            X, Y
        }

        public enum Order {
            ASC, DESC
        }

        private Order order;
        private Dimension dimension;

        public void configure(Dimension dimension, Order order) {
            this.dimension = dimension;
            this.order = order;
        }

        @Override
        public int compare(GameObject o1, GameObject o2) {
            TransformComponent t1 = o1.getComponent(TransformComponent.class);
            TransformComponent t2 = o2.getComponent(TransformComponent.class);

            float mul = -1;

            if(order == Order.DESC) mul = 1;

            float res = 0;
            if (dimension == Dimension.X) {
                res = (t2.position.x - t1.position.x) * mul;
            } else {
                res = (t2.position.y - t1.position.y) * mul;
            }

            if(res > 0) return 1;
            if(res < 0) return -1;

            return 0;
        }
    }
}
