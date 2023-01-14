package com.talosvfx.talos.runtime.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class VectorField {

    public int xSize;
    public int ySize;
    public int zSize;

    Vector3[][][] field;

    float scale = 1f;

    Vector2 fieldPos = new Vector2();

    public VectorField() {

    }

    public VectorField (FileHandle handle) {
        setBakedData(handle);
    }

    public void setBakedData(FileHandle fileHandle) {
        if(!fileHandle.extension().equals("fga")) {
            // throw exception and return
            return;
        }

        String content = fileHandle.readString();
        String[] arr = content.split(",");
        xSize = Integer.parseInt(arr[0]);
        ySize = Integer.parseInt(arr[1]);
        zSize = Integer.parseInt(arr[2]);

        field = new Vector3[xSize][ySize][zSize];

        int index = 3;

        for(int i = 0; i < xSize; i++) {
            for(int j = 0; j < ySize; j++) {
                for(int k = 0; k < zSize; k++) {
                    field[i][j][k] = new Vector3(readParam(arr, index++), readParam(arr, index++), readParam(arr, index++));
                }
            }
        }
    }

    private float readParam(String[] arr, int index) {
        return Float.parseFloat(arr[index]);
    }

    public Vector2 getValue(Vector2 pos, Vector2 result) {
        float x = (((pos.x - fieldPos.x) / scale) * 0.5f + 0.5f) * xSize;
        float y = (((pos.y - fieldPos.y) / scale) * 0.5f + 0.5f) * ySize;
        int z = 0;

        if(MathUtils.floor(x) < 0 || MathUtils.ceil(x) > xSize - 1 || MathUtils.floor(y) < 0 || MathUtils.ceil(y) > ySize - 1) {
            result.set(0, 0);
            return result;
        }

        if(MathUtils.floor(x) > xSize - 1 || MathUtils.ceil(x) < 0 || MathUtils.floor(y) > ySize - 1 || MathUtils.ceil(y) < 0) {
            result.set(0, 0);
            return result;
        }

        Vector3 v11 = field[MathUtils.floor(x)][MathUtils.floor(y)][z];
        Vector3 v12 = field[MathUtils.floor(x)][MathUtils.ceil(y)][z];
        Vector3 v22 = field[MathUtils.ceil(x)][MathUtils.ceil(y)][z];
        Vector3 v21 = field[MathUtils.ceil(x)][MathUtils.floor(y)][z];

        float resX = blerp(v11.x, v12.x, v22.x, v21.x, x-(int)x, y-(int)y);
        float resY = blerp(v11.y, v12.y, v22.y, v21.y, x-(int)x, y-(int)y);

        result.set(resX*1f, resY*1f);

        return result;
    }

    private static float lerp(float s, float e, float t) {
        return s + (e - s) * t;
    }

    private static float blerp(final Float c00, float c01, float c11, float c10, float tx, float ty) {
        return lerp(lerp(c00, c10, tx), lerp(c01, c11, tx), ty);
    }

    public void setScale(float scaleVal) {
        scale = scaleVal;
    }

    public void setPosition(float x, float y) {
        fieldPos.set(x, y);
    }
}
