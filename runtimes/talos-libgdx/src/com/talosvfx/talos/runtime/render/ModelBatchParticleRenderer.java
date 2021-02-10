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
import com.talosvfx.talos.runtime.ParticleDrawable;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.drawables.TextureRegionDrawable;
import com.talosvfx.talos.runtime.render.p3d.Sprite3D;

public class ModelBatchParticleRenderer implements ParticleRenderer, RenderableProvider {

    private ModelBatch modelBatch;
    private Environment environment;
    private ParticleEffectInstance particleEffectInstance;
    private PerspectiveCamera worldCamera;

    private ObjectMap<Texture, Material> materialMap = new ObjectMap<>();

    private Pool<Sprite3D> sprite3DPool;

    private Array<Sprite3D> cleanBuffer = new Array<>();


    public ModelBatchParticleRenderer() {
        // test shit


        sprite3DPool = new Pool<Sprite3D>() {
            @Override
            protected Sprite3D newObject() {
                return new Sprite3D();
            }
        };
    }

    public Material getMaterial(Texture texture) {
        if(!materialMap.containsKey(texture)) {
            Material tmpMaterial = new Material(
                    TextureAttribute.createDiffuse(texture),
                    new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE, 1f),
                    FloatAttribute.createAlphaTest(0.1f)
            );
            materialMap.put(texture, tmpMaterial);
        }

        return materialMap.get(texture);
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
        ParticleDrawable drawable = particle.drawable;
        if(drawable instanceof TextureRegionDrawable) {
            TextureRegionDrawable textureRegionDrawable = (TextureRegionDrawable) drawable;
            Texture texture = textureRegionDrawable.getTextureRegion().getTexture();


            // this is the place to create renderables for each particle
            Sprite3D tmpSprite = sprite3DPool.obtain();
            cleanBuffer.add(tmpSprite);
            tmpSprite.getSprite().setRegion(texture);
            tmpSprite.setMaterial(getMaterial(texture));
            tmpSprite.setSize(particle.size.x, particle.size.y);

            tmpSprite.setPosition(particle.getX(), particle.getY());
            modelBatch.render(tmpSprite);
        }
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
