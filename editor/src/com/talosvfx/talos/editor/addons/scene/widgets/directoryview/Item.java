package com.talosvfx.talos.editor.addons.scene.widgets.directoryview;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.utils.FileUtils;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.util.Locale;

class Item extends Widget implements ActorCloneable<Item> {
	private Image icon;
	private Image brokenStatus;
	EditableLabel label;

	FileHandle fileHandle;
	GameAsset<?> gameAsset;

	private GameObject basicGameObject;

	private boolean selected;
	private boolean mouseover;

	private float padTop = 5, padTopSmall = 2, padTopBig = 7;
	private float padLeft = 5, padLeftSmall = 2, padLeftBig = 7;
	private float padBottom = 5, padBottomSmall = 2, padBottomBig = 7;
	private float padRight = 5, padRightSmall = 2, padRightBig = 7;
	private float space = 5, spaceSmall = 2, spaceBig = 7;

	public Item () {
		Skin skin = SharedResources.skin;
		icon = new Image(null, Scaling.fit, Align.center);
		brokenStatus = new Image(SharedResources.skin.newDrawable("ic-fileset-file"));
		brokenStatus.setColor(Color.RED);
		label = new EditableLabel("text", skin);
		label.getLabel().setAlignment(Align.center);
		setTouchable(Touchable.enabled);

		label.getTextField().setTextFieldFilter(AssetRepository.ASSET_NAME_FIELD_FILTER);
	}

	public void setFile (FileHandle fileHandle) {
		this.fileHandle = fileHandle;

		String name = fileHandle.name();
		label.setText(name);
		label.getLabel().setWrap(true);
		label.getLabel().setEllipsis(true);

		if (fileHandle.isDirectory()) {
			FileHandle[] content = fileHandle.list();
			if (content.length == 0) {
				icon.setDrawable(SharedResources.skin.getDrawable("ic-folder-big-empty"));
			} else {
				icon.setDrawable(SharedResources.skin.getDrawable("ic-folder-big"));
			}
		} else {
			final String extension = fileHandle.extension().toLowerCase(Locale.US); //ew
			if (GameAssetType.SPRITE.getExtensions().contains(extension)) {
				// preview the asset instead of setting an icon
				updatePreview();
			} else {
				// get and set the icon by the type of the extension
				icon.setDrawable(FileUtils.getFileIconByType(extension));
			}
		}

		GameAsset<?> assetForPath = AssetRepository.getInstance().getAssetForPath(fileHandle, true);
		if (assetForPath != null) {
			gameAsset = assetForPath;
		}

		if (assetForPath != null) {
//			GameObject parent = new GameObject();
//			parent.addComponent(new TransformComponent());
//			basicGameObject = parent;
////
////			AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
////			boolean success = AssetImporter.createAssetInstance(assetForPath, parent) != null;
////			if (parent.getGameObjects() == null || parent.getGameObjects().size == 0) {
////				success = false;
////			}
////			AssetImporter.fromDirectoryView = false;
////
////			if (success) {
////				//Game asset is legit, lets try to make one
////				GameObject copy = new GameObject();
////				copy.addComponent(new TransformComponent());
////
////				AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
////				AssetImporter.createAssetInstance(assetForPath, copy);
////				AssetImporter.fromDirectoryView = false;
////
////				MainRenderer uiSceneRenderer = SceneEditorWorkspace.getInstance().getUISceneRenderer();
////				Stage stage = SceneEditorWorkspace.getInstance().getStage();
////				uiSceneRenderer.setCamera((OrthographicCamera)stage.getCamera());
////				GameObjectActor gameObjectActor = new GameObjectActor(uiSceneRenderer, basicGameObject, copy, true);
////				gameObjectActor.setFillParent(true);
////			}
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		if (selected) {
			Drawable bg = ColorLibrary.obtainBackground(SharedResources.skin, "white", ColorLibrary.BackgroundColor.DARK_GRAY);
			bg.draw(batch, getX(), getY(), getWidth(), getHeight());
		} else if (mouseover) {
			Drawable bg = ColorLibrary.obtainBackground(SharedResources.skin, "white", ColorLibrary.BackgroundColor.LIGHT_GRAY);
			bg.draw(batch, getX(), getY(), getWidth(), getHeight());
		}

		float w = getWidth(), h = getHeight();
		float padTop, padLeft, padBottom, padRight, space;
		if (w < 75) {
			padTop = padTopSmall;
			padLeft = padLeftSmall;
			padBottom = padBottomSmall;
			padRight = padRightSmall;
			space = spaceSmall;
		} else if (w >= 75) {
			padTop = this.padTop;
			padLeft = this.padLeft;
			padBottom = this.padBottom;
			padRight = this.padRight;
			space = this.space;
		} else {
			padTop = padTopBig;
			padLeft = padLeftBig;
			padBottom = padBottomBig;
			padRight = padRightBig;
			space = spaceBig;
		}

		w -= padLeft + padRight;
		h -= padTop + padBottom;

		float lw = w, lh = label.getLabel().getHeight();
		float lx = getX() + padLeft, ly = getY() + padBottom;
		label.setPosition(lx, ly);
		label.setWidth(lw);
		label.act(Gdx.graphics.getDeltaTime());
		label.draw(batch, parentAlpha);

		float iw = w, ih = h - lh - space - padTop;
		float ix = getX() + padLeft;
		float iy = ly + lh + space;
		icon.setSize(iw, ih);
		icon.setPosition(ix, iy);
		icon.draw(batch, parentAlpha);

		if (gameAsset != null && gameAsset.isBroken()) {
			float bsix = getX() + padLeft, bsiy = iy;
			brokenStatus.setPosition(bsix, bsiy);
			brokenStatus.draw(batch, parentAlpha);
			batch.setColor(Color.WHITE);
		}
	}

	@Override
	public Item copyActor (Item copyFrom) {
		setFile(copyFrom.fileHandle);
		return this;
	}

	public boolean isSelected () {
		return selected;
	}

	public void select () {
		selected = true;
	}

	public void deselect () {
		selected = false;
		if (label.isEditMode()) {
			label.finishTextEdit();
		}
	}

	public void setMouseover (boolean over) {
		mouseover = over;
	}

	public FileHandle getFileHandle () {
		return fileHandle;
	}

	public void rename () {
		label.setStage(getStage());
		label.setEditMode();
	}

	public GameAsset<?> getGameAsset () {
		return gameAsset;
	}

	public void updatePreview () {
		try {
			final Texture texture = new Texture(fileHandle);
			final TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
			icon.setDrawable(drawable);
			icon.setScaling(Scaling.fit);
		} catch (Exception e) {
			//invalid png
			final Texture texture = new Texture(Gdx.files.internal("addons/scene/missing/missing.png"));
			final TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
			icon.setDrawable(drawable);
			icon.setScaling(Scaling.fit);
		}
	}
}
