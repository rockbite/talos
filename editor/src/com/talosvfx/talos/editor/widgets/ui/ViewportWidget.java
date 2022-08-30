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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedSet;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.LayerType;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.addons.scene.utils.EntitySelectionBuffer;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.SpriteTransformGizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.TransformGizmo;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.utils.CameraController;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.editor.widgets.ui.gizmos.Gizmos;
import com.talosvfx.talos.editor.widgets.ui.gizmos.GroupSelectionGizmo;

import java.util.Comparator;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.fromDirectoryView;

public abstract class ViewportWidget extends Table {

	protected OrthographicCamera camera;

	protected Matrix4 emptyTransform = new Matrix4();
	private Matrix4 prevTransform = new Matrix4();
	private Matrix4 prevProjection = new Matrix4();

	public CameraController cameraController;

	protected Color bgColor = new Color(Color.BLACK);

	protected float maxZoom = 0.01f;
	protected float minZoom = 200f;

	protected ShapeRenderer shapeRenderer;
	protected final EntitySelectionBuffer entitySelectionBuffer;

	private float gridSize;
	private float worldWidth = 1f;

	private Vector3 tmp = new Vector3();
	private Vector2 vec2 = new Vector2();

	protected InputListener inputListener;

	protected boolean canMoveAround;
	private boolean isInViewPort;
	private boolean isDragging;
	private boolean inputListenersEnabled = true;

	protected Gizmos gizmos = new Gizmos();

	public OrderedSet<GameObject> selection = new OrderedSet<>();
	protected GameObject entityUnderMouse;

	protected boolean locked;

	private boolean hasRulers = false;
	private Table yRulerTable;
	private Table xRulerTable;

	private float gridUnit;
	private float gridXStart;
	private float gridYStart;
	private float gridXEnd;
	private float gridYEnd;


	protected GroupSelectionGizmo groupSelectionGizmo;

	public ViewportWidget () {
		shapeRenderer = new ShapeRenderer();
		entitySelectionBuffer = new EntitySelectionBuffer();
		camera = new OrthographicCamera();
		camera.viewportWidth = 10;

		setTouchable(Touchable.enabled);

		cameraController = new CameraController(camera);
		cameraController.setInvert(true);
		cameraController.setBoundsProvider(this);

		addPanListener();
		addGizmoListener();

		groupSelectionGizmo = new GroupSelectionGizmo(this);
		gizmos.gizmoList.add(groupSelectionGizmo);
	}

	protected void addRulers () {
		Skin skin = TalosMain.Instance().getSkin();
		xRulerTable = new Table(skin);
		xRulerTable.background("panel_input_bg");
		addActor(xRulerTable);

		yRulerTable = new Table(skin);
		yRulerTable.background("panel_input_bg");
		addActor(yRulerTable);

		hasRulers = true;
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

				if (canMoveAround) return false;

				if (locked) {
					return true;
				}


				boolean hasSelection = selection.size > 0;
				boolean hasEntityUnderMouse = entityUnderMouse != null;

				if (hasSelection) {
					//Check if we keep selection
					if (hasEntityUnderMouse && entityUnderMouse == selection.first()) {
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

							//We aren't over the pixel or the gizmo, unselect
							requestSelectionClear();
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
							selectGameObject(hitGizmo.getGameObject());

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
				if (canMoveAround) return;

				if (locked) {
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

				if (canMoveAround) return;

				if (locked) {
					return;
				}


				super.touchUp(event, x, y, pointer, button);
			}

			@Override
			public boolean mouseMoved (InputEvent event, float x, float y) {
				if (canMoveAround) return super.mouseMoved(event, x, y);

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
		};

		addListener(gizmoListener);
		return gizmoListener;
	}

	private boolean canTouchGizmo (Gizmo testGizmo) {
		if (testGizmo == null) return false;
		if (testGizmo instanceof GroupSelectionGizmo) return true;

		if (!testGizmo.getGameObject().isEditorVisible()) return false;
		if (testGizmo.getGameObject().isEditorTransformLocked()) return false;

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

	public void initGizmos (GameObject gameObject, ViewportWidget parent) {
		makeGizmosFor(gameObject, parent);
		Array<GameObject> childObjects = gameObject.getGameObjects();
		if (childObjects != null) {
			for (GameObject childObject : childObjects) {
				makeGizmosFor(childObject, parent);
				initGizmos(childObject, parent);
			}
		}
	}

	public void initGizmos (GameObjectContainer gameObjectContainer, ViewportWidget parent) {
		Array<GameObject> childObjects = gameObjectContainer.getGameObjects();
		if (childObjects != null) {
			for (GameObject childObject : childObjects) {
				initGizmos(childObject, parent);
			}
		}
	}

	public void makeGizmosFor (GameObject gameObject, ViewportWidget parent) {
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


				if (gizmo != null) {
					gameObjectGizmoMap.put(gizmo.getClass(), gizmo);

					gizmo.setGameObject(gameObject);

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
				return -Integer.compare(o1.getPriority(), o2.getPriority());
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
				return -Integer.compare(o1.getPriority(), o2.getPriority());
			}
		});

		for (Gizmo gizmo : gizmos.gizmoList) {
			if (gizmo.hit(x, y))
				return gizmo;
		}

		return null;
	}

	protected void addPanListener () {
		addListener(new InputListener() {
			@Override
			public boolean scrolled (InputEvent event, float x, float y, float amountX, float amountY) {
				float currWidth = camera.viewportWidth * camera.zoom;
				float nextWidth = currWidth * (1f + amountY * 0.1f);
				float nextZoom = nextWidth / camera.viewportWidth;
				camera.zoom = nextZoom;

				camera.zoom = MathUtils.clamp(camera.zoom, minZoom, maxZoom);
				camera.update();

				return true;
			}

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				cameraController.touchDown((int)x, (int)y, pointer, button);
				return !event.isHandled();
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				isDragging = false;
				cameraController.touchUp((int)x, (int)y, pointer, button);
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				// can't move around disable dragging
				if (!canMoveAround)
					return;

				isDragging = true;

				cameraController.touchDragged((int)x, (int)y, pointer);
			}

			@Override
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				isInViewPort = true;

				super.enter(event, x, y, pointer, fromActor);
				TalosMain.Instance().UIStage().getStage().setScrollFocus(ViewportWidget.this);
			}

			@Override
			public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (pointer != -1)
					return; //Only care about exit/enter from mouse move
				TalosMain.Instance().UIStage().getStage().setScrollFocus(null);

				isInViewPort = false;
				super.exit(event, x, y, pointer, toActor);
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

		Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float aspect = getWidth() / getHeight();

		camera.viewportHeight = camera.viewportWidth / aspect;

		camera.update();

		prevTransform.set(batch.getTransformMatrix());
		prevProjection.set(batch.getProjectionMatrix());

		batch.setProjectionMatrix(camera.combined);
		batch.setTransformMatrix(emptyTransform);

		batch.begin();
		drawContent(batch, parentAlpha);

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

		if (hasRulers) {
			configureRulers();
		}

		super.draw(batch, parentAlpha);
	}

	private void configureRulers () {
		xRulerTable.clearChildren();
		xRulerTable.setWidth(getWidth());
		float rulerHeight = 20f;
		xRulerTable.setY(getHeight() - rulerHeight);
		xRulerTable.setHeight(rulerHeight);
		float xStart = gridXStart;
		while (xStart <= gridXEnd) {
			xStart += gridUnit;

			String coordText;
			int testInt = (int)xStart;
			float tmp = xStart - testInt;
			coordText = tmp > 0 ? "" + xStart : "" + testInt;
			Label coordinateLabel = new Label(coordText, getSkin());
			coordinateLabel.setX(getLocalFromWorld(xStart, 0).x - coordinateLabel.getWidth() / 2f);
			xRulerTable.addActor(coordinateLabel);
		}

		yRulerTable.clearChildren();
		float yStart = gridYStart;

		float maxWidth = 0;
		yRulerTable.clearChildren();
		yRulerTable.setHeight(getHeight());
		while (yStart <= gridYEnd) {
			yStart += gridUnit;

			String coordText;
			int testInt = (int)yStart;
			float tmp = yStart - testInt;
			coordText = tmp > 0 ? "" + yStart : "" + testInt;
			Label coordinateLabel = new Label(coordText, getSkin());
			coordinateLabel.setY(getLocalFromWorld(0, yStart).y - coordinateLabel.getHeight() / 2f);
			if (maxWidth < coordinateLabel.getWidth()) {
				maxWidth = coordinateLabel.getWidth();
			}
			yRulerTable.addActor(coordinateLabel);
		}
		yRulerTable.setWidth(maxWidth);
	}

	protected void getEntityUnderMouse () {
		Vector2 touchSpace = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		Vector2 uiSpace = screenToLocalCoordinates(touchSpace);

		uiSpace.x /= getWidth();
		uiSpace.y /= getHeight();

		Color color = entitySelectionBuffer.getPixelAtNDC(uiSpace);


		GameObject root = SceneEditorWorkspace.getInstance().getRootGO();
		if (root != null) {
			entityUnderMouse = findEntityForColourEncodedUUID(color, root);
		} else {
			entityUnderMouse = null;
		}

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

	@Override
	public void act (float delta) {
		super.act(delta);

		// allow moving around if space bar is pressed and is in viewport or has dragged from viewport
		canMoveAround = Gdx.input.isKeyPressed(Input.Keys.SPACE) && (isInViewPort || isDragging);

		if (canMoveAround) {
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

	public abstract void drawContent (Batch batch, float parentAlpha);

	public OrthographicCamera getCamera () {
		return camera;
	}

	public float getCameraPosX () {
		return camera.position.x;
	}

	public float getCameraPosY () {
		return camera.position.y;
	}

	public float getCameraZoom () {
		return camera.zoom;
	}

	public void setCameraPos (float x, float y) {
		camera.position.set(x, y, 0);
	}

	public void setCameraZoom (float zoom) {
		camera.zoom = zoom;
	}

	public void setViewportWidth (float width) {
		camera.viewportWidth = width;
		camera.update();
	}

	protected void setWorldSize (float worldWidth) {
		this.worldWidth = worldWidth;
		updateNumbers();
	}

	private void updateNumbers () {
		camera.zoom = worldWidth / camera.viewportWidth;
		gridSize = worldWidth / 40f;
		float minWidth = gridSize * 4f;
		float maxWidth = worldWidth * 10f;
		minZoom = minWidth / camera.viewportWidth;
		maxZoom = maxWidth / camera.viewportWidth;
		camera.update();
	}

	protected void resetCamera () {
		camera.position.set(0, 0, 0);
		camera.zoom = worldWidth / camera.viewportWidth;
	}

	protected void drawGrid (Batch batch, float parentAlpha) {
		Gdx.gl.glLineWidth(1f);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		float zeroAlpha = 0.8f;
		float mainLinesAlpha = 0.2f;
		float smallLinesAlpha = 0.1f;
		float linesToAppearAlpha = 0.01f;

		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Color gridColor = new Color(Color.GRAY);

		float thickness = pixelToWorld(1.2f);

		float distanceThatLinesShouldBe = pixelToWorld(150f);
		gridUnit = nextPowerOfTwo(distanceThatLinesShouldBe);

		float previousUnit = gridUnit / 2;
		linesToAppearAlpha = MathUtils.lerp(smallLinesAlpha, linesToAppearAlpha, (distanceThatLinesShouldBe - previousUnit) / (gridUnit - previousUnit));
		smallLinesAlpha = MathUtils.lerp(mainLinesAlpha, smallLinesAlpha, (distanceThatLinesShouldBe - previousUnit) / (gridUnit - previousUnit));

		int baseLineDivisor = 4;

		float visibleWidth = camera.viewportWidth * camera.zoom;
		float visibleHeight = camera.viewportHeight * camera.zoom;

		float cameraX = camera.position.x;
		float cameraY = camera.position.y;

		float visibleStartX = cameraX - visibleWidth / 2;
		float visibleStartY = cameraY - visibleHeight / 2;
		float visibleEndX = cameraX + visibleWidth / 2;
		float visibleEndY = cameraY + visibleHeight / 2;


		gridColor.a =  zeroAlpha * parentAlpha;
		shapeRenderer.setColor(gridColor);
		drawLine(batch, 0, cameraY - visibleHeight / 2, 0, cameraY + visibleHeight / 2, thickness, 0);
		drawLine(batch, visibleStartX, 0, visibleEndX, 0, thickness, 0);


		gridXStart = gridUnit * MathUtils.floor(visibleStartX / gridUnit) ;

		// drawing vertical lines
		for (float i = gridXStart; i < visibleEndX; i += gridUnit) {
			for (int j = 1; j <= baseLineDivisor; j++) {
				float smallUnitSize = gridUnit / baseLineDivisor;
				float x1 = i + j * smallUnitSize;

				for (int k = 1; k <= baseLineDivisor; k++) {
					float nextUnitSize = (gridUnit / baseLineDivisor) / baseLineDivisor;

					gridColor.a =  linesToAppearAlpha * parentAlpha;
					shapeRenderer.setColor(gridColor);
					drawLine(batch, x1 + k * nextUnitSize, cameraY - visibleHeight / 2, x1 + k * nextUnitSize, cameraY + visibleHeight / 2, thickness, j);
				}

				gridColor.a =  smallLinesAlpha * parentAlpha;
				shapeRenderer.setColor(gridColor);

				drawLine(batch, x1, cameraY - visibleHeight / 2, x1, cameraY + visibleHeight / 2, thickness, i);
			}


			if (i == 0) continue;
			gridColor.a =  mainLinesAlpha * parentAlpha;
			shapeRenderer.setColor(gridColor);
			drawLine(batch, i, cameraY - visibleHeight / 2, i, cameraY + visibleHeight / 2, thickness, i);
			gridXEnd = i;
		}

		gridYStart = gridUnit * MathUtils.floor(visibleStartY / gridUnit);

		// drawing vertical lines
		for (float i = gridYStart; i < visibleEndY; i += gridUnit) {
			for (int j = 1; j <= baseLineDivisor; j++) {
				float smallUnitSize = gridUnit / baseLineDivisor;
				float y1 = i + j * smallUnitSize;

				for (int k = 1; k <= baseLineDivisor; k++) {
					float nextUnitSize = (gridUnit / baseLineDivisor) / baseLineDivisor;

					gridColor.a =  linesToAppearAlpha * parentAlpha;
					shapeRenderer.setColor(gridColor);
					drawLine(batch, cameraX - visibleWidth / 2, y1 + k * nextUnitSize, cameraX + visibleWidth / 2, y1 + k * nextUnitSize, thickness, i);
				}

				gridColor.a =  smallLinesAlpha * parentAlpha;
				shapeRenderer.setColor(gridColor);
				drawLine(batch, cameraX - visibleWidth / 2, y1, cameraX + visibleWidth / 2, y1, thickness, i);
			}

			if (i == 0) continue;
			gridColor.a =  mainLinesAlpha * parentAlpha;
			shapeRenderer.setColor(gridColor);
			drawLine(batch, cameraX - visibleWidth / 2, i, cameraX + visibleWidth / 2,  i,  thickness, i);
			gridYEnd = i;
		}

		shapeRenderer.end();
	}

	private float nextPowerOfTwo (float value) {
		boolean negative = false;
		boolean smallerOne = false;
		if (value < 0) {
			negative = true;
			value *= -1;
		}

		if (value < 1 ) {
			value = 1 / value;
			smallerOne = true;
		}

		float unit = MathUtils.nextPowerOfTwo(MathUtils.ceil(value));
		if (smallerOne) {
			unit = 1 / unit;
			unit *= 2;
		}

		return unit;
	}

	private void drawLine (Batch batch, float x1, float y1, float x2, float y2, float thickness, float coord) {

		boolean debug = false;
		shapeRenderer.rectLine(x1, y1, x2, y2, thickness);

		if (debug) {
			BitmapFont bitmapFont = new BitmapFont();
			bitmapFont.getData().scale(1.5f);
			bitmapFont.setColor(1, 1, 1, 1);
			batch.begin();
			bitmapFont.draw(batch, " " + coord, x1, y1 + 30f);
			bitmapFont.draw(batch, " " + coord, x2 - 70f, y2);
			batch.flush();
			batch.end();
			bitmapFont.dispose();
		}
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

	protected Vector2 getLocalFromWorld (float x, float y) {
		getViewportBounds(Rectangle.tmp);
		camera.project(tmp.set(x, y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		Vector2 vector2 = screenToLocalCoordinates(new Vector2(tmp.x, (Rectangle.tmp.height - tmp.y) + Rectangle.tmp.y + 50)); // 50 is top bar height :(((((
		vec2.set(vector2);

		return vec2;
	}

	public Vector2 getWorldFromLocal (float x, float y) {
		Vector2 vector2 = localToScreenCoordinates(new Vector2(x, y));

		getViewportBounds(Rectangle.tmp);

		camera.unproject(tmp.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);

		vec2.set(tmp.x, tmp.y);

		return vec2;
	}

	protected Vector3 getWorldFromLocal (Vector3 vec) {
		Vector2 vector2 = localToScreenCoordinates(new Vector2(vec.x, vec.y));

		getViewportBounds(Rectangle.tmp);

		camera.unproject(vec.set(vector2.x, vector2.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		return vec;
	}

	public Vector3 getTouchToWorld (float x, float y) {
		Vector3 vec = new Vector3(x, y, 0);

		getViewportBounds(Rectangle.tmp);

		camera.unproject(vec.set(vec.x, vec.y, 0), Rectangle.tmp.x, Rectangle.tmp.y, Rectangle.tmp.width, Rectangle.tmp.height);
		return vec;
	}

	protected float pixelToWorld (float pixelSize) {
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
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, selection));
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
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, selection));
	}

	public void addToSelection (GameObject gameObject) {
		if (!selection.contains(gameObject)) {
			selection.add(gameObject);
		}
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, selection));
	}

	protected void setSelection (Array<GameObject> gameObjects) {
		selection.clear();

		selection.addAll(gameObjects);
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, selection));
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
			Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(this, selection));

			return true;
		}

		return false;
	}

	public void selectGameObjectExternally (GameObject gameObject) {
		if (fromDirectoryView)
			return;

		selectGameObject(gameObject);
		getStage().setKeyboardFocus(this);
	}

	private void selectGameObject (GameObject gameObject) {
		if (gameObject == null)
			return;
		Array<GameObject> tmp = new Array<>();
		tmp.add(gameObject);

		setSelection(tmp);
	}

	private void drawGizmos (Batch batch, float parentAlpha) {
		for (int i = 0; i < this.gizmos.gizmoList.size; i++) {
			Gizmo gizmo = this.gizmos.gizmoList.get(i);
			gizmo.setSizeForUIElements(getWidth(),getWorldWidth() * camera.zoom);

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
		entitySelectionBuffer.begin(camera);
	}
	protected void endEntitySelectionBuffer() {
		entitySelectionBuffer.end();

	}

	protected void drawEntitiesForSelection () {
	}
}
