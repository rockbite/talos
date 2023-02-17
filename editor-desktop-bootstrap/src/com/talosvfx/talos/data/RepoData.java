package com.talosvfx.talos.data;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RepoData {

	private List<ChannelData> versions;

	public List<ChannelData> getVersions () {
		return versions;
	}

	public void sort () {
		versions.sort(new Comparator<ChannelData>() {
			@Override
			public int compare (ChannelData o1, ChannelData o2) {
				return o1.compareTo(o2);
			}
		});
	}

	public ChannelData getChannel (String selectedChannel) {
		Optional<ChannelData> first = versions.stream().filter(channelData -> channelData.getVersionIdentifier().equals(selectedChannel)).findFirst();
		return first.orElse(null);
	}
}
