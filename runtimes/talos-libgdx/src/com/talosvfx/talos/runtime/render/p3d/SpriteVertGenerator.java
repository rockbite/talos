package com.talosvfx.talos.runtime.render.p3d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class SpriteVertGenerator {

    public static float[] data = new float[(3 + 1) * 2 * 3];

    public static Vertex vertex = new Vertex();

    private static class Vertex {

        Vector3 data = new Vector3();

        public Vertex () {

        }

        public Vertex (float x, float y, float z) {
            data.set(x, y, z);
        }

        public void set (float x, float y, float z) {
            data.set(x, y, z);
        }
    }

    public static float[] getSprite(Vector3 position, Vector3 rotation, Color color, float width, float height) {

        int idx = 0;

        // triangle A
        data[idx++] = position.x - width/2f;
        data[idx++] = position.y - height/2f;
        data[idx++] = position.z;
        data[idx++] = color.toFloatBits();

        data[idx++] = position.x - width/2f;
        data[idx++] = position.y + height/2f;
        data[idx++] = position.z;
        data[idx++] = color.toFloatBits();

        data[idx++] = position.x + width/2f;
        data[idx++] = position.y - height/2f;
        data[idx++] = position.z;
        data[idx++] = color.toFloatBits();


        // triangle B

        data[idx++] = position.x - width/2f;
        data[idx++] = position.y + height/2f;
        data[idx++] = position.z;
        data[idx++] = color.toFloatBits();

        data[idx++] = position.x + width/2f;
        data[idx++] = position.y + height/2f;
        data[idx++] = position.z;
        data[idx++] = color.toFloatBits();

        data[idx++] = position.x + width/2f;
        data[idx++] = position.y - height/2f;
        data[idx++] = position.z;
        data[idx++] = color.toFloatBits();

        return data;
    }
}
