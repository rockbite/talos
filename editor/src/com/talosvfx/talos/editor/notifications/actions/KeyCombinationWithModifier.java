package com.talosvfx.talos.editor.notifications.actions;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public abstract class KeyCombinationWithModifier implements KeyCombination {
    protected Array<ModifierKey> modifierKeys = new Array<>();

    protected ObjectSet<ModifierKey> pressedModifierKeys = new ObjectSet<>();

    public KeyCombinationWithModifier(ModifierKey... modifierKeys) {
        for (ModifierKey modifierKey : modifierKeys) {
            this.modifierKeys.add(modifierKey);
        }
    }

    public void act(float delta) {

    }

    @Override
    public void resetState() {

    }

    @Override
    public boolean shouldExecute() {
        for (ModifierKey modifierKey : modifierKeys) {
            if (!pressedModifierKeys.contains(modifierKey)) {
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
