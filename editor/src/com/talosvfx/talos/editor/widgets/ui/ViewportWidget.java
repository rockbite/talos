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

package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.render.Render;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.maps.LayerType;
import com.talosvfx.talos.runtime.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.utils.EntitySelectionBuffer;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.SpriteTransformGizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.TransformGizmo;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.editor.utils.grid.GridPropertyProvider;
import com.talosvfx.talos.editor.utils.grid.GridRenderer;
import com.talosvfx.talos.editor.utils.grid.RulerRenderer;
import com.talosvfx.talos.editor.widgets.ui.gizmos.Gizmos;
import com.talosvfx.talos.editor.widgets.ui.gizmos.GroupSelectionGizmo;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.scene.components.MapComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.function.Supplier;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.fromDirectoryView;
import static com.talosvfx.talos.editor.utils.InputUtils.ctrlPressed;

public abstract class ViewportWidget extends Table {

	private static final Logger logger = LoggerFactory.getLogger(ViewportWidget.class);

	@Getter
	private final VisImageButton dropdownForWorld;

	protected Matrix4 emptyTransform = new Matrix4();
	private Matrix4 prevTransform = new Matrix4();
	private Matrix4 prevProjection = new Matrix4();


	protected float maxZoom = 0.01f;
	protected float minZoom = 200f;

	protected ShapeRenderer shapeRenderer;
	protected final EntitySelectionBuffer entitySelectionBuffer;

	private float gridSize;
	private float worldWidth = 1f;

	private Vector3 tmp = new Vector3();
	private Color tmpColor = new Color();
	private Vector2 vec2 = new Vector2();

	protected InputListener inputListener;
	protected boolean isDragging;
	private boolean inputListenersEnabled = true;

	protected Gizmos gizmos = new Gizmos();

	public OrderedSet<GameObject> selection = new OrderedSet<>();
	protected GameObject entityUnderMouse;

	protected boolean locked;

	protected GridPropertyProvider gridPropertyProvider;

	protected GridRenderer gridRenderer;
	protected RulerRenderer rulerRenderer;


	protected GroupSelectionGizmo groupSelectionGizmo;
	private boolean panRequiresSpace = false;

	@Getter
	protected ViewportViewSettings viewportViewSettings;

    public ViewportWidget() {
		 viewportViewSettings = new ViewportViewSettings(this);

        shapeRenderer = Render.instance().shapeRenderer();
		entitySelectionBuffer = new EntitySelectionBuffer();
		initializeGridPropertyProvider();
		gridRenderer = new GridRenderer(gridPropertyProvider, this);
		rulerRenderer = new RulerRenderer(gridPropertyProvider, this);

		setTouchable(Touchable.enabled);


		addPanListener();
		addGizmoListener();

		groupSelectionGizmo = new GroupSelectionGizmo(this);
		gizmos.gizmoList.add(groupSelectionGizmo);

		Table fullscreenUITable = new Table();
		fullscreenUITable.top().defaults().top();
		fullscreenUITable.setFillParent(true);

		Table topToolbar = new Table();

		int rulerPadding = 20;
		rulerPadding += 5; //additional padding

		fullscreenUITable.add(topToolbar).growX().height(20).padTop(rulerPadding).padLeft(rulerPadding).padRight(4);

		topToolbar.right();

		float iconSize = 15;


		dropdownForWorld = new VisImageButton(SharedResources.skin.getDrawable("eye"));
		dropdownForWorld.getImage().setScaling(Scaling.fill);

		VisTable viewTable = createViewSettingsDialog();

		dropdownForWorld.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				if (dropdownForWorld.isChecked()) {
					addActor(viewTable);

					Vector2 vector2 = new Vector2();
					vector2.set(dropdownForWorld.getWidth(), 0);
					dropdownForWorld.localToActorCoordinates(ViewportWidget.this, vector2);
					viewTable.pack();
					viewTable.setPosition(vector2.x - viewTable.getWidth(), vector2.y - viewTable.getHeight());

				} else {
					viewTable.remove();
				}
			}
		});
		topToolbar.add(dropdownForWorld).size(iconSize);

		addActor(fullscreenUITable);
	}

	private VisTable createViewSettingsDialog () {
		VisTable visTable = new VisTable();
		visTable.pad(10);

		visTable.defaults();

		visTable.setBackground(SharedResources.skin.getDrawable("background-fill"));
		visTable.setTouchable(Touchable.enabled);
		visTable.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
			}
		});


		VisTable cameraTable = new VisTable();
		cameraTable.defaults().pad(5);
		cameraTable.left().defaults().left();

		VisLabel cameraLabel = new VisLabel("Camera");

		VisLabel cameraTypeLabel = new VisLabel("Type");
		VisSelectBox<String> cameraTypeBox = new VisSelectBox<>();
		cameraTypeBox.setItems("Orthographic", "Perspective");
		cameraTypeBox.setSelected(viewportViewSettings.getCurrentCamera() instanceof OrthographicCamera ? "Orthographic" : "Perspective");
		cameraTypeBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (cameraTypeBox.getSelected().equals("Orthographic")) {
					viewportViewSettings.setOrthographic();
				} else if (cameraTypeBox.getSelected().equals("Perspective")) {
					viewportViewSettings.setPerspective();
				}
			}
		});

		VisLabel fovLabel = new VisLabel("Fov");
		VisTextField fovField = new VisTextField(viewportViewSettings.getFov()+"");
		fovField.setTextFieldFilter(new FloatRangeDigitFilter());
		fovField.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				String text = fovField.getText();
				if (text != null && !text.isEmpty()) {
					viewportViewSettings.setFov(Float.parseFloat(text));
				}
			}
		});

		VisLabel nearLabel = new VisLabel("Near");
		VisTextField nearField  = new VisTextField(viewportViewSettings.getNear()+"");
		nearField.setTextFieldFilter(new FloatRangeDigitFilter());
		nearField.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				String text = nearField.getText();
				if (text != null && !text.isEmpty()) {
					viewportViewSettings.setNear(Float.parseFloat(text));
				}
			}
		});

		VisLabel farLabel = new VisLabel("Far");
		VisTextField farField  = new VisTextField(viewportViewSettings.getFar()+"");
		farField.setTextFieldFilter(new FloatRangeDigitFilter());
		farField.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				String text = farField.getText();
				if (text != null && !text.isEmpty()) {
					viewportViewSettings.setFar(Float.parseFloat(text));
				}
			}
		});

		VisCheckBox checkbox3D = new VisCheckBox("3D");
		VisCheckBox zUpCheckbox = new VisCheckBox("+Z up");

		checkbox3D.setChecked(viewportViewSettings.is3D());
		zUpCheckbox.setChecked(viewportViewSettings.isPositiveZUp());

		checkbox3D.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				viewportViewSettings.set3D(checkbox3D.isChecked());
			}
		});
		zUpCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				viewportViewSettings.setPositiveZUp(zUpCheckbox.isChecked());
			}
		});

		cameraTable.add(cameraLabel).colspan(2);
		cameraTable.row();

		cameraTable.add(cameraTypeLabel);
		cameraTable.add(cameraTypeBox);


		cameraTable.row();
		cameraTable.add(fovLabel);
		cameraTable.add(fovField);

		cameraTable.row();
		cameraTable.add(nearLabel);
		cameraTable.add(nearField);

		cameraTable.row();
		cameraTable.add(farLabel);
		cameraTable.add(farField);

		cameraTable.row();
		cameraTable.add(checkbox3D);
		cameraTable.add(zUpCheckbox);

		VisTable axisTable = new VisTable();
		axisTable.defaults().pad(5);
		axisTable.left().defaults().left();

		VisLabel axisLabel = new VisLabel("Axis");

		axisTable.add(axisLabel).colspan(2);
		axisTable.row();

		VisCheckBox axisCheckbox = new VisCheckBox("Axis");
		VisCheckBox gridCheckbox = new VisCheckBox("Grid");

		axisCheckbox.setChecked(viewportViewSettings.isShowAxis());
		gridCheckbox.setChecked(viewportViewSettings.isShowGrid());

		axisCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				viewportViewSettings.setShowAxis(axisCheckbox.isChecked());
			}
		});
		gridCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				viewportViewSettings.setShowGrid(gridCheckbox.isChecked());
			}
		});

		VisLabel gridSizeLabel = new VisLabel("Grid Size");
		VisTextField gridSizeField = new VisTextField(viewportViewSettings.getGridSize() + "");
		gridSizeField.setTextFieldFilter(new FloatRangeDigitFilter());

		gridSizeField.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				String text = gridSizeField.getText();
				if (text != null && !text.isEmpty()) {
					viewportViewSettings.setGridSize(Float.parseFloat(text));
				}
			}
		});

		axisTable.add(axisCheckbox).colspan(2);
		axisTable.row();
		axisTable.add(gridCheckbox).colspan(2);
		axisTable.row();

		axisTable.add(gridSizeLabel);
		axisTable.add(gridSizeField);


		visTable.add(cameraTable).growX();
		visTable.row();
		visTable.add(new VisImage("white")).growX().height(1);
		visTable.row();
		visTable.add(axisTable).growX();

		visTable.pack();

		return visTable;
	}

	public void unselectGizmos () {
		selectGizmos(new ObjectSet<>());
	}

	public void selectGizmos (ObjectSet<GameObject> gameObjects) {
		for (Gizmo gizmo : this.gizmos.gizmoList) {
			gizmo.setSelected(false);
		}

		if (gameObjects.size == 1) {
			Array<Gizmo> gizmos = this.gizmos.gizmoMap.get(gameObjects.first());
			if (gizmos != null) {
				for (Gizmo gizmo : gizmos) {
					gizmo.setSelected(true);
				}
			}
		} else {
			for (GameObject gameObject : gameObjects) {
				Array<Gizmo> gizmos = this.gizmos.gizmoMap.get(gameObject);
				if (gizmos != null) {
					for (Gizmo gizmo : gizmos) {
						if (gizmo.isMultiSelect()) {
							gizmo.setSelected(true);
						}
					}
				}
			}
		}
	}

	protected InputListener addGizmoListener () {
		InputListener gizmoListener = new InputListener() {

			Gizmo hitGizmo = null;

			int countOfSameTouchDown = 0;
			boolean dragOnFirstTime = true;

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Vector2 hitCords = getWorldFromLocal(x, y);
				if (event.isCancelled()) return false;

				if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && canMoveAround()) return false;

				if (locked) {
					return true;
				}

				boolean hasSelection = selection.size > 0;
				boolean hasEntityUnderMouse = entityUnderMouse != null;

				if (hasSelection) {
					//Check if we keep selection
					if (hasEntityUnderMouse && selection.contains(entityUnderMouse)) {
						//Same shit, but lets update our gizmo

						countOfSameTouchDown++;

						hitGizmo = hitGizmoGameObject(hitCords.x, hitCords.y, selection.first());

						if (hitGizmo != null) {
							hitGizmo.touchDown(hitCords.x, hitCords.y, button);
						}

						return true;
					} else {
						Gizmo testGizmo = hitGizmoGameObject(hitCords.x, hitCords.y, selection.first());

						//We aren't over the pixel for entity, but we hit its gizmo
						if (canTouchGizmo(testGizmo)) {
							countOfSameTouchDown++;

							hitGizmo = testGizmo;

							hitGizmo.touchDown(hitCords.x, hitCords.y, button);
							return true;

						} else {
							countOfSameTouchDown = 0;

							if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
								if (entityUnderMouse != null && !entityUnderMouse.isEditorTransformLocked()) {
									addToSelection(entityUnderMouse);
								}

							} else {
								//We aren't over the pixel or the gizmo, unselect
								requestSelectionClear();
								if (hasEntityUnderMouse && !entityUnderMouse.isEditorTransformLocked()) {
									hitGizmo = hitGizmoGameObject(hitCords.x, hitCords.y, entityUnderMouse);
									selectGameObject(entityUnderMouse);

									if (hitGizmo != null) {
										hitGizmo.touchDown(hitCords.x, hitCords.y, button);
									}
									getStage().setKeyboardFocus(ViewportWidget.this);
									event.handle();
									return true;
								}
							}

						}
					}
				} else {
					//We don't have a selection so we just add if we are under or if we hit a gizmo

					GameObject touchDownedGameObject = null;

					if (hasEntityUnderMouse) {
						touchDownedGameObject = entityUnderMouse;
						if(!touchDownedGameObject.active || touchDownedGameObject.isEditorTransformLocked()){
							return true;
						}
						hitGizmo = hitGizmoGameObject(hitCords.x, hitCords.y, touchDownedGameObject);
						selectGameObject(touchDownedGameObject);

						if (hitGizmo != null) {
							hitGizmo.touchDown(hitCords.x, hitCords.y, button);
						}
						getStage().setKeyboardFocus(ViewportWidget.this);
						event.handle();
						return true;

					} else {
						hitGizmo = hitGizmo(hitCords.x, hitCords.y);
						if (canTouchGizmo(hitGizmo)) {
							hitGizmo.touchDown(hitCords.x, hitCords.y, button);
							getStage().setKeyboardFocus(ViewportWidget.this);
							event.handle();
							return true;
						}
					}

				}

				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && isDragging) return;

				if (locked) {
					return;
				}

				if (ctrlPressed()) {
					return;
				}

				if (hitGizmo instanceof GroupSelectionGizmo || (dragOnFirstTime || countOfSameTouchDown >= 1)) {
					Vector2 hitCords = getWorldFromLocal(x, y);

					for (Gizmo gizmo : gizmos.gizmoList) {
						if (gizmo.isSelected() && gizmo == hitGizmo) {
							gizmo.touchDragged(hitCords.x, hitCords.y);
						}
					}
				}

				super.touchDragged(event, x, y, pointer);

			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (hitGizmo != null) {
					Vector2 hitCords = getWorldFromLocal(x, y);
					hitGizmo.touchUp(hitCords.x, hitCords.y);
				}

				hitGizmo = null;

				if (canMoveAround()) return;

				if (locked) {
					return;
				}


				super.touchUp(event, x, y, pointer, button);
			}

			@Override
			public boolean mouseMoved (InputEvent event, float x, float y) {
				if (canMoveAround()) return super.mouseMoved(event, x, y);

				if (locked) {
					return true;
				}

				Vector2 hitCords = getWorldFromLocal(x, y);
				for (int i = 0; i < ViewportWidget.this.gizmos.gizmoList.size; i++) {
					Gizmo item = ViewportWidget.this.gizmos.gizmoList.get(i);
					if (item.isSelected()) {
						item.mouseMoved(hitCords.x, hitCords.y);
					}
				}

				return super.mouseMoved(event, x, y);
			}

			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if (locked) {
					return true;
				}

				for (Gizmo gizmo : ViewportWidget.this.gizmos.gizmoList) {
					if (gizmo.isSelected()) {
						gizmo.keyDown(event, keycode);
					}
				}
				return super.keyDown(event, keycode);
			}

			@Override
			public boolean keyUp (InputEvent event, int keycode) {
				if (locked) {
					return true;
				}

				for (Gizmo gizmo : ViewportWidget.this.gizmos.gizmoList) {
					if (gizmo.isSelected()) {
						gizmo.keyUp(event, keycode);
					}
				}
				return super.keyUp(event, keycode);
			}
		};

		addListener(gizmoListener);
		return gizmoListener;
	}

	private boolean canTouchGizmo (Gizmo testGizmo) {
		if (testGizmo == null) return false;
		if (testGizmo instanceof GroupSelectionGizmo) return true;

		if (!testGizmo.getGameObject().isEditorVisible()) return false;

		return true;
	}

	private void removeGizmos () {
		//todo clear on change, not on start
		for (Gizmo gizmo : gizmos.gizmoList) {
			gizmo.remove();
		}
		gizmos.gizmoList.clear();
		gizmos.gizmoMap.clear();

		//Always put this one back in
		gizmos.gizmoList.add(groupSelectionGizmo);
	}

	public void removeGizmos (GameObject gameObject) {
		Array<Gizmo> list = gizmos.gizmoMap.get(gameObject);
		if (list == null) {
			System.out.println("No gimzmo for " + gameObject);
			return;
		} else {
			System.out.println("removing gizmos for " + gameObject);
		}
		for (Gizmo gizmo : list) {
			gizmo.notifyRemove();
			gizmo.remove();
		}
		gizmos.gizmoList.removeAll(list, true);
		gizmos.gizmoMap.remove(gameObject);
	}

	public void initGizmos (GameObjectContainer gameObjectContainer, GameObject gameObject, ViewportWidget parent) {
		makeGizmosFor(gameObjectContainer, gameObject, parent);
		Array<GameObject> childObjects = gameObject.getGameObjects();
		if (childObjects != null) {
			for (GameObject childObject : childObjects) {
				makeGizmosFor(gameObjectContainer, childObject, parent);
				initGizmos(gameObjectContainer,childObject, parent);
			}
		}
	}

	public void initGizmos (GameObjectContainer gameObjectContainer, ViewportWidget parent) {
		Array<GameObject> childObjects = gameObjectContainer.getGameObjects();
		if (childObjects != null) {
			for (GameObject childObject : childObjects) {
				initGizmos(gameObjectContainer, childObject, parent);
			}
		}
	}

	public void makeGizmosFor (GameObjectContainer gameObjectContainer, GameObject gameObject, ViewportWidget parent) {
		ObjectMap<GameObject, Array<Gizmo>> gizmoMap = parent.gizmos.gizmoMap;
		Array<Gizmo> gizmoList = parent.gizmos.gizmoList;
		if (gizmoMap.containsKey(gameObject)) {
			return;
		}

		ObjectMap<Class<? extends Gizmo>, Gizmo> gameObjectGizmoMap = new ObjectMap<>();

		Iterable<AComponent> components = gameObject.getComponents();
		for (AComponent component : components) {
			Array<Gizmo> gizmos = GizmoRegister.makeGizmosFor(component);


			for (Gizmo gizmo : gizmos) {
				gizmo.setViewport(parent);


				if (gizmo != null) {
					gameObjectGizmoMap.put(gizmo.getClass(), gizmo);

					gizmo.setGameObject(gameObjectContainer, gameObject);

					Array<Gizmo> list = gizmoMap.get(gameObject);
					if (list == null)
						list = new Array<>();

					gizmoMap.put(gameObject, list);
					gizmoList.add(gizmo);
					list.add(gizmo);
				}
			}
		}

		//Lets check for 'smart' linking
		if (gameObjectGizmoMap.containsKey(TransformGizmo.class) && gameObjectGizmoMap.containsKey(SpriteTransformGizmo.class)) {
			TransformGizmo transformGizmo = (TransformGizmo)gameObjectGizmoMap.get(TransformGizmo.class);
			SpriteTransformGizmo smartTransformGizmo = (SpriteTransformGizmo)gameObjectGizmoMap.get(SpriteTransformGizmo.class);
			transformGizmo.linkToSmart(smartTransformGizmo);
		}



	}

	protected Gizmo hitGizmoGameObject (float x, float y, GameObject gameObject) {
		gizmos.gizmoList.sort(new Comparator<Gizmo>() {
			@Override
			public int compare (Gizmo o1, Gizmo o2) {
				return Integer.compare(o1.getPriority(), o2.getPriority());
			}
		});

		for (Gizmo gizmo : gizmos.gizmoList) {
			if (!gizmo.isControllingGameObject(gameObject)) continue;
			if (gizmo.hit(x, y))
				return gizmo;
		}

		return null;
	}

	protected Gizmo hitGizmo (float x, float y) {
		gizmos.gizmoList.sort(new Comparator<Gizmo>() {
			@Override
			public int compare (Gizmo o1, Gizmo o2) {
				return Integer.compare(o1.getPriority(), o2.getPriority());
			}
		});

		for (Gizmo gizmo : gizmos.gizmoList) {
			if (gizmo.hit(x, y)) {
				return gizmo;
			}
		}

		return null;
	}


	private Array<EventListener> storedListeners = new Array<>();
	public void disableListeners () {
		DelayedRemovalArray<EventListener> listeners = getListeners();
		storedListeners.addAll(listeners);
		clearListeners();
	}

	public void restoreListeners () {
		for (EventListener storedListener : storedListeners) {
			addListener(storedListener);
		}
	}

	// flag to check if already scrolled once in a frame.
	boolean scrolledInFrame = false;
	private Vector2 cameraPosTmp = new Vector2();
	private Vector2 mousePosTmp = new Vector2();
	protected void addPanListener () {
		addListener(new InputListener() {
			boolean canPan = false;

			@Override
			public boolean scrolled (InputEvent event, float x, float y, float amountX, float amountY) {
//				float currWidth = camera.viewportWidth * camera.zoom;
//				float nextWidth = currWidth * (1f + amountY * 0.1f);
//				float nextZoom = nextWidth / camera.viewportWidth;

				float currentZoom = viewportViewSettings.getZoom();
				float stepScale = zoomStepScale(currentZoom, minZoom, maxZoom);
				currentZoom += amountY * stepScale;
				currentZoom = MathUtils.clamp(currentZoom, minZoom, 10);
				viewportViewSettings.setZoom(currentZoom);

				if (amountY < 0 && !scrolledInFrame && !viewportViewSettings.is3D()) {
					Vector3 cameraPos = viewportViewSettings.getCurrentCamera().position;
					Vector2 cameraPosTmp = new Vector2(cameraPos.x, cameraPos.y);
					Vector2 mousePosTmp = new Vector2(getMouseCordsOnScene());
					float current = (currentZoom - minZoom) / (maxZoom - minZoom);
					current = current > 0 ? 1 : 0;
					cameraPosTmp.lerp(mousePosTmp, 0.05f * current);
					//cameraPos.set(cameraPosTmp.x, cameraPosTmp.y, cameraPos.z);
					scrolledInFrame = true;
				}

				return true;
			}

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

				if(panRequiresSpace && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) return false;

				canPan = canMoveAround();
				InputAdapter inputAdapter = viewportViewSettings.getCurrentCameraControllerSupplier().get();
				inputAdapter.touchDown((int)x, (int)y, pointer, button);
				return !event.isHandled();
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				isDragging = false;
				InputAdapter inputAdapter = viewportViewSettings.getCurrentCameraControllerSupplier().get();

				inputAdapter.touchUp((int)x, (int)y, pointer, button);
				canPan = false;
			}


			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				// can't move around disable dragging
				if (!canPan)
					return;
				isDragging = true;
				InputAdapter inputAdapter = viewportViewSettings.getCurrentCameraControllerSupplier().get();

				inputAdapter.touchDragged((int)x, (int)y, pointer);
			}
		});
	}

	Vector2 temp = new Vector2();

	private void enableClickListener () {
		if (inputListener == null)
			return;
		if (inputListenersEnabled)
			return;
		inputListenersEnabled = true;
		addListener(inputListener);
	}

	private void disableClickListener () {
		if (inputListener == null)
			return;
		if (!inputListenersEnabled)
			return;
		inputListenersEnabled = false;
		removeListener(inputListener);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		clearScreen(batch, parentAlpha);
		batch.end();


		localToScreenCoordinates(temp.set(0, 0));
		int x = (int)temp.x;
		int y = (int)temp.y;

		localToScreenCoordinates(temp.set(getWidth(), getHeight()));

		int x2 = (int)temp.x;
		int y2 = (int)temp.y;

		int ssWidth = x2 - x;
		int ssHeight = y - y2;

		HdpiUtils.glViewport(x, Gdx.graphics.getHeight() - y, ssWidth, ssHeight);


		float aspect = getWidth() / getHeight();
		viewportViewSettings.update(aspect);

		prevTransform.set(batch.getTransformMatrix());
		prevProjection.set(batch.getProjectionMatrix());

		Camera camera = viewportViewSettings.getCurrentCameraSupplier().get();

		batch.setProjectionMatrix(camera.combined);
		batch.setTransformMatrix(emptyTransform);

		batch.begin();
        if (batch instanceof PolygonBatch) {
            drawContent((PolygonBatch)batch, parentAlpha);
        }

        HdpiUtils.glViewport(x, Gdx.graphics.getHeight() - y, ssWidth, ssHeight);

        if (!locked) {
            drawGizmos(batch, parentAlpha);
        }

		batch.end();

		HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		batch.setProjectionMatrix(prevProjection);
		batch.setTransformMatrix(prevTransform);
		batch.begin();

		getEntityUnderMouse();

//		Debug entity secltion
//		if (entityUnderMouse != null) {
//			batch.draw(entitySelectionBuffer.getFrameBuffer().getColorBufferTexture(), getX(), getY(), getWidth(), getHeight(), 0, 0, 1, 1);
//			System.out.println(entityUnderMouse.uuid.toString() + " " + this.getClass());
//		}

		boolean debugEntityIDS = false;

		if (debugEntityIDS) {
			BitmapFont bitmapFont = new BitmapFont();

			for (GameObject gameObject : selection) {
				if (gameObject.hasComponent(TransformComponent.class)) {
					Vector2 entityPos = new Vector2();
					TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
					entityPos.set(transformComponent.worldPosition.x, transformComponent.worldPosition.y);
					Vector2 localFromWorld = getLocalFromWorld(entityPos.x, entityPos.y);
					float entityX = localFromWorld.x;
					float entityY = localFromWorld.y;
					bitmapFont.setColor(1, 1, 1, 1);
					batch.setColor(1, 1, 1, 1);
					bitmapFont.draw(batch, gameObject.getName() + " " + gameObject.uuid.toString(), entityX, entityY);
				}
			}

			batch.flush();

			bitmapFont.dispose();
		}

		if (!viewportViewSettings.is3D()) {
			rulerRenderer.configureRulers();
		}

		super.draw(batch, parentAlpha);
	}

	 protected void clearScreen(Batch batch, float parentAlpha) {
		 tmpColor.set(batch.getColor());
		 Color backgroundColor = gridRenderer.gridPropertyProvider.getBackgroundColor();
		 batch.setColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, parentAlpha);
		 batch.draw(SharedResources.skin.getRegion("white"), 0, 0, getWidth(), getHeight());
		 batch.setColor(tmpColor);
	 }

	protected void getEntityUnderMouse () {
		Vector2 touchSpace = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		Vector2 uiSpace = screenToLocalCoordinates(touchSpace);

		uiSpace.x /= getWidth();
		uiSpace.y /= getHeight();

		Color color = entitySelectionBuffer.getPixelAtNDC(uiSpace);

		GameObject root = getRootSceneObject();
		if (root != null) {
			entityUnderMouse = findEntityForColourEncodedUUID(color, root);
		} else {
			entityUnderMouse = null;
		}

	}

	protected GameObject getRootSceneObject () {
		return null;
	}

	protected GameObject findEntityForColourEncodedUUID (Color color, GameObject object) {
		Color colourForEntityUUID = EntitySelectionBuffer.getColourForEntityUUID(object);


		if (rgbCompare(color, (colourForEntityUUID))) {
			return object;
		} else {
			if (object.getGameObjects() != null) {
				for (GameObject childGameObject : object.getGameObjects()) {
					GameObject childObjectFound = findEntityForColourEncodedUUID(color, childGameObject);
					if (childObjectFound != null) {
						return childObjectFound;
					}
				}
			}
		}

		if (object.hasComponent(MapComponent.class)) {
			MapComponent mapComponent = object.getComponent(MapComponent.class);
			for (int i = 0; i < mapComponent.getLayers().size; i++) {
				TalosLayer talosLayer = mapComponent.getLayers().get(i);
				if (talosLayer.getType() == LayerType.DYNAMIC_ENTITY) {
					Array<GameObject> layerRootEntities = talosLayer.getRootEntities();
					for (int j = 0; j < layerRootEntities.size; j++) {
						GameObject gameObject = layerRootEntities.get(j);
						GameObject mapEntityForColourEncodedUUID = findEntityForColourEncodedUUID(color, gameObject);



						if (mapEntityForColourEncodedUUID != null) {
							return mapEntityForColourEncodedUUID;
						}
					}
				}
			}
		}

		return null;
	}

	private boolean rgbCompare (Color color, Color colourForEntityUUID) {
		int inR = (int)(color.r * 256);
		int inG = (int)(color.g * 256);
		int inB = (int)(color.b * 256);

		int testR = (int)(colourForEntityUUID.r * 256);
		int testG = (int)(colourForEntityUUID.g * 256);
		int testB = (int)(colourForEntityUUID.b * 256);

		if (inR != testR) return false;
		if (inG != testG) return false;
		if (inB != testB) return false;
		return true;
	}

	protected void drawGroup (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}

    public abstract void drawContent(PolygonBatch batch, float parentAlpha);

	@Override
	public void act (float delta) {
		super.act(delta);

		scrolledInFrame = false;

		if (isDragging) {
			CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.GRABBED);
			disableClickListener();
		} else {
			enableClickListener();
		}

		for (int i = 0; i < this.gizmos.gizmoList.size; i++) {
			Gizmo gizmo = this.gizmos.gizmoList.get(i);
			gizmo.act(delta);
		}
	}

	protected void drawAxis () {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setProjectionMatrix(camera.combined);

		shapeRenderer.setColor(Color.valueOf("E33750")); //x
		shapeRenderer.line(-100, 0, 0, 100, 0, 0);
		shapeRenderer.setColor(Color.valueOf("88D407")); //Y
		shapeRenderer.line(0, -100, 0, 0, 100, 0);
		shapeRenderer.setColor(Color.valueOf("2F82DF")); //z
		shapeRenderer.line(0, 0, -100, 0, 0, 100);

		shapeRenderer.end();
	}

	// allow moving around if space bar is pressed and is in viewport or has dragged from viewport
	protected boolean canMoveAround() {
		return Gdx.input.isKeyPressed(Input.Keys.SPACE) || (isDragging);
	}


	public void setWorldSize (float worldWidth) {
		this.viewportViewSettings.setWorldWidth(worldWidth);
		this.worldWidth = worldWidth;
		updateNumbers();
	}

	private void updateNumbers () {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		viewportViewSettings.setZoom(worldWidth / camera.viewportWidth);
		gridSize = worldWidth / 40f;
		float minWidth = gridSize * 4f;
		float maxWidth = worldWidth * 10f;
		minZoom = minWidth / camera.viewportWidth;
		maxZoom = maxWidth / camera.viewportWidth;
		camera.update();
	}


	private void getViewportBounds (Rectangle out) {
		localToScreenCoordinates(temp.set(0, 0));
		int x = (int)temp.x;
		int y = (int)temp.y;

		localToScreenCoordinates(temp.set(getWidth(), getHeight()));

		int x2 = (int)temp.x;
		int y2 = (int)temp.y;

		int ssWidth = x2 - x;
		int ssHeight = y - y2;

		y = Gdx.graphics.getHeight() - y;

		out.set(x, y, ssWidth, ssHeight);
	}

	public Vector2 getLocalFromWorld (float x, float y) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		getViewportBounds(Rectangle.tmp);
		camera.project(tmp.set(x, y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		tmp.y = Gdx.graphics.getHeight() - tmp.y;
		Vector2 vector2 = screenToLocalCoordinates(new Vector2(tmp.x, tmp.y));
		vec2.set(vector2);

		return vec2;
	}

	public Vector2 getWorldFromLocal (float x, float y) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		Vector2 vector2 = localToScreenCoordinates(new Vector2(x, y));

		getViewportBounds(Rectangle.tmp);

		camera.unproject(tmp.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);

		vec2.set(tmp.x, tmp.y);

		return vec2;
	}

	protected Vector3 getWorldFromLocal (Vector3 vec) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		Vector2 vector2 = localToScreenCoordinates(new Vector2(vec.x, vec.y));

		getViewportBounds(Rectangle.tmp);

		camera.unproject(vec.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		return vec;
	}

	public Vector3 getTouchToWorld (float x, float y) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		Vector3 vec = new Vector3(x, y, 0);

		getViewportBounds(Rectangle.tmp);

		camera.unproject(vec.set(vec.x, vec.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		return vec;
	}

	protected float pixelToWorld (float pixelSize) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		tmp.set(0, 0, 0);
		camera.unproject(tmp);
		float baseline = tmp.x;

		tmp.set(pixelSize, 0, 0);
		camera.unproject(tmp);
		float pos = tmp.x;

		return Math.abs(pos - baseline) * (getStage().getWidth() / getWidth()); //TODO: I am sure there is a better way to do this
	}

	public float getWorldWidth () {
		return worldWidth;
	}

	public void requestSelectionClear () {
		for (GameObject gameObject : selection) {
			if (gizmos.gizmoMap.containsKey(gameObject)) {
				Array<Gizmo> gizmo = gizmos.gizmoMap.get(gameObject);
				for (int j = 0; j < gizmo.size; j++) {
					gizmo.get(j).setSelected(false);
				}
			}
		}
		clearSelection();
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(getEventContext(), selection));
	}

	protected void clearSelection () {
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

				GameObject gameObject = gizmo.getGameObject();
				if (gameObject.isEditorTransformLocked()) continue;

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

	public void removeFromSelection (GameObject gameObject) {
		selection.remove(gameObject);
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(getEventContext(), selection));
	}

	public void addToSelection (GameObject gameObject) {
		if (!hierarchicallyContains(gameObject)) {
			selection.add(gameObject);
		}
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(getEventContext(), selection));
	}

	// Checks if gameObject or its ancestors are already in the selection or not
	private boolean hierarchicallyContains (GameObject gameObject) {
		GameObject temp = gameObject;
		while (temp != null) {
			if (selection.contains(temp)) return true;
			temp = temp.parent;
		}
		return false;
	}

	protected void setSelection (Array<GameObject> gameObjects) {
		selection.clear();

		for (GameObject gameObject : gameObjects) {
			selection.add(gameObject);
		}

		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(getEventContext(), selection));
	}

	protected void selectGameObjectAndChildren (GameObject gameObject) {
		selection.add(gameObject);

		Array<GameObject> children = gameObject.getGameObjects();

		if (children != null) {
			for (int i = 0; i < children.size; i++) {
				selectGameObjectAndChildren(children.get(i));
			}
		}
	}

	protected boolean deselectOthers (GameObject exceptThis) {
		if (selection.size > 1 && selection.contains(exceptThis)) {
			selection.clear();
			selection.add(exceptThis);
			Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(getEventContext(), selection));
			return true;
		}

		return false;
	}

	protected abstract Object getEventContext();

	public void selectGameObjectExternally (GameObject gameObject) {
		if (fromDirectoryView)
			return;

		selectGameObject(gameObject);
	}

	private void selectGameObject (GameObject gameObject) {
		if (gameObject == null)
			return;
		Array<GameObject> tmp = new Array<>();
		tmp.add(gameObject);

		setSelection(tmp);
	}

	private void drawGizmos (Batch batch, float parentAlpha) {
		float zoom = viewportViewSettings.getZoom();
		for (int i = 0; i < this.gizmos.gizmoList.size; i++) {
			Gizmo gizmo = this.gizmos.gizmoList.get(i);
			gizmo.setSizeForUIElements(getWidth(),getWorldWidth() * zoom);

			gizmo.draw(batch, parentAlpha);
		}
	}

	public void lockGizmos () {
		locked = true;
	}

	public void unlockGizmos () {
		locked = false;
	}


	protected void beginEntitySelectionBuffer () {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();
		entitySelectionBuffer.begin(camera);
	}
	protected void endEntitySelectionBuffer() {
		entitySelectionBuffer.end();

	}

	protected void drawEntitiesForSelection () {
	}

	public abstract void initializeGridPropertyProvider ();

	public void panRequiresSpace(boolean panRequiresSpace) {
		this.panRequiresSpace = panRequiresSpace;
	}

	public void resetToDefaults () {


	}

	public Vector2 getMouseCordsOnScene () {
		final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		this.screenToLocalCoordinates(vec);
		Vector2 local = getWorldFromLocal(vec.x, vec.y);
		return local;
	}
	public Vector3 getCameraPos () {
		ViewportViewSettings settings = viewportViewSettings.getViewportWidget().viewportViewSettings;
		return new Vector3(settings.getCurrentCamera().position);
	}

	public float getCameraZoom() {
		ViewportViewSettings settings = viewportViewSettings.getViewportWidget().viewportViewSettings;
		return settings.getZoom();
	}

	public void setCameraPos (Vector3 pos) {
		ViewportViewSettings settings = viewportViewSettings.getViewportWidget().viewportViewSettings;
		settings.getCurrentCamera().position.set(pos);
	}

	public void setCameraZoom (float zoom) {
		ViewportViewSettings settings = viewportViewSettings.getViewportWidget().viewportViewSettings;
		settings.setZoom(zoom);
	}

	private static float zoomStepScale (float currentZoom, float minZoom, float maxZoom) {
		float current = (currentZoom - minZoom) / (maxZoom - minZoom);
		current = MathUtils.clamp(current, 0, 1); // won't happen, but just in case lol
		float scale = Interpolation.slowFast.apply(0.0025f,1, current);
		return scale;
	}
}
