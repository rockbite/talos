package com.talosvfx.talos.editor.project;

public class RecentsEntry {

	String path;
	public int time;

	public RecentsEntry () {

	}

	public RecentsEntry (String path, int time) {
		this.path = path;
		this.time = time;
	}

	@Override
	public boolean equals (Object obj) {
		return path.equals(((RecentsEntry)obj).path);
	}
}
