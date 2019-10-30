package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.*;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.ViewportWidget;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class BvBWorkspace extends ViewportWidget {

    private BvBAssetProvider assetProvider;
    private SkeletonContainer skeletonContainer = new SkeletonContainer();
    private SpriteBatchParticleRenderer talosRenderer;
    private SkeletonRenderer renderer;

    private boolean paused = false;
    private float speedMultiplier = 1f;

    private Array<ParticleEffectDescriptor> vfxLibrary = new Array<>();

    BvBWorkspace() {
        setModeUI();

        assetProvider = new BvBAssetProvider();

        talosRenderer = new SpriteBatchParticleRenderer(null);

        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(false); // PMA results in correct blending without outlines. (actually should be true, not sure why this ruins scene2d later, probably blend screwup, will check later)

        setCameraPos(0, 0);
        bgColor.set(0.1f, 0.1f, 0.1f, 1f);
    }

    public void setModeUI() {
        setWorldSize(1280f);
    }

    public void setModeGame() {
        setWorldSize(10f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(skeletonContainer != null) {
            skeletonContainer.update(delta);
        }
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();
        drawGrid(batch, parentAlpha);
        batch.begin();

        drawSpine(batch, parentAlpha);
        drawVFX(batch, parentAlpha);
    }

    private void drawSpine(Batch batch, float parentAlpha) {
        Skeleton skeleton = skeletonContainer.getSkeleton();
        AnimationState animationState = skeletonContainer.getAnimationState();
        if(skeleton == null) return;

        skeleton.setPosition(0, 0);
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.


        int a1 = batch.getBlendSrcFunc();
        int a2 = batch.getBlendDstFunc();
        int a3 = batch.getBlendSrcFuncAlpha();
        int a4 = batch.getBlendDstFuncAlpha();
        renderer.draw(batch, skeleton); // Draw the skeleton images.

        // fixing back the blending because PMA is shit
        batch.setBlendFunctionSeparate(a1, a2, a3, a4);
    }

    private void drawVFX(Batch batch, float parentAlpha) {
        talosRenderer.setBatch(batch);
        for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
            for(ParticleEffectInstance particleEffectInstance: effect.getParticleEffects()) {
                talosRenderer.render(particleEffectInstance);
            }
        }
    }

    public void setAnimation(FileHandle jsonFileHandle) {
        FileHandle atlasFileHandle = Gdx.files.absolute(jsonFileHandle.pathWithoutExtension() + ".atlas");
        jsonFileHandle = TalosMain.Instance().ProjectController().findFile(jsonFileHandle);
        atlasFileHandle = TalosMain.Instance().ProjectController().findFile(atlasFileHandle);

        skeletonContainer.setAnimation(jsonFileHandle, atlasFileHandle);
    }

    public void addParticleToLibrary(FileHandle handle) {
        ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
        assetProvider.setParticleFolder(handle.parent().path());
        descriptor.setAssetProvider(assetProvider);
        descriptor.load(handle);

        vfxLibrary.add(descriptor);

        //remove this
        BoundEffect effect = skeletonContainer.addEffect(descriptor);
        effect.setPositionAttachement(skeletonContainer.getSkeleton().getRootBone().toString());

    }
}
