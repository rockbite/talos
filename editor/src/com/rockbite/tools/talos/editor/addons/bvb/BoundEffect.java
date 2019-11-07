package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.EventData;
import com.esotericsoftware.spine.Slot;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class BoundEffect implements Json.Serializable, IPropertyProvider  {

    /**
     * parent skeleton container
     */
    SkeletonContainer parent;

    /**
     * name of this effect
     */
    String name;

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
     * is it rendered behind animation or in front
     */
    private boolean isBehind;

    /**
     * if true this spawns only once at remains forever no matter what
     */
    private boolean forever = false;

    /**
     * each effect hsa it's own instance of scope payload, we want this global values local to effect type
     */
    private ScopePayload scopePayload;

    /**
     * Start and complete event names, if empty then in sync with skeleton's animation loop
     */
    private String startEvent = "";

    private String completeEvent = "";

    /**
     * System vars
     */
    Vector2 tmpVec = new Vector2();
    NumericalValue val = new NumericalValue();

    public BoundEffect() {
        scopePayload = new ScopePayload();
        particleEffects = new Array<>();
        valueAttachments = new Array<>();
    }

    public BoundEffect(String name, ParticleEffectDescriptor descriptor, SkeletonContainer container) {
        parent = container;
        this.name = name;
        this.particleEffectDescriptor = descriptor;
        scopePayload = new ScopePayload();
        particleEffects = new Array<>();
        valueAttachments = new Array<>();
    }

    public void setForever(boolean isForever) {
        if(isForever && !forever) {
            particleEffects.clear();
            ParticleEffectInstance instance = spawnEffect();
            instance.loopable = true; // this is evil
        }
        forever = isForever;
    }

    private ParticleEffectInstance spawnEffect() {
        ParticleEffectInstance instance = particleEffectDescriptor.createEffectInstance();
        instance.setScope(scopePayload);
        particleEffects.add(instance);

        return instance;
    }

    public void update(float delta) {
        // value attachments
        for(AttachmentPoint attachmentPoint: valueAttachments) {
            if(attachmentPoint.isStatic()) {
                scopePayload.setDynamicValue(attachmentPoint.getSlotId(), attachmentPoint.getStaticValue());
            } else {
                Bone bone = parent.getBoneByName(attachmentPoint.getBoneName());
                float rotation = bone.getWorldRotationX();
                Color color = Color.WHITE;
                for(Slot slot: parent.getSkeleton().getSlots()) {
                    if(slot.getBone().getData().getName().equals(bone.getData().getName())) {
                        //can be many
                        color = slot.getColor();
                        break;
                    }
                }
                tmpVec.set(parent.getBonePosX(attachmentPoint.getBoneName()), parent.getBonePosY(attachmentPoint.getBoneName()));
                tmpVec.add(attachmentPoint.getOffsetX(), attachmentPoint.getOffsetY());

                if (attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.POSITION) {
                    val.set(tmpVec.x, tmpVec.y);
                } else if (attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.ROTATION) {
                    val.set(rotation);
                } else if(attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.TRANSPARENCY) {
                    val.set(color.a);
                } else if(attachmentPoint.getAttachmentType() == AttachmentPoint.AttachmentType.COLOR) {
                    val.set(color.r, color.g, color.b);
                }

                scopePayload.setDynamicValue(attachmentPoint.getSlotId(), val);
            }
        }

        // update position for each instance and update effect itself
        for(ParticleEffectInstance instance: particleEffects) {
            if (positionAttachment != null) {
                if(positionAttachment.isStatic()) {
                    instance.setPosition(positionAttachment.getStaticValue().get(0), positionAttachment.getStaticValue().get(1));
                } else {
                    instance.setPosition(parent.getBonePosX(positionAttachment.getBoneName()) + positionAttachment.getOffsetX(), parent.getBonePosY(positionAttachment.getBoneName()) + positionAttachment.getOffsetY());
                }

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

    public void removePositionAttachment() {
        positionAttachment = null;
    }

    public void setPositionAttachment(String bone) {
        positionAttachment = new AttachmentPoint();
        positionAttachment.setTypeAttached(bone, -1);
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
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        LabelWidget effectName = new LabelWidget("effect name") {
            @Override
            public String getValue() {
               return name;
            }
        };

        CheckboxWidget behind = new CheckboxWidget("is behind") {
            @Override
            public Boolean getValue() {
                return isBehind;
            }

            @Override
            public void valueChanged(Boolean value) {
                isBehind = value;
            }
        };

        SelectBoxWidget startEventWidget = new SelectBoxWidget("Start Emitting") {
            @Override
            public Array<String> getOptionsList() {
                return getEvents();
            }

            @Override
            public String getValue() {
                return startEvent;
            }

            @Override
            public void valueChanged(String value) {
               setStartEvent(value);
            }
        };

        SelectBoxWidget completeEventWidget = new SelectBoxWidget("Stop Emitting") {
            @Override
            public Array<String> getOptionsList() {
                return getEvents();
            }

            @Override
            public String getValue() {
                return completeEvent;
            }

            @Override
            public void valueChanged(String value) {
                setCompleteEvent(value);
            }
        };

        AttachmentPointWidget position = new AttachmentPointWidget() {
            @Override
            public Array<Bone> getBoneList() {
                return parent.getSkeleton().getBones();
            }

            @Override
            public AttachmentPoint getValue() {
                return positionAttachment;
            }
        };

        GlobalValuePointsWidget globalValues = new GlobalValuePointsWidget() {
            @Override
            public Array<Bone> getBoneList() {
                return parent.getSkeleton().getBones();
            }

            @Override
            public Array<AttachmentPoint> getValue() {
                return valueAttachments;
            }
        };

        properties.add(effectName);
        properties.add(behind);
        properties.add(startEventWidget);
        properties.add(completeEventWidget);
        properties.add(position);
        properties.add(globalValues);

        return properties;
    }

    private void setStartEvent(String value) {
        startEvent = value;
    }

    private void setCompleteEvent(String value) {
        completeEvent = value;
    }

    protected Array<String> getEvents() {
        Array<EventData> events = parent.getSkeleton().getData().getEvents();
        Array<String> result = new Array<>();
        result.add("");
        for(EventData eventData: events) {
            result.add(eventData.getName());
        }
        return result;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Effect: " + name;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    public AttachmentPoint getPositionAttachment() {
        return positionAttachment;
    }

    public Array<AttachmentPoint> getAttachments() {
        return valueAttachments;
    }

    public void updateEffect(ParticleEffectDescriptor descriptor) {
        particleEffectDescriptor = descriptor;
        if(forever) {
            particleEffects.clear();
            ParticleEffectInstance instance = spawnEffect();
            instance.loopable = true; // this is evil
        }
        // else this will get auto-spawned on next event call anyway.
    }

    public void setParent(SkeletonContainer parent) {
        this.parent = parent;
    }

    @Override
    public void write(Json json) {
        json.writeValue("effectName", name);
        json.writeValue("isBehind", isBehind);
        json.writeValue("positionAttachment", positionAttachment);
        json.writeValue("valueAttachments", valueAttachments);
        json.writeValue("startEvent", startEvent);
        json.writeValue("completeEvent", completeEvent);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        String effectName = jsonData.getString("effectName");
        String effectPath = parent.getWorkspace().getPath(effectName + ".p");
        FileHandle effectHandle = TalosMain.Instance().ProjectController().findFile(effectPath);
        this.name = effectName;

        //TODO: refactor this
        ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
        descriptor.setAssetProvider(TalosMain.Instance().TalosProject().getProjectAssetProvider());
        descriptor.load(effectHandle);
        parent.getWorkspace().getVfxLibrary().put(name, descriptor);

        // track this file
        TalosMain.Instance().FileTracker().trackFile(effectHandle, parent.getWorkspace().bvb.particleTracker);

        this.particleEffectDescriptor = descriptor;

        positionAttachment = json.readValue(AttachmentPoint.class, jsonData.get("positionAttachment"));
        JsonValue valueAttachmentsJson = jsonData.get("valueAttachments");
        for(JsonValue valueAttachmentJson: valueAttachmentsJson) {
            AttachmentPoint point = json.readValue(AttachmentPoint.class, valueAttachmentJson);
            valueAttachments.add(point);
        }

        setStartEvent(jsonData.getString("startEvent", ""));
        setCompleteEvent(jsonData.getString("completeEvent", ""));

        isBehind = jsonData.getBoolean("isBehind");

        setForever(startEvent.equals("") && completeEvent.equals(""));
    }

    public String getStartEvent() {
        return startEvent;
    }

    public String getCompleteEvent() {
        return completeEvent;
    }
}
