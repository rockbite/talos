package com.talosvfx.talos.editor.project2.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.InputUtils;

public class Shortcuts extends InputAdapter {

	@Override
	public boolean keyDown (int keycode) {
		//todo make system

		if (keycode == Input.Keys.S && (InputUtils.ctrlPressed() || InputUtils.macCmdPressed())) {
			SaveRequest saveRequest = Notifications.obtainEvent(SaveRequest.class);
			Notifications.fireEvent(saveRequest);
			return true;
		}

		if (keycode == Input.Keys.Z && (InputUtils.ctrlPressed() || InputUtils.macCmdPressed())) {
			SharedResources.globalSaveStateSystem.onUndoRequest();
		}

		return false;

	}
}
