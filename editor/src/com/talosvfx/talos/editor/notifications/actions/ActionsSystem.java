package com.talosvfx.talos.editor.notifications.actions;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.actions.enums.Actions;
import com.talosvfx.talos.editor.notifications.actions.implementations.GeneralAction;
import com.talosvfx.talos.editor.notifications.events.actions.ActionContextEvent;
import com.talosvfx.talos.editor.notifications.events.actions.ActionEvent;
import com.talosvfx.talos.editor.notifications.events.actions.IActionEvent;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
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

        KeyboardCombination copyCombination = new KeyboardCombination(Input.Keys.C, false, ModifierKey.CTRL);
        GeneralAction copyAction = new GeneralAction(Actions.ActionType.COPY, ActionContextType.FOCUSED_APP, copyCombination, null);
        allActions.add(copyAction);

        KeyboardCombination saveCombination = new KeyboardCombination(Input.Keys.S, false, ModifierKey.CTRL);
        GeneralAction saveAction = new GeneralAction(Actions.ActionType.SAVE, ActionContextType.GLOBAL, saveCombination, null);
        allActions.add(saveAction);

        KeyboardCombination openCombination = new KeyboardCombination(Input.Keys.O, false, ModifierKey.CTRL);
        GeneralAction openAction = new GeneralAction(Actions.ActionType.OPEN, ActionContextType.FOCUSED_APP, openCombination, null);
        allActions.add(openAction);
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

    public void act(float delta) {
        for (IAction action : allActions) {
            action.getActiveCombination().act(delta);
        }
        checkActionState();
    }

    public void runAction(IAction action) {
        Notifications.fireEvent(getEventForAction(action));
        logger.info("ACTION IS RUN - " + action.getActionType().name);
    }

    public void clearAfterRunning() {
        for (IAction action : allActions) {
            action.clearAfterRunning();
        }
    }

    private TalosEvent getEventForAction(IAction action) {
        IActionEvent actionEvent = Notifications.obtainEvent(getActionEventTypeForContextType(action.getContextType()));
        actionEvent.setActionType(action.getActionType());
        ActionContextType contextType = action.getContextType();

        if (contextType == ActionContextType.FOCUSED_APP) {
            ActionContextEvent contextEvent = (ActionContextEvent) actionEvent;
            AppManager appManager = SharedResources.appManager;
            AppManager.BaseApp focusedApp = appManager.getFocusedApp();
            contextEvent.setContext(focusedApp);
        }

        return actionEvent;
    }

    Class<? extends IActionEvent> getActionEventTypeForContextType (ActionContextType type) {
        switch (type) {
            case GLOBAL:
                return ActionEvent.class;
            case FOCUSED_APP:
                return ActionContextEvent.class;
        }

        return null;
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
