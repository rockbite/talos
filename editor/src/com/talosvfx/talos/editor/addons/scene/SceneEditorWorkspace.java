package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.esotericsoftware.spine.SkeletonData;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.events.scene.AddToSelectionEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.RemoveFromSelectionEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.RequestSelectionClearEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.SelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.logic.*;
import com.talosvfx.talos.editor.addons.scene.logic.components.*;
import com.talosvfx.talos.editor.addons.scene.maps.LayerType;
import com.talosvfx.talos.editor.addons.scene.maps.MapEditorState;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.utils.PolygonSpriteBatchMultiTexture;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.FileWatching;
import com.talosvfx.talos.editor.addons.scene.widgets.*;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.assets.GameAssetOpenEvent;
import com.talosvfx.talos.editor.project2.GlobalDragAndDrop;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.projectdata.SceneData;
import com.talosvfx.talos.editor.utils.NamingUtils;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import com.talosvfx.talos.editor.utils.grid.property_providers.StaticBoundedGridPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.editor.widgets.ui.gizmos.GroupSelectionGizmo;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.fromDirectoryView;
import static com.talosvfx.talos.editor.addons.scene.widgets.gizmos.SmartTransformGizmo.getLatestFreeOrderingIndex;
import static com.talosvfx.talos.editor.utils.InputUtils.ctrlPressed;

public class SceneEditorWorkspace extends ViewportWidget implements Json.Serializable, Observer {

	private static final Logger logger = LoggerFactory.getLogger(SceneEditorWorkspace.class);
	public final TemplateListPopup templateListPopup;

	private String projectPath;

	private SavableContainer currentContainer;
	private GameAsset<Scene> gameAsset;

	private MainRenderer renderer;
	private final MainRenderer uiSceneRenderer;

	private String changeVersion = "";
	private SnapshotService snapshotService;

	private AssetListPopup assetListPopup;
	private GameObjectListPopup gameObjectListPopup;

	private FileTracker fileTracker = new FileTracker();
	private FileWatching fileWatching = new FileWatching();
	private float reloadScheduled = -1;

	public MapEditorState mapEditorState;
	public MapEditorToolbar mapEditorToolbar;

	public boolean exporting = false;


	private float sprayInnerRadius = 10;
	private float sprayOuterRadius = 15;
	private int innerSprayCount = 100;
	private int outerSprayCount = 100;
	private Random rand;

	//for map
	private StaticBoundedGridPropertyProvider staticGridPropertyProvider;

	public static boolean isEnterPressed (int keycode) {
		switch (keycode) {
			case Input.Keys.ENTER:
			case Input.Keys.NUMPAD_ENTER:
				return true;
			default:
				return false;
		}
	}

	public MainRenderer getUISceneRenderer () {
		return uiSceneRenderer;
	}

	public GameObject getGOWith (AComponent component) {
		return getChildHavingComponent(getRootGO(), component);
	}

	public GameObject getChildHavingComponent (GameObject root, AComponent component) {
		if (root.hasComponent(component.getClass())) {
			AComponent aComponent = root.getComponent(component.getClass());
			if (aComponent == component) {
				return root;
			}
		}

		Array<GameObject> children = root.getGameObjects();

		if (children == null) {
			return null;
		}

		for (int i = 0; i < children.size; i++) {
			GameObject child = children.get(i);
			GameObject childHavingComponent = getChildHavingComponent(child, component);
			if (childHavingComponent != null) {
				return childHavingComponent;
			}
		}

		return null;
	}

	// selections
	private Image selectionRect;

	public SceneEditorWorkspace () {

		setSkin(SharedResources.skin);
		setWorldSize(10);
		mapEditorToolbar = new MapEditorToolbar(SharedResources.skin);

		snapshotService = new SnapshotService();
		mapEditorState = new MapEditorState();

		Notifications.registerObserver(this);


		GizmoRegister.init(SharedResources.configData.getGameObjectConfigurationXMLRoot());

		assetListPopup = new AssetListPopup<>();
		gameObjectListPopup = new GameObjectListPopup();
		templateListPopup = new TemplateListPopup(SharedResources.configData.getGameObjectConfigurationXMLRoot());
		templateListPopup.setListener(new TemplateListPopup.ListListener() {
			@Override
			public void chosen (XmlReader.Element template, float x, float y) {
				Vector2 pos = new Vector2(x, y);
				String templateName = template.getAttribute("name");
				SceneUtils.createObjectByTypeName(currentContainer, templateName, pos, null, templateName);
			}
		});

		initListeners();

		renderer = new MainRenderer();
		uiSceneRenderer = new MainRenderer();

		Skin skin = SharedResources.skin;
		selectionRect = new Image(skin.getDrawable("orange_row"));
		selectionRect.setSize(0, 0);
		selectionRect.setVisible(false);
		addActor(selectionRect);

		addActor(rulerRenderer);

		rand = new Random();

		SharedResources.globalDragAndDrop.addTarget(new DragAndDrop.Target(SceneEditorWorkspace.this) {
			@Override
			public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				if (currentContainer == null) return false;

				GlobalDragAndDrop.BaseDragAndDropPayload object = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();

				if (object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
					//We support single game asset drops

					return true;
				}

				return false;
			}

			@Override
			public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				GlobalDragAndDrop.BaseDragAndDropPayload object = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();

				if (object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
					//We support single game asset drops
					GlobalDragAndDrop.GameAssetDragAndDropPayload gameAssetPayload = (GlobalDragAndDrop.GameAssetDragAndDropPayload)object;
					if (gameAssetPayload.getGameAsset().type == GameAssetType.SPRITE) {
						GameAsset<Texture> gameAsset = (GameAsset<Texture>)gameAssetPayload.getGameAsset();

						Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
						Vector3 touchToWorld = getTouchToWorld(vec.x, vec.y);
						vec.set(touchToWorld.x, touchToWorld.y);

						SceneUtils.createSpriteObject(currentContainer, gameAsset, vec, currentContainer.getSelfObject());

					}
					return;
				}
				logger.info("TODO other implementations of drag drop payloads");

			}
		});
	}




	protected void initListeners () {
		inputListener = new InputListener() {

			Vector2 vec = new Vector2();

			// selection stuff
			boolean dragged = false;
			Vector2 startPos = new Vector2();
			Rectangle rectangle = new Rectangle();
			boolean upWillClear = true;

			GameObject selectedGameObject;

			private boolean painting = false;
			private boolean spraying = false;
			private boolean erasing = false;

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

				if (mapEditorState.isEditing()) {
					if (mapEditorState.isPainting()) {
						//Place a tile and return
						paintTileAt(x, y);
						painting = true;
						return true;
					} else if (mapEditorState.isSpraying()) {
						// Spray tiles and return
						sprayTilesAt();
						spraying = true;
						return true;
					} else if (mapEditorState.isErasing()) {
						TalosLayer layerSelected = mapEditorState.getLayerSelected();
						if (layerSelected != null) {
							if (layerSelected.getType() == LayerType.STATIC) {
								eraseTileAt(x, y);
							} else {
								eraseEntityAt(x, y);
							}
						}
						erasing = true;
						return true;
					}

					return super.touchDown(event, x, y, pointer, button);
				}

				upWillClear = true;
				dragged = false;

				Vector2 hitCords = getWorldFromLocal(x, y);

				if (button == 1 && !event.isCancelled()) {
					final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
					screenToLocalCoordinates(vec);
					localToStageCoordinates(vec);

					Vector2 location = new Vector2(vec);
					Vector2 createLocation = new Vector2(hitCords);
					templateListPopup.showPopup(getStage(), location, createLocation);

					return true;
				}

				if (button == 2 || ctrlPressed()) {
					selectionRect.setVisible(true);
					selectionRect.setSize(0, 0);
					selectionRect.setPosition(x, y);
					startPos.set(x, y);

					return true;
				}


				return false;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				super.touchDragged(event, x, y, pointer);

				if (mapEditorState.isEditing()) {
					if (mapEditorState.isPainting()) {

						//Check to see if we are in static tile first

						if (mapEditorState.getLayerSelected() != null) {
							if (mapEditorState.getLayerSelected().getType() == LayerType.STATIC) {
								//Place a tile and return
								paintTileAt(x, y);
							}
						}
						return;

					} else if (mapEditorState.isPainting()) {
						if (mapEditorState.getLayerSelected() != null) {
							if (mapEditorState.getLayerSelected().getType() == LayerType.STATIC) {
								//Place a tile and return
								eraseTileAt(x, y);
							} else {
//								eraseEntityAt(x, y);
							}
						}
						return;
					}

					return;
				}

				dragged = true;


				if (selectionRect.isVisible()) {
					vec.set(x, y);
					vec.sub(startPos);
					if (vec.x < 0) {
						rectangle.setX(x);
					} else {
						rectangle.setX(startPos.x);
					}
					if (vec.y < 0) {
						rectangle.setY(y);
					} else {
						rectangle.setY(startPos.y);
					}
					rectangle.setWidth(Math.abs(vec.x));
					rectangle.setHeight(Math.abs(vec.y));

					selectionRect.setPosition(rectangle.x, rectangle.y);
					selectionRect.setSize(rectangle.getWidth(), rectangle.getHeight());
				}
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				Vector2 hitCords = getWorldFromLocal(x, y);

				Gizmo gizmo = hitGizmo(hitCords.x, hitCords.y);

				if (painting) {
					painting = false;
					return;
				}
				if (spraying) {
					spraying = false;
					return;
				}
				if (erasing) {
					erasing = false;
					return;
				}


				if (selectionRect.isVisible()) {
					upWillClear = false;
					selectGizmosByRect(rectangle);
				} else if (upWillClear) {
					FocusManager.resetFocus(getStage());
					requestSelectionClear();
				} else {
					if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
						// deselect all others, if they are selected
						deselectOthers(selectedGameObject);
					}
				}


				selectionRect.setVisible(false);
			}

			@Override
			public boolean keyDown (InputEvent event, int keycode) {

				if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
					ObjectSet<GameObject> deleteList = new ObjectSet<>();
					deleteList.addAll(selection);
					requestSelectionClear();
					deleteGameObjects(deleteList);
				}

				if (keycode == Input.Keys.C && ctrlPressed()) {
					copySelected();
				}

				if (keycode == Input.Keys.V && ctrlPressed()) {
					pasteFromClipboard();
				}

				if (keycode == Input.Keys.A && ctrlPressed()) {
					selectAll();
				}

				if (keycode == Input.Keys.G && ctrlPressed()) {
					convertSelectedIntoGroup();
				}

				if (keycode == Input.Keys.ESCAPE) {
					escapePressed();
				}

				return super.keyDown(event, keycode);
			}
		};

		addListener(inputListener);
	}

	@EventHandler
	public void selectExternal (SelectGameObjectExternallyEvent event) {
		selectGameObjectExternally(event.getGameObject());
	}

	@EventHandler
	public void addToSelectionEvent (AddToSelectionEvent addToSelectionEvent) {
		addToSelection(addToSelectionEvent.getGameObject());
	}

	@EventHandler
	public void removeFromSelectionEvent (RemoveFromSelectionEvent removeFromSelectionEvent) {
		removeFromSelection(removeFromSelectionEvent.getGameObject());
	}

	private void escapePressed() {
		performSelectionClear();
		mapEditorState.escapePressed();
	}

	private void convertSelectedIntoGroup () {
		if (selection.isEmpty() || selection.size == 1) {
			return;
		}

		Array<GameObject> selectedObjects = new Array<>();
		selectedObjects.addAll(selection.orderedItems());

		GameObject rootGO = getRootGO();
		GameObject topestLevelObjectsParentFor = getTopestLevelObjectsParentFor(rootGO, selectedObjects);


		GameObject dummyParent = SceneUtils.createEmpty(currentContainer, new Vector2(groupSelectionGizmo.getCenterX(), groupSelectionGizmo.getCenterY()), topestLevelObjectsParentFor);

		// This is being done in the next frame because relative positioning is calculated based on render position of the objects
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {

				logger.info("Redo reposition game object and restructure on group");
//				for (GameObject gameObject : selectedObjects) {
//					SceneEditorAddon.get().workspace.repositionGameObject(dummyParent, gameObject);
//				}
//
//				SceneEditorAddon.get().hierarchy.restructureGameObjects(selectedObjects);
//
//				selectGameObjectExternally(dummyParent);
			}
		});
	}

	public GameObject getGameObjectForUUID (String uuid) {
		GameObject rootGO = getRootGO();
		if (rootGO == null) {
			return null;
		}

		if (rootGO.uuid.toString().equals(uuid)) {
			return rootGO;
		}

		return rootGO.getChildByUUID(uuid);
	}

	private GameObject getTopestLevelObjectsParentFor (GameObject gameObject, Array<GameObject> gameObjects) {
		Array<GameObject> childGameObjects = gameObject.getGameObjects();
		if (childGameObjects == null) {
			return null;
		}

		for (GameObject object : gameObjects) {
			if (childGameObjects.contains(object, true)) {
				return gameObject;
			}
		}

		for (GameObject object : childGameObjects) {
			GameObject topestLevelObjectsParentFor = getTopestLevelObjectsParentFor(object, gameObjects);
			if (topestLevelObjectsParentFor != null) {
				return topestLevelObjectsParentFor;
			}
		}

		return null;
	}

	private void eraseTileAt (float x, float y) {
		if (mapEditorState.isErasing()) {
			int mouseCellX = gridRenderer.getMouseCellX();
			int mouseCellY = gridRenderer.getMouseCellY();
			//Targets
			TalosLayer layerSelected = mapEditorState.getLayerSelected();
			if (layerSelected != null) {
				layerSelected.removeTile(mouseCellX, mouseCellY);
			}

		}
	}

	private void eraseEntityAt (float x, float y) {
		Vector2 worldFromLocal = getWorldFromLocal(x, y);
		TalosLayer layerSelected = mapEditorState.getLayerSelected();
		if (layerSelected != null) {
			if (entityUnderMouse != null) {
				layerSelected.removeEntity(entityUnderMouse);
			}
		}
	}

	private void paintTileAt (float x, float y) {

		if (mapEditorState.isPainting()) {
			TalosLayer layerSelected = mapEditorState.getLayerSelected();
			if (layerSelected != null) {
				GameAsset<TilePaletteData> gameResource = layerSelected.getGameResource();
				if (gameResource.isBroken()) {
					return;
				}

				//Need to redo this to support tile selection. For now we can check speficailyl what we are painting
				LayerType type = layerSelected.getType();

				GameObject gameObjectWeArePainting = mapEditorState.getGameObjectWeArePainting();
				if (gameObjectWeArePainting != null) {
					if (type == LayerType.DYNAMIC_ENTITY) {
						GameObject gameObject = AssetRepository.getInstance().copyGameObject(gameObjectWeArePainting);
						Gizmo.TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();
						TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
						tileDataComponent.getVisualOffset().set(transformSettings.transformOffsetX, transformSettings.transformOffsetY);
						layerSelected.getRootEntities().add(gameObject);
						gameObject.isPlacing = false;
					} else {
						System.out.println("Can't paint entity into static layer");
					}
				}

//				Array<GameAsset<?>> selectedGameAssets = palette.selectedGameAssets;

//				if (selectedGameAssets.size > 1) {
//					System.out.println("Multi stamp not supported yet");
//				} else if (selectedGameAssets.size == 1) {
//					GameAsset<?> gameAssetToPaint = selectedGameAssets.first();
//
//					//Paint it into the layer
//					if (type == LayerType.STATIC) {
//						if (gameAssetToPaint.type != GameAssetType.SPRITE) {
//							System.out.println("Trying to paint a non sprite into a static layer");
//							return;
//						}
//
//						StaticTile staticTile = new StaticTile(gameAssetToPaint, new GridPosition(mouseCellX, mouseCellY));
//						layerSelected.setStaticTile(staticTile);
//
//					} else {
//						//Always do it like entities
//
//						AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
//						GameObject tempParent = new GameObject();
//						boolean success = AssetImporter.createAssetInstance(gameAssetToPaint, tempParent);
//						if (tempParent.getGameObjects() == null || tempParent.getGameObjects().size == 0) {
//							success = false;
//						}
//						AssetImporter.fromDirectoryView = false;
//
//						if (success) {
//							//We can add this to layer entities
//							layerSelected.getRootEntities().add(tempParent.getGameObjects().first());
//						}
//
//					}
//				}
			}

		}
	}

	private void sprayTilesAt () {

		if (mapEditorState.isSpraying()) {
			TalosLayer layerSelected = mapEditorState.getLayerSelected();
			if (layerSelected != null) {
				Vector2 origin = getMouseCordsOnScene();
				LayerType type = layerSelected.getType();

				GameObject gameObjectWeArePainting = mapEditorState.getGameObjectWeArePainting();
				if (gameObjectWeArePainting != null) {
					if (type == LayerType.DYNAMIC_ENTITY) {
						double innerRadius = sprayInnerRadius;
						double outerRadius = sprayOuterRadius;
						double twopi = 2 * Math.PI;
						// draw inner circle
						for (int i = 1; i <= innerSprayCount; i++) {
							double theta = twopi * rand.nextDouble();
							double r = innerRadius * Math.sqrt(rand.nextDouble());
							double x = r * Math.cos(theta);
							double y = r * Math.sin(theta);

							TransformComponent transformComponent = gameObjectWeArePainting.getComponent(TransformComponent.class);
							transformComponent.position.set((float) x + origin.x, (float) y + origin.y);

							GameObject gameObject = AssetRepository.getInstance().copyGameObject(gameObjectWeArePainting);
							Gizmo.TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();
							TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
							tileDataComponent.getVisualOffset().set(transformSettings.transformOffsetX, transformSettings.transformOffsetY);
							layerSelected.getRootEntities().add(gameObject);
						}
						// draw outer circle
						for (int i = 1; i <= outerSprayCount; i++) {
							double theta = twopi * rand.nextDouble();
							double r = (outerRadius - innerRadius) * Math.sqrt(Math.abs(rand.nextGaussian())) + innerRadius;
							double x = r * Math.cos(theta);
							double y = r * Math.sin(theta);

							TransformComponent transformComponent = gameObjectWeArePainting.getComponent(TransformComponent.class);
							transformComponent.position.set((float) x + origin.x, (float) y + origin.y);

							GameObject gameObject = AssetRepository.getInstance().copyGameObject(gameObjectWeArePainting);
							Gizmo.TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();
							TileDataComponent tileDataComponent = gameObject.getComponent(TileDataComponent.class);
							tileDataComponent.getVisualOffset().set(transformSettings.transformOffsetX, transformSettings.transformOffsetY);
							layerSelected.getRootEntities().add(gameObject);
						}
					} else {
						System.out.println("Can't paint entity into static layer");
					}
				}
			}
		}
	}


	public static boolean isRenamePressed (int keycode) {
		if (TalosMain.Instance().isOsX()) {
			return isEnterPressed(keycode);
		} else {
			return keycode == Input.Keys.F2;
		}
	}



	public void openPrefab (FileHandle fileHandle) {
		if (currentContainer != null) {
			currentContainer.save();
		}
		Prefab scene = new Prefab();
		scene.path = fileHandle.path();
		scene.loadFromPath();
		openSavableContainer(scene);
		TalosMain.Instance().UIStage().saveProjectAction();
	}

	public void openScene (FileHandle fileHandle) {
		if (currentContainer != null) {
			currentContainer.save();
		}
		Scene scene = new Scene();
		scene.path = fileHandle.path();
		scene.loadFromPath();
		openSavableContainer(scene);
		TalosMain.Instance().UIStage().saveProjectAction();
	}

	public void convertToPrefab (GameObject gameObject) {
		String name = gameObject.getName();

		String path = getProjectPath() + File.separator + "assets";

		logger.info("Redo convert to prefab");
//		if (SceneEditorAddon.get().projectExplorer.getCurrentFolder() != null) {
//			path = SceneEditorAddon.get().projectExplorer.getCurrentFolder().path();
//		}

		FileHandle handle = AssetImporter.suggestNewNameForFileHandle(path, name, "prefab");
		if (handle != null) {
			GameObject gamePrefab = new GameObject();
			gamePrefab.setName("Prefab");

			gamePrefab.addGameObject(gameObject);

			Prefab prefab = new Prefab();
			prefab.path = handle.path();
			prefab.root = gamePrefab;
			prefab.save();
			AssetRepository.getInstance().rawAssetCreated(handle, true);
//			SceneEditorAddon.get().projectExplorer.reload();
		}
	}

	public static class ClipboardPayload {
		public Array<GameObject> objects = new Array<>();
		public Array<Vector2> objectWorldPositions = new Array<>();
		public Vector2 cameraPositionAtCopy = new Vector2(0, 0);
	}

	public void copySelected () {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		ClipboardPayload payload = new ClipboardPayload();
		Array<GameObject> gameObjects = selection.orderedItems();
		for (int i = 0; i < selection.size; i++) {
			GameObject value = gameObjects.get(i);
			payload.objects.add(value);
			if (value.hasComponentType(TransformComponent.class)) {
				payload.objectWorldPositions.add(value.getComponent(TransformComponent.class).worldPosition);
			} else {
				payload.objectWorldPositions.add(new Vector2());
			}
		}
		Vector3 camPos = camera.position;
		payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

		Json json = new Json();
		String clipboard = json.toJson(payload);
		Gdx.app.getClipboard().setContents(clipboard);
	}

	public void pasteFromClipboard () {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		String clipboard = Gdx.app.getClipboard().getContents();

		Json json = new Json();

		try {
			ClipboardPayload payload = json.fromJson(ClipboardPayload.class, clipboard);
			Vector3 camPosAtPaste = camera.position;
			Vector2 offset = new Vector2(camPosAtPaste.x, camPosAtPaste.y);
			offset.sub(payload.cameraPositionAtCopy);
			GameObject parent = currentContainer.root;
			if (selection.size == 1 && selection.first() != currentContainer.root) {
				parent = selection.first().parent;
			}

			clearSelection();
			for (int i = 0; i < payload.objects.size; i++) {
				GameObject gameObject = payload.objects.get(i);

				String name = NamingUtils.getNewName(gameObject.getName(), currentContainer.getAllGONames());

				gameObject.setName(name);
				SceneUtils.randomizeChildrenUUID(gameObject);
				parent.addGameObject(gameObject);
				if (gameObject.hasComponentType(TransformComponent.class)) {
					TransformComponent component = gameObject.getComponent(TransformComponent.class);
					component.worldPosition.set(payload.objectWorldPositions.get(i));
					GameObject.projectInParentSpace(parent, gameObject);
					component.position.add(offset);
				}
				initGizmos(gameObject, this);
				Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));
				addToSelection(gameObject);
			}

			AssetRepository.getInstance().saveGameAssetResourceJsonToFile(gameAsset, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write (Json json) {

	}

	@Override
	public void read (Json json, JsonValue jsonData) {

	}

	@Override
	public void act (float delta) {
		super.act(delta);

		if (mapEditorState.isEditing()) {
			boolean painting = mapEditorState.isPainting();
			boolean spraying = mapEditorState.isSpraying();
			if (painting) {
				if (mapEditorState.getLayerSelected() != null) {

					GameObject gameObjectWeArePainting = mapEditorState.getGameObjectWeArePainting();

					if (gameObjectWeArePainting != null) {

						//We need to place this at the cursor position, snap with shift
						Vector3 touchToLocal = getTouchToWorld(Gdx.input.getX(), Gdx.input.getY());

						TransformComponent transformComponent = gameObjectWeArePainting.getComponent(TransformComponent.class);

						if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {

							float gridSizeX = 1;
							float gridSizeY = 1;
							if (mapEditorState.getLayerSelected() != null) {
								gridSizeX = mapEditorState.getLayerSelected().getTileSizeX();
								gridSizeY = mapEditorState.getLayerSelected().getTileSizeY();
							}

							Gizmo.TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();

							float transformOffsetModX = transformSettings.transformOffsetX % gridSizeX;
							float transformOffsetModY = transformSettings.transformOffsetY % gridSizeY;

							touchToLocal.x /= gridSizeX;
							touchToLocal.x = MathUtils.floor(touchToLocal.x);
							touchToLocal.x *= gridSizeX;

							touchToLocal.y /= gridSizeY;
							touchToLocal.y = MathUtils.floor(touchToLocal.y);
							touchToLocal.y *= gridSizeY;

							transformComponent.position.set(touchToLocal.x + transformOffsetModX, touchToLocal.y + transformOffsetModY);

						} else {
							transformComponent.position.set(touchToLocal.x, touchToLocal.y);
						}
					}
				}

			} else if (spraying) {
//				do nothing yet
			}
		}

		if (reloadScheduled > 0) {
			reloadScheduled -= delta;
			if (reloadScheduled <= 0) {
				reloadScheduled = -1;
			}
		}
	}

	@Override
	public void drawContent (PolygonBatch batch, float parentAlpha) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		batch.end();

		((DynamicGridPropertyProvider) gridPropertyProvider).distanceThatLinesShouldBe = pixelToWorld(150);
		if (mapEditorState.isEditing()) {
			staticGridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
			staticGridPropertyProvider.setHighlightCursorHover(true);
			if (camera instanceof OrthographicCamera) {
				staticGridPropertyProvider.update((OrthographicCamera)camera, parentAlpha);
			}
			gridRenderer.setGridPropertyProvider(staticGridPropertyProvider);
			rulerRenderer.setGridPropertyProvider(staticGridPropertyProvider);
			if (viewportViewSettings.isShowGrid()) {
				gridRenderer.drawGrid(batch, shapeRenderer);
			}
			renderer.setRenderParentTiles(false);

			if (mapEditorState.isSpraying()) {
				// show the spray radius
				Gdx.gl.glLineWidth(5.0f);
				shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
				Vector2 vec = getMouseCordsOnScene();
				shapeRenderer.circle(vec.x, vec.y, sprayInnerRadius, 20);
				shapeRenderer.circle(vec.x, vec.y, sprayOuterRadius, 20);
				shapeRenderer.end();
				Gdx.gl.glLineWidth(1.0f);
			}
		} else {
			gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
			if (camera instanceof OrthographicCamera) {
				gridPropertyProvider.update((OrthographicCamera)camera, parentAlpha);
			}
			gridRenderer.setGridPropertyProvider(gridPropertyProvider);
			rulerRenderer.setGridPropertyProvider(gridPropertyProvider);
			if (viewportViewSettings.isShowGrid() && !viewportViewSettings.is3D()) {
				gridRenderer.drawGrid(batch, shapeRenderer);
			}
			renderer.setRenderParentTiles(false);
		}


		if (viewportViewSettings.isShowAxis()) {
			drawAxis();
		}

		batch.begin();

		renderer.setCamera(camera);
		drawMainRenderer(batch, parentAlpha);

		batch.end();

		beginEntitySelectionBuffer();
		drawEntitiesForSelection();
		endEntitySelectionBuffer();

		batch.begin();

	}

	private void drawMainRenderer (PolygonBatch batch, float parentAlpha) {
		if (currentContainer == null)
			return;

		renderer.setLayers(getLayerList());
		renderer.update(currentContainer.getSelfObject());
		renderer.render(batch, new MainRenderer.RenderState(), currentContainer.getSelfObject());
	}


	public void cleanWorkspace () {

	}

	public String writeExport () {
		exporting = true;
		AssetRepository.getInstance().exportToFile();

		// write rest of files
		String exportType = TalosMain.Instance().Prefs().getString("exportType", "Default");
		if (exportType.equals("Default")) {
			// default behaviour
		} else if (exportType.equals("Custom Script")) {
			String sceneEditorExportScriptPath = TalosMain.Instance().Prefs().getString("sceneEditorExportScriptPath", null);
			if (sceneEditorExportScriptPath != null) {
				FileHandle handle = Gdx.files.absolute(sceneEditorExportScriptPath);

				if (!handle.exists()) {
					handle = Gdx.files.absolute(projectPath + File.separator + sceneEditorExportScriptPath);
				}

				if (handle.exists() && !handle.isDirectory()) {
					Runtime rt = Runtime.getRuntime();

					try {
						String nodeCommand = "node";
						String buildScriptPath = handle.path();
						String projectDirectoryPath = "\"" + projectPath  + "\"";
						String projectFilePath = "\"" + TalosMain.Instance().ProjectController().getExportPath() + "\"";

						if (TalosMain.Instance().isOsX()) {
							File nodeBinary = new File(nodeCommand);
							if(!nodeBinary.exists()) {
								nodeCommand = "/opt/homebrew/bin/node";
							}
							ProcessBuilder pb = new ProcessBuilder("bash", "-l", "-c", nodeCommand + " " + buildScriptPath + " " + projectDirectoryPath + " " + projectFilePath);
							pb.inheritIO();
							pb.start();
						} else {
							ProcessBuilder pb = new ProcessBuilder(nodeCommand, buildScriptPath, projectDirectoryPath, projectFilePath);
							pb.inheritIO();
							pb.start();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		exporting = false;

		return "";
	}

	public void setProjectPath (String path) {
		projectPath = path;
	}

	// if asset is updated externally, do something about it maybe
	public void updateAsset (FileHandle handle) {

	}


	public void openSavableContainer (SavableContainer mainScene) {
		if (mainScene == null)
			return;
//		sceneEditorAddon.hierarchy.loadEntityContainer(mainScene);
		currentContainer = mainScene;

		// process all game objects
		gizmos.gizmoList.clear();
		gizmos.gizmoMap.clear();
		gizmos.gizmoList.add(groupSelectionGizmo);
		initGizmos(mainScene, this);

		clearSelection();

		selectPropertyHolder(mainScene);

		if (mainScene instanceof Scene) {
			gridPropertyProvider.getBackgroundColor().set(Color.valueOf("#272727"));
			//todo redo
//			updateSettingsFromSceneSettings();
		} else {
			gridPropertyProvider.getBackgroundColor().set(Color.valueOf("#241a00"));
		}
	}

	public void selectPropertyHolder (IPropertyHolder propertyHolder) {
		//if (mapEditorState.isEditing()) return;
//		IPropertyHolder currentHolder = SceneEditorAddon.get().propertyPanel.getCurrentHolder();
//		if (propertyHolder == null || currentHolder == propertyHolder)
//			return;

		Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderSelected.class).setTarget(propertyHolder));
	}

	private void selectAll () {
		selection.clear();
		Array<GameObject> gameObjects = currentContainer.getGameObjects();
		if (gameObjects != null) {
			for (int i = 0; i < gameObjects.size; i++) {
				selectGameObjectAndChildren(gameObjects.get(i));
			}
		}
	}

	public void deleteGameObjects (ObjectSet<GameObject> gameObjects) {
		if (currentContainer != null) {
			for (GameObject gameObject : gameObjects) {

				if (gameObject == null)
					continue;

				GameObject parent = gameObject.getParent();
				if (parent != null) {

					Array<GameObject> deletedObjects = null;

					if (parent.hasGOWithName(gameObject.getName())) {
						deletedObjects = parent.deleteGameObject(gameObject);
					}

					if (deletedObjects != null) {
						for (GameObject deletedObject : deletedObjects) {
							Notifications.fireEvent(Notifications.obtainEvent(GameObjectDeleted.class).setTarget(deletedObject));
						}
					}

				} else {
					Notifications.fireEvent(Notifications.obtainEvent(GameObjectDeleted.class).setTarget(gameObject));
				}

			}
		}
	}

	@EventHandler
	public void onGameObjectCreated (GameObjectCreated event) {
		// call set dirty method on the next frame so that the game asset is already set
		// TODO: refactor the order of the event and setting data
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
			}
		});
	}

	@EventHandler
	public void onComponentUpdated (ComponentUpdated event) {
		AComponent component = event.getComponent();
		if (event.isNotifyUI()) {

			if (!event.wasRapid()) {
//				TalosMain.Instance().ProjectController().setDirty();
			}
		}
	}

	@EventHandler
	public void onGameObjectDeleted (GameObjectDeleted event) {
		GameObject target = event.getTarget();

		// remove gizmos
		removeGizmos(target);

	}

	@EventHandler
	public void onGameObjectNameChanged (GameObjectNameChanged event) {
	}

	@EventHandler
	public void onGameObjectSelectionChanged (GameObjectSelectionChanged event) {
		//If this didn't come from scene editor ignore it
		if (event.getContext() == this) {

			ObjectSet<GameObject> gameObjects = event.get();

			if (event.get().size == 1) { //Only select gizmos if one is selected
				selectGizmos(gameObjects);
			} else {
				unselectGizmos();
				groupSelectionGizmo.setSelected(true);
			}

			// now for properties

			if (gameObjects.size == 0) {
				// we select the main container then
				if (currentContainer instanceof Scene) {
					Scene scene = (Scene) currentContainer;
					selectPropertyHolder(scene);
				}
			} else {
				if (gameObjects.size == 1) {
					selectPropertyHolder(gameObjects.first());
				} else {
					selectPropertyHolder(new MultiPropertyHolder(gameObjects));
				}
			}
		}

		mapEditorState.update(event);
	}

	public void changeGOName (GameObject gameObject, String suggestedName) {
		if (suggestedName.equals(gameObject.getName()))
			return;

		String finalName = NamingUtils.getNewName(suggestedName, currentContainer.getAllGONames());


		String oldName = gameObject.getName();

		gameObject.setName(finalName);

		GameObjectNameChanged event = Notifications.obtainEvent(GameObjectNameChanged.class);
		event.target = gameObject;
		event.oldName = oldName;
		event.newName = finalName;

		Notifications.fireEvent(event);
	}

	@Override
	protected boolean canMoveAround () {
		boolean initialMusts = true;
		if (!initialMusts) {
			return false;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			return true;
		}

		if(ctrlPressed()) {
			return false;
		}

		Vector3 touchToLocal = getTouchToWorld(Gdx.input.getX(), Gdx.input.getY());
		Gizmo gizmo = hitGizmo(touchToLocal.x, touchToLocal.y);

		if (gizmo == null && entityUnderMouse == null) {
			return true;
		}

		if(gizmo != null && !(gizmo instanceof GroupSelectionGizmo) && gizmo.getGameObject().isEditorTransformLocked()) {
			return true;
		}

		if(entityUnderMouse != null && entityUnderMouse.isEditorTransformLocked()) {
			return true;
		}

		return false;
	}

	@Override
	protected GameObject getRootSceneObject () {
		if (this.currentContainer != null) {
			return currentContainer.root;
		} else {
			return super.getRootSceneObject();
		}
	}

	public Vector2 getMouseCordsOnScene () {
		final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		this.screenToLocalCoordinates(vec);
		Vector2 local = getWorldFromLocal(vec.x, vec.y);
		return local;
	}

	public void loadFromScene (GameAsset<Scene> scene) {
		gameAsset = scene;
		openSavableContainer(scene.getResource());
	}

	@Deprecated
	public void loadFromData (Json json, JsonValue jsonData, boolean fromMemory) {
		String path = jsonData.getString("currentScene", "");

		AssetRepository.init();
		AssetRepository.getInstance().loadAssetsForProject(Gdx.files.absolute(projectPath));

		String currentFolderPath = null;
//		ProjectExplorerWidget projectExplorer = sceneEditorAddon.projectExplorer;
//		if (projectExplorer.getCurrentFolder() != null) {
//			currentFolderPath = projectExplorer.getCurrentFolder().path();
//		}

		read(json, jsonData);

		if (fromMemory && currentFolderPath != null) {
//			projectExplorer.select(currentFolderPath);
		}
//
//		FileHandle sceneFileHandle = AssetImporter.get(path);
//		if (sceneFileHandle.exists()) {
//			SavableContainer container;
//			if (sceneFileHandle.extension().equals("prefab")) {
//				container = new Prefab();
//			} else {
//				container = new Scene();
//			}
//			container.path = sceneFileHandle.path();
//			if (fromMemory) {
//				container.load(snapshotService.getSnapshot(changeVersion, AssetImporter.relative(container.path)));
//			} else {
//				container.loadFromPath();
//				snapshotService.saveSnapshot(changeVersion, AssetImporter.relative(container.path), container.getAsString());
//			}
//
//			openSavableContainer(container);
//		}
//
//		if (!fromMemory) {
//			Notifications.fireEvent(Notifications.obtainEvent(ProjectOpened.class));
//		}else{
//			Toast toast = Toast.makeToast("last action reversed", Toast.LENGTH_SHORT, Align.bottomRight);
//			toast.show();
//		}
	}


	public String getRelativePath (String fullPath) {
		String projectFullPath = getProjectPath();
		return fullPath.replace(projectFullPath, "").substring(1);
	}

	public void repositionGameObject (GameObject parentToMoveTo, GameObject childThatHasMoved) {
		if (parentToMoveTo == null) {
			parentToMoveTo = currentContainer.getSelfObject();
		}

		if (childThatHasMoved.parent != null) {
			childThatHasMoved.parent.removeObject(childThatHasMoved);
		}

		parentToMoveTo.addGameObject(childThatHasMoved);
		GameObject.projectInParentSpace(parentToMoveTo, childThatHasMoved);
		//for updating left panel values

		logger.info("redo reposition game object");


//		SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
//		sceneEditorAddon.workspace.selectGameObjectExternally(childThatHasMoved);
//
//		TalosMain.Instance().ProjectController().setDirty();
	}



	public Array<String> getLayerList () {
		SceneData sceneData = SharedResources.currentProject.getSceneData();

		return sceneData.getRenderLayers();
	}

	public GameObject getRootGO () {
		if (currentContainer == null)
			return null;
		return currentContainer.getSelfObject();
	}

	@EventHandler
	public void onLayerListUpdated (LayerListUpdated event) {
		Array<String> layerList = getLayerList();
		// find all game objects and if any of them is on layer that does not exist, change its layer to default
		Array<GameObject> list = new Array<>();
		list = currentContainer.getSelfObject().getChildrenByComponent(RendererComponent.class, list);

		for (GameObject gameObject : list) {
			RendererComponent component = gameObject.getComponentAssignableFrom(RendererComponent.class);
			String sortingLayer = component.getSortingLayer();
			if (!layerList.contains(sortingLayer, false)) {
				component.setSortingLayer("Default");
			}
		}
	}

	public MainRenderer getRenderer () {
		return renderer;
	}

	public String getProjectPath () {
		return projectPath;
	}

	public AssetListPopup getAssetListPopup () {
		return assetListPopup;
	}

	public GameObjectListPopup getGameObjectListPopup () {
		return gameObjectListPopup;
	}

	public FileHandle getProjectFolder () {
		return Gdx.files.absolute(projectPath);
	}

	public FileHandle getAssetsFolder () {
		return Gdx.files.absolute(projectPath + File.separator + "assets");
	}

	@EventHandler
	public void onProjectOpened (ProjectOpened event) {
		// setup file tracker
		try {
			fileWatching.startWatchingCurrentProject();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onProjectDirectoryContentsChanged (ProjectDirectoryContentsChanged event) {
		if (event.getChanges().directoryStructureChange()) {
			boolean nonMeta = false;
			for (FileHandle added : event.getChanges().added) {
				if (!added.extension().equals("meta")) {
					nonMeta = true;
				}
			}
			if (nonMeta) {
				//reloadScheduled = 0.5f;
			}
		}
	}

	@EventHandler
	public void onPropertyHolderEdited (PropertyHolderEdited event) {
		Object parentOfPropertyHolder = event.parentOfPropertyHolder;


//		IPropertyHolder currentHolder = SceneEditorAddon.get().propertyPanel.getCurrentHolder();
//		if (currentHolder != null) {
//			if (currentHolder instanceof MultiPropertyHolder) {
//				ObjectSet<IPropertyHolder> holders = ((MultiPropertyHolder)currentHolder).getHolders();
//				boolean setDirty = false;
//				for (IPropertyHolder holder : holders) {
//					if (holder instanceof AMetadata) {
//						AssetImporter.saveMetadata((AMetadata)holder);
//					} else {
//						setDirty = true;
//					}
//				}
//				if (setDirty && !event.fastChange) {
//					TalosMain.Instance().ProjectController().setDirty();
//				}
//			} else {
//				if (currentHolder instanceof AMetadata) {
//					AssetImporter.saveMetadata((AMetadata)currentHolder);
//				} else {
//					if (!event.fastChange) {
//						TalosMain.Instance().ProjectController().setDirty();
//					}
//				}
//			}
//		}
	}

	@EventHandler
	public void onAssetPathChanged (AssetPathChanged event) {
		Array<AComponent> list = new Array<>();

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				for (AComponent component : list) {
					Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(component, false));
				}
			}
		});
	}



	public void dispose () {
		fileWatching.shutdown();
	}

	public void showMapEditToolbar () {
		mapEditorToolbar.build();
		mapEditorToolbar.addAction(Actions.fadeOut(0));
		mapEditorToolbar.addAction(Actions.fadeIn(0.3f));
		addActor(mapEditorToolbar);
	}

	public void hideMapEditToolbar () {
		mapEditorToolbar.addAction(Actions.sequence(Actions.fadeOut(0.3f), Actions.removeActor()));
		unlockGizmos();
	}

	public SavableContainer getCurrentContainer () {
		return currentContainer;
	}

	@Override
	protected void drawEntitiesForSelection () {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		super.drawEntitiesForSelection();
		renderer.skipUpdates = true;

		renderer.setRenderParentTiles(false);

		PolygonSpriteBatchMultiTexture customBatch = entitySelectionBuffer.getCustomBatch();
		customBatch.setUsingCustomColourEncoding(true);
		customBatch.setProjectionMatrix(camera.combined);

		customBatch.begin();
		renderer.setCamera(camera);
		drawMainRenderer(customBatch, 1f);

		customBatch.end();
		renderer.skipUpdates = false;

	}

	@Override
	public void initializeGridPropertyProvider () {
		gridPropertyProvider = new DynamicGridPropertyProvider();
		gridPropertyProvider.getBackgroundColor().set(0.1f, 0.1f, 0.1f, 1f);

		staticGridPropertyProvider = new StaticBoundedGridPropertyProvider();
		staticGridPropertyProvider.hideZero();
		staticGridPropertyProvider.getBackgroundColor().set(0.1f, 0.1f, 0.1f, 1f);

	}

	@EventHandler
	public void onRequestSelectionClear (RequestSelectionClearEvent clearEvent) {
		requestSelectionClear();
	}

	@Override
	public void requestSelectionClear() {
		if(mapEditorState.isEditing()) return;

		performSelectionClear();
	}

	public void performSelectionClear() {
		for (GameObject gameObject : selection) {
			if (gizmos.gizmoMap.containsKey(gameObject)) {
				Array<Gizmo> gizmo = gizmos.gizmoMap.get(gameObject);
				for (int j = 0; j < gizmo.size; j++) {
					gizmo.get(j).setSelected(false);
				}
			}
		}
		clearSelection();
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, selection));
	}

	@EventHandler
	public void onGameAssetOpened (GameAssetOpenEvent gameAssetOpenEvent) {
		GameAsset<?> gameAsset = gameAssetOpenEvent.getGameAsset();
		if (gameAsset.type == GameAssetType.SCENE) {

		}
	}
}
