package com.talosvfx.talos.runtime.render;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.p3d.Sprite3D;

public class ModelBatchParticleRenderer implements ParticleRenderer, RenderableProvider {

    private ModelBatch modelBatch;
    private Environment environment;
    private ParticleEffectInstance particleEffectInstance;
    private PerspectiveCamera worldCamera;

    private ObjectMap<String, Material> materialMap = new ObjectMap<>();

    private Pool<Sprite3D> sprite3DPool;
    private Material tmpMaterial;

    private Array<Sprite3D> cleanBuffer = new Array<>();


    public ModelBatchParticleRenderer() {
        // test shit
        tmpMaterial = new Material(
                TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("white.png"))),
                new BlendingAttribute(false, 1f),
                FloatAttribute.createAlphaTest(0.1f)
        );

        sprite3DPool = new Pool<Sprite3D>() {
            @Override
            protected Sprite3D newObject() {
                return new Sprite3D();
            }
        };
    }


    @Override
    public void render(ParticleEffectInstance particleEffectInstance) {
        this.particleEffectInstance = particleEffectInstance;

        for (int i = 0; i < particleEffectInstance.getEmitters().size; i++) {
            final IEmitter particleEmitter = particleEffectInstance.getEmitters().get(i);
            if(!particleEmitter.isVisible()) continue;

            for (int j = 0; j < particleEmitter.getActiveParticleCount(); j++) {
                processParticle(particleEmitter.getActiveParticles().get(j), particleEffectInstance.alpha);
            }
        }
        sprite3DPool.freeAll(cleanBuffer);
        cleanBuffer.clear();
    }

    private void processParticle(Particle particle, float alpha) {
        // this is the place to create renderables for each particle
        Sprite3D tmpSprite = sprite3DPool.obtain();
        cleanBuffer.add(tmpSprite);
        tmpSprite.setSize(3, 3);
        tmpSprite.setMaterial(tmpMaterial);
        tmpSprite.setPosition(particle.getX(), particle.getY());
        modelBatch.render(tmpSprite);
    }

    public void setBatch(ModelBatch batch) {
        modelBatch = batch;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

    }

    public void setWorld(PerspectiveCamera worldCamera, Environment environment) {
        this.worldCamera = worldCamera;
        this.environment = environment;
    }
}
