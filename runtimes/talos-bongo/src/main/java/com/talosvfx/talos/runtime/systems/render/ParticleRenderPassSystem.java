package com.talosvfx.talos.runtime.systems.render;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.rockbite.bongo.engine.components.singletons.Cameras;
import com.rockbite.bongo.engine.gltf.scene.shader.DefaultSceneShaderProvider;
import com.rockbite.bongo.engine.gltf.scene.shader.SceneShaderProvider;
import com.rockbite.bongo.engine.gltf.scene.shader.bundled.ShadedShader;
import com.rockbite.bongo.engine.render.AutoReloadingShaderProgram;
import com.rockbite.bongo.engine.render.ShaderSourceProvider;
import com.rockbite.bongo.engine.systems.RenderPassSystem;
import com.talosvfx.talos.runtime.vfx.IEmitter;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.ParticlePointData;
import com.talosvfx.talos.runtime.vfx.ParticlePointGroup;
import com.talosvfx.talos.runtime.vfx.ScopePayload;
import com.talosvfx.talos.runtime.components.Particle;
import com.talosvfx.talos.runtime.vfx.modules.*;
import com.talosvfx.talos.runtime.vfx.render.ParticleRenderer;
import com.talosvfx.talos.runtime.vfx.render.p3d.Simple3DBatch;

public class ParticleRenderPassSystem extends RenderPassSystem implements ParticleRenderer {

    //SINGLETONS
    private Cameras cameras;
    private Simple3DBatch simple3DBatch;


    //MAPPERS
    private ComponentMapper<Particle> particleMapper;
    private AutoReloadingShaderProgram shaderProgram;

    private ShaderProgram override;

    private Vector3 tempVec3 = new Vector3();

    public ParticleRenderPassSystem () {
        this(Particle.class);
    }


    public ParticleRenderPassSystem (Class<? extends Component> componentClazz) {
        this(
                new DefaultSceneShaderProvider(ShaderSourceProvider.resolveVertex("core/particle", Files.FileType.Classpath), ShaderSourceProvider.resolveFragment("core/particle", Files.FileType.Classpath), ShadedShader.class),
                componentClazz
        );

        simple3DBatch = new Simple3DBatch(4000, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.TexCoords(0)));
        shaderProgram = new AutoReloadingShaderProgram(ShaderSourceProvider.resolveVertex("core/particle"), ShaderSourceProvider.resolveFragment("core/particle"));

    }

    public ParticleRenderPassSystem (SceneShaderProvider sceneShaderProvider, Class<? extends Component>... componentsToGather) {
        super(sceneShaderProvider, componentsToGather);
    }

    private void setShader (ShaderProgram shader) {
        this.override = shader;
        simple3DBatch.end();

        simple3DBatch.begin(cameras.getGameCamera(), override);
    }

    @Override
    protected void initialize () {
        super.initialize();
    }

    @Override
    protected void createSubscriptions () {
        createSubscriptions(Particle.class);
    }


    @Override
    protected void collectRendables () {
        //Override with custom renderable

    }

    private float delta;
    /**
     * Process the system.
     */
    @Override
    protected void processSystem () {
        delta += Gdx.graphics.getDeltaTime();

        RenderContext renderContext = renderUtils.getRenderContext();
        renderContext.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);

        simple3DBatch.begin(cameras.getGameCamera(), shaderProgram.getShaderProgram());

        float deltaTime = Math.min(1 / 30f, Gdx.graphics.getDeltaTime());

        IntBag entities = renderObjectsSubscription.getEntities();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            int entityID = entities.get(i);

            Particle particle = particleMapper.get(entityID);


            ParticleEffectInstance particleEffectInstance = particle.getParticleEffectInstance();
            if (particleEffectInstance == null) continue;

            particle.getTransform().getTranslation(tempVec3);

            particleEffectInstance.setPosition(tempVec3.x, tempVec3.y, tempVec3.z);
            particleEffectInstance.update(deltaTime);


            particleEffectInstance.render(this);
        }

        simple3DBatch.end();

        renderContext.begin();
    }

    @Override
    public Camera getCamera () {
        return cameras.getGameCamera();
    }

    @Override
    public void render (ParticleEffectInstance particleEffectInstance) {


        simple3DBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (int i = 0; i < particleEffectInstance.getEmitters().size; i++) {
            final IEmitter particleEmitter = particleEffectInstance.getEmitters().get(i);
            if (!particleEmitter.isVisible()) continue;
            if (particleEmitter.isBlendAdd()) {
                simple3DBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                if (particleEmitter.isAdditive()) {
                    simple3DBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                } else {
                    simple3DBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
            }

            if (particleEmitter.getEmitterModule() == null) continue;

            final DrawableModule drawableModule = particleEmitter.getDrawableModule();
            if (drawableModule == null) continue;

            MeshGeneratorModule meshGenerator = drawableModule.getMeshGenerator();
            if (meshGenerator == null) continue;
            meshGenerator.setRenderMode(true);

            if (drawableModule == null) continue;
            if (drawableModule.getMaterialModule() == null) continue;
            ParticlePointDataGeneratorModule particlePointDataGeneratorModule = drawableModule.getPointDataGenerator();
            if (particlePointDataGeneratorModule == null) continue;

            int cachedMode = particleEmitter.getScope().getRequestMode();
            int cachedRequesterID = particleEmitter.getScope().getRequesterID();

            particleEmitter.getScope().setCurrentRequestMode(ScopePayload.SUB_PARTICLE_ALPHA);

            meshGenerator.render(this, drawableModule.getMaterialModule(), particleEmitter.pointData());


//            simple3DBatch.end();
//			ShapeRenderer shapeRenderer = new ShapeRenderer();
//			shapeRenderer.setProjectionMatrix(cameras.getGameCamera().combined);
//			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//			Array<ParticlePointGroup> pointData = particleEmitter.pointData();
//			for (ParticlePointGroup group : pointData) {
//				shapeRenderer.setColor(1f, 0, 0, 1f);
//				for (ParticlePointData particlePointData : group.pointDataArray) {
//                    shapeRenderer.setColor(Color.RED);
//					shapeRenderer.circle(particlePointData.x, particlePointData.y, 0.15f, 20);
//                }
//			}
//			shapeRenderer.end();
//

            particleEmitter.getScope().setCurrentRequestMode(cachedMode);
            particleEmitter.getScope().setCurrentRequesterID(cachedRequesterID);

//            simple3DBatch.begin(cameras.getGameCamera(), shaderProgram.getShaderProgram());
        }

        simple3DBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void render (float[] verts, MaterialModule materialModule) {
        if (materialModule instanceof SpriteMaterialModule) {
            TextureRegion textureRegion = ((SpriteMaterialModule) materialModule).getTextureRegion();


            simple3DBatch.render(verts, textureRegion.getTexture());
        }

    }

    @Override
    public void render (float[] verts, int vertCount, short[] tris, int triCount, MaterialModule materialModule) {
        if (materialModule instanceof SpriteMaterialModule) {
            TextureRegion textureRegion = ((SpriteMaterialModule) materialModule).getTextureRegion();

            simple3DBatch.render(verts, vertCount, tris, triCount, textureRegion.getTexture());
        } else if (materialModule instanceof ShaderMaterialModule) {
            ShaderMaterialModule shaderMaterialModule = (ShaderMaterialModule) materialModule;


            ShaderProgram shader = shaderMaterialModule.getShaderInstance().shaderProgram;
            if (shader != null) {
                simple3DBatch.end();

                simple3DBatch.begin(cameras.getGameCamera(), shader);
                shader.setUniformf("u_time", delta);

                //tood proper bind

                simple3DBatch.render(verts, vertCount, tris, triCount, null);
                simple3DBatch.end();

                simple3DBatch.begin(cameras.getGameCamera(), shaderProgram.getShaderProgram());
            }
        }
    }
}
