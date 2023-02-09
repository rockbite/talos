package com.talosvfx.talos.data;

public class LocalPreferences {

	private String selectedChannel;
	private boolean autoLaunch;

	public String getSelectedChannel () {
		return selectedChannel;
	}

	public void setSelectedChannel (String selectedChannel) {
		this.selectedChannel = selectedChannel;
	}

	public boolean isAutoLaunch () {
		return autoLaunch;
	}

	public void setAutoLaunch (boolean autoLaunch) {
		this.autoLaunch = autoLaunch;
	}
}
