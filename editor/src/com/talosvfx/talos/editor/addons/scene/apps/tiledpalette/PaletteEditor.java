package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedSet;
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

import java.util.UUID;

public class PaletteEditor extends AEditorApp<GameAsset<TilePaletteData>> {
	private String title;
	private DragAndDrop.Target target;
	private ModeToggle modeToggle;
	private PaletteEditorWorkspace paletteEditorWorkspace;

	enum PaletteImportMode {
		NONE,
		TILE,
		ENTITY
	}

	protected PaletteImportMode currentImportMode = PaletteImportMode.NONE;

	protected PaletteEditMode currentEditMode = PaletteEditMode.FREE_TRANSLATE;

	private Toolbar toolbar;

	enum PaletteEditMode {
		FREE_TRANSLATE, // translation of the parent tile + translation of transform
		FREE_TRANSFORM, // free transform of transform component with gizmo
		PARENT_TILE_EDIT,
		FAKE_HEIGHT_EDIT
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
		content = new Table();

		paletteEditorWorkspace = new PaletteEditorWorkspace(this);
		modeToggle = new ModeToggle();

		this.content.add(paletteEditorWorkspace).minSize(336, 696).grow().row();
		this.content.add(modeToggle).growX().height(32);

		Table toolbarContainer = new Table();
		toolbarContainer.setFillParent(true);
		toolbarContainer.top();
		this.content.addActor(toolbarContainer);

		toolbar = new Toolbar(this);
		toolbarContainer.add(toolbar).expandX().padTop(RulerRenderer.RULER_SIZE + 8);

		addDefaultButtons();

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

	public boolean isParentTileEditMode () {
		return currentEditMode == PaletteEditMode.PARENT_TILE_EDIT;
	}

	public boolean isFakeHeightEditMode () {
		return currentEditMode == PaletteEditMode.FAKE_HEIGHT_EDIT;
	}

	private void addDefaultButtons () {
		// visual toggle


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

//		 set default import mode
		modeToggle.getEntityBtn().toggle();

		paletteEditorWorkspace.addListener(new PaletteListener() {
			@Override
			public boolean lostFocus (PaletteEvent e) {
				toolbar.translate.toggle();
				return super.lostFocus(e);
			}

			@Override
			public void startTranslate(PaletteEvent e) {
				toolbar.translate.toggle();
				return;
			}

			@Override
			public void startGizmoEdit(PaletteEvent e) {
				toolbar.editGizmo.toggle();
				return;
			}
		});
	}

	public OrderedSet<GameObject> getSelection () {
		return paletteEditorWorkspace.selection;
	}

	public void unlockGizmos() {
		paletteEditorWorkspace.unlockGizmos();
	}

	public void lockGizmos() {
		paletteEditorWorkspace.lockGizmos();
	}

	public void startFakeHeightEditMode() {
		paletteEditorWorkspace.startFakeHeightEditMode();
	}
}
