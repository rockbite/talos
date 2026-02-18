package com.talosvfx.talos.editor.project2.batch;

import com.badlogic.gdx.utils.Array;

public class BatchExportScript {

	public String action;
	public Array<BatchExportEntry> projects = new Array<>();

	public static class BatchExportEntry {
		public String projectPath;
		public String exportPath;
	}
}
