package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.SkeletonData;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.MultiPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.editor.project2.apps.SceneHierarchyApp;
import com.talosvfx.talos.editor.widgets.ui.gizmos.GroupSelectionGizmo;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.events.commands.GONameChangeCommand;
import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.addons.scene.events.scene.AddToSelectionEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.DeSelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.RemoveFromSelectionEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.RequestSelectionClearEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.SelectGameObjectExternallyEvent;
import com.talosvfx.talos.runtime.maps.TilePaletteData;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.SavableContainer;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.components.*;
import com.talosvfx.talos.runtime.maps.LayerType;
import com.talosvfx.talos.editor.addons.scene.maps.MapEditorState;
import com.talosvfx.talos.runtime.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.utils.PolygonSpriteBatchMultiTexture;
import com.talosvfx.talos.editor.addons.scene.utils.FileWatching;
import com.talosvfx.talos.editor.addons.scene.widgets.*;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.layouts.LayoutApp;
import com.talosvfx.talos.editor.notifications.EventContextProvider;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.assets.GameAssetOpenEvent;
import com.talosvfx.talos.editor.project2.GlobalDragAndDrop;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.SceneEditorApp;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.runtime.scene.render.RenderState;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import com.talosvfx.talos.editor.utils.grid.property_providers.StaticBoundedGridPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.utils.TransformSettings;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.function.Supplier;

import static com.talosvfx.talos.editor.utils.InputUtils.ctrlPressed;

public class SceneEditorWorkspace extends ViewportWidget implements Json.Serializable, Observer, EventContextProvider<SavableContainer> {

	private static final Logger logger = LoggerFactory.getLogger(SceneEditorWorkspace.class);
	public final TemplateListPopup templateListPopup;
	private final SceneEditorApp sceneEditorApp;

	private String projectPath;

	private SavableContainer currentContainer;
	private GameAsset<Scene> gameAsset;

	private AligningToolsPane aligningToolsPane;

	private MainRenderer renderer;
	private final MainRenderer uiSceneRenderer;

	private String changeVersion = "";
	private SnapshotService snapshotService;

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

	public void getChildrenHavingComponentClass (GameObject root, Class<? extends AComponent> componentClass, Array<GameObject> array) {

		// TODO: 23.02.23 dummy refactor
		if (root == null) {
			return;
		}

		if (root.hasComponent(componentClass)) {
			array.add(root);
		}

		Array<GameObject> children = root.getGameObjects();

		if (children == null) {
			return;
		}

		for (int i = 0; i < children.size; i++) {
			GameObject child = children.get(i);
			getChildrenHavingComponentClass(child, componentClass, array);
		}
	}

	// selections
	private Image selectionRect;

	public SceneEditorWorkspace (SceneEditorApp sceneEditorApp) {
		this.sceneEditorApp = sceneEditorApp;

		setSkin(SharedResources.skin);
		setWorldSize(10);
		mapEditorToolbar = new MapEditorToolbar(SharedResources.skin);

		snapshotService = new SnapshotService();
		mapEditorState = new MapEditorState();

		Notifications.registerObserver(this);

		aligningToolsPane = new AligningToolsPane(groupSelectionGizmo.getViewportWidget().selection);


		GizmoRegister.init(RuntimeContext.getInstance().configData.getGameObjectConfigurationXMLRoot());

		templateListPopup = new TemplateListPopup(RuntimeContext.getInstance().configData.getGameObjectConfigurationXMLRoot());
		templateListPopup.setListener(new TemplateListPopup.ListListener() {
			@Override
			public void chosen (XmlReader.Element template, float x, float y) {
				Vector2 pos = new Vector2(x, y);
				String templateName = template.getAttribute("name");
				final GameObject newObjectInstance = SceneUtils.createObjectByTypeName(currentContainer, templateName, pos, null, templateName);
				// if possible get smart selection from hierarchy widget and
				// add the new game object as child of that target

				if (gameAsset != null) {
					SceneHierarchyApp hierarchyApp = SharedResources.appManager.getAppForAsset(SceneHierarchyApp.class, gameAsset);
					if (hierarchyApp != null) {
						HierarchyWidget hierarchyWidget = hierarchyApp.getHierarchyWidget();
						GameObject smartSelection = hierarchyWidget.getSmartSelection();
						boolean hasTarget = smartSelection != null;
						if (newObjectInstance != null && hasTarget) {
							Gdx.app.postRunnable(() -> {
								SceneUtils.repositionGameObject(currentContainer, smartSelection, newObjectInstance);
								ObjectSet<GameObject> targets = new ObjectSet<>();
								targets.add(newObjectInstance);
								Notifications.fireEvent(Notifications.obtainEvent(GameObjectsRestructured.class).set(getEventContext(), targets));

								Array<GameObject> toExpand = new Array<>();
								toExpand.add(smartSelection);
								hierarchyWidget.getTree().findExpandedObjects(toExpand);
							});
						}
					}
				}

				Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(newObjectInstance));
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
				// TODO: this needs a nicer system

				if (object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
					//We support single game asset drops
					GlobalDragAndDrop.GameAssetDragAndDropPayload gameAssetPayload = (GlobalDragAndDrop.GameAssetDragAndDropPayload)object;
					GameObject newGameObject = null;

					if (gameAssetPayload.getGameAsset().type == GameAssetType.SPRITE) {
						GameAsset<AtlasSprite> gameAsset = (GameAsset<AtlasSprite>)gameAssetPayload.getGameAsset();

						Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
						Vector3 touchToWorld = getTouchToWorld(vec.x, vec.y);
						vec.set(touchToWorld.x, touchToWorld.y);

						newGameObject = SceneUtils.createSpriteObject(currentContainer, gameAsset, vec, currentContainer.getSelfObject());

						//forcefully make active if we aren't active
						LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
						SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());

					} else if (gameAssetPayload.getGameAsset().type == GameAssetType.PREFAB) {
						GameAsset<Prefab> gameAsset = (GameAsset<Prefab>)gameAssetPayload.getGameAsset();

						Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
						Vector3 touchToWorld = getTouchToWorld(vec.x, vec.y);
						vec.set(touchToWorld.x, touchToWorld.y);

						SceneUtils.createFromPrefab(currentContainer, gameAsset, vec, currentContainer.getSelfObject());

						//forcefully make active if we aren't active
						LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
						SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());
					} else if (gameAssetPayload.getGameAsset().type == GameAssetType.SKELETON) {
						GameAsset<SkeletonData> gameAsset = (GameAsset<SkeletonData>)gameAssetPayload.getGameAsset();

						Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
						Vector3 touchToWorld = getTouchToWorld(vec.x, vec.y);
						vec.set(touchToWorld.x, touchToWorld.y);

						newGameObject = SceneUtils.createSpineObject(currentContainer, gameAsset, vec, currentContainer.getSelfObject());

						//forcefully make active if we aren't active
						LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
						SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());

					} else if (gameAssetPayload.getGameAsset().type == GameAssetType.VFX) {
						GameAsset<VFXProjectData> gameAsset = (GameAsset<VFXProjectData>)gameAssetPayload.getGameAsset();

						Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
						Vector3 touchToWorld = getTouchToWorld(vec.x, vec.y);
						vec.set(touchToWorld.x, touchToWorld.y);

						newGameObject = SceneUtils.createParticle(currentContainer, gameAsset, vec, currentContainer.getSelfObject());

						//forcefully make active if we aren't active
						LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
						SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());

					}

					// if possible get smart selection from hierarchy widget and
					// add the new game object as child of that target
					if (gameAsset != null) {
						SceneHierarchyApp hierarchyApp = SharedResources.appManager.getAppForAsset(SceneHierarchyApp.class, gameAsset);
						if (hierarchyApp != null) {
							HierarchyWidget hierarchyWidget = hierarchyApp.getHierarchyWidget();
							GameObject smartSelection = hierarchyWidget.getSmartSelection();
							boolean hasTarget = smartSelection != null;
							if (newGameObject != null && hasTarget) {
								final GameObject child = newGameObject;
								Gdx.app.postRunnable(() -> {
									SceneUtils.repositionGameObject(currentContainer, smartSelection, child);
									ObjectSet<GameObject> targets = new ObjectSet<>();
									targets.add(child);
									Notifications.fireEvent(Notifications.obtainEvent(GameObjectsRestructured.class).set(getEventContext(), targets));

									hierarchyWidget.getTree().findNode(child).expandTo();
								});
							}
						}
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
				// clear smart selection
				if (button == 0) {
					if (gameAsset != null) {
						SceneHierarchyApp hierarchyApp = SharedResources.appManager.getAppForAsset(SceneHierarchyApp.class, gameAsset);
						if (hierarchyApp != null) {
							hierarchyApp.getHierarchyWidget().clearSmartSelectoin();
						}
					}
				}


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
				upWillClear = false;
				dragged = false;

				Vector2 hitCords = getWorldFromLocal(x, y);

				if (button == 1 && !event.isCancelled() && !event.isStopped()) {

					// TODO: 23.02.23 dummy refactor
					if (currentContainer == null) {
						return true;
					}
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

				// set focus to scene
				SharedResources.stage.setKeyboardFocus(SceneEditorWorkspace.this);

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
		};

		addListener(inputListener);
	}

	@EventHandler
	public void selectExternal (SelectGameObjectExternallyEvent event) {
		selectGameObjectExternally(event.getGameObject());
	}

	@EventHandler
	public void onSave (SaveRequest event) {
		Array<GameObject> components = new Array<>();
		getChildrenHavingComponentClass(getRootGO(), PaintSurfaceComponent.class, components);
		for (GameObject gameObjects : components) {
			PaintSurfaceComponent paintSurfaceComponent = gameObjects.getComponent(PaintSurfaceComponent.class);
			paintSurfaceComponent.saveOnFile();
		}
	}

	@EventHandler
	public void deSelectEternal (DeSelectGameObjectExternallyEvent event) {
		removeFromSelection(event.getGameObject());
	}

	@EventHandler
	public void addToSelectionEvent (AddToSelectionEvent addToSelectionEvent) {
		addToSelection(addToSelectionEvent.getGameObject());
	}

	@EventHandler
	public void removeFromSelectionEvent (RemoveFromSelectionEvent removeFromSelectionEvent) {
		removeFromSelection(removeFromSelectionEvent.getGameObject());
	}

	public void deleteSelected () {
		ObjectSet<GameObject> deleteList = new ObjectSet<>();
		deleteList.addAll(selection);
		requestSelectionClear();

		if (currentContainer != null) {
			SceneUtils.deleteGameObjects(currentContainer, deleteList);
		}
	}

	public void escapePressed() {
		performSelectionClear();
		mapEditorState.escapePressed();
	}

	public void convertSelectedIntoGroup () {
		if (selection.isEmpty() || selection.size == 1) {
			return;
		}

		ObjectSet<GameObject> selectedObjects = new ObjectSet<>();
		selectedObjects.addAll(selection.orderedItems());

		SceneUtils.convertToGroup(currentContainer, selectedObjects);
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
						TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();
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
							TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();
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
							TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();
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

	@Override
	public SavableContainer getContext() {
		return currentContainer;
	}

	public static class ClipboardPayload {
		public Array<GameObject> objects = new Array<>();
		public Array<Vector2> objectWorldPositions = new Array<>();
		public Vector2 cameraPositionAtCopy = new Vector2(0, 0);
		public boolean shouldCut = false;
	}

	public void copySelected () {

		// TODO: 23.02.23 dummy refactor
		if (currentContainer == null) {
			return;
		}

		SceneUtils.copy(gameAsset, selection);
	}

	public void cutSelected () {

		// TODO: 23.02.23 dummy refactor
		if (currentContainer == null) {
			return;
		}

		SceneUtils.cut(gameAsset, selection);
	}

	public void pasteFromClipboard () {

		// TODO: 23.02.23 dummy refactor
		if (currentContainer == null) {
			return;
		}

		boolean hasGameAsset = gameAsset != null;
		boolean hasHierarchyApp = hasGameAsset && SharedResources.appManager.getAppForAsset(SceneHierarchyApp.class, gameAsset) != null;
		boolean hasSmartSelection = hasHierarchyApp && SharedResources.appManager.getAppForAsset(SceneHierarchyApp.class, gameAsset).getHierarchyWidget().getSmartSelection() != null;

		if (hasSmartSelection) {
			SceneHierarchyApp hierarchyApp = SharedResources.appManager.getAppForAsset(SceneHierarchyApp.class, gameAsset);
			HierarchyWidget hierarchyWidget = hierarchyApp.getHierarchyWidget();
			GameObject smartSelection = hierarchyWidget.getSmartSelection();
			SceneUtils.shouldPasteTo(currentContainer, smartSelection);
		} else {
			SceneUtils.shouldPasteToParent(currentContainer);
		}

		SceneUtils.paste(gameAsset);
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

							TransformSettings transformSettings = gameObjectWeArePainting.getTransformSettings();

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
			if (viewportViewSettings.isShowGrid() && !viewportViewSettings.is3D() && !viewportViewSettings.isGridOnTop()) {
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

		HdpiUtils.glViewport((int) currentGlViewport.x, (int) currentGlViewport.y, (int) currentGlViewport.width, (int) currentGlViewport.height);

		if (viewportViewSettings.isShowGrid() && !viewportViewSettings.is3D() && viewportViewSettings.isGridOnTop()) {
			gridRenderer.drawGrid(batch, shapeRenderer);
		}

		batch.begin();

	}

	private Matrix4 tempProj = new Matrix4();
	private Matrix4 tempTrans = new Matrix4();

	private void drawMainRenderer (PolygonBatch batch, float parentAlpha) {
		if (currentContainer == null)
			return;

		renderer.setLayers(getLayerList());
		renderer.update(currentContainer.getSelfObject());
		renderer.render(batch, new RenderState(), currentContainer);
	}


	private Array<GameObject> tempArray = new Array<>();
	public void openSavableContainer (SavableContainer mainScene) {
		if (mainScene == null)
			return;
//		sceneEditorAddon.hierarchy.loadEntityContainer(mainScene);
		currentContainer = mainScene;

		// process all game objects
		gizmos.gizmoList.clear();
		gizmos.gizmoMap.clear();
		gizmos.gizmoList.add(groupSelectionGizmo);

		boolean shouldRegisterRoot = mainScene instanceof Prefab;

		createAndInitGizmos(mainScene, mainScene.getSelfObject(), this, shouldRegisterRoot);

		tempArray.clear();
		for (GameObject gameObject : selection) {
			GameObject childByName = mainScene.getSelfObject().getChildByName(gameObject.getName(), true);
			if (childByName != null) {
				tempArray.add(childByName);
			}
		}

		clearSelection();
		selection.addAll(tempArray);

		selectPropertyHolder(PropertyWrapperProviders.getOrCreateHolder(mainScene));

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

	public void selectAll () {

		// TODO: 23.02.23 dummy refactor
		if (currentContainer == null) {
			return;
		}

		selection.clear();
		Array<GameObject> gameObjects = currentContainer.getGameObjects();
		if (gameObjects != null) {
			for (int i = 0; i < gameObjects.size; i++) {
				selectGameObjectAndChildren(gameObjects.get(i));
			}
		}
	}

	@EventHandler
	public void onGameObjectCreated (GameObjectCreated event) {
		GameObject gameObject = event.getTarget();
		createAndInitGizmos(getRootSceneObject(), gameObject, this, true);
	}

	@EventHandler
	public void onComponentRemove (ComponentRemoved event) {
		removeGizmos(event.getGameObject());
		createAndInitGizmos(getRootSceneObject(), event.getGameObject(), this, true);
	}

	@EventHandler
	public void onComponentUpdated (ComponentUpdated event) {
		AComponent component = event.getComponent();
		if (event.isNotifyUI()) {

			if (!event.isRapid()) {
//				TalosMain.Instance().ProjectController().setDirty();
			}
		}
	}

	@EventHandler
	public void onRoutineUpdated (RoutineUpdated event) {
        GameObject rootGO = getRootGO();
        Array<RoutineRendererComponent> updatedComponents = new Array<>();
		GameAsset<RoutineStageData> routineStageData = event.routineAsset;
		updateRoutinePropertiesForGOs(rootGO, routineStageData, updatedComponents);
		for (RoutineRendererComponent updatedComponent : updatedComponents) {
			SceneUtils.componentUpdated(rootGO, updatedComponent.getGameObject(), updatedComponent, true); //We set rapid to true so it doesn't save
		}


		//Check if any got updateed and we need to save
		for (RoutineRendererComponent updatedComponent : updatedComponents) {
			if (updatedComponent.isRequiresWrite()) {
				AssetRepository.getInstance().assetChanged(gameAsset);
				return;
			}
		}
	}

	private void updateRoutinePropertiesForGOs (GameObject gameObject, GameAsset<RoutineStageData> routineAsset, Array<RoutineRendererComponent> updatedComponents) {
		if (gameObject.hasComponent(RoutineRendererComponent.class)) {
			RoutineRendererComponent component = gameObject.getComponent(RoutineRendererComponent.class);
			if (component.routineInstance != null) {
				if (routineAsset.equals(component.getGameResource())) {
					updatedComponents.add(component);
				}
			}
		}

		Array<GameObject> children = gameObject.getGameObjects();
		if (children != null) {
			for (int i = 0; i < children.size; i++) {
				GameObject child = children.get(i);
				updateRoutinePropertiesForGOs(child, routineAsset, updatedComponents);
			}
		}
	}

	@EventHandler
	public void onGameObjectDeleted (GameObjectDeleted event) {
		GameObject target = event.getTarget();

		GameObject parent = target.getParent();
		parent.removeObject(target);

		// remove gizmos
		removeGizmos(target);

	}

	@EventHandler
	public void onGameObjectNameChanged (GameObjectNameChanged event) {
	}

	@EventHandler
	public void onGameObjectSelectionChanged (GameObjectSelectionChanged event) {
		ObjectSet<GameObject> gameObjects = event.get();

		if (event.get().size == 1) { //Only select gizmos if one is selected
			selectGizmos(gameObjects);
		} else {
			unselectGizmos();
			groupSelectionGizmo.setSelected(true);
		}

		// update internal selection state for viewport widget caused by external selection change
		selection.clear();
		selection.addAll(gameObjects);

		// now for properties

		if (gameObjects.size == 0) {
			// we select the main container then
			if (currentContainer instanceof Scene) {
				Scene scene = (Scene) currentContainer;
				selectPropertyHolder(PropertyWrapperProviders.getOrCreateHolder(scene));
			} else if (currentContainer instanceof Prefab) {
				Prefab prefab = (Prefab) currentContainer;
				selectPropertyHolder(PropertyWrapperProviders.getOrCreateHolder(prefab));
			}
		} else {
			if (gameObjects.size == 1) {
				selectPropertyHolder(PropertyWrapperProviders.getOrCreateHolder(gameObjects.first()));
			} else {
				ObjectSet<IPropertyHolder> tempList = new ObjectSet<>();
				for (GameObject gameObject : gameObjects) {
					tempList.add(PropertyWrapperProviders.getOrCreateHolder(gameObject));
				}
				selectPropertyHolder(PropertyWrapperProviders.getOrCreateHolder(new MultiPropertyHolder<>(tempList)));
				if(aligningToolsPane.getParent() == null) {
					groupSelectionGizmo.getViewportWidget().addActor(aligningToolsPane);
				}
			}
		}

		mapEditorState.update(event);
	}

	@EventHandler
	public void GONameChangeCommand(GONameChangeCommand command) {
		changeGOName(command.getGo(), command.getSuggestedName());
	}

	public void changeGOName (GameObject gameObject, String suggestedName) {
		if(suggestedName == null || suggestedName.isEmpty()) {
			suggestedName = "gameObject";
		}
		if (suggestedName.equals(gameObject.getName()))
			return;

		GameObjectContainer container = currentContainer;
		if(gameObject.getParent() != null) {
			container = gameObject.getParent();
		}
		String finalName = NamingUtils.getNewName(suggestedName, container.getAllGONames());

		String oldName = gameObject.getName();

		gameObject.setName(finalName);

		GameObjectNameChanged event = Notifications.obtainEvent(GameObjectNameChanged.class);
		event.target = gameObject;
		event.oldName = oldName;
		event.newName = finalName;

		Notifications.fireEvent(event);

		SceneUtils.markContainerChanged(container);
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
		if (!selection.isEmpty()) {
			for (GameObject gameObject : selection) {
				if (!gameObject.isEditorVisible()) {
					continue;
				}
				Gizmo gizmo = hitGizmoGameObject(touchToLocal.x, touchToLocal.y, gameObject);
				if (gizmo != null) {
					return false;
				}

			}
		}

		Gizmo gizmo = hitGizmo(touchToLocal.x, touchToLocal.y);
		if (gizmo == null && entityUnderMouse == null) {
			return true;
		}


		if (entityUnderMouse != null && entityUnderMouse.isEditorTransformLocked()) {
			return true;
		}

		if (gizmo != null) {
			// This is for the case when the object is not editor visible, and you drag from it
			if (entityUnderMouse == null) {
				if (gizmo instanceof GroupSelectionGizmo) {
					GroupSelectionGizmo groupSelectionGizmo = (GroupSelectionGizmo) gizmo;
					for (GameObject gameObject : groupSelectionGizmo.getGameObjects()) {
						if (!gameObject.isEditorVisible() || gameObject.isEditorTransformLocked()) {
							return true;
						}
					}
				}
			} else {
				if (gizmo instanceof GroupSelectionGizmo) {

				} else {
					if (!gizmo.getGameObject().isEditorVisible() || gizmo.getGameObject().isEditorTransformLocked()) {
						return true;
					}
				}
			}
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

	public Array<SceneLayer> getLayerList () {
		SceneData sceneData = SharedResources.currentProject.getSceneData();

		return sceneData.getRenderLayers();
	}

	public GameObject getRootGO () {
		if (currentContainer == null)
			return null;
		return currentContainer.getSelfObject();
	}

	public MainRenderer getRenderer () {
		return renderer;
	}

	public String getProjectPath () {
		return projectPath;
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
		// TODO: 11/24/2022 THIS IS A TEMPORARY CHANGES BEFORE TOM's REFACTOR FOR THE WHOLE THING
		for (FileHandle fileHandle : event.getChanges().changed) {
			GameAsset<?> assetForPath = AssetRepository.getInstance().getAssetForPath(fileHandle, true);
			if (assetForPath != null) {
				for (GameAsset.GameAssetUpdateListener listener : assetForPath.listeners) {
					listener.onUpdate();
				}
			}
		}

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
		renderer.setRenderingEntitySelectionBuffer(true);
		renderer.skipUpdates = true;

		renderer.setRenderParentTiles(false);

		PolygonSpriteBatchMultiTexture customBatch = entitySelectionBuffer.getCustomBatch();
		customBatch.setUsingCustomColourEncoding(true);
		customBatch.setProjectionMatrix(camera.combined);
		customBatch.disableBlending();

		customBatch.begin();
		renderer.setCamera(camera);
		drawMainRenderer(customBatch, 1f);

		customBatch.end();
		renderer.skipUpdates = false;
		renderer.setRenderingEntitySelectionBuffer(false);
		customBatch.enableBlending();
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

	@Override
	protected GameObjectContainer getEventContext() {
		return currentContainer;
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
		aligningToolsPane.remove();
		GameObjectSelectionChanged<GameObjectContainer> gameObjectSelectionChanged = Notifications.obtainEvent(GameObjectSelectionChanged.class);
		gameObjectSelectionChanged.set(getEventContext(), selection);
		Notifications.fireEvent(gameObjectSelectionChanged);
	}

	@EventHandler
	public void onGameAssetOpened (GameAssetOpenEvent gameAssetOpenEvent) {
		GameAsset<?> gameAsset = gameAssetOpenEvent.getGameAsset();
		if (gameAsset.type == GameAssetType.SCENE) {
			this.gameAsset = (GameAsset<Scene>) gameAsset;
		}
	}
}
