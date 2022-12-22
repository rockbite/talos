package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

import java.util.Comparator;

public class StaggerNode extends AbstractRoutineNode {

    private final PositionComparator positionComparator;

    public StaggerNode() {
        positionComparator = new PositionComparator();
    }

    @Override
    public Object getOutputValue(String name, ObjectMap<String, Object> params) {

        if(params != null) {

            String strategy = (String) getWidgetValue("strategy");
            String sorting = (String) getWidgetValue("sorting");

            Array<GameObject> neighbours = (Array<GameObject>) params.get("neighbours");
            GameObject target = (GameObject) params.get("targetGO");

            float interpolation = 0;

            if(neighbours == null || neighbours.size == 0) return 0;

            if(strategy.equals("INDEX")) {
                interpolation = (float) params.get("chunkIndex");
            } else if(strategy.equals("YPOS") || strategy.equals("XPOS")) {

                Array<GameObject> orderedArray = new Array<>();
                orderedArray.addAll(neighbours);
                PositionComparator.Dimension dimension = PositionComparator.Dimension.X;
                PositionComparator.Order order = PositionComparator.Order.ASC;
                if(strategy.equals("YPOS")) dimension = PositionComparator.Dimension.Y;
                if(sorting.equals("DESC")) order = PositionComparator.Order.DESC;

                positionComparator.configure(dimension, order);

                orderedArray.sort(positionComparator);

                interpolation = (float)orderedArray.indexOf(target, true)/orderedArray.size;
            }

            float min = (float) getWidgetValue("min");
            float max = (float) getWidgetValue("max");
            return min + (max - min) * interpolation;
        }

        return 0;
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
