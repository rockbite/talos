package com.talosvfx.talos.editor.notifications.actions;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.notifications.actions.implementations.CopyAction;
import com.talosvfx.talos.editor.notifications.actions.implementations.SaveAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionsSystem extends InputAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ActionsSystem.class);

    private Array<Action> allActions = new Array<>();

    private Array<InputAdapter> injectedAdapters = new Array<>();

    public void injectAdapter(InputAdapter inputAdapter) {
        injectedAdapters.add(inputAdapter);
    }

    public void removeAdapter(InputAdapter inputAdapter) {
        injectedAdapters.removeValue(inputAdapter, true);
    }

    public ActionsSystem() {
        KeyboardKeyCombination copyActionCombination = new KeyboardKeyCombination(Input.Keys.C, true, ModifierKey.CTRL);
        ActionKeyCombination copyActionKeyCombination = new ActionKeyCombination(InputSource.KEYBOARD, copyActionCombination);
        allActions.add(new CopyAction(copyActionKeyCombination, null));

        KeyboardKeyCombination keyboardKeyCombination = new KeyboardKeyCombination(Input.Keys.S, false, ModifierKey.CTRL);
        ActionKeyCombination actionKeyCombination = new ActionKeyCombination(InputSource.KEYBOARD, keyboardKeyCombination);
        allActions.add(new SaveAction(actionKeyCombination, null));
    }

    private boolean checkActionState() {
        boolean isRun = false;
        for (Action action : allActions) {
            if (action.isReadyToRun()) {
                runAction(action);
                isRun = true;
            }
        }

        if (isRun) {
            clearAfterRunning();
        }

        return isRun;
    }

    public void act (float delta) {
        for (Action action : allActions) {
            action.getActionKeyCombination().keyCombination.act(delta);
        }
        checkActionState();
    }

    public void runAction(Action action) {
        action.runAction();
        logger.info("ACTION IS RUN - " + action.getFullName());
    }

    public void clearAfterRunning() {
        for (Action action : allActions) {
            action.clearAfterRunning();
        }
    }


    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (Action action : allActions) {
            action.getActionKeyCombination().keyCombination.mouseMoved();
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.mouseMoved(screenX, screenY);
        }

        return checkActionState();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (Action action : allActions) {
            action.getActionKeyCombination().keyCombination.touchDown(button);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.touchDown(screenX, screenY, pointer, button);
        }

        return checkActionState();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        for (Action action : allActions) {
            action.getActionKeyCombination().keyCombination.scrolled(amountY);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.scrolled(amountX, amountY);
        }

        return checkActionState();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (Action action : allActions) {
            action.getActionKeyCombination().keyCombination.touchUp(button);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.touchUp(screenX, screenY, pointer, button);
        }

        return checkActionState();
    }

    @Override
    public boolean keyDown(int keycode) {
        for (Action action : allActions) {
            action.getActionKeyCombination().keyCombination.keyDown(keycode);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.keyDown(keycode);
        }

        return checkActionState();
    }

    @Override
    public boolean keyUp(int keycode) {
        for (Action action : allActions) {
            action.getActionKeyCombination().keyCombination.keyUp(keycode);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.keyUp(keycode);
        }

        return checkActionState();
    }
}
