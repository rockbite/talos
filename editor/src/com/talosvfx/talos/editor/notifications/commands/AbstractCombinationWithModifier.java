package com.talosvfx.talos.editor.notifications.commands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.ObjectSet;

public abstract class AbstractCombinationWithModifier implements Combination {
    protected Array<ModifierKey> modifierKeys = new Array<>();

    protected boolean isAnyModifier;

    protected ObjectSet<ModifierKey> pressedModifierKeys = new ObjectSet<>();

    public AbstractCombinationWithModifier(ModifierKey... modifierKeys) {
        for (ModifierKey modifierKey : modifierKeys) {
            this.modifierKeys.add(modifierKey);
        }

        checkForAllModifiers();
    }

    public boolean hasModifier(ModifierKey modifierKey) {
        return modifierKeys.contains(modifierKey, false);
    }

    public void addModifierKey(ModifierKey modifierKey) {
        this.modifierKeys.add(modifierKey);
        checkForAllModifiers();
    }

    private void checkForAllModifiers() {
        isAnyModifier = true;
        for (ModifierKey value : ModifierKey.values()) {
            if (!this.modifierKeys.contains(value, false)) {
                isAnyModifier = false;
                break;
            }
        }
    }

    public void removeModifierKey(ModifierKey modifierKey) {
        this.modifierKeys.removeValue(modifierKey, false);
        isAnyModifier = false;
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

    @Override
    public String toString() {
        CharArray modifiersString = new CharArray();
        for (ModifierKey modifierKey : modifierKeys) {
            modifiersString.append(modifierKey).append(" ");
        }

        return modifiersString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCombinationWithModifier)) return false;

        AbstractCombinationWithModifier that = (AbstractCombinationWithModifier) o;

        if (modifierKeys.size != that.modifierKeys.size) {
            return false;
        }

        for (ModifierKey modifierKey : modifierKeys) {
            if (!that.modifierKeys.contains(modifierKey, false)) {
                return false;
            }
        }

        for (ModifierKey modifierKey : that.modifierKeys) {
            if (!(modifierKeys.contains(modifierKey, false))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return modifierKeys.hashCode();
    }
}
