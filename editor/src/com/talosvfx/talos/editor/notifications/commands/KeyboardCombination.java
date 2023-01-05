package com.talosvfx.talos.editor.notifications.commands;

public class KeyboardCombination extends AbstractCombinationWithModifier {
    private int regularKey;
    private boolean repeat;

    private float repeatStartTime = 0.5f;
    private float repeatTime = 0.1f;
    private float repeatCooldown = this.repeatStartTime;
    private boolean timerCooledDown = false;
    private boolean firstTimeDone = false;

    private boolean isRegularKeyPressed = false;

    public KeyboardCombination(int regularKey, boolean repeat, ModifierKey... modifierKeys) {
        super(modifierKeys);
        this.regularKey = regularKey;
        this.repeat = repeat;
    }

    @Override
    public void resetState() {
    }

    @Override
    public void commandIsRun() {
        firstTimeDone = true;
    }

    @Override
    public CombinationType getCombinationType() {
        return CombinationType.KEYBOARD;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (repeat) {
            if (areAllButtonsPressed()) {
                repeatCooldown -= delta;
                timerCooledDown = false;
                if (repeatCooldown <= 0) {
                    repeatCooldown = repeatTime;
                    timerCooledDown = true;
                }
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        boolean areAllButtonsPressed = areAllButtonsPressed();
        if (repeat) {
            if (!areAllButtonsPressed) {
                return false;
            }
            if (!firstTimeDone) {
                return true;
            }
            return timerCooledDown;
        } else {
            return areAllButtonsPressed && !firstTimeDone;
        }
    }

    private boolean areAllButtonsPressed () {
        return isRegularKeyPressed && super.shouldExecute();
    }

    @Override
    public void keyDown(int keycode) {
        super.keyDown(keycode);
        if (ModifierKey.getModifierFromKey(keycode) == null) {
            isRegularKeyPressed = false;
        }

        if (keycode == regularKey) {
            isRegularKeyPressed = true;
        }
    }

    @Override
    public void keyUp(int keycode) {
        super.keyUp(keycode);
        if (keycode == regularKey) {
            isRegularKeyPressed = false;
        }

        if (!areAllButtonsPressed()) {
            firstTimeDone = false;
            timerCooledDown = false;
            repeatCooldown = repeatStartTime;
        }
    }
}
