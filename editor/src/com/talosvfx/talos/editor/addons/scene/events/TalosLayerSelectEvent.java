package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class TalosLayerSelectEvent implements TalosEvent {

	public TalosLayer layer;

	public TalosLayerSelectEvent () {}

	public TalosLayerSelectEvent (TalosLayer layer) {
		this.layer = layer;
	}

	@Override
	public void reset () {
		layer = null;
	}
}
