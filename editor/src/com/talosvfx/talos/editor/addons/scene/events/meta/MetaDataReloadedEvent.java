package com.talosvfx.talos.editor.addons.scene.events.meta;

import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Data;

@Data
public class MetaDataReloadedEvent implements TalosEvent {

	private AMetadata metadata;

	@Override
	public void reset () {

	}
}
