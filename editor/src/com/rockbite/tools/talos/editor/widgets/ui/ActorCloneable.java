package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface ActorCloneable<T extends Actor>{
	T copyActor(T copyFrom);
}
