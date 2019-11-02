package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.*;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;

public class SkeletonContainer {

    private Skeleton skeleton;
    private AnimationState animationState;
    private String currAnimName;

    private Array<BoundEffect> boundEffects = new Array<>();

    private Vector2 tmp = new Vector2();

    public SkeletonContainer() {


    }

    public void setAnimation(FileHandle jsonHandle, FileHandle atlasHandle) {
        TextureAtlas atlas = new TextureAtlas(atlasHandle);
        SkeletonJson json = new SkeletonJson(atlas);

        json.setScale(1f); // should be user set
        SkeletonData skeletonData = json.readSkeletonData(jsonHandle);

        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        skeleton.setPosition(0, 0);

        if(currAnimName == null) {
            currAnimName = skeleton.getData().getAnimations().get(0).getName();
        } else {
            if(skeletonData.findAnimation(currAnimName) == null) {
                // this animation no longer exists.
                currAnimName = skeleton.getData().getAnimations().get(0).getName();
            }
        }

        AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.
        animationState = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
        animationState.setTimeScale(1f);
        // Queue animations on track 0.
        animationState.setAnimation(0, currAnimName, true);

        animationState.update(0.1f); // Update the animation time.
        animationState.apply(skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.\
        skeleton.setPosition(0, 0);
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                super.event(entry, event);
            }

            @Override
            public void start(AnimationState.TrackEntry entry) {
                super.start(entry);
            }

            @Override
            public void end(AnimationState.TrackEntry entry) {
                super.end(entry);
            }
        });
    }

    public void update(float delta, boolean isSkeletonPaused) {
        if(skeleton == null) return;

        if(!isSkeletonPaused) {
            animationState.update(delta);
            animationState.apply(skeleton);
        }

        for(BoundEffect effect: boundEffects) {
            effect.update(delta);
        }
    }


    public Skeleton getSkeleton() {
        return skeleton;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    public float getBoneRotation(String boneName) {
        Bone bone = skeleton.findBone(boneName);
        if(bone != null) {
            return bone.getRotation();
        }

        return 0;
    }

    public float getBonePosX(String boneName) {
        Bone bone = skeleton.findBone(boneName);
        if(bone != null) {
            return bone.getWorldX();
        }

        return 0;
    }

    public float getBonePosY(String boneName) {
        Bone bone = skeleton.findBone(boneName);
        if(bone != null) {
            return bone.getWorldY();
        }

        return 0;
    }

    public Array<BoundEffect> getBoundEffects() {
        return boundEffects;
    }

    public BoundEffect addEffect(ParticleEffectDescriptor descriptor) {
        BoundEffect boundEffect = new BoundEffect(this, descriptor);
        boundEffect.setForever(true);
        boundEffects.add(boundEffect);

        return boundEffect;
    }

    public Bone findClosestBone(Vector2 pos) {
        Bone closestBone = skeleton.getRootBone();
        float minDist = getBoneDistance(closestBone, pos);

        for(Bone bone: skeleton.getBones()) {
            float dist = getBoneDistance(bone, pos);
            if(minDist > dist) {
                minDist = dist;
                closestBone = bone;
            }
        }
        return closestBone;
    }

    public float getBoneDistance(Bone bone, Vector2 pos) {
        tmp.set(pos);
        return tmp.dst(bone.getWorldX(), bone.getWorldY());
    }

    public Bone getBoneByName(String boneName) {
        return skeleton.findBone(boneName);
    }
}
