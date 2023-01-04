package com.talosvfx.talos.editor.notifications.actions;

public class ActionCombinationWrapper {
    public InputSource inputSource;

    public Combination combination;

    public ActionCombinationWrapper(Combination combination) {
        this.combination = combination;
        inputSource = combination instanceof KeyboardCombination ? InputSource.KEYBOARD : InputSource.MOUSE;
    }
}
