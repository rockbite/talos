package com.talosvfx.talos.editor.notifications.commands;


import com.badlogic.gdx.Input;
import lombok.Getter;

public class MouseCombination extends AbstractCombinationWithModifier {
    @Getter
    private MouseCommand mouseCommand;
    private boolean isMouseCommandDone = false;

    public MouseCombination(MouseCommand mouseCommand, ModifierKey... modifierKeys) {
        super(modifierKeys);
        this.mouseCommand = mouseCommand;
    }


    @Override
    public MouseCombination copy() {
        MouseCombination mouseCombination = new MouseCombination(mouseCommand);
        for (ModifierKey modifierKey : modifierKeys) {
            mouseCombination.modifierKeys.add(modifierKey);
        }
        return mouseCombination;
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

    @Override
    public String toString() {
        String superText = super.toString();
        return superText + mouseCommand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MouseCombination that = (MouseCombination) o;

        if (super.equals(that)) {
            return false;
        }

        return mouseCommand == that.mouseCommand;
    }

    @Override
    public int hashCode() {
        return mouseCommand != null ? mouseCommand.hashCode() : 0;
    }
}
