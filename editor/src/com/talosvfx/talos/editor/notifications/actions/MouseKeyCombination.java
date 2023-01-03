package com.talosvfx.talos.editor.notifications.actions;


import com.badlogic.gdx.Input;

public class MouseKeyCombination extends KeyCombinationWithModifier {
    private MouseAction mouseAction;
    private boolean isMouseActionDone = false;

    public MouseKeyCombination (MouseAction mouseAction, ModifierKey... modifierKeys) {
        super(modifierKeys);
        this.mouseAction = mouseAction;
    }

    @Override
    public boolean shouldExecute() {
        return isMouseActionDone && super.shouldExecute();
    }

    @Override
    public void resetState() {
        super.resetState();
        isMouseActionDone = false;
    }

    @Override
    public void mouseMoved() {
        if (mouseAction == MouseAction.MOVE) {
            isMouseActionDone = true;
        }
    }

    @Override
    public void touchDown(int button) {
        if (button == Input.Buttons.MIDDLE && mouseAction == MouseAction.WHEEL_IN) {
            isMouseActionDone = true;
        }
    }

    @Override
    public void touchUp(int button) {
        if (button == Input.Buttons.MIDDLE && mouseAction == MouseAction.WHEEL_OUT) {
            isMouseActionDone = true;
        }
    }

    @Override
    public void scrolled(float amountY) {
        if (mouseAction == MouseAction.WHEEL_UP) {
            isMouseActionDone = amountY > 0;
        }
        if (mouseAction == MouseAction.WHEEL_DOWN) {
            isMouseActionDone = amountY < 0;
        }
    }

    @Override
    public void actionIsRun() {

    }
}
