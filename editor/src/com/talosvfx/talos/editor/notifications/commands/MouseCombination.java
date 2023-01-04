package com.talosvfx.talos.editor.notifications.commands;


import com.badlogic.gdx.Input;

public class MouseCombination extends AbstractCombinationWithModifier {
    private MouseCommand mouseCommand;
    private boolean isMouseCommandDone = false;

    public MouseCombination(MouseCommand mouseCommand, ModifierKey... modifierKeys) {
        super(modifierKeys);
        this.mouseCommand = mouseCommand;
    }

    @Override
    public boolean shouldExecute() {
        return isMouseCommandDone && super.shouldExecute();
    }

    @Override
    public void resetState() {
        super.resetState();
        isMouseCommandDone = false;
    }

    @Override
    public void mouseMoved() {
        isMouseCommandDone = false;
        if (mouseCommand == MouseCommand.MOVE) {
            isMouseCommandDone = true;
        }
    }

    @Override
    public void touchDown(int button) {
        isMouseCommandDone = false;
        if (button == Input.Buttons.MIDDLE && mouseCommand == MouseCommand.WHEEL_IN) {
            isMouseCommandDone = true;
        }
    }

    @Override
    public void touchUp(int button) {
        isMouseCommandDone = false;
        if (button == Input.Buttons.MIDDLE && mouseCommand == MouseCommand.WHEEL_OUT) {
            isMouseCommandDone = true;
        }
        if (button == Input.Buttons.LEFT && mouseCommand == MouseCommand.LEFT) {
            isMouseCommandDone = true;
        }
        if (button == Input.Buttons.RIGHT && mouseCommand == MouseCommand.RIGHT) {
            isMouseCommandDone = true;
        }
    }

    @Override
    public void scrolled(float amountY) {
        isMouseCommandDone = false;
        if (mouseCommand == MouseCommand.WHEEL_DOWN) {
            isMouseCommandDone = amountY > 0;
        }
        if (mouseCommand == MouseCommand.WHEEL_UP) {
            isMouseCommandDone = amountY < 0;
        }
    }

    @Override
    public void commandIsRun() {
        isMouseCommandDone = false;
    }

    @Override
    public CombinationType getCombinationType() {
        return CombinationType.MOUSE;
    }
}
