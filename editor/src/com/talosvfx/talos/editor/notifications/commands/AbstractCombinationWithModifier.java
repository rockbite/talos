package com.talosvfx.talos.editor.notifications.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public abstract class AbstractCombinationWithModifier implements Combination {
    protected Array<ModifierKey> modifierKeys = new Array<>();

    protected boolean isAnyModifier;

    protected ObjectSet<ModifierKey> pressedModifierKeys = new ObjectSet<>();

    public AbstractCombinationWithModifier(ModifierKey... modifierKeys) {
        for (ModifierKey modifierKey : modifierKeys) {
            this.modifierKeys.add(modifierKey);
        }

        isAnyModifier = true;
        for (ModifierKey value : ModifierKey.values()) {
            if (!this.modifierKeys.contains(value, false)) {
                isAnyModifier = false;
                break;
            }
        }
    }

    public void act(float delta) {

    }

    @Override
    public void resetState() {

    }

    @Override
    public boolean shouldExecute() {
        if (isAnyModifier) {
            return !pressedModifierKeys.isEmpty();
        }

        for (ModifierKey modifierKey : modifierKeys) {
            if (!pressedModifierKeys.contains(modifierKey)) {
                return false;
            }
        }

        for (ModifierKey modifierKey : pressedModifierKeys) {
            if (!modifierKeys.contains(modifierKey, false)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void mouseMoved() {

    }

    @Override
    public void touchDown(int button) {

    }

    @Override
    public void touchUp(int button) {

    }

    @Override
    public void keyDown(int keycode) {
        ModifierKey modifierFromKey = ModifierKey.getModifierFromKey(keycode);
        if (modifierFromKey != null) {
            pressedModifierKeys.add(modifierFromKey);
        }
    }

    @Override
    public void keyUp(int keycode) {
        ModifierKey modifierFromKey = ModifierKey.getModifierFromKey(keycode);
        if (modifierFromKey != null) {
            pressedModifierKeys.remove(modifierFromKey);
        }
    }

    @Override
    public void scrolled(float amountY) {

    }
}
