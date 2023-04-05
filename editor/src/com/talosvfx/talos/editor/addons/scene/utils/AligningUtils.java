package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import lombok.Getter;


enum PositionAlign {

    LEFT(-0.5f),
    RIGHT(0.5f),
    TOP(0.5f),
    BOTTOM(-0.5f),
    CENTER(0);

    @Getter
    private float multiplayer;

    PositionAlign(float multiplayer) {
        this.multiplayer = multiplayer;
    }

}

public class AligningUtils {
    static Vector2 temp = new Vector2();

    public static Vector2 getSize(GameObject gameObject) {
        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        if (gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);
            return temp.set(spriteRendererComponent.getWidth() * transformComponent.scale.x,
                    spriteRendererComponent.getHeight() * transformComponent.scale.y);
        }
        return temp.set(transformComponent.scale.x, transformComponent.scale.y);
    }

    public static Vector2 calculateCenterPosition(Array<GameObject> selection) {
        float xSum = 0;
        float ySum = 0;

        for (GameObject gameObject : selection) {
            Vector2 position = gameObject.getComponent(TransformComponent.class).position;
            xSum += position.x;
            ySum += position.y;
        }

        temp.set(xSum / selection.size, ySum / selection.size);

        return temp;
    }

    public static float calculateLeftX(Array<GameObject> selection) {
        TransformComponent firstObjectComponent = selection.get(0).getComponent(TransformComponent.class);
        float leftX = firstObjectComponent.position.x
                - getSize(selection.get(0)).x / 2;
        for (GameObject gameObject : selection) {
            TransformComponent component = gameObject.getComponent(TransformComponent.class);
            float x = component.position.x - getSize(gameObject).x / 2;
            if (x < leftX) {
                leftX = x;
            }
        }
        return leftX;
    }

    public static float calculateRightX(Array<GameObject> selection) {
        TransformComponent firstObjectComponent = selection.get(0).getComponent(TransformComponent.class);
        float leftX = firstObjectComponent.position.x
                + getSize(selection.get(0)).x / 2;
        for (GameObject gameObject : selection) {
            TransformComponent component = gameObject.getComponent(TransformComponent.class);
            float x = component.position.x + getSize(gameObject).x / 2;
            if (x > leftX) {
                leftX = x;
            }
        }
        return leftX;
    }

    public static float calculateTopY(Array<GameObject> selection) {
        TransformComponent firstObjectComponent = selection.get(0).getComponent(TransformComponent.class);
        float topY = firstObjectComponent.position.y
                + getSize(selection.get(0)).y / 2;
        for (GameObject gameObject : selection) {
            TransformComponent component = gameObject.getComponent(TransformComponent.class);
            float y = component.position.y + getSize(gameObject).y / 2;
            if (y > topY) {
                topY = y;
            }
        }
        return topY;
    }

    public static float calculateBottomY(Array<GameObject> selection) {
        TransformComponent firstObjectComponent = selection.get(0).getComponent(TransformComponent.class);
        float bottomY = firstObjectComponent.position.y
                - getSize(selection.get(0)).y / 2;
        for (GameObject gameObject : selection) {
            TransformComponent component = gameObject.getComponent(TransformComponent.class);
            float y = component.position.y - getSize(gameObject).y / 2;
            if (y < bottomY) {
                bottomY = y;
            }
        }
        return bottomY;
    }

    public static float calculateVerticalSpace(float end, float start, int size) {
        return (end - start) / (size);
    }

    public static void distributeItemsVertical(Array<GameObject> gameObjects, PositionAlign positionAlign) {
        float multiplayer = positionAlign.getMultiplayer();
        gameObjects.sort((o1, o2) -> {
            TransformComponent componentO1 = o1.getComponent(TransformComponent.class);
            TransformComponent componentO2 = o2.getComponent(TransformComponent.class);
            return Float.compare(componentO2.position.y, componentO1.position.y);
        });

        TransformComponent topComponent = gameObjects.get(0).getComponent(TransformComponent.class);
        TransformComponent bottomComponent = gameObjects.get(gameObjects.size - 1).getComponent(TransformComponent.class);

        float topY = topComponent.position.y;
        float bottomY = bottomComponent.position.y;

        float space = AligningUtils.calculateVerticalSpace(bottomY, topY, gameObjects.size - 1);


        for (int i = 0; i < gameObjects.size; i++) {
            TransformComponent component = gameObjects.get(i).getComponent(TransformComponent.class);
            float y = topY + i * space + multiplayer * getSize(gameObjects.get(i)).y;
            component.position.set(component.position.x, y);
        }
    }

    public static void distributeItemsHorizontal(Array<GameObject> gameObjects, PositionAlign positionAlign) {
        float multiplayer = positionAlign.getMultiplayer();
        gameObjects.sort((o1, o2) -> {
            TransformComponent componentO1 = o1.getComponent(TransformComponent.class);
            TransformComponent componentO2 = o2.getComponent(TransformComponent.class);
            return Float.compare(componentO2.position.x, componentO1.position.x);
        });

        TransformComponent topComponent = gameObjects.get(0).getComponent(TransformComponent.class);
        TransformComponent bottomComponent = gameObjects.get(gameObjects.size - 1).getComponent(TransformComponent.class);

        float leftX = topComponent.position.x;
        float rightX = bottomComponent.position.x;

        float space = AligningUtils.calculateVerticalSpace(rightX, leftX, gameObjects.size - 1);


        for (int i = 0; i < gameObjects.size; i++) {
            TransformComponent component = gameObjects.get(i).getComponent(TransformComponent.class);
            float x = leftX + i * space + multiplayer * getSize(gameObjects.get(i)).x;
            component.position.set(x, component.position.y);
        }
    }

    public static void updateItemsY(Array<GameObject> gameObjects, float y, PositionAlign positionAlign) {
        float multiplayer = positionAlign.getMultiplayer();
        for (GameObject gameObject : gameObjects) {
            TransformComponent component = gameObject.getComponent(TransformComponent.class);
            component.position.set(component.position.x, y + multiplayer * getSize(gameObject).y);
        }
    }

    public static void updateItemsX(Array<GameObject> gameObjects, float x, PositionAlign positionAlign) {
        float multiplayer = positionAlign.getMultiplayer();
        for (GameObject gameObject : gameObjects) {
            TransformComponent component = gameObject.getComponent(TransformComponent.class);
            component.position.set(x + multiplayer * getSize(gameObject).x, component.position.y);
        }
    }

    public static void alignHorizontalTop(Array<GameObject> gameObjects) {
        updateItemsY(gameObjects, calculateTopY(gameObjects), PositionAlign.BOTTOM);
    }

    public static void alignHorizontalBottom(Array<GameObject> gameObjects) {
        updateItemsY(gameObjects, calculateBottomY(gameObjects), PositionAlign.TOP);
    }

    public static void alignHorizontalCenter(Array<GameObject> gameObjects) {
        updateItemsY(gameObjects, calculateCenterPosition(gameObjects).y, PositionAlign.CENTER);
    }

    public static void alignVerticalLeft(Array<GameObject> gameObjects) {
        updateItemsX(gameObjects, calculateLeftX(gameObjects), PositionAlign.RIGHT);
    }

    public static void alignVerticalRight(Array<GameObject> gameObjects) {
        updateItemsX(gameObjects, calculateRightX(gameObjects), PositionAlign.LEFT);
    }

    public static void alignVerticalCenter(Array<GameObject> gameObjects) {
        updateItemsX(gameObjects, calculateCenterPosition(gameObjects).x, PositionAlign.CENTER);
    }


    public static void distributeItemsVerticalCenter(Array<GameObject> gameObjects) {
        AligningUtils.distributeItemsVertical(gameObjects, PositionAlign.CENTER);
    }

    public static void distributeItemsVerticalDown(Array<GameObject> gameObjects) {
        AligningUtils.distributeItemsVertical(gameObjects, PositionAlign.BOTTOM);
    }

    public static void distributeItemsVerticalUp(Array<GameObject> gameObjects) {
        AligningUtils.distributeItemsVertical(gameObjects, PositionAlign.TOP);
    }

    public static void distributeItemsHorizontalCenter(Array<GameObject> gameObjects) {
        AligningUtils.distributeItemsHorizontal(gameObjects, PositionAlign.CENTER);
    }

    public static void distributeItemsHorizontalRight(Array<GameObject> gameObjects) {
        AligningUtils.distributeItemsHorizontal(gameObjects, PositionAlign.RIGHT);
    }

    public static void distributeItemsHorizontalLeft(Array<GameObject> gameObjects) {
        AligningUtils.distributeItemsHorizontal(gameObjects, PositionAlign.LEFT);
    }
}
