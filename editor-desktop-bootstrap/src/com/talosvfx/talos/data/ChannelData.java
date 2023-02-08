package com.talosvfx.talos.data;

import org.jetbrains.annotations.NotNull;

public class ChannelData implements Comparable<ChannelData> {

	private String versionIdentifier;
	private String latestVersionString;

	public void setLatestVersionString (String latestVersionString) {
		this.latestVersionString = latestVersionString;
	}

	public void setVersionIdentifier (String versionIdentifier) {
		this.versionIdentifier = versionIdentifier;
	}

	public String getVersionIdentifier () {
		return versionIdentifier;
	}

	public String getLatestVersionString () {
		return latestVersionString;
	}

	@Override
	public String toString () {
		return versionIdentifier;
	}

	@Override
	public int compareTo (@NotNull ChannelData o) {

		Version thisVersion = new Version(latestVersionString);
		Version otherVersion = new Version(o.latestVersionString);


		return thisVersion.compareTo(otherVersion);
	}

	static class Version implements Comparable<Version> {
		int major;
		int minor;
		int patch;
		boolean snapshot;

		public Version (String latestVersionString) {
			String baseVersion = latestVersionString;
			if (latestVersionString.contains("SNAPSHOT")) {
				baseVersion = baseVersion.split("-SNAPSHOT")[0];
				snapshot = true;
			}

			String[] versionSplit = baseVersion.split("\\.");

			major = Integer.parseInt(versionSplit[0]);
			minor = Integer.parseInt(versionSplit[1]);
			patch = Integer.parseInt(versionSplit[2]);
		}

		@Override
		public int compareTo (@NotNull Version o) {
			if (major > o.major) return -1;
			if (major < o.major) return 1;

			if (minor > o.minor) return -1;
			if (minor < o.minor) return 1;

			if (patch > o.patch) return -1;
			if (patch < o.patch) return 1;

			//Its the same

			if (snapshot == o.snapshot) return 0;

			if (snapshot) return -1;
			if (o.snapshot) return 1;

			return 0;
		}
	}
}
