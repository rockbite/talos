package com.talosvfx.talos.runtime.render.p3d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class SpriteVertGenerator {

    public static float[] data = new float[(3 + 1 + 2) * 2 * 3];

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

    static Vector3 tempPos = new Vector3();
    public static float[] getSprite(Vector3 position, Vector3 rotation, Color color, float width, float height) {

        int idx = 0;



        tempPos.set(- width/2f,  - height/2f, 0);
        tempPos.rotate(rotation.x, 0, 0, 1);
        tempPos.rotate(rotation.y, 0, 1, 0);
        tempPos.add(position);

        // triangle A
        data[idx++] = tempPos.x;
        data[idx++] = tempPos.y;
        data[idx++] = tempPos.z;
        data[idx++] = color.toFloatBits();
        data[idx++] = 0;
        data[idx++] = 0;

        tempPos.set(- width/2f, height/2f, 0);
        tempPos.rotate(rotation.x, 0, 0, 1);
        tempPos.rotate(rotation.y, 0, 1, 0);

        tempPos.add(position);

        data[idx++] = tempPos.x;
        data[idx++] = tempPos.y;
        data[idx++] = tempPos.z;
        data[idx++] = color.toFloatBits();
        data[idx++] = 0;
        data[idx++] = 1;

        tempPos.set(width/2f, - height/2f, 0);
        tempPos.rotate(rotation.x, 0, 0, 1);
        tempPos.rotate(rotation.y, 0, 1, 0);

        tempPos.add(position);


        data[idx++] = tempPos.x;
        data[idx++] = tempPos.y;
        data[idx++] = tempPos.z;
        data[idx++] = color.toFloatBits();
        data[idx++] = 1;
        data[idx++] = 0;

        // triangle B

        tempPos.set( - width/2f,  + height/2f, 0);
        tempPos.rotate(rotation.x, 0, 0, 1);
        tempPos.rotate(rotation.y, 0, 1, 0);

        tempPos.add(position);

        data[idx++] = tempPos.x;
        data[idx++] = tempPos.y;
        data[idx++] = tempPos.z;
        data[idx++] = color.toFloatBits();
        data[idx++] = 0;
        data[idx++] = 1;

        tempPos.set( + width/2f,  height/2f,0);
        tempPos.rotate(rotation.x, 0, 0, 1);
        tempPos.rotate(rotation.y, 0, 1, 0);

        tempPos.add(position);

        data[idx++] = tempPos.x;
        data[idx++] = tempPos.y;
        data[idx++] = tempPos.z;
        data[idx++] = color.toFloatBits();
        data[idx++] = 1;
        data[idx++] = 1;

        tempPos.set(width/2f,  - height/2f,0);
        tempPos.rotate(rotation.x, 0, 0, 1);
        tempPos.rotate(rotation.y, 0, 1, 0);

        tempPos.add(position);

        data[idx++] = tempPos.x;
        data[idx++] = tempPos.y;
        data[idx++] = tempPos.z;
        data[idx++] = color.toFloatBits();
        data[idx++] = 1;
        data[idx++] = 0;

        return data;
    }
}
