package com.talosvfx.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.talosvfx.talos.TalosMain;

public class TalosInputProcessor implements InputProcessor {

    public static boolean ctrlPressed () {

        //todo
        if (true) return false;

        if (TalosMain.Instance().isOsX()) {
            return Gdx.input.isKeyPressed(Input.Keys.SYM) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        } else {
            return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        }
    }

    @Override
    public boolean keyDown (int keycode) {
        //todo
        if (true) return false;

        if (keycode == Input.Keys.Z && ctrlPressed() && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            TalosMain.Instance().ProjectController().undo();
            return true;
        }

        if (keycode == Input.Keys.Z && ctrlPressed() && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            TalosMain.Instance().ProjectController().redo();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped (char character) {
        return false;
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved (int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return false;
    }
}
