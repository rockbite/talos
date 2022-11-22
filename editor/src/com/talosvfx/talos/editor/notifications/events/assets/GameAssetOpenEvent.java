package com.talosvfx.talos.editor.notifications.events.assets;

import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;
import lombok.Setter;

public class GameAssetOpenEvent implements TalosEvent {

	@Getter@Setter
	private GameAsset<?> gameAsset;


	@Override
	public void reset () {

	}
}
