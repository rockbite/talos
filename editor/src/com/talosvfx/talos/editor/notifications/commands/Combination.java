package com.talosvfx.talos.editor.notifications.commands;

public interface Combination {
    boolean shouldExecute();

    void resetState();

    void mouseMoved();

    void touchDown(int button);

    void touchUp(int button);

    void keyDown(int keycode);

    void keyUp(int keycode);

    void scrolled(float amountY);

    void act(float delta);

    void commandIsRun();

    CombinationType getCombinationType();

    Combination copy();
}
