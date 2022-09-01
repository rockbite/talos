package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TileDataComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.utils.grid.RulerRenderer;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

import java.awt.*;
import java.util.UUID;

public class PaletteEditor extends AEditorApp<GameAsset<TilePaletteData>> {
	private String title;
	private DragAndDrop.Target target;
	private ModeToggle modeToggle;
	private PaletteEditorWorkspace paletteEditorWorkspace;

	private PaletteListener defaultPaletteListener;
	private Table buttonMainMenu;

	enum PaletteImportMode {
		NONE,
		TILE,
		ENTITY
	}

	protected PaletteImportMode currentImportMode = PaletteImportMode.NONE;

	protected PaletteEditMode currentEditMode = PaletteEditMode.NONE;

	enum PaletteEditMode {
		NONE,
		FREE_TRANSLATE, // translation of the parent tile + translation of transform
		FREE_TRANSFORM, // free transform of transform component with gizmo
		PARENT_TILE_AND_FAKE_HEIGHT,
	}

	public PaletteEditor (GameAsset<TilePaletteData> paletteData) {
		super(paletteData);
		identifier = paletteData.nameIdentifier;
		title = paletteData.nameIdentifier;
		initContent();

		for (ObjectMap.Entry<GameAsset<?>, GameObject> entry : paletteData.getResource().gameObjects) {
			SceneEditorWorkspace.getInstance().initGizmos(entry.value, paletteEditorWorkspace);
		}
	}

	@Override
	public void initContent () {
		Skin skin = TalosMain.Instance().getSkin();

		content = new Table();

		paletteEditorWorkspace = new PaletteEditorWorkspace(this);
		modeToggle = new ModeToggle();

		this.content.add(paletteEditorWorkspace).minSize(336, 696).grow().row();
		this.content.add(modeToggle).growX().height(32);

		Table toolbar = new Table();
		toolbar.setFillParent(true);
		toolbar.top().left();

		buttonMainMenu = new Table();
		buttonMainMenu.setBackground(skin.newDrawable("square-bordered"));
		toolbar.add(buttonMainMenu).expandX().padTop(RulerRenderer.RULER_SIZE + 8);

		addDefaultButtons();

		this.content.addActor(toolbar);

		// register the drag and drop target
		target = new PaletteDragAndDropTarget(paletteEditorWorkspace);
		SceneEditorAddon.get().projectExplorer.getDirectoryViewWidget().registerTarget(target);

		// lock gizmo by default
		paletteEditorWorkspace.lockGizmos();
	}

	@Override
	public String getTitle () {
		return title;
	}

	@Override
	public void onHide () {
		super.onHide();

		AssetRepository.getInstance().saveGameAssetResourceJsonToFile(object);

		SceneEditorAddon.get().projectExplorer.getDirectoryViewWidget().unregisterTarget(target);
		for (ObjectMap.Entry<GameAsset<?>, GameObject> entry : object.getResource().gameObjects) {
			SceneEditorWorkspace.getInstance().removeGizmos(entry.value);
		}
		Notifications.registerObserver(paletteEditorWorkspace);

	}

	public void addGameAsset (GameAsset<?> gameAsset, float x, float y) {
		//Check if we already have it
		GameAsset<TilePaletteData> tilePaletteGameAsset = object;

		TilePaletteData paletteData = tilePaletteGameAsset.getResource();
		ObjectMap<UUID, GameAsset<?>> references = paletteData.references;

		UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;
		if (references.containsKey(gameAssetUUID)) {
			System.out.println("Adding a duplicate, ignoring");
			return;
		}

		PaletteEditorWorkspace workspace = paletteEditorWorkspace;
		Vector2 worldSpace = workspace.getWorldFromLocal(x, y);

		references.put(gameAssetUUID, gameAsset);

		tilePaletteGameAsset.dependentGameAssets.add(gameAsset);

		if (PaletteEditor.this.currentImportMode == PaletteImportMode.TILE) {
			if (gameAsset.type == GameAssetType.SPRITE) {

				final int lowestX = MathUtils.floor(worldSpace.x);
				final int lowestY = MathUtils.floor(worldSpace.y);
				addSprite(gameAsset, lowestX, lowestY);
			} else {
				System.out.println("Cannot add " + gameAsset.type + " in TILE mode");
			}
		} else { // PaletteEditor.this.currentFilterMode == PaletteFilterMode.ENTITY)
			GameObject gameObject = addEntity(gameAsset);
			if (gameObject != null) {
				TransformComponent component = gameObject.getComponent(TransformComponent.class);
				SceneEditorWorkspace.getInstance().initGizmos(gameObject, paletteEditorWorkspace);

				TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);

				// if the game object has size, place parent tiles under the covering range of the sprite and center the sprite
				if (gameObject.hasComponent(SpriteRendererComponent.class)) {
					final SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

					// update parent tiles
					final ObjectSet<GridPosition> parentTiles = new ObjectSet<>();

					final int lowestX = MathUtils.floor(worldSpace.x);
					final int lowestY = MathUtils.floor(worldSpace.y);
					final int highestX = (int)(lowestX + spriteRendererComponent.size.x);
					final int highestY = (int)(lowestY + spriteRendererComponent.size.y);

					for (int i = lowestX; i <= highestX; i++) {
						for (int j = lowestY; j <= highestY; j++) {
							final GridPosition gridPosition = new GridPosition(i, j);
							parentTiles.add(gridPosition);
						}
					}
					tileDataComponent.setParentTiles(parentTiles);

					// center the sprite
					final float xOffset = (highestX - lowestX + 1) / 2f;
					final float yOffset = (highestY - lowestY + 1) / 2f;
					component.position.set(xOffset, yOffset);
				} else {
					final GridPosition gridPos = new GridPosition(MathUtils.floor(worldSpace.x), MathUtils.floor(worldSpace.y));
					component.position.set(0, 0);
					tileDataComponent.getParentTiles().add(gridPos);
				}
			}
		}

		AssetRepository.getInstance().saveGameAssetResourceJsonToFile(tilePaletteGameAsset);
	}

	public void removeGameAsset (GameAsset<?> gameAsset) {
		GameAsset<TilePaletteData> tilePaletteGameAsset = object;

		TilePaletteData paletteData = tilePaletteGameAsset.getResource();
		// first remove from selections

		ObjectMap<UUID, GameAsset<?>> references = paletteData.references;

		UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;

		tilePaletteGameAsset.dependentGameAssets.removeValue(gameAsset, true);

		references.remove(gameAssetUUID);

		AssetRepository.getInstance().saveGameAssetResourceJsonToFile(tilePaletteGameAsset);
	}

	public void addSprite (GameAsset<?> gameAsset, float x, float y) {
		object.getResource().addSprite(gameAsset, new Vector2(x, y));

	}

	public GameObject addEntity (GameAsset<?> gameAsset) {
		return object.getResource().addEntity(gameAsset);
	}

	public void removeEntity (GameAsset<?> gameAsset) {
		object.getResource().removeEntity(gameAsset);
		removeGameAsset(gameAsset);
	}

	public static boolean validForImport (GameAsset<?> gameAsset) {
		boolean valid = (gameAsset.type == GameAssetType.SPRITE) || (gameAsset.type == GameAssetType.SKELETON) || (gameAsset.type == GameAssetType.VFX) || (gameAsset.type == GameAssetType.PREFAB);
		return valid;
	}

	private class PaletteDragAndDropTarget extends DragAndDrop.Target {

		public PaletteDragAndDropTarget (Actor actor) {
			super(actor);
		}

		@Override
		public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
			return payload.getObject() instanceof GameAsset<?> && validForImport((GameAsset<?>)payload.getObject());
		}

		@Override
		public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
			GameAsset<?> gameAsset = (GameAsset<?>)payload.getObject();
			addGameAsset(gameAsset, x, y);
		}
		// keep reference of what 'GameAsset' is selected. Our reference should be GameAsset type, but algorithms to select the entities/sprites can two combined
		// different approaches

		// also, when dropping sprite snap to grid
		// save after every edit event such as drop, remove or just move
		// draw mode such as brush and shit
		// main renderer
	}

	public GameAsset<TilePaletteData> getObject () {
		return object;
	}

	public PaletteEditor.PaletteEditMode getEditMode () {
		return currentEditMode;
	}

	public boolean isFreeTransformEditMode () {
		return currentEditMode == PaletteEditMode.FREE_TRANSFORM;
	}

	public boolean isFreeTranslateEditMode () {
		return currentEditMode == PaletteEditMode.FREE_TRANSLATE;
	}

	public boolean isParentTileAndFakeHeightEditMode () {
		return currentEditMode == PaletteEditor.PaletteEditMode.PARENT_TILE_AND_FAKE_HEIGHT;
	}

	private void addDefaultButtons () {
		Skin skin = TalosMain.Instance().getSkin();

		Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
		buttonStyle.up = null;
		buttonStyle.down = ColorLibrary.obtainBackground(skin, "square-bordered-selected", ColorLibrary.BackgroundColor.WHITE);
		buttonStyle.checked = ColorLibrary.obtainBackground(skin, "square-bordered-selected", ColorLibrary.BackgroundColor.WHITE);

		SquareButton select = new SquareButton(skin, skin.getDrawable("arrow-icon"), true,"Select mode");
		select.setStyle(buttonStyle);
		SquareButton editGizmo = new SquareButton(skin, skin.getDrawable("image-transform-icon"), true,"Gizmo Edit mode");
		editGizmo.setStyle(buttonStyle);
		SquareButton editTile = new SquareButton(skin, skin.getDrawable("add-remove-tile-icon"), true,"Tile Edit mode");
		editTile.setStyle(buttonStyle);
		SquareButton editLine = new SquareButton(skin, skin.getDrawable("set-line-icon"), true,"Line Edit mode");
		editLine.setStyle(buttonStyle);
		buttonMainMenu.clearChildren();
		buttonMainMenu.add(select).size(32);
		buttonMainMenu.add(editGizmo).size(32);
		buttonMainMenu.add(editTile).size(32);
		buttonMainMenu.add(editLine).size(32);

		// visual toggle
		select.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (select.isChecked()) {
					editGizmo.setChecked(false);
					editTile.setChecked(false);
					editLine.setChecked(false);
				} else if (!editGizmo.isChecked() && !editTile.isChecked() && !editLine.isChecked()){
					event.cancel();
				}
			}
		});
		editGizmo.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (editGizmo.isChecked()) {
					select.setChecked(false);
					editTile.setChecked(false);
					editLine.setChecked(false);
				} else if (!select.isChecked() && !editTile.isChecked() && !editLine.isChecked()){
					event.cancel();
				}
			}
		});
		editTile.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (editTile.isChecked()) {
					select.setChecked(false);
					editGizmo.setChecked(false);
					editLine.setChecked(false);
				} else if (!select.isChecked() && !editGizmo.isChecked() && !editLine.isChecked()){
					event.cancel();
				}
			}
		});
		editLine.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (editLine.isChecked()) {
					select.setChecked(false);
					editGizmo.setChecked(false);
					editTile.setChecked(false);
				} else if (!select.isChecked() && !editGizmo.isChecked() && !editTile.isChecked()){
					event.cancel();
				}
			}
		});

//		SquareButton entity = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"), "Entity mode");
//		SquareButton tileEntity = new SquareButton(skin, skin.getDrawable("combined_icon"), "TileEntity mode");
//		SquareButton delete = new SquareButton(skin, skin.getDrawable("eraser_icon"), "Eraser");
//		SquareButton editParentTileAndFakeHeight = new SquareButton(skin, skin.getDrawable("icon-edit"), "Edit entity");
//
//		tile.setDisabled(false);
//		entity.setDisabled(false);
//		tileEntity.setDisabled(false);
//		delete.setDisabled(false);
//		editParentTileAndFakeHeight.setDisabled(false);

		modeToggle.getTileBtn().addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton btn = (TextButton) actor;
				if (btn.isChecked()) {
					currentImportMode = PaletteImportMode.TILE;
				}
			}
		});

		modeToggle.getEntityBtn().addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton btn = (TextButton) actor;
				if (btn.isChecked()) {
					currentImportMode = PaletteImportMode.ENTITY;
				}
			}
		});

		// set default import mode
		modeToggle.getEntityBtn().toggle();

//		delete.addListener(new ClickListener() {
//			@Override
//			public void clicked (InputEvent event, float x, float y) {
//				super.clicked(event, x, y);
//				Array<GameAsset> markedForDeletion = new Array<>();
//				for (GameObject selectedGameObject : paletteEditorWorkspace.selection) {
//					for (ObjectMap.Entry<GameAsset<?>, GameObject> gameObject : object.getResource().gameObjects) {
//						if (gameObject.value == selectedGameObject) {
//							markedForDeletion.add(gameObject.key);
//						}
//					}
//				}
//				for (GameAsset gameAsset : markedForDeletion) {
//					removeEntity(gameAsset);
//				}
//				paletteEditorWorkspace.requestSelectionClear();
//				editParentTileAndFakeHeight.setDisabled(true);
//			}
//		});
//
//		// mode buttons should react on palette changes
//		defaultPaletteListener = new PaletteListener() {
//			@Override
//			public boolean selected (PaletteEvent e, GameAsset<?> gameAsset, PaletteImportMode mode) {
//				if (mode != PaletteImportMode.NONE) {
//					currentImportMode = mode;
//					tile.setChecked(false);
//					entity.setChecked(false);
//					tileEntity.setChecked(false);
//					switch (currentImportMode) {
//					case TILE:
//						tile.setChecked(true);
//						break;
//					case ENTITY:
//						entity.setChecked(true);
//						break;
//					case TILE_ENTITY:
//						tileEntity.setChecked(true);
//						break;
//					}
//				}
//
//				editParentTileAndFakeHeight.setDisabled(false);
//
//				return false;
//			}
//
//			public boolean lostFocus (PaletteEvent e) {
//				editParentTileAndFakeHeight.setDisabled(true);
//				if (isFreeTransformEditMode())
//					endFreeTransformEditMode();
//				if (isFreeTranslateEditMode())
//					endFreeTranslateEditMode();
//				return super.lostFocus(e);
//			}
//		};
//		paletteEditorWorkspace.addListener(defaultPaletteListener);
//
//		editParentTileAndFakeHeight.addListener(new ClickListener() {
//			@Override
//			public void clicked (InputEvent event, float x, float y) {
//				super.clicked(event, x, y);
//				if (!editParentTileAndFakeHeight.isDisabled()) {
//					paletteEditorWorkspace.removeListener(defaultPaletteListener);
//					startParentTileAndFakeHeightEditMode();
//				}
//			}
//		});
//
//		if (paletteEditorWorkspace.selection.isEmpty()) {
//			editParentTileAndFakeHeight.setDisabled(true);
//		}
//
//		ButtonGroup<SquareButton> buttonButtonGroup = new ButtonGroup<>();
//		buttonButtonGroup.add(tile, entity, tileEntity);
//		buttonButtonGroup.setMaxCheckCount(1);
//		buttonButtonGroup.setMinCheckCount(1);
//
//		tileEntity.setChecked(true);
//
//		buttonMainMenu.clear();
//		buttonMainMenu.add(tileEntity);
//		buttonMainMenu.add(tile);
//		buttonMainMenu.add(entity);
//		buttonMainMenu.add().expandX();
//		buttonMainMenu.add(editParentTileAndFakeHeight);
//		buttonMainMenu.add(delete);
	}

	void startFreeTranslateEditMode () {
		currentEditMode = PaletteEditMode.FREE_TRANSLATE;
	}

	void endFreeTranslateEditMode () {
		currentEditMode = PaletteEditMode.NONE;
		// extra code for exiting the mode
	}

	void startFreeTransformEditMode () {
		currentEditMode = PaletteEditMode.FREE_TRANSFORM;
		paletteEditorWorkspace.unlockGizmos();
	}

//	void endFreeTransformEditMode () {
//		currentEditMode = PaletteEditMode.NONE;
//		paletteEditorWorkspace.lockGizmos();
//		// extra code for exiting mode
//	}

//	void startParentTileAndFakeHeightEditMode () {
//		currentEditMode = PaletteEditMode.PARENT_TILE_AND_FAKE_HEIGHT;
//		addParentTileAndFakeHeightEditButtons();
//		paletteEditorWorkspace.startEditMode();
//	}

//	void endParentTileAndFakeHeightEditMode () {
//		currentEditMode = PaletteEditMode.NONE;
//		addDefaultButtons();
//	}

//	private void addParentTileAndFakeHeightEditButtons () {
//		Skin skin = TalosMain.Instance().getSkin();
//
//		SquareButton cancel = new SquareButton(skin, skin.getDrawable("ic-proc-error"), "Cancel");
//		SquareButton accept = new SquareButton(skin, skin.getDrawable("ic-proc-success"), "Accept");
//
//		cancel.addListener(new ClickListener() {
//			@Override
//			public void clicked (InputEvent event, float x, float y) {
//				super.clicked(event, x, y);
//				endParentTileAndFakeHeightEditMode();
//				paletteEditorWorkspace.revertEditChanges();
//			}
//		});
//
//		accept.addListener(new ClickListener() {
//			@Override
//			public void clicked (InputEvent event, float x, float y) {
//				super.clicked(event, x, y);
//				float tmpHeightOffset = paletteEditorWorkspace.getTmpHeightOffset();
//				GameObject gameObject = paletteEditorWorkspace.getSelectedGameObject();
//				TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
//				TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
//				tileDataComponent.setFakeZ(tmpHeightOffset - (tileDataComponent.getBottomLeftParentTile().y + transformComponent.position.y));
//
//				AssetRepository.getInstance().saveGameAssetResourceJsonToFile(object);
//
//				endParentTileAndFakeHeightEditMode();
//			}
//		});
//
//		ButtonGroup<SquareButton> buttonButtonGroup = new ButtonGroup<>();
//		buttonButtonGroup.add(cancel, accept);
//		buttonButtonGroup.setMaxCheckCount(1);
//		buttonButtonGroup.setMinCheckCount(1);
//
//		buttonMainMenu.clear();
//		buttonMainMenu.add(cancel);
//		buttonMainMenu.add(accept);
//	}
}
