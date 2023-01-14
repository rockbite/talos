package com.talosvfx.talos.editor.project2;

import lombok.Data;

@Data
public class RecentProject implements Comparable<RecentProject> {

	private String projectPath;
	private long saveTime;

	@Override
	public int compareTo (RecentProject o) {
		return -Long.compare(saveTime, o.saveTime);
	}
}
