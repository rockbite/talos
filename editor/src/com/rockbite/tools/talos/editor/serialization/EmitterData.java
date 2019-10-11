package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;
import com.rockbite.tools.talos.runtime.serialization.ConnectionData;

public class EmitterData {

	public String name;
	public Array<ModuleWrapper> modules = new Array<>();
	public Array<ConnectionData> connections = new Array<>();
	public Array<GroupData> groups = new Array();

	public EmitterData () {

	}


}
