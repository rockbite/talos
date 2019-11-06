package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;
import com.rockbite.tools.talos.runtime.utils.VectorField;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class VectorFieldModule extends Module {

    public static final int SIZE_SCALE = 0;
    public static final int FORCE_SCALE = 1;
    public static final int POSITION = 2;

    public static final int ANGLE = 0;
    public static final int VELOCITY = 1;


    NumericalValue scale;
    NumericalValue force;
    NumericalValue position;
    NumericalValue angle;
    NumericalValue velocity;

    Vector2 pos = new Vector2();
    Vector2 tmp = new Vector2();

    VectorField vectorField;

    @Override
    protected void defineSlots() {

        vectorField = new VectorField();

        scale = createInputSlot(SIZE_SCALE);
        force = createInputSlot(FORCE_SCALE);
        position = createInputSlot(POSITION);

        angle = createOutputSlot(ANGLE);
        velocity = createOutputSlot(VELOCITY);
    }

    @Override
    public void processValues() {
        float scaleVal = 1f;
        if(!scale.isEmpty()) {
            scaleVal = scale.getFloat();
        }

        float forceVal = 1f;
        if(!force.isEmpty()) {
            forceVal = force.getFloat();
        }

        NumericalValue posNumVal = getScope().get(ScopePayload.PARTICLE_POSITION);
        pos.set(posNumVal.get(0), posNumVal.get(1));

        vectorField.setScale(scaleVal);
        vectorField.setPosition(position.get(0), position.get(1));

        tmp = vectorField.getValue(pos, tmp);
        tmp.scl(forceVal);

        angle.set(tmp.angle());
        velocity.set(tmp.len());
    }

    @Override
    public void write(Json json) {
        super.write(json);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        String fgaAssetName = jsonData.getString("fgaAssetName", "");
        if(!fgaAssetName.isEmpty()) {
            AssetProvider assetProvider = graph.getEffectDescriptor().getAssetProvider();
            //assetProvider.getBinary(fgaAssetName);
        }
    }
}
