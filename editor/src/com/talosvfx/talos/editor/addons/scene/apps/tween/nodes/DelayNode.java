package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

public class DelayNode extends AbstractGenericTweenNode {

    @Override
    protected void onSignalReceived(String command, Object[] payload) {

        if(command.equals("execute")) {
            runGenericTween();
        }
    }
}
