package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.*;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.OptionDialogListener;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;


public class SkeletonContainer implements Json.Serializable, IPropertyProvider {

    private final BvBWorkspace workspace;

    private Skeleton skeleton;
    private AnimationState animationState;

    private Animation currentAnimation;
    private Skin currentSkin;

    private ObjectMap<String, ObjectMap<String, Array<BoundEffect>>> boundEffects = new ObjectMap<>();

    private Vector2 tmp = new Vector2();

    private Vector2 scale = new Vector2(1f, 1f);

    private Array<Event> events = new Array<>();

    private Comparator<String> alphabeticalComparator;

    public SkeletonContainer(BvBWorkspace workspace) {
        this.workspace = workspace;

        alphabeticalComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public boolean setSkeleton(FileHandle jsonFileHandle) {
        FileHandle atlasFileHandle = Gdx.files.absolute(jsonFileHandle.pathWithoutExtension() + ".atlas");
        jsonFileHandle = TalosMain.Instance().ProjectController().findFile(jsonFileHandle);
        atlasFileHandle = TalosMain.Instance().ProjectController().findFile(atlasFileHandle);

        boolean success = setSkeleton(jsonFileHandle, atlasFileHandle);

        TalosMain.Instance().ProjectController().setDirty();

        return success;
    }

    public boolean setSkeleton(FileHandle jsonHandle, FileHandle atlasHandle) {
        SkeletonJson json;
        if(atlasHandle == null) {
            json = new SkeletonJson(new EmptyAttachmentLoader() {

            });
        } else {
            TextureAtlas atlas = new TextureAtlas(atlasHandle);
            json = new SkeletonJson(new SkeletonAttachmentLoader(atlas));
        }

        json.setScale(workspace.spineScaleWidget.getValue());
        final SkeletonData skeletonData = json.readSkeletonData(jsonHandle);

        // before moving forward let's check if things are missing
        boolean hasGoners = checkForGoners(skeletonData);

        if (!hasGoners) {
            configureSkeleton(skeletonData);
        }

        return !hasGoners;
    }

    public boolean checkForGoners(final SkeletonData skeletonData) {
        final ObjectSet<String> skinsToRemove = new ObjectSet<>();
        final ObjectSet<String> animationsToRemove = new ObjectSet<>();
        final Array<BoundEffect> effectsToRemove = new Array<>();

        for(String skinName: boundEffects.keys()) {
            if(skeletonData.findSkin(skinName) == null) {
                skinsToRemove.add(skinName);
            }
            for(String animationName: boundEffects.get(skinName).keys()) {
                if(skeletonData.findAnimation(animationName) == null) {
                    animationsToRemove.add(animationName);
                }
                for(BoundEffect effect: boundEffects.get(skinName).get(animationName)) {
                    if(!effect.getPositionAttachment().isStatic() && skeletonData.findBone(effect.getPositionAttachment().getBoneName()) == null) {
                        effectsToRemove.add(effect);
                    }
                    for(AttachmentPoint point: effect.getAttachments()) {
                        if(!point.isStatic() && skeletonData.findBone(point.getBoneName()) == null) {
                            if(!effectsToRemove.contains(effect, true)) {
                                effectsToRemove.add(effect);
                            }
                        }
                    }
                }
            }
        }


        if(skinsToRemove.size > 0 || animationsToRemove.size > 0 || effectsToRemove.size > 0) {
            String description = "Skeleton data has been reloaded with some breaking changes (missing skins, animations or bones). \n This will result in removal of ";
            description += skinsToRemove.size + " skins, ";
            description += animationsToRemove.size + " animations, ";
            description += effectsToRemove.size + " effects.";
            description += "\n Are you sure you want to update this skeleton?";

            Dialogs.showOptionDialog(TalosMain.Instance().UIStage().getStage(), "Skeleton Reload Issue", description, Dialogs.OptionDialogType.YES_NO, new OptionDialogListener() {
                @Override
                public void yes() {
                    for(String skinName: skinsToRemove) {
                        boundEffects.remove(skinName);
                    }

                    for(String skinName: boundEffects.keys()) {
                        for(String animName: animationsToRemove) {
                            boundEffects.get(skinName).remove(animName);
                        }
                    }

                    for(BoundEffect effect: effectsToRemove) {
                        if(workspace.selectedEffect == effect) {
                            workspace.effectUnselected(effect);
                        }
                    }

                    for(String skinName: boundEffects.keys()) {
                        for(String animationName: boundEffects.get(skinName).keys()) {
                            boundEffects.get(skinName).get(animationName).removeAll(effectsToRemove, true);
                        }
                    }

                    configureSkeleton(skeletonData);
                    workspace.bvb.properties.showPanel(SkeletonContainer.this);

                    TalosMain.Instance().ProjectController().setDirty();
                }

                @Override
                public void no() {
                    TalosMain.Instance().ProjectController().closeCurrentTab();
                }

                @Override
                public void cancel() {
                    TalosMain.Instance().ProjectController().closeCurrentTab();
                }
            });

            return true;
        }

        return false;

    }

    public void configureSkeleton(SkeletonData skeletonData) {
        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        skeleton.setPosition(0, 0);

        skeleton.setScale(scale.x, scale.y);

        currentAnimation = skeleton.getData().getAnimations().get(0);
        currentSkin = skeleton.getData().getSkins().first();
        if(currentSkin.getName().equals("default") && skeleton.getData().getSkins().size > 1) {
            currentSkin = skeleton.getData().getSkins().get(1); // never load the default
        }
        skeleton.setSkin(currentSkin);
        skeleton.setSlotsToSetupPose();

        AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.
        animationState = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
        animationState.setTimeScale(1f);
        // Queue animations on track 0.
        animationState.setAnimation(0, currentAnimation, true);

        animationState.update(0.1f); // Update the animation time.
        animationState.apply(skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.\
        skeleton.setPosition(0, 0);
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        events.clear();
        for (Animation.Timeline timeline: currentAnimation.getTimelines()) {
            if(timeline instanceof Animation.EventTimeline) {
                Animation.EventTimeline eventTimeline = (Animation.EventTimeline) timeline;
                for (Event event: eventTimeline.getEvents()) {
                    events.add(event);
                }
            }
        }

        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                super.event(entry, event);

                getWorkspace().flyLabel(event.getData().getName());

                for(BoundEffect boundEffect: getBoundEffects()) {
                    String startEvent = boundEffect.getStartEvent();
                    String completeEvent = boundEffect.getCompleteEvent();
                    if(startEvent.equals(event.getData().getName())) {
                        boundEffect.startInstance(); // TODO: comment this things when doing it with timers
                    }
                    if(completeEvent.equals(event.getData().getName())) {
                       boundEffect.completeInstance();
                    }
                }
            }

            @Override
            public void start(AnimationState.TrackEntry entry) {

                super.start(entry);
            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
                /**
                 * A loop has been done, so stopping and starting
                 */
                for(BoundEffect boundEffect: getBoundEffects()) {
                    String completeEventName = boundEffect.getCompleteEvent();
                    if(completeEventName.equals("")) {
                        boundEffect.completeInstance();
                    }
                    String startEventName = boundEffect.getStartEvent();
                    if(startEventName.equals("")) {
                        boundEffect.startInstance();
                    }
                }

                super.complete(entry);
            }
        });

        effectScopeUpdated();
    }

    public void update(float delta, boolean isSkeletonPaused) {
        if(skeleton == null) return;

        if(!isSkeletonPaused) {
            animationState.update(delta);
            animationState.apply(skeleton);
        }

        for(BoundEffect effect: getBoundEffects()) {
            float animTime = getAnimationState().getTracks().first().getTrackTime();
            float duration = getCurrentAnimation().getDuration();
            float innerTime = animTime % duration;

            if(!effect.isContinuous()) {
                if (innerTime - delta < effect.getTimePosition() && innerTime >= effect.getTimePosition()) {
                    // time to start
                    //effect.startInstance(); // TODO: do this later when all works
                }
            }

            effect.update(delta);
        }
    }


    public Skeleton getSkeleton() {
        return skeleton;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    public Animation getCurrentAnimation() {return currentAnimation; }

    public float getBoneRotation(String boneName) {
        Bone bone = skeleton.findBone(boneName);
        if(bone != null) {
            return bone.getWorldRotationX();
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

    public Array<BoundEffect> getBoundEffects(String skinName, String animationName) {
        if(boundEffects.get(skinName) == null) {
            boundEffects.put(skinName, new ObjectMap<String, Array<BoundEffect>>());
        }
        ObjectMap<String, Array<BoundEffect>> animations = boundEffects.get(skinName);
        if(animations.get(animationName) == null) {
            animations.put(animationName, new Array<BoundEffect>());
        }

        return animations.get(animationName);
    }

    public Array<BoundEffect> getBoundEffects() {
        if(boundEffects.get(currentSkin.getName()) == null) {
            boundEffects.put(currentSkin.getName(), new ObjectMap<String, Array<BoundEffect>>());
        }
        ObjectMap<String, Array<BoundEffect>> animations = boundEffects.get(currentSkin.getName());
        if(animations.get(currentAnimation.getName()) == null) {
            animations.put(currentAnimation.getName(), new Array<BoundEffect>());
        }

        return animations.get(currentAnimation.getName());
    }

    public BoundEffect addEffect(String skinName, String animationName, BoundEffect effect) {
        getBoundEffects(skinName, animationName).add(effect);

        effect.setDrawOrder(getBoundEffects().size-1); // todo this is not going to work well and is not tested

        return effect;
    }

    public BoundEffect addEffect(String name, ParticleEffectDescriptor descriptor) {
        BoundEffect boundEffect = new BoundEffect(name, descriptor, this);
        //boundEffect.setForever(true);

        getBoundEffects().add(boundEffect);
        boundEffect.setDrawOrder(getBoundEffects().size-1); // todo this is not going to work well and is not tested

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

    public BoundEffect updateEffect(String name, ParticleEffectDescriptor descriptor) {
        for(ObjectMap<String, Array<BoundEffect>> skins: boundEffects.values()) {
            for(Array<BoundEffect> animations: skins.values()) {
                for(BoundEffect effect: animations) {
                    if(effect.name.equals(name)) {
                        // found it
                        effect.updateEffect(descriptor);
                        return effect;
                    }
                }
            }
        }

        return null;
    }



    public Array<String> getUsedParticleEffectNames() {
        Array<String> result = new Array<>();

        if(skeleton == null) return result;

        for(String skinName: boundEffects.keys()) {
            for(String animationName: boundEffects.get(skinName).keys()) {
                for(BoundEffect effect: boundEffects.get(skinName).get(animationName)) {
                   result.add(effect.name);
                }
            }
        }

        return result;
    }

    public void writeExport(Json json) {
        if(skeleton == null) return;

        json.writeValue("skeletonName", skeleton.getData().getName());

        json.writeArrayStart("boundEffects");
        for(String skinName: boundEffects.keys()) {
            for(String animationName: boundEffects.get(skinName).keys()) {
                for(BoundEffect effect: boundEffects.get(skinName).get(animationName)) {
                    json.writeObjectStart();
                    json.writeValue("skin", skinName);
                    json.writeValue("animation", animationName);
                    json.writeValue("data", effect);
                    json.writeObjectEnd();
                }
            }
        }
        json.writeArrayEnd();

        return;
    }

    @Override
    public void write(Json json) {
        if(skeleton == null) return;

        json.writeValue("skeletonName", skeleton.getData().getName());
        json.writeArrayStart("boundEffects");
        for(String skinName: boundEffects.keys()) {
            for(String animationName: boundEffects.get(skinName).keys()) {
                for(BoundEffect effect: boundEffects.get(skinName).get(animationName)) {
                    json.writeObjectStart();
                    json.writeValue("skin", skinName);
                    json.writeValue("animation", animationName);
                    json.writeValue("data", effect);
                    json.writeObjectEnd();
                }
            }
        }
        json.writeArrayEnd();
        json.writeValue("currSkin", currentSkin.getName());
        json.writeValue("currAnimation", currentAnimation.getName());
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        String skeletonName = jsonData.getString("skeletonName", "");
        if(skeletonName.isEmpty()) return;

        String skeletonPath = workspace.getPath(skeletonName + ".json");
        FileHandle jsonHandle = TalosMain.Instance().ProjectController().findFile(skeletonPath);

        // track this file
        TalosMain.Instance().FileTracker().trackFile(jsonHandle, workspace.bvb.spineTracker);


        boundEffects.clear();
        // now let's load bound effects
        JsonValue boundEffects = jsonData.get("boundEffects");
        for(JsonValue boundEffect: boundEffects) {
            String skin = boundEffect.getString("skin");
            String animation = boundEffect.getString("animation");
            JsonValue data = boundEffect.get("data");
            BoundEffect effect = new BoundEffect();
            effect.setParent(this);
            try {
                effect.read(json, data);
                addEffect(skin, animation, effect);
            } catch (Exception e) {
                // can't add this effect as it failed to load
            }
        }
        boolean wasSet = setSkeleton(jsonHandle);

        if (wasSet) {
            String currentSkinName = jsonData.getString("currSkin", currentSkin.getName());
            String currentAnimationName = jsonData.getString("currAnimation", currentAnimation.getName());

            currentSkin = skeleton.getData().findSkin(currentSkinName);
            if(currentSkin == null) {
                currentSkin = skeleton.getData().getDefaultSkin();
            }
            skeleton.setSkin(currentSkin);
            skeleton.setSlotsToSetupPose();
            currentAnimation = skeleton.getData().findAnimation(currentAnimationName);
            animationState.setAnimation(0, currentAnimation, true);

            effectScopeUpdated();
        }
    }

    public void clear() {
        skeleton = null;
        boundEffects.clear();
    }

    public BvBWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties() {

        Array<PropertyWidget> properties = new Array<>();

        LabelWidget skeletonName = new LabelWidget("skeleton name", new Supplier<String>() {
            @Override
            public String get() {
                if(skeleton != null) {
                    return skeleton.getData().getName();
                }
                return "N/A";
            }
        });

        SelectBoxWidget currentSkinWidget = new SelectBoxWidget("skin", new Supplier<String>() {
            @Override
            public String get() {
                if(currentSkin != null) {
                    return currentSkin.getName();
                }
                return "N/A";
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                currentSkin = skeleton.getData().findSkin(value);
                skeleton.setSkin(currentSkin);
                skeleton.setSlotsToSetupPose();
                effectScopeUpdated();
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                if(skeleton != null) {
                    Array<String> result = new Array<>();
                    for(Skin skin : skeleton.getData().getSkins()) {
                        if(skin.getName().equals("default")) {
                            continue;
                        }
                        result.add(skin.getName());
                    }
                    return result;
                }
                return null;
            }
        });

        SelectBoxWidget currentAnimationWidget = new SelectBoxWidget("animation", new Supplier<String>() {
            @Override
            public String get() {
                if(currentAnimation != null) {
                    return currentAnimation.getName();
                }
                return "N/A";
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                currentAnimation = skeleton.getData().findAnimation(value);
                animationState.setAnimation(0, currentAnimation, true);

                workspace.effectUnselected(workspace.selectedEffect);

                effectScopeUpdated();
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                if(skeleton != null) {
                    Array<String> result = new Array<>();
                    for(Animation animation : skeleton.getData().getAnimations()) {
                        result.add(animation.getName());
                    }

                    result.sort(alphabeticalComparator);

                    return result;
                }
                return null;
            }
        });

        properties.add(skeletonName);
        properties.add(currentSkinWidget);
        properties.add(currentAnimationWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle() {
        return "Skeleton Properties";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return this.getClass();
    }

    public BoundEffect getEffectByName(String selectedEffect) {
        if(selectedEffect == null) return null;
        for(BoundEffect effect: getBoundEffects()) {
            if(effect.name.equals(selectedEffect)) {
                return effect;
            }
        }

        return null;
    }

    public void removeEffect(BoundEffect effect) {
        getBoundEffects().removeValue(effect, true);
    }

    public void setScale(float x, float y) {
        scale.set(x, y);
        if(skeleton != null) {
            skeleton.setScale(x, y);
        }
    }

    private void effectScopeUpdated() {
        workspace.effectScopeUpdated();
    }

    public Array<Event> getEvents() {
        return events;
    }

    public BoundEffect findEffect (Skeleton skeleton, Slot slot) {
        final String boneName = slot.getBone().getData().getName();

        final Skin skin = skeleton.getSkin();
        final ObjectMap<String, Array<BoundEffect>> entries = boundEffects.get(skin.getName());

        for (Array<BoundEffect> value : entries.values()) {
            for (int i = 0; i < value.size; i++) {
                final BoundEffect boundEffect = value.get(i);
                if (boundEffect.getPositionAttachment().getBoneName().equals(boneName)) {
                    return boundEffect;
                }
            }
        }
        return null;
    }
}
