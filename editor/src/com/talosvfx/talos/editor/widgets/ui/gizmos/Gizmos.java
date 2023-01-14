package com.talosvfx.talos.editor.widgets.ui.gizmos;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;

public class Gizmos {

	public Array<Gizmo> gizmoList = new Array<>();
	public ObjectMap<GameObject, Array<Gizmo>> gizmoMap = new ObjectMap<>();

}
