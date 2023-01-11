package com.talosvfx.talos.editor.project2.savestate;

import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.utils.Toasts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveSystem implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(SaveSystem.class);

	public SaveSystem () {
		Notifications.registerObserver(this);
	}

	@EventHandler
	public void onSave (SaveRequest event) {
		if (SharedResources.currentProject != null) {
			TalosProjectData currentProject = SharedResources.currentProject;
			try {
				currentProject.save();
				Toasts.getInstance().showInfoToast("Project saved");
			} catch (Exception e) {
				logger.error("Failure to save", e);
				Toasts.getInstance().showErrorToast("Failure to save " + e.getMessage());
			}
		} else {
			Toasts.getInstance().showInfoToast("No project to save");
		}
	}
}
