package com.rockbite.tools.talos.editor.serialization;

public class ConnectionData {

	public int moduleFrom;
	public int moduleTo;
	public int slotFrom;
	public int slotTo;

	public ConnectionData () {

	}

	public ConnectionData (int moduleFrom, int moduleTo, int slotFrom, int slotTo) {
		this.moduleFrom = moduleFrom;
		this.moduleTo = moduleTo;
		this.slotFrom = slotFrom;
		this.slotTo = slotTo;
	}

}
