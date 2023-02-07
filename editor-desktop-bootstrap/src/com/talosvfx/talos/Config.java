package com.talosvfx.talos;

import org.update4j.Configuration;
import org.update4j.FileMetadata;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

public class Config {

	private final Channel releaseChannel;

	public Config (Channel releaseChannel) {
		this.releaseChannel = releaseChannel;

		String editorBuildPath = "../editor-desktop/build/libs/";
		String configFileToCreate = "dist/config.xml";

		String channelPath = getChannelPath(releaseChannel);

		File talosJar = new File(editorBuildPath + getTalosJarName(releaseChannel));
		File configFile = new File(configFileToCreate);

		Configuration config = Configuration.builder()
			.property("version", releaseChannel.getDisplayString())
			.property("snapshot", String.valueOf(releaseChannel.isSnapshot()))
			.property("user.location", "${user.home}/talos/")
			.basePath("${user.location}")
			.baseUri("https://editor.talosvfx.com/editor-binaries/")
			.file(FileMetadata.readFrom(talosJar.toPath())
				.path("${user.location}/" + channelPath + "/" + getTalosJarName(releaseChannel))
				.uri(getTalosJarName(releaseChannel))
				.classpath())
			.property("default.launcher.main.class", "com.talosvfx.talos.TalosLauncher")
			.build();

		try (Writer out = Files.newBufferedWriter(configFile.toPath().toAbsolutePath())) {
			config.write(out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getChannelPath (Channel releaseChannel) {
		if (releaseChannel.isSnapshot()) {
			return releaseChannel.getMajor() + "." + releaseChannel.getMinor() + "-SNAPSHOT";
		} else {
			return releaseChannel.getMajor() + "." + releaseChannel.getMinor();
		}
	}

	private String getTalosJarName (Channel releaseChannel) {
		String baseString = "editor-desktop-" + releaseChannel.getMajor() + "." + releaseChannel.getMinor() + "." + releaseChannel.getPatch();
		if (releaseChannel.isSnapshot()) {
			baseString  += "-SNAPSHOT";
		}
		baseString += ".jar";
		return baseString;
	}

	public static void main (String[] args) {

		//Parse the release channel from versioning

		new Config(new Channel("2.0.0-SNAPSHOT"));
	}
}
