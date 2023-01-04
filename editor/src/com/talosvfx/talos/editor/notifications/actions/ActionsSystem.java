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

    private Array<IAction> allActions = new Array<>();

    private Array<InputAdapter> injectedAdapters = new Array<>();

    public void injectAdapter(InputAdapter inputAdapter) {
        injectedAdapters.add(inputAdapter);
    }

    public void removeAdapter(InputAdapter inputAdapter) {
        injectedAdapters.removeValue(inputAdapter, true);
    }

    public ActionsSystem() {
        MouseCombination copyActionCombination = new MouseCombination(MouseAction.WHEEL_IN, ModifierKey.CTRL);
        allActions.add(new CopyAction(copyActionCombination, null));

        KeyboardCombination keyboardKeyCombination = new KeyboardCombination(Input.Keys.S,false, ModifierKey.CTRL);
        allActions.add(new SaveAction(keyboardKeyCombination, null));
    }

    private boolean checkActionState() {
        boolean isRun = false;
        for (IAction action : allActions) {
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
        for (IAction action : allActions) {
            action.getActiveCombination().act(delta);
        }
        checkActionState();
    }

    public void runAction(IAction IAction) {
        IAction.runAction();
        logger.info("ACTION IS RUN - " + IAction.getFullName());
    }

    public void clearAfterRunning() {
        for (IAction action : allActions) {
            action.clearAfterRunning();
        }
    }


    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (IAction action : allActions) {
            action.getActiveCombination().mouseMoved();
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.mouseMoved(screenX, screenY);
        }

        return checkActionState();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (IAction action : allActions) {
            action.getActiveCombination().touchDown(button);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.touchDown(screenX, screenY, pointer, button);
        }

        return checkActionState();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        for (IAction action : allActions) {
            action.getActiveCombination().scrolled(amountY);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.scrolled(amountX, amountY);
        }

        return checkActionState();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (IAction action : allActions) {
            action.getActiveCombination().touchUp(button);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.touchUp(screenX, screenY, pointer, button);
        }

        return checkActionState();
    }

    @Override
    public boolean keyDown(int keycode) {
        for (IAction action : allActions) {
            action.getActiveCombination().keyDown(keycode);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.keyDown(keycode);
        }

        return checkActionState();
    }

    @Override
    public boolean keyUp(int keycode) {
        for (IAction action : allActions) {
            action.getActiveCombination().keyUp(keycode);
        }

        for (InputAdapter inputAdapter : injectedAdapters) {
            inputAdapter.keyUp(keycode);
        }

        return checkActionState();
    }
}
