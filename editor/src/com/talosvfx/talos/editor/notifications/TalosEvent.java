package com.talosvfx.talos.editor.notifications;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pool;

public interface TalosEvent extends Pool.Poolable {
	@Override
	void reset ();


	default boolean notifyThroughSocket () {
		return false;
	}

	default String getEventType () {
		return "";
	}

	default Json getAdditionalData (Json json) {
		return json;
	}

	default Json getMainData (Json json) {
		return json;
	}

}
