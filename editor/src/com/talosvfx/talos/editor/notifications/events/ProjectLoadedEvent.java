package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import lombok.Getter;
import lombok.Setter;

public class ProjectLoadedEvent implements TalosEvent {

	@Getter@Setter
	private TalosProjectData projectData;

	@Override
	public void reset () {

	}
}
