package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.AssetFileDroppedEvent;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import lombok.Data;

public class GlobalDragAndDrop {

	private DragAndDrop dragAndDrop;

	public GlobalDragAndDrop () {
		dragAndDrop = new DragAndDrop();
	}

	public void addTarget (DragAndDrop.Target targetObject) {
		dragAndDrop.addTarget(targetObject);

	}

	public void addSource (DragAndDrop.Source source) {
		dragAndDrop.addSource(source);
	}

	public void addSource (Actor dragSourceActor, BaseDragAndDropPayload payloadWrapper) {

		dragAndDrop.addSource(new DragAndDrop.Source(dragSourceActor) {
			@Override
			public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {

				DragAndDrop.Payload payload = new DragAndDrop.Payload();

				Actor dragging;

				if (dragSourceActor instanceof ActorCloneable) {
					dragging = ((ActorCloneable)dragSourceActor).copyActor(dragSourceActor);
				} else {
					dragging = new Label("Dragging label", SharedResources.skin);
				}

				payload.setDragActor(dragging);
				payload.setObject(payloadWrapper);

				return payload;
			}

			@Override
			public void drag (InputEvent event, float x, float y, int pointer) {
				super.drag(event, x, y, pointer);
			}

			@Override
			public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
				super.dragStop(event, x, y, pointer, payload, target);
			}
		});
	}

	public void fakeDragDrop (int screenX, int screenY, String[] absoluteFiles) {
		ArrayDragAndDropPayload arrayDragAndDropPayload = new ArrayDragAndDropPayload();
		Array<BaseDragAndDropPayload> items = arrayDragAndDropPayload.getItems();
		for (String absoluteFileHandle : absoluteFiles) {
			FileHandle absolute = Gdx.files.absolute(absoluteFileHandle);
			items.add(new FileHandleDragAndDropPayload(absolute));
		}

		Table dummyActor = new Table();
		dummyActor.setSize(16, 16);

		Vector2 screenCoords = new Vector2(screenX, screenY);
		SharedResources.stage.screenToStageCoordinates(screenCoords);

		dummyActor.setPosition(screenCoords.x, screenCoords.y);

		SharedResources.stage.addActor(dummyActor);
		DragAndDrop.Source source = new DragAndDrop.Source(dummyActor) {
			@Override
			public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
				DragAndDrop.Payload payload = new DragAndDrop.Payload();

				Actor dragging = new Label("Dragging label", SharedResources.skin);

				payload.setDragActor(dragging);
				payload.setObject(arrayDragAndDropPayload);

				return payload;
			}

			@Override
			public void drag (InputEvent event, float x, float y, int pointer) {
				super.drag(event, x, y, pointer);
			}

			@Override
			public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
				super.dragStop(event, x, y, pointer, payload, target);
			}
		};

		dragAndDrop.addSource(source);
		DelayedRemovalArray<EventListener> listeners = dummyActor.getCaptureListeners();
		DragListener dragListener = (DragListener)listeners.peek();

		InputEvent obtain = Pools.obtain(InputEvent.class);
		obtain.setStage(SharedResources.stage);

		int dragTime = dragAndDrop.getDragTime();
		dragAndDrop.setDragTime(0);
		dragListener.dragStart(obtain, 0, 0, 0);
		dragListener.drag(obtain, 0, 0, 0);
		dragListener.dragStop(obtain, 0, 0, 0);

		dragAndDrop.setDragTime(dragTime);
		Pools.free(obtain);

		dragAndDrop.removeSource(source);


		dummyActor.remove();
	}

	public static class BaseDragAndDropPayload {

		public BaseDragAndDropPayload () {
		}
	}

	@Data
	public static class ArrayDragAndDropPayload extends BaseDragAndDropPayload {

		private Array<BaseDragAndDropPayload> items = new Array<>();

	}

	@Data
	public static class FileHandleDragAndDropPayload extends BaseDragAndDropPayload {

		private final FileHandle handle;

		public FileHandleDragAndDropPayload (FileHandle handle) {
			this.handle = handle;
		}

	}

	@Data
	public static class GameAssetDragAndDropPayload extends BaseDragAndDropPayload {

		private final GameAsset<?> gameAsset;

		public GameAssetDragAndDropPayload (GameAsset<?> gameAsset) {
			this.gameAsset = gameAsset;
		}
	}







}
