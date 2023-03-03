package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.vfx.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.vfx.ScopePayload;
import com.talosvfx.talos.runtime.vfx.utils.VectorField;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VectorFieldModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(VectorFieldModule.class);

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

    public String fgaFileName;
    VectorField vectorField;

    @Override
    protected void defineSlots() {
        scale = createInputSlot(SIZE_SCALE);
        force = createInputSlot(FORCE_SCALE);
        position = createInputSlot(POSITION);

        angle = createOutputSlot(ANGLE);
        velocity = createOutputSlot(VELOCITY);
    }

    @Override
    public void processCustomValues () {
        if(vectorField == null) return;
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
    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        super.setModuleGraph(graph);

        logger.error("Not supported, fga needs to be made into an asset");
//        final AssetProvider assetProvider = graph.getEffectDescriptor().getAssetProvider();
//        if(fgaFileName != null && !fgaFileName.isEmpty()) {
//            setVectorField(assetProvider.findAsset(fgaFileName, VectorField.class), fgaFileName);
//        }
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("fgaAssetName", fgaFileName);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        fgaFileName = jsonData.getString("fgaAssetName", "");
    }

    public void setVectorField(VectorField vectorField, String fileName) {
        this.vectorField = vectorField;
        fgaFileName = fileName;
    }

    public VectorField getVectorField() {
        return vectorField;
    }
}
