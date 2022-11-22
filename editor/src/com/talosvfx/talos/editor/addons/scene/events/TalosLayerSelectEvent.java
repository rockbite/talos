package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;

public class TalosLayerSelectEvent implements Notifications.Event {

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
