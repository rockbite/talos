package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.ObjectMap;

public class CursorUtil {


	public static CursorType currentCursorType = null;
	public static Cursor currentActualCursor = null;
	public static Cursor currentChosenModeCursor = null;
	public static Cursor currentDynamicCursor = null;


	private static ObjectMap<CursorType, Boolean> dynamicCursorFlags = new ObjectMap<>();

	public static void setDynamicModeCursor (CursorType cursorType) {
		dynamicCursorFlags.put(cursorType, true);
	}

	public static void checkAndReset () {
		//Find what cursor to use

		CursorType dynamicCursorFlag = getDynamicCursorFlag();
		if (dynamicCursorFlag != null) {
			if (currentCursorType != dynamicCursorFlag) {
				currentDynamicCursor = dynamicCursorFlag.cursor;
			}
		} else {
			currentDynamicCursor = null;
		}


		//Prioritize dynamic over chosen
		if (currentDynamicCursor != null) {
			if (currentActualCursor != currentDynamicCursor) {
				currentActualCursor = currentDynamicCursor;
				Gdx.graphics.setCursor(currentActualCursor);
			}
		} else if (currentChosenModeCursor != null) { //Choose the chosen one if we don't have a dynamic one
			if (currentActualCursor != currentChosenModeCursor) {
				currentActualCursor = currentChosenModeCursor;
				Gdx.graphics.setCursor(currentActualCursor);
			}
		} else {
			currentActualCursor = null;
			Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
		}

		dynamicCursorFlags.clear();
	}



	public static CursorType getDynamicCursorFlag () {
		for (CursorType value : CursorType.values()) {
			if (dynamicCursorFlags.containsKey(value)) {
				if (dynamicCursorFlags.get(value)) {
					return value;
				}
			}
		}
		return null;
	}

	public enum CursorType {
		//Order is priority for dynamic
		RESIZE(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursors/left-down-up.png")), 16, 16)),
		ROTATE(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursors/right-rotate-up.png")), 16, 16)),
		GRABBED(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursors/hand_grabbed.png")), 8, 8)),
		PICKER(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursors/picker.png")), 8, 8)),
		MOVE_ALL_DIRECTIONS(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursors/move_all_directions.png")), 8, 8)),
		MOVE_HORIZONTALLY(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursors/move_horizontally.png")), 8, 8)),
		MOVE_VERTICALLY(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursors/move_vertically.png")), 8, 8));

		private final Cursor cursor;

		CursorType (Cursor newCursor) {
			this.cursor = newCursor;
		}

		public Cursor getCursor () {
			return cursor;
		}
	}



}
