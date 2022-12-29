package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.utils.XmlReader;
import com.esotericsoftware.spine.Animation;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;

public class SetSpineAnimationNode extends RoutineNode {

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
    public void receiveSignal(String portName) {
        String animationName = fetchStringValue("animation");
        boolean loop = fetchBooleanValue("repeat");

        GameObject go = (GameObject) routineInstanceRef.getSignalPayload();

        if(animationName != null && !animationName.isEmpty() || go != null) {
            SpineRendererComponent component = go.getComponent(SpineRendererComponent.class);

            if(component != null) {
                Animation animation = component.skeleton.getData().findAnimation(animationName);
                component.animationState.setAnimation(0, animation, loop);
            }
        }

        sendSignal("onComplete");
    }
}
