package com.talosvfx.talos.editor.notifications.actions;

public class ActionKeyCombination {
    public InputSource inputSource;

    public KeyCombination keyCombination;

    public ActionKeyCombination(InputSource inputSource, KeyCombination keyCombination) {
        this.inputSource = inputSource;
        this.keyCombination = keyCombination;
    }
}
