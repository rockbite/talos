package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.esotericsoftware.spine.SkeletonData;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.logic.*;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.LayerType;
import com.talosvfx.talos.editor.addons.scene.maps.MapEditorState;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.FileWatching;
import com.talosvfx.talos.editor.addons.scene.widgets.AssetListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.MapEditorToolbar;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.TemplateListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.TransformGizmo;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.utils.GridDrawer;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.fromDirectoryView;
import static com.talosvfx.talos.editor.addons.scene.widgets.gizmos.SmartTransformGizmo.getLatestFreeOrderingIndex;

public class SceneEditorWorkspace extends ViewportWidget implements Json.Serializable, Notifications.Observer {

	private static SceneEditorWorkspace instance;
	public final TemplateListPopup templateListPopup;

	private SceneEditorAddon sceneEditorAddon;
	private String projectPath;

	private SavableContainer currentContainer;


	private Array<GameObject> selection = new Array<>();
	private MainRenderer renderer;
	private final MainRenderer uiSceneRenderer;

	private String changeVersion = "";
	private SnapshotService snapshotService;

	private AssetListPopup assetListPopup;

	private FileTracker fileTracker = new FileTracker();
	private FileWatching fileWatching = new FileWatching();
	private float reloadScheduled = -1;

	public Array<String> layers = new Array<>();

	private final GridDrawer gridDrawer;
	public GridProperties gridProperties = new GridProperties();
	public MapEditorState mapEditorState;
	private MapEditorToolbar mapEditorToolbar;

	public MainRenderer getUISceneRenderer () {
		return uiSceneRenderer;
	}

	public static class GridProperties {
		public Supplier<float[]> sizeProvider;
		public int subdivisions = 0;
	}

	// selections
	private Image selectionRect;

	private AssetRepository assetRepository;

	public SceneEditorWorkspace () {

		layers.clear();
		layers.add("Default");
		layers.add("UI");
		layers.add("Misc");

		setSkin(TalosMain.Instance().getSkin());
		setWorldSize(10);
		mapEditorToolbar = new MapEditorToolbar(TalosMain.Instance().getSkin());

		snapshotService = new SnapshotService();
		mapEditorState = new MapEditorState();

		Notifications.registerObserver(this);

		FileHandle list = Gdx.files.internal("addons/scene/go-templates.xml");
		XmlReader xmlReader = new XmlReader();
		XmlReader.Element root = xmlReader.parse(list);

		GizmoRegister.init(root);

		assetListPopup = new AssetListPopup<>();
		templateListPopup = new TemplateListPopup(root);
		templateListPopup.setListener(new TemplateListPopup.ListListener() {
			@Override
			public void chosen (XmlReader.Element template, float x, float y) {
				Vector2 pos = new Vector2(x, y);
				createObjectByTypeName(template.getAttribute("name"), pos, null);
			}
		});

		clearListeners();
		initListeners();
		addPanListener();

		renderer = new MainRenderer();
		uiSceneRenderer = new MainRenderer();

		Stage stage = TalosMain.Instance().UIStage().getStage();
		Skin skin = TalosMain.Instance().getSkin();
		selectionRect = new Image(skin.getDrawable("orange_row"));
		selectionRect.setSize(0, 0);
		selectionRect.setVisible(false);
		addActor(selectionRect);

		gridDrawer = new GridDrawer(this, camera, gridProperties);
	}

	public void createEmpty (Vector2 position) {
		createObjectByTypeName("empty", position, null);
	}

	public void createEmpty (GameObject parent) {
		createObjectByTypeName("empty", null, parent);
	}

	public void createEmpty (Vector2 position, GameObject parent) {
		createObjectByTypeName("empty", position, parent);
	}

	public GameObject createSpriteObject (GameAsset<Texture> spriteAsset, Vector2 sceneCords, GameObject parent) {
		GameObject spriteObject = createObjectByTypeName("sprite", sceneCords, parent);
		SpriteRendererComponent component = spriteObject.getComponent(SpriteRendererComponent.class);

		if (!fromDirectoryView) {
			component.orderingInLayer = getLatestFreeOrderingIndex(component.sortingLayer);
		}
		component.setGameAsset(spriteAsset);

		return spriteObject;
	}

	public GameObject createSpineObject (GameAsset<SkeletonData> asset, Vector2 sceneCords, GameObject parent) {
		GameObject spineObject = createObjectByTypeName("spine", sceneCords, parent);
		SpineRendererComponent rendererComponent = spineObject.getComponent(SpineRendererComponent.class);

		if (!fromDirectoryView) {
			rendererComponent.orderingInLayer = getLatestFreeOrderingIndex(rendererComponent.sortingLayer);
		}
		rendererComponent.setGameAsset(asset);

		return spineObject;
	}

	public GameObject createParticle (GameAsset<ParticleEffectDescriptor> asset, Vector2 sceneCords, GameObject parent) {
		GameObject particleObject = createObjectByTypeName("particle", sceneCords, parent);
		ParticleComponent component = particleObject.getComponent(ParticleComponent.class);

		if (!fromDirectoryView) {
			component.orderingInLayer = getLatestFreeOrderingIndex(component.sortingLayer);
		}
		component.setGameAsset(asset);

		return particleObject;
	}

	public GameObject createObjectByTypeName (String idName, Vector2 position, GameObject parent) {
		GameObject gameObject = new GameObject();
		XmlReader.Element template = templateListPopup.getTemplate(idName);

		String name = getUniqueGOName(template.getAttribute("nameTemplate", "gameObject"), true);
		gameObject.setName(name);
		initComponentsFromTemplate(gameObject, templateListPopup.getTemplate(idName));

		if (position != null && gameObject.hasComponent(TransformComponent.class)) {
			// oh boi always special case with this fuckers
			TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
			transformComponent.position.set(position.x, position.y);
		}

		if (parent == null) {
			currentContainer.addGameObject(gameObject);
		} else {
			parent.addGameObject(gameObject);
		}

		if (!fromDirectoryView) {
			initGizmos(gameObject, this);
			Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run () {
					selectGameObjectExternally(gameObject);
				}
			});
		}

		return gameObject;
	}

	public GameObject createFromPrefab (GameAsset<Prefab> prefabToCopy, Vector2 position, GameObject parent) {

		Prefab prefab = Prefab.from(prefabToCopy.getRootRawAsset().handle);

		GameObject gameObject = prefab.root.getGameObjects().first();
		String name = getUniqueGOName(prefab.name, true);
		gameObject.setName(name);

		if (parent == null) {
			currentContainer.addGameObject(gameObject);
		} else {
			parent.addGameObject(gameObject);
		}

		if (!fromDirectoryView) {
			initGizmos(gameObject, this);

			Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));

			selectGameObjectExternally(gameObject);
		}

		return gameObject;
	}

	private String getUniqueGOName (String nameTemplate) {
		return getUniqueGOName(nameTemplate, false);
	}

	private String getUniqueGOName (String nameTemplate, boolean keepOriginal) {
		if (fromDirectoryView) {
			return UUID.randomUUID().toString().substring(0, 10);
		}

		int number = 0;

		String name = nameTemplate;

		if (!keepOriginal) {
			name = nameTemplate + number;
		}

		while (currentContainer.hasGOWithName(name)) {
			number++;
			name = nameTemplate + number;
		}

		return name;
	}

	private void initComponentsFromTemplate (GameObject gameObject, XmlReader.Element template) {
		XmlReader.Element container = template.getChildByName("components");
		Array<XmlReader.Element> componentsXMLArray = container.getChildrenByName("component");
		for (XmlReader.Element componentXML : componentsXMLArray) {
			String className = componentXML.getAttribute("className");
			String classPath = templateListPopup.componentClassPath;

			try {
				Class clazz = ClassReflection.forName(classPath + "." + className);
				Object instance = ClassReflection.newInstance(clazz);
				AComponent component = (AComponent)instance;
				gameObject.addComponent(component);
			} catch (Exception e) {

			}
		}
	}



	protected void initListeners () {
		inputListener = new InputListener() {

			Vector2 vec = new Vector2();
			Gizmo touchedGizmo = null;

			// selection stuff
			boolean dragged = false;
			Vector2 startPos = new Vector2();
			Rectangle rectangle = new Rectangle();
			boolean upWillClear = true;

			GameObject selectedGameObject;

			private boolean painting = false;
			private boolean erasing = true;

			@Override
			public boolean mouseMoved (InputEvent event, float x, float y) {

				Vector2 hitCords = getWorldFromLocal(x, y);
				for (int i = 0; i < SceneEditorWorkspace.this.gizmos.gizmoList.size; i++) {
					Gizmo item = SceneEditorWorkspace.this.gizmos.gizmoList.get(i);
					if (item.isSelected()) {
						item.mouseMoved(hitCords.x, hitCords.y);
					}
				}

				return super.mouseMoved(event, x, y);
			}

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

				if (mapEditorState.isEditing()) {
					if (mapEditorState.painting) {
						//Place a tile and return
						paintTileAt(x, y);
						painting = true;
						return true;
					} else if (mapEditorState.erasing) {
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
				touchedGizmo = null;

				Vector2 hitCords = getWorldFromLocal(x, y);

				Gizmo gizmo = hitGizmo(hitCords.x, hitCords.y);

				selectedGameObject = null;

				if (gizmo != null) {
					touchedGizmo = gizmo;
					GameObject gameObject = touchedGizmo.getGameObject();
					upWillClear = false;
					if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !touchedGizmo.catchesShift()) {
						// toggling
						if (selection.contains(gameObject, true)) {
							removeFromSelection(gameObject);
						} else {
							addToSelection(gameObject);
							selectedGameObject = gameObject;
						}
					} else {
						if (!selection.contains(gameObject, true)) {
							selectGameObject(gameObject);
							selectedGameObject = gameObject;
						}
					}

					Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));

					touchedGizmo.touchDown(hitCords.x, hitCords.y, button);
					// also tell all other selected gizmos about this touchdown


					for (int i = 0; i < SceneEditorWorkspace.this.gizmos.gizmoList.size; i++) {
						Gizmo item = SceneEditorWorkspace.this.gizmos.gizmoList.get(i);
						if (item.isSelected() && item.getClass().equals(touchedGizmo.getClass()) && item != touchedGizmo) {
							item.touchDown(hitCords.x, hitCords.y, button);
						}
					}

					getStage().setKeyboardFocus(SceneEditorWorkspace.this);

					event.handle();

					templateListPopup.remove();

					return true;
				} else {
					touchedGizmo = null;

					if (button == 1 && !event.isCancelled()) {
						final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
						(TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);

						Vector2 location = new Vector2(vec);
						Vector2 createLocation = new Vector2(hitCords);
						templateListPopup.showPopup(getStage(), location, createLocation);

						return true;
					}
				}

				if (button == 2 || ctrlPressed()) {
					selectionRect.setVisible(true);
					selectionRect.setSize(0, 0);
					startPos.set(x, y);

					return true;
				}

				clearSelection();
				Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
				getStage().setKeyboardFocus(SceneEditorWorkspace.this);

				return false;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				super.touchDragged(event, x, y, pointer);

				if (mapEditorState.isEditing()) {
					if (mapEditorState.painting) {

						//Check to see if we are in static tile first

						if (mapEditorState.getLayerSelected() != null) {
							if (mapEditorState.getLayerSelected().getType() == LayerType.STATIC) {
								//Place a tile and return
								paintTileAt(x, y);
							}
						}
						return;

					} else if (mapEditorState.erasing) {
						if (mapEditorState.getLayerSelected() != null) {
							if (mapEditorState.getLayerSelected().getType() == LayerType.STATIC) {
								//Place a tile and return
								eraseTileAt(x, y);
							} else  {
//								eraseEntityAt(x, y);
							}
						}
						return;
					}

					return;
				}

				dragged = true;

				Vector2 hitCords = getWorldFromLocal(x, y);

				if (touchedGizmo != null) {
					touchedGizmo.touchDragged(hitCords.x, hitCords.y);
					for (int i = 0; i < SceneEditorWorkspace.this.gizmos.gizmoList.size; i++) {
						Gizmo item = SceneEditorWorkspace.this.gizmos.gizmoList.get(i);
						if (item.isSelected() && item.getClass().equals(touchedGizmo.getClass()) && item != touchedGizmo) {
							item.touchDragged(hitCords.x, hitCords.y);
						}
					}
				}

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
				;
				if (erasing) {
					erasing = false;
					return;
				}
				;

				if (touchedGizmo != null) {
					touchedGizmo.touchUp(hitCords.x, hitCords.y);
					for (int i = 0; i < SceneEditorWorkspace.this.gizmos.gizmoList.size; i++) {
						Gizmo item = SceneEditorWorkspace.this.gizmos.gizmoList.get(i);
						if (item.isSelected() && item.getClass().equals(touchedGizmo.getClass()) && item != touchedGizmo) {
							item.touchUp(hitCords.x, hitCords.y);
						}
					}
				}

				touchedGizmo = null;

				if (selectionRect.isVisible()) {
					upWillClear = false;
					selectGizmosByRect(rectangle);
					Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
				} else if (upWillClear) {
					FocusManager.resetFocus(getStage());
					clearSelection();
					Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
				} else {
					if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
						// deselect all others, if they are selected
						if (deselectOthers(selectedGameObject)) {
							Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
						}
					}
				}

				getStage().setKeyboardFocus(SceneEditorWorkspace.this);

				selectionRect.setVisible(false);
			}

			@Override
			public boolean keyDown (InputEvent event, int keycode) {

				if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
					Array<GameObject> deleteList = new Array<>();
					deleteList.addAll(selection);
					clearSelection();
					deleteGameObjects(deleteList);
					Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
				}

				if (keycode == Input.Keys.C && ctrlPressed()) {
					copySelected();
				}

				if (keycode == Input.Keys.V && ctrlPressed()) {
					pasteFromClipboard();
				}

				if (keycode == Input.Keys.A && ctrlPressed()) {
					selectAll();
					Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
				}

				if (keycode == Input.Keys.Z && ctrlPressed() && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
					TalosMain.Instance().ProjectController().undo();
				}

				if (keycode == Input.Keys.Z && ctrlPressed() && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
					TalosMain.Instance().ProjectController().redo();
				}

				for (Gizmo gizmo : SceneEditorWorkspace.this.gizmos.gizmoList) {
					if (gizmo.isSelected()) {
						gizmo.keyDown(event, keycode);
					}
				}

				return super.keyDown(event, keycode);
			}
		};

		addListener(inputListener);
	}

	private void eraseTileAt (float x, float y) {
		if (mapEditorState.erasing) {
			int mouseCellX = gridDrawer.getMouseCellX();
			int mouseCellY = gridDrawer.getMouseCellY();
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
			layerSelected.removeEntity(worldFromLocal.x, worldFromLocal.y);
		}
	}

	private void paintTileAt (float x, float y) {
		Vector2 worldFromLocal = getWorldFromLocal(x, y);

		if (mapEditorState.painting) {
			int mouseCellX = gridDrawer.getMouseCellX();
			int mouseCellY = gridDrawer.getMouseCellY();
			//Targets

			TalosLayer layerSelected = mapEditorState.getLayerSelected();
			if (layerSelected != null) {
				GameAsset<TilePaletteData> gameResource = layerSelected.getGameResource();
				TilePaletteData palette = gameResource.getResource();

				Array<GameAsset<?>> selectedGameAssets = palette.selectedGameAssets;

				if (selectedGameAssets.size > 1) {
					System.out.println("Multi stamp not supported yet");
				} else if (selectedGameAssets.size == 1) {
					GameAsset<?> gameAssetToPaint = selectedGameAssets.first();

					//Paint it into the layer
					LayerType type = layerSelected.getType();
					if (type == LayerType.STATIC) {
						if (gameAssetToPaint.type != GameAssetType.SPRITE) {
							System.out.println("Trying to paint a non sprite into a static layer");
							return;
						}

						StaticTile staticTile = new StaticTile(gameAssetToPaint, new GridPosition(mouseCellX, mouseCellY));
						layerSelected.setStaticTile(staticTile);

					} else {
						//Always do it like entities

						AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
						GameObject tempParent = new GameObject();
						boolean success = AssetImporter.createAssetInstance(gameAssetToPaint, tempParent);
						if (tempParent.getGameObjects() == null || tempParent.getGameObjects().size == 0) {
							success = false;
						}
						AssetImporter.fromDirectoryView = false;

						if (success) {
							//We can add this to layer entities
							layerSelected.getRootEntities().add(tempParent.getGameObjects().first());
						}

					}

				}
			}

		}
	}

	public static boolean isRenamePressed (int keycode) {
		if (TalosMain.Instance().isOsX()) {
			return keycode == Input.Keys.ENTER;
		} else {
			return keycode == Input.Keys.F2;
		}
	}

	public static boolean ctrlPressed () {
		if (TalosMain.Instance().isOsX()) {
			return Gdx.input.isKeyPressed(Input.Keys.SYM) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
		} else {
			return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
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
	}

	public void openScene (FileHandle fileHandle) {
		if (currentContainer != null) {
			currentContainer.save();
		}
		Scene scene = new Scene();
		scene.path = fileHandle.path();
		scene.loadFromPath();
		openSavableContainer(scene);
	}

	public void convertToPrefab (GameObject gameObject) {
		String name = gameObject.getName();

		String path = getProjectPath() + File.separator + "assets";
		if (SceneEditorAddon.get().projectExplorer.getCurrentFolder() != null) {
			path = SceneEditorAddon.get().projectExplorer.getCurrentFolder().path();
		}

		FileHandle handle = AssetImporter.suggestNewName(path, name, "prefab");
		if (handle != null) {
			GameObject gamePrefab = new GameObject();
			gamePrefab.setName("Prefab");

			gamePrefab.addGameObject(gameObject);

			Prefab prefab = new Prefab();
			prefab.path = handle.path();
			prefab.root = gamePrefab;
			prefab.save();
			AssetRepository.getInstance().rawAssetCreated(handle, true);
			SceneEditorAddon.get().projectExplorer.reload();
		}
	}

	public static class ClipboardPayload {
		public Array<GameObject> objects = new Array<>();
		public Vector2 cameraPositionAtCopy = new Vector2(0, 0);
	}

	private void copySelected () {
		ClipboardPayload payload = new ClipboardPayload();
		payload.objects.addAll(selection);
		Vector3 camPos = getCamera().position;
		payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

		Json json = new Json();
		String clipboard = json.toJson(payload);
		Gdx.app.getClipboard().setContents(clipboard);
	}

	private void pasteFromClipboard () {
		String clipboard = Gdx.app.getClipboard().getContents();

		Json json = new Json();

		try {
			ClipboardPayload payload = json.fromJson(ClipboardPayload.class, clipboard);
			Vector3 camPosAtPaste = getCamera().position;
			Vector2 offset = new Vector2(camPosAtPaste.x, camPosAtPaste.y);
			offset.sub(payload.cameraPositionAtCopy);

			clearSelection();
			for (GameObject gameObject : payload.objects) {
				String name = getUniqueGOName(gameObject.getName(), false);
				gameObject.setName(name);
				currentContainer.addGameObject(gameObject);
				TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
				transformComponent.position.add(offset);
				initGizmos(gameObject, this);
				Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));
				addToSelection(gameObject);
			}
			Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
		} catch (Exception e) {

		}
	}

	@Override
	public void write (Json json) {

		changeVersion = UUID.randomUUID().toString();

		if (projectPath != null) {
			json.writeArrayStart("layers");
			for (String layer : layers) {
				json.writeValue(layer);
			}
			json.writeArrayEnd();

			json.writeValue("currentScene", AssetImporter.relative(currentContainer.path));

			json.writeValue("changeVersion", changeVersion);
		}
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		changeVersion = jsonData.getString("changeVersion", "");

		ProjectExplorerWidget projectExplorer = sceneEditorAddon.projectExplorer;

		projectExplorer.loadDirectoryTree(projectPath);

		JsonValue layersJson = jsonData.get("layers");
		if (layersJson != null) {
			layers.clear();
			for (JsonValue layerJson : layersJson) {
				layers.add(layerJson.asString());
			}
		}

		String path = jsonData.getString("currentScene", "");
		FileHandle sceneFileHandle = AssetImporter.get(path);
		if (sceneFileHandle.exists()) {
			SavableContainer container;
			if (sceneFileHandle.extension().equals("prefab")) {
				container = new Prefab();
			} else {
				container = new Scene();
			}
			container.path = sceneFileHandle.path();
			container.loadFromPath();
			openSavableContainer(container);
		}

//        SceneEditorAddon.get().assetImporter.housekeep(projectPath);
	}

	@Override
	public void act (float delta) {
		if (!(TalosMain.Instance().Project() instanceof SceneEditorProject))
			return;
		super.act(delta);


		if (reloadScheduled > 0) {
			reloadScheduled -= delta;
			if (reloadScheduled <= 0) {
				reloadScheduled = -1;
				reloadProjectExplorer();
			}
		}
	}

	@Override
	public void drawContent (Batch batch, float parentAlpha) {
		if (!(TalosMain.Instance().Project() instanceof SceneEditorProject))
			return;
		batch.end();

		if (mapEditorState.isEditing()) {
			gridDrawer.highlightCursorHover = true;
			gridDrawer.drawGrid();
		} else {
			drawGrid(batch, parentAlpha);
		}

		batch.begin();

		renderer.setCamera(camera);
		drawMainRenderer(batch, parentAlpha);


	}

	private void drawMainRenderer (Batch batch, float parentAlpha) {
		if (currentContainer == null)
			return;

		renderer.update(currentContainer.getSelfObject());
		renderer.render(batch, new MainRenderer.RenderState(), currentContainer.getSelfObject());
	}

	public void setAddon (SceneEditorAddon sceneEditorAddon) {
		this.sceneEditorAddon = sceneEditorAddon;
	}

	public static SceneEditorWorkspace getInstance () {
		if (instance == null) {
			instance = new SceneEditorWorkspace();
		}
		return instance;
	}

	public void cleanWorkspace () {

	}

	public String writeExport () {

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
						String command = "node " + handle.path() + " \"" + projectPath + "\" \"" + TalosMain.Instance().ProjectController().getExportPath() + "\"";
						if (TalosMain.Instance().isOsX()) {
							ProcessBuilder pb = new ProcessBuilder("bash", "-l", "-c", command);
							pb.inheritIO();
							pb.start();
						} else {
							ProcessBuilder pb = new ProcessBuilder(command);
							pb.inheritIO();
							pb.start();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return "";
	}

	public void setProjectPath (String path) {
		projectPath = path;
	}


	// if asset is updated externally, do something about it maybe
	public void updateAsset (FileHandle handle) {

	}

	public void reloadProjectExplorer () {
		ProjectExplorerWidget projectExplorer = sceneEditorAddon.projectExplorer;
		projectExplorer.loadDirectoryTree(projectPath);
	}

	public void openSavableContainer (SavableContainer mainScene) {
		if (mainScene == null)
			return;
		sceneEditorAddon.hierarchy.loadEntityContainer(mainScene);
		currentContainer = mainScene;

		// process all game objects
		initGizmos(mainScene, this);

		clearSelection();

		selectPropertyHolder(mainScene);

		if (mainScene instanceof Scene) {
			bgColor.set(Color.BLACK);
		} else {

			bgColor.set(Color.valueOf("#241a00"));
		}
	}


	public void selectPropertyHolder (IPropertyHolder propertyHolder) {
		if (propertyHolder == null)
			return;

		Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderSelected.class).setTarget(propertyHolder));
	}

	public void selectGameObjectExternally (GameObject gameObject) {
		if (fromDirectoryView)
			return;

		selectGameObject(gameObject);
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
	}

	private void selectGameObject (GameObject gameObject) {
		if (gameObject == null)
			return;
		Array<GameObject> tmp = new Array<>();
		tmp.add(gameObject);

		setSelection(tmp);
	}

	public void removeFromSelection (GameObject gameObject) {
		selection.removeValue(gameObject, true);
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
	}

	public void addToSelection (GameObject gameObject) {
		if (!selection.contains(gameObject, true)) {
			selection.add(gameObject);
		}
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
	}

	private void setSelection (Array<GameObject> gameObjects) {
		selection.clear();

		selection.addAll(gameObjects);
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

	private void selectGameObjectAndChildren (GameObject gameObject) {
		selection.add(gameObject);

		Array<GameObject> children = gameObject.getGameObjects();

		if (children != null) {
			for (int i = 0; i < children.size; i++) {
				selectGameObjectAndChildren(children.get(i));
			}
		}
	}

	private boolean deselectOthers (GameObject exceptThis) {
		if (selection.size > 1 && selection.contains(exceptThis, true)) {
			selection.clear();
			selection.add(exceptThis);

			return true;
		}

		return false;
	}

	public void requestSelectionClear () {
		clearSelection();
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
	}

	private void clearSelection () {
		selection.clear();
	}

	public void selectGizmosByRect (Rectangle rectangle) {
		if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			clearSelection();
		}
		for (int i = 0; i < gizmos.gizmoList.size; i++) {
			Gizmo gizmo = gizmos.gizmoList.get(i);
			if (gizmo instanceof TransformGizmo) {
				TransformGizmo transformGizmo = (TransformGizmo)gizmo;
				Vector2 worldPos = transformGizmo.getWorldPos();
				Vector2 local = getLocalFromWorld(worldPos.x, worldPos.y);

				if (rectangle.contains(local)) {
					if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
						addToSelection(gizmo.getGameObject());
					} else {
						addToSelection(gizmo.getGameObject());
					}

				}
			}
		}
	}

	public void deleteGameObjects (Array<GameObject> gameObjects) {
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
    public void onGameObjectCreated(GameObjectCreated event) {
        // call set dirty method on the next frame so that the game asset is already set
        // TODO: refactor the order of the event and setting data
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                TalosMain.Instance().ProjectController().setDirty();
            }
        });
    }

	@EventHandler
	public void onComponentUpdated (ComponentUpdated event) {
		AComponent component = event.getComponent();
		sceneEditorAddon.propertyPanel.propertyProviderUpdated(component);

		if (!event.wasRapid()) {
			TalosMain.Instance().ProjectController().setDirty();
		}
	}

	@EventHandler
	public void onGameObjectDeleted (GameObjectDeleted event) {
		GameObject target = event.getTarget();
		sceneEditorAddon.propertyPanel.notifyPropertyHolderRemoved(target);

		// remove gizmos
		removeGizmos(target);

		TalosMain.Instance().ProjectController().setDirty();
	}

	@EventHandler
	public void onGameObjectNameChanged (GameObjectNameChanged event) {
		TalosMain.Instance().ProjectController().setDirty();
	}

	@EventHandler
	public void onGameObjectSelectionChanged (GameObjectSelectionChanged event) {
		mapEditorState.update(event);
		Array<GameObject> gameObjects = event.get();

		selectGizmos(gameObjects);

		// now for properties

		if (selection.size == 0) {
			// we select the main container then
			if (currentContainer instanceof Scene) {
				Scene scene = (Scene)currentContainer;
				selectPropertyHolder(scene);
			}
		} else {
			if (selection.size == 1) {
				selectPropertyHolder(gameObjects.first());
			} else {
				MultiPropertyHolder multiPropertyHolder = new MultiPropertyHolder(gameObjects);
				selectPropertyHolder(multiPropertyHolder);
			}
		}
	}


	public void changeGOName (GameObject gameObject, String suggestedName) {
		if (suggestedName.equals(gameObject.getName()))
			return;

		String finalName = getUniqueGOName(suggestedName, true);

		String oldName = gameObject.getName();

		gameObject.setName(finalName);

		GameObjectNameChanged event = Notifications.obtainEvent(GameObjectNameChanged.class);
		event.target = gameObject;
		event.oldName = oldName;
		event.newName = finalName;

		Notifications.fireEvent(event);
	}

	public Vector2 getMouseCordsOnScene () {
		final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		this.screenToLocalCoordinates(vec);
		Vector2 local = getWorldFromLocal(vec.x, vec.y);
		return local;
	}

	public String saveData (boolean toMemory) {
		Json json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);
		String data = json.prettyPrint(sceneEditorAddon.workspace);

		SavableContainer savableContainer = currentContainer;
		if (savableContainer != null) {
			if (toMemory) {
				snapshotService.saveSnapshot(changeVersion, AssetImporter.relative(savableContainer.path), savableContainer.getAsString());
			} else {
				savableContainer.save();
			}
		}

		return data;
	}

	public void loadFromData (Json json, JsonValue jsonData, boolean fromMemory) {
		String path = jsonData.getString("currentScene", "");

		AssetRepository.init();
		AssetRepository.getInstance().loadAssetsForProject(Gdx.files.absolute(projectPath).child("assets"));

		read(json, jsonData);

		FileHandle sceneFileHandle = AssetImporter.get(path);
		if (sceneFileHandle.exists()) {
			SavableContainer container;
			if (sceneFileHandle.extension().equals("prefab")) {
				container = new Prefab();
			} else {
				container = new Scene();
			}
			container.path = sceneFileHandle.path();
			if (fromMemory) {
				container.load(snapshotService.getSnapshot(changeVersion, AssetImporter.relative(container.path)));
			} else {
				container.loadFromPath();
				snapshotService.saveSnapshot(changeVersion, AssetImporter.relative(container.path), container.getAsString());
			}

//			openSavableContainer(container);
		}

		if (!fromMemory) {
			Notifications.fireEvent(Notifications.obtainEvent(ProjectOpened.class));
		}
	}

	public void repositionGameObject (GameObject parentToMoveTo, GameObject childThatHasMoved) {
		if (parentToMoveTo == null) {
			parentToMoveTo = currentContainer.getSelfObject();
		}

		if (childThatHasMoved.parent != null) {
			childThatHasMoved.parent.removeObject(childThatHasMoved);
		}

		parentToMoveTo.addGameObject(childThatHasMoved);

		TalosMain.Instance().ProjectController().setDirty();
	}

	public Array<String> getLayerList () {
		return layers;
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
			RendererComponent component = gameObject.getComponentSlow(RendererComponent.class);
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
		IPropertyHolder currentHolder = SceneEditorAddon.get().propertyPanel.getCurrentHolder();
		if (currentHolder != null) {
			if (currentHolder instanceof MultiPropertyHolder) {
				Array<IPropertyHolder> holders = ((MultiPropertyHolder)currentHolder).getHolders();
				boolean setDirty = false;
				for (IPropertyHolder holder : holders) {
					if (holder instanceof AMetadata) {
						AssetImporter.saveMetadata((AMetadata)holder);
					} else {
						setDirty = true;
					}
				}
				if (setDirty) {
					TalosMain.Instance().ProjectController().setDirty();
				}
			} else {
				if (currentHolder instanceof AMetadata) {
					AssetImporter.saveMetadata((AMetadata)currentHolder);
				} else {
					TalosMain.Instance().ProjectController().setDirty();
				}
			}
		}
	}

	@EventHandler
	public void onAssetPathChanged (AssetPathChanged event) {
		Array<AComponent> list = new Array<>();
		notifyAssetPathChanged(list, currentContainer.getSelfObject(), event);

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				for (AComponent component : list) {
					Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(component, false));
				}
			}
		});
	}

	private void notifyAssetPathChanged (Array<AComponent> list, GameObject gameObject, AssetPathChanged event) {
//        Iterable<AComponent> components = gameObject.getComponents();
//        for(AComponent component: components) {
//            boolean affected = component.notifyAssetPathChanged(event.oldRelativePath, event.newRelativePath);
//            if(affected) {
//                list.add(component);
//            }
//        }
//
//        Array<GameObject> gameObjects = gameObject.getGameObjects();
//        if(gameObjects != null) {
//            for(GameObject child: gameObjects) {
//                notifyAssetPathChanged(list, child, event);
//            }
//        }
		System.out.println("Does nothing anymore");
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
	}

    public SavableContainer getCurrentContainer() {
        return currentContainer;
    }

}
