package com.talosvfx.talos.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TextureUnpacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ExportOptimizer {

	private static final Logger logger = LoggerFactory.getLogger(ExportOptimizer.class);

	@Data
	public static class UnpackPayload {

		private String targetAtlasPath;
		private String imageDir;
		private String outDir;

		public UnpackPayload () {}

		public void set (String targetAtlas, String imageDir, String outDir) {
			this.targetAtlasPath = targetAtlas;
			this.imageDir = imageDir;
			this.outDir = outDir;
		}
	}

	@Data
	public static class PackPayload {

		private TexturePacker.Settings settings;
		private String input;
		private String output;
		private String packFileName;

		public PackPayload () {}

		public void set (TexturePacker.Settings settings, String input, String output, String packFileName) {
			this.settings = settings;
			this.input = input;
			this.output = output;
			this.packFileName = packFileName;
		}
	}

	@Data
	public static class ExportPayload {
		private Array<UnpackPayload> unpackPayloads = new Array<>();
		private PackPayload packPayload;
	}

	public static void unpackAndPack (ExportPayload exportPayload) {


		for (UnpackPayload unpackPayload : exportPayload.getUnpackPayloads()) {
			logger.info("Trying to unpack {}", unpackPayload.targetAtlasPath);

			TextureUnpacker.main(new String[]{unpackPayload.targetAtlasPath, unpackPayload.imageDir, unpackPayload.outDir});
			logger.info("Pack finished {}", unpackPayload.targetAtlasPath);
		}

		PackPayload packPayload = exportPayload.packPayload;
		packPayload.settings.fast = true;
		TexturePacker.process(packPayload.settings, packPayload.input, packPayload.output, packPayload.packFileName);
	}

	public static void main (String[] args) throws FileNotFoundException {
		if (args.length == 1) {
			Json json = new Json();

			System.out.println("args: " + args[0]);
			logger.info("Packing config {}", args[0]);
			FileInputStream fileInputStream = new FileInputStream(args[0]);
			ExportPayload exportPayload = json.fromJson(ExportPayload.class, fileInputStream);

			try {
				unpackAndPack(exportPayload);
			} catch (Exception e) {
				logger.error("Error un/packing", e);
				System.exit(-1);
			}
		} else {
			logger.error("Invalid argument, must be the ExportPayload in json format");
		}
	}
}
