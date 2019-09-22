package com.rockbite.tools.talos.editor.serialization;

public class ConnectionData {

	private int moduleFrom;
	private int moduleTo;
	private int slotFrom;
	private int slotTo;

	public ConnectionData () {

	}

	public ConnectionData (int moduleFrom, int moduleTo, int slotFrom, int slotTo) {
		this.moduleFrom = moduleFrom;
		this.moduleTo = moduleTo;
		this.slotFrom = slotFrom;
		this.slotTo = slotTo;
	}

}
