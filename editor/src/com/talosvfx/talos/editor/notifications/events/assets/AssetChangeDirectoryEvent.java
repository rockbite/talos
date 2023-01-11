package com.talosvfx.talos.editor.notifications.events.assets;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;
import lombok.Setter;

public class AssetChangeDirectoryEvent implements TalosEvent {

	@Getter@Setter
	private FileHandle path;

	@Override
	public void reset () {

	}
}
