package com.talosvfx.talos.runtime.render;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.modules.DrawableModule;
import com.talosvfx.talos.runtime.modules.MaterialModule;
import com.talosvfx.talos.runtime.modules.MeshGeneratorModule;
import com.talosvfx.talos.runtime.modules.ParticlePointDataGeneratorModule;
import com.talosvfx.talos.runtime.modules.SpriteMaterialModule;
import com.talosvfx.talos.runtime.render.p3d.Simple3DBatch;
import com.talosvfx.talos.runtime.render.p3d.Sprite3D;
import com.talosvfx.talos.runtime.values.DrawableValue;

public class Particle3DRenderer implements ParticleRenderer, RenderableProvider {

    private ParticleEffectInstance particleEffectInstance;

    private ObjectMap<Texture, Material> materialMap = new ObjectMap<>();

    private Pool<Sprite3D> sprite3DPool;

    private Array<Sprite3D> cleanBuffer = new Array<>();
    private Simple3DBatch batch;

    private Vector3 pos = new Vector3();
    private Vector3 rot = new Vector3();
    private PerspectiveCamera worldCamera;

    public Particle3DRenderer (PerspectiveCamera worldCamera) {
        this.worldCamera = worldCamera;
        sprite3DPool = new Pool<Sprite3D>() {
            @Override
            protected Sprite3D newObject() {
                return new Sprite3D();
            }
        };
    }

    @Override
    public Camera getCamera () {
        return worldCamera;
    }

    @Override
    public void render(ParticleEffectInstance particleEffectInstance) {
        this.particleEffectInstance = particleEffectInstance;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (int i = 0; i < particleEffectInstance.getEmitters().size; i++) {
            final IEmitter particleEmitter = particleEffectInstance.getEmitters().get(i);
            if(!particleEmitter.isVisible()) continue;
            if(particleEmitter.isBlendAdd()) {
                batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                if (particleEmitter.isAdditive()) {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                } else {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
            }


            MeshGeneratorModule meshGenerator = particleEmitter.getParticleModule().getMeshGenerator();
            if (meshGenerator == null) continue;
            meshGenerator.setRenderMode(true);

            DrawableModule drawableModule = particleEmitter.getDrawableModule();
            if (drawableModule == null) continue;
            if (drawableModule.getMaterialModule() == null) continue;
            ParticlePointDataGeneratorModule particlePointDataGeneratorModule = particleEmitter.getParticleModule().getPointDataGenerator();
            if (particlePointDataGeneratorModule == null) continue;

            int cachedMode = particleEmitter.getScope().getRequestMode();
            int cachedRequesterID = particleEmitter.getScope().getRequesterID();

            particleEmitter.getScope().setCurrentRequestMode(ScopePayload.SUB_PARTICLE_ALPHA);

            meshGenerator.render(this, drawableModule.getMaterialModule(), particlePointDataGeneratorModule.pointData);

            particleEmitter.getScope().setCurrentRequestMode(cachedMode);
            particleEmitter.getScope().setCurrentRequesterID(cachedRequesterID);
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


    }

    @Override
    public void render (float[] verts, MaterialModule materialModule) {
        if (materialModule instanceof SpriteMaterialModule) {
            DrawableValue drawableValue = ((SpriteMaterialModule)materialModule).getDrawableValue();
            TextureRegion textureRegion = drawableValue.getDrawable().getTextureRegion();

            batch.render(verts, textureRegion.getTexture());
        }
    }

    Color tempColour = new Color();
    private void processParticle(Particle particle, float alpha) {
//        if(drawable instanceof TextureRegionDrawable) {
//            TextureRegionDrawable textureRegionDrawable = (TextureRegionDrawable) drawable;
//            Texture texture = textureRegionDrawable.getTextureRegion().getTexture();
//
//            pos.set(particle.getX(), particle.getY(), 0);
//            rot.set(particle.rotation.x, particle.rotation.y, particle.rotation.z); // xy, yz, zx
//            tempColour.set(particle.color);
//            tempColour.a = particle.transparency;
//            float[] verts = SpriteVertGenerator.getSprite(pos, rot, tempColour, particle.size.x, particle.size.y);
//
//            batch.render(verts, texture);
//        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

    }

    public void setBatch (Simple3DBatch batch) {
        this.batch = batch;
    }
}
