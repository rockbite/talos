package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class MoveByNode extends AbstractGenericTweenNode {

    private Vector2 originalPos = new Vector2();
    private Vector2 moveBy = new Vector2();
    private TransformComponent transform;

    @Override
    protected void startTween(GameObject target) {
        transform = target.getComponent(TransformComponent.class);

        originalPos.set(transform.position);
        moveBy.set((float)getWidgetValue("X"), (float)getWidgetValue("Y"));
    }

    @Override
    protected void tick(float alpha) {

        transform.position.x = originalPos.x + moveBy.x * alpha;
        transform.position.y = originalPos.y + moveBy.y * alpha;

    }
}
