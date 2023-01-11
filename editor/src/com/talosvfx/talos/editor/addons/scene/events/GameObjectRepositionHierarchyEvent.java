package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Data;

@Data
public class GameObjectRepositionHierarchyEvent implements TalosEvent {

	private GameObject parent;
	private GameObject child;

	@Override
	public void reset () {

	}
}
