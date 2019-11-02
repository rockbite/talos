package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.editor.data.PropertyProviderCenter;
import com.rockbite.tools.talos.editor.wrappers.MutableProperty;
import com.rockbite.tools.talos.editor.wrappers.Property;
import com.rockbite.tools.talos.editor.wrappers.IPropertyProvider;
import com.rockbite.tools.talos.editor.wrappers.ImmutableProperty;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class BoundEffect implements IPropertyProvider {

    /**
     * parent skeleton container
     */
    SkeletonContainer parent;

    /**
     * even though this is one effect, many instances of it can be rendered at the same time
     * in cases when it starts more often then finishes
     */
    private Array<ParticleEffectInstance> particleEffects;

    /**
     * Particle effect descriptor that knows how to spawn the instances
     */
    private ParticleEffectDescriptor particleEffectDescriptor;

    /**
     * positional attachments to the bones
     */
    private Array<AttachmentPoint> valueAttachments;
    private AttachmentPoint positionAttachment;

    /**
     * Sets scope global values to all particle effect instances to use
     */
    private ObjectMap<Integer, NumericalValue> globalValues;

    /**
     * is it rendered behind animation or in front
     */
    private boolean isBehind;

    /**
     * rotation bound to certain global value index
     */
    private int rotationBindIndex = -1;
    private String rotationBindBone;

    /**
     * if true this spawns only once at remains forever no matter what
     */
    private boolean forever = false;

    /**
     * each effect hsa it's own instance of scope payload, we want this global values local to effect type
     */
    private ScopePayload scopePayload;

    /**
     * System vars
     */
    private NumericalValue tmp = new NumericalValue();


    public BoundEffect(SkeletonContainer container, ParticleEffectDescriptor descriptor) {
        parent = container;
        this.particleEffectDescriptor = descriptor;
        scopePayload = new ScopePayload();
        particleEffects = new Array<>();
        valueAttachments = new Array<>();
        globalValues = new ObjectMap<>();
    }

    public void setForever(boolean isForever) {
        if(isForever && !forever) {
            particleEffects.clear();
            ParticleEffectInstance instance = particleEffectDescriptor.createEffectInstance();
            instance.setScope(scopePayload);
            particleEffects.add(instance);
            instance.loopable = true; // this is evil
        }
        forever = isForever;
    }

    public void update(float delta) {
        // setting values
        for(Integer key: globalValues.keys()) {
            NumericalValue value = globalValues.get(key);
            if(value != null) {
                scopePayload.setDynamicValue(key, value);
            }
        }

        // apply rotation info of bone
        if(rotationBindIndex >= 0) {
            scopePayload.setDynamicValue(rotationBindIndex, parent.getBoneRotation(rotationBindBone));
        }

        // value attachments
        for(AttachmentPoint attachmentPoint: valueAttachments) {
            NumericalValue val = tmp;
            val.set(parent.getBonePosX(attachmentPoint.getBoneName()) + attachmentPoint.getOffsetX(), parent.getBonePosY(attachmentPoint.getBoneName()) + attachmentPoint.getOffsetY());
            scopePayload.setDynamicValue(attachmentPoint.getIndex(), val);
        }

        // update position for each instance and update effect itself
        for(ParticleEffectInstance instance: particleEffects) {
            if (positionAttachment != null) {
                instance.setPosition(parent.getBonePosX(positionAttachment.getBoneName()) + positionAttachment.getOffsetX(), parent.getBonePosY(positionAttachment.getBoneName()) + positionAttachment.getOffsetY());

                instance.update(delta);
            }
        }
    }

    public void setBehind(boolean isBehind) {
        this.isBehind = isBehind;
    }

    public boolean isBehind() {
        return isBehind;
    }

    public void setGlobalValue(Integer key, NumericalValue value) {
        globalValues.put(key, value);
    }

    public void removePositionAttachment() {
        positionAttachment = null;
    }

    public void setPositionAttachement(String bone) {
        positionAttachment = new AttachmentPoint();
        positionAttachment.setBoneName(bone);
    }

    public void addValueAttachment(int index, String bone) {
        AttachmentPoint point = new AttachmentPoint(index);
        point.setBoneName(bone);
        if(!valueAttachments.contains(point, false)) {
            valueAttachments.add(point);
        }
    }

    public void bindRotation(int index, String bone) {
        rotationBindIndex = index;
        rotationBindBone = bone;
    }

    public void unBindRotation() {
        rotationBindIndex = -1;
        rotationBindBone = null;
    }

    public void startInstance() {
        if(forever) return;

        ParticleEffectInstance instance = particleEffectDescriptor.createEffectInstance();
        instance.setScope(scopePayload);
        particleEffects.add(instance);
    }

    public void completeInstance() {
        if(forever) return;

        for(ParticleEffectInstance instance: particleEffects) {
            instance.allowCompletion();
        }
    }

    public Array<ParticleEffectInstance> getParticleEffects() {
        return particleEffects;
    }

    @Override
    public Array<Property> getListOfProperties () {
        Array<Property> properties = new Array<>();

        Property<String> boneName = new ImmutableProperty<>("Bone Name", positionAttachment.getBoneName());
        Property<Float> offsetX = new MutableProperty<Float>("Offset X", positionAttachment.getOffsetX()) {
            @Override
            public void changed (Float newValue) {
                positionAttachment.setOffsetX(newValue);
            }
        };
        Property<Float> offsetY = new MutableProperty<Float>("Offset Y", positionAttachment.getOffsetY()) {
            @Override
            public void changed (Float newValue) {
                positionAttachment.setOffsetY(newValue);
            }
        };
        Property<Boolean> behind = new MutableProperty<Boolean>("Is Behind", isBehind) {
            @Override
            public void changed (Boolean newValue) {
                isBehind = newValue;
            }
        };

        properties.add(boneName, offsetX, offsetY, behind);
        return properties;
    }

    @Override
    public String getTitle () {
        return "Bound Effect Properties";
    }
}
