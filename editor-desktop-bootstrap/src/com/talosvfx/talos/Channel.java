package com.talosvfx.talos;

public class Channel {

	private int major;
	private int minor;
	private int patch;

	private String displayString;

	private boolean snapshot;

	public Channel (String versionString) {

		if (versionString.contains("SNAPSHOT")) {
			snapshot = true;
			versionString = versionString.split("-SNAPSHOT")[0];
		}

		displayString = versionString;

		String[] versions = versionString.split("\\.");


		major = Integer.parseInt(versions[0]);
		minor = Integer.parseInt(versions[1]);
		patch = Integer.parseInt(versions[2]);

	}

	public int getMajor () {
		return major;
	}

	public int getMinor () {
		return minor;
	}

	public int getPatch () {
		return patch;
	}

	public String getDisplayString () {
		return displayString;
	}

	public boolean isSnapshot () {
		return snapshot;
	}
}
