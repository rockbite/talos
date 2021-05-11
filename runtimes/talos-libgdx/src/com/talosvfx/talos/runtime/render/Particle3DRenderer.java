package com.talosvfx.talos.runtime.render;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.drawables.TextureRegionDrawable;
import com.talosvfx.talos.runtime.render.p3d.Simple3DBatch;
import com.talosvfx.talos.runtime.render.p3d.Sprite3D;
import com.talosvfx.talos.runtime.render.p3d.SpriteVertGenerator;

public class Particle3DRenderer implements ParticleRenderer, RenderableProvider {

    private ParticleEffectInstance particleEffectInstance;

    private ObjectMap<Texture, Material> materialMap = new ObjectMap<>();

    private Pool<Sprite3D> sprite3DPool;

    private Array<Sprite3D> cleanBuffer = new Array<>();
    private Simple3DBatch batch;

    private Vector3 pos = new Vector3();
    private Vector3 rot = new Vector3();


    public Particle3DRenderer () {
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

    Color tempColour = new Color();
    private void processParticle(Particle particle, float alpha) {
        ParticleDrawable drawable = particle.drawable;
        if(drawable instanceof TextureRegionDrawable) {
            TextureRegionDrawable textureRegionDrawable = (TextureRegionDrawable) drawable;
            Texture texture = textureRegionDrawable.getTextureRegion().getTexture();

            pos.set(particle.getX(), particle.getY(), 0);
            rot.set(particle.rotation.x, particle.rotation.y, particle.rotation.z); // xy, yz, zx
            tempColour.set(particle.color);
            tempColour.a = particle.transparency;
            float[] verts = SpriteVertGenerator.getSprite(pos, rot, tempColour, particle.size.x, particle.size.y);

            batch.render(verts, texture);
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

    }

    public void setBatch (Simple3DBatch batch) {
        this.batch = batch;
    }
}
