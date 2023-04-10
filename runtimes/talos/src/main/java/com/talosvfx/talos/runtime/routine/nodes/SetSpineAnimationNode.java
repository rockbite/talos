package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;

public class SetSpineAnimationNode extends RoutineNode {

    private ObjectMap<AnimationState, AnimationState.AnimationStateListener> listeners = new ObjectMap<>();

    @Override
    protected void constructNode(XmlReader.Element config) {
        super.constructNode(config);

        // hack in the animation list
        Port port = new Port();
        port.name = "animation";
        port.nodeRef = this;
        port.connectionType = ConnectionType.DATA;
        port.portType = PortType.INPUT;
        inputs.put(port.name, port);
    }

    @Override
    protected void configureNode(JsonValue properties) {
        super.configureNode(properties);

        inputs.get("animation").valueOverride = properties.getString("animation");
    }

    @Override
    public void receiveSignal(String portName) {
        String animationName = fetchStringValue("animation");
        boolean loop = fetchBooleanValue("repeat");
        boolean add = fetchBooleanValue("add");
        boolean clearTrack = fetchBooleanValue("clearTrack");
        int track = fetchIntValue("track");
        GameObject gameObject = fetchGameObjectValue("gameObject");

        if (gameObject == null) {
            gameObject = (GameObject) routineInstanceRef.getSignalPayload();
        }

        if(animationName != null && !animationName.isEmpty() && gameObject != null) {
            SpineRendererComponent component = gameObject.getComponent(SpineRendererComponent.class);


            if(component != null) {
                SkeletonData skeletonData = component.skeleton.getData();
                Animation animation = skeletonData.findAnimation(animationName);
                if(animation == null) {
                    animation = skeletonData.getAnimations().first();
                }

                AnimationState animationState = component.animationState;


                if (!listeners.containsKey(animationState)) {
                    AnimationState.AnimationStateAdapter listener = new AnimationState.AnimationStateAdapter() {
                        @Override
                        public void complete (AnimationState.TrackEntry entry) {
                            if (entry.getAnimation().getName().equals(animationName)) {
                                sendSignal("onAnimationComplete");
                            }
                        }
                    };
                    animationState.addListener(listener);
                    listeners.put(animationState, listener);
                }


                if (clearTrack) {
                    animationState.clearTrack(track);
                    animationState.setEmptyAnimation(track, 0.1f);
                } else {

                    if (add) {
                        animationState.addAnimation(track, animation, loop, 0);
                    } else {
                        AnimationState.TrackEntry current = animationState.getCurrent(track);
                        if (current == null || current.getAnimation() != animation) {
                            animationState.setAnimation(track, animation, loop);
                        }
                    }
                }
            }
        }


        sendSignal("onComplete");
    }



    @Override
    public void reset () {
        super.reset();
        for (ObjectMap.Entry<AnimationState, AnimationState.AnimationStateListener> listener : listeners) {
            listener.key.removeListener(listener.value);
        }
        listeners.clear();
    }
}
