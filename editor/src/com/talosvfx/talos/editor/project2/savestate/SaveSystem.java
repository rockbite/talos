package com.talosvfx.talos.editor.project2.savestate;

import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.utils.Toasts;

public class SaveSystem implements Observer {

	public SaveSystem () {
		Notifications.registerObserver(this);
	}

	@EventHandler
	public void onSave (SaveRequest event) {

		Toasts.getInstance().showInfoToast("Project saved");
	}
}
