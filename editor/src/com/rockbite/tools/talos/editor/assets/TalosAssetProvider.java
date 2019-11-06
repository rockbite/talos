/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.assets.BaseAssetProvider;
import com.rockbite.tools.talos.runtime.utils.VectorField;

import java.io.File;

public class TalosAssetProvider extends BaseAssetProvider {

	private TextureAtlas atlas = new TextureAtlas();
	private ObjectMap<String, VectorField> vectorFields = new ObjectMap<>();

	private ObjectMap<String, UnknownHandleToAssetParser> extensionToAssetParser = new ObjectMap<>();

	private interface UnknownHandleToAssetParser {
		void toAsset (FileHandle handle);
	}

	public TalosAssetProvider () {
		registerDefaultHandlers();
		registerUnknownHandlerParsers();
		addDefaultAssets();
	}


	public void addToAtlas (String name, TextureRegion region) {
		atlas.addRegion(name, region);
	}

	public void addVectorField (String name, VectorField vectorField) {
		vectorFields.put(name, vectorField);
	}


	private void registerDefaultHandlers () {
		setAssetHandler(TextureRegion.class,this::findRegionOrLoad);
		setAssetHandler(VectorField.class, this::findVectorOrLoad);
	}

	private void registerUnknownHandlerParsers () {
		extensionToAssetParser.put("png", handle -> addToAtlas(handle.nameWithoutExtension(), new TextureRegion(new Texture(handle))));
		extensionToAssetParser.put("jpg", handle -> addToAtlas(handle.nameWithoutExtension(), new TextureRegion(new Texture(handle))));
		extensionToAssetParser.put("fga", handle -> addVectorField(handle.nameWithoutExtension(), new VectorField(handle)));
	}

	private void addDefaultAssets () {
		addTexture(Gdx.files.internal("fire.png"));
	}

	private void addTexture (FileHandle path) {
		Texture texture = new Texture(path);
		addToAtlas(path.nameWithoutExtension(), new TextureRegion(texture));
	}

	private VectorField findVectorOrLoad (String assetName) {
		VectorField vectorField = vectorFields.get(assetName);
		if (vectorField == null) {
			//Look in all paths, and hopefully load the requested asset, or fail (crash)
			final FileHandle file = findFile(assetName);

			vectorField = new VectorField();
			vectorField.setBakedData(file);
		}
		return vectorField;
	}

	private TextureRegion findRegionOrLoad (String assetName) {
		final TextureAtlas.AtlasRegion region = atlas.findRegion(assetName);
		if (region == null) {
			//Look in all paths, and hopefully load the requested asset, or fail (crash)
			final FileHandle file = findFile(assetName);
			if (file == null || !file.exists()) {
				throw new GdxRuntimeException("No region found for: " + assetName + " from provider");
			}
			Texture texture = new Texture(file);
			TextureRegion textureRegion = new TextureRegion(texture);
			atlas.addRegion(assetName, textureRegion);
			return textureRegion;
		}
		return region;
	}

	private FileHandle findFile (String regionName) {
		String fileName = regionName + ".png";
		FileHandle handle = Gdx.files.absolute(TalosMain.Instance().ProjectController().getCurrentProjectPath() + File.separator + fileName);
		return TalosMain.Instance().ProjectController().findFile(handle);
	}

	public TextureRegion replaceRegion (FileHandle handle) {
		Texture texture = new Texture(handle);
		final TextureRegion textureRegion = new TextureRegion(texture);

		final Array<TextureAtlas.AtlasRegion> regions = atlas.getRegions();

		for (int i = 0; i < regions.size; i++) {
			if (regions.get(i).name.equalsIgnoreCase(handle.nameWithoutExtension())) {
				regions.removeIndex(i);
				break;
			}
		}

		atlas.addRegion(handle.nameWithoutExtension(), textureRegion);

		return textureRegion;
	}

	public void addUnknownResource (String absolutePath) {
		final FileHandle absolute = Gdx.files.absolute(absolutePath);
		extensionToAssetParser.get(absolute.extension()).toAsset(absolute);
	}
}
