package com.talosvfx.talos.editor.notifications;

import com.badlogic.gdx.utils.Pool;

public interface TalosEvent extends Pool.Poolable {
	@Override
	void reset ();
}
