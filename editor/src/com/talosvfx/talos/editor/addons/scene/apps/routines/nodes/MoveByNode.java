package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class MoveByNode extends AbstractGenericRoutineNode {

    @Override
    protected void startTween(GameObject target, GenericTweenData data) {
        TransformComponent transform = target.getComponent(TransformComponent.class);
        Vector2 original = new Vector2(transform.position);
        ObjectMap<String, Object> params = buildParams(target);
        Vector2 offset = new Vector2((float)getWidgetValue("X", params), (float)getWidgetValue("Y", params));

        data.misc = new ObjectMap<>();
        data.misc.put("transform", transform);
        data.misc.put("original", original);
        data.misc.put("offset", offset);
    }

    @Override
    protected void tick(String target, GenericTweenData data, float alpha) {
        Vector2 original = (Vector2) data.misc.get("original");
        Vector2 offset = (Vector2) data.misc.get("offset");
        TransformComponent transform = (TransformComponent) data.misc.get("transform");

        alpha = data.interpolation.apply(alpha);

        transform.position.x = original.x + offset.x * alpha;
        transform.position.y = original.y + offset.y * alpha;
    }
}
