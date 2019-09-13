package com.rockbite.tools.talos.runtime;

public class Value {

    public static byte FLOAT = 0;
    public static byte BOOLEAN = 1;
    public static byte STRING = 2;

    public String stringVar;
    public boolean booleanVar;
    public float[] floatVars = new float[3];
    private byte floatSize = 3;
    private byte type;
    public boolean isDefault = false;

    public Value resetAsFloat(byte size) {
        floatSize = size;
        type = FLOAT;
        stringVar = null;

        return this;
    }

    public Value resetAsString() {
        type = STRING;

        return this;
    }

    public Value resetAsBoolean() {
        type = BOOLEAN;

        return this;
    }

    public void set(Value value) {
        // also if sizes don't match in case of float then this should not be happening at all
        if(value.type == FLOAT) {
            for(int i = 0; i < floatSize; i++) {
                floatVars[i] = value.floatVars[i];
            }
        }
    }

    public void set(float floatVar) {
        if(type == FLOAT) {
            for(int i = 0; i < floatSize; i++) {
                floatVars[i] = floatVar;
            }
        }
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
