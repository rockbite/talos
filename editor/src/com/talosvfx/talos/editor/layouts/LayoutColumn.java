package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;

public class LayoutColumn extends LayoutItem {

	Array<LayoutItem> rows = new Array<>();
	Array<LayoutResizeWidget> resizeWidgets = new Array<>();

	public LayoutColumn (Skin skin, LayoutGrid grid) {
		super(skin, grid);
	}


	public enum LayoutDirection {
		UP,RIGHT,DOWN,LEFT
	}

	public void addRowContainer (LayoutItem newLayoutContent, boolean up) {
		addRowContainer(newLayoutContent, up, null);
	}

	public void addRowContainer (LayoutItem newLayoutContent, boolean up, @Null LayoutItem relative) {

		if (relative != null) {
			//We are relative to some other actor
			int idxRelative = rows.indexOf(relative, true);
			if (!up) {
//				idxRelative -= 0; Don't move it
			} else {
				idxRelative += 1;
			}

			rows.insert(idxRelative, newLayoutContent);
			takeThirtyPercent(newLayoutContent, idxRelative, !up ? idxRelative + 1 : idxRelative - 1);

		} else {
			if (!up) {
				rows.insert(0, newLayoutContent);
				if (rows.size == 1) {
					newLayoutContent.setRelativeWidth(1f);
					newLayoutContent.setRelativeHeight(1f);
				} else {
					takeThirtyPercent(newLayoutContent, 0, 1);
				}

			} else {
				rows.add(newLayoutContent);
				if (rows.size == 1) {
					newLayoutContent.setRelativeWidth(1f);
					newLayoutContent.setRelativeHeight(1f);
				} else {
					takeThirtyPercent(newLayoutContent, 1, 0);
				}
			}
		}

		addActor(newLayoutContent);

		if (rows.size > 1) {
			LayoutResizeWidget resizeWidget = new LayoutResizeWidget(skin, grid, this);
			resizeWidgets.add(resizeWidget);
			addActor(resizeWidget);
		}
	}


	float startTouchX;
	float startTouchY;
	float uiToTouch;

	float startRelativeHeightTop;
	float startRelativeHeightBottom;
	float totalPixelHeightToDistribute;

	@Override
	public void touchedTownResizeWidget (LayoutResizeWidget layoutResizeWidget, InputEvent event, float x, float y, int pointer) {
		int idx = resizeWidgets.indexOf(layoutResizeWidget, true);


		startTouchX = Gdx.input.getX();
		startTouchY = Gdx.input.getY();

		Vector2 screenCoords = new Vector2(0, 0);
		Vector2 screenCoords2 = new Vector2(1, 0);
		localToScreenCoordinates(screenCoords);
		localToScreenCoordinates(screenCoords2);

		uiToTouch = screenCoords2.x - screenCoords.x;

		//idx of 0 is between the 0th-1st element

		LayoutItem bottom = rows.get(idx);
		LayoutItem top = rows.get(idx + 1);

		startRelativeHeightBottom = bottom.getRelativeHeight();
		startRelativeHeightTop = top.getRelativeHeight();

		totalPixelHeightToDistribute = getHeight();

		super.touchedTownResizeWidget(layoutResizeWidget, event, x, y, pointer);
	}

	@Override
	public void draggedResizeWidget (LayoutResizeWidget layoutResizeWidget, InputEvent event, float x, float y, int pointer) {
		int idx = resizeWidgets.indexOf(layoutResizeWidget, true);

		//This determines which bordering thingies we are resizing


		Vector2 screenCoords = new Vector2(Gdx.input.getX(), Gdx.input.getY()).sub(startTouchX, startTouchY);
		screenCoords.scl(uiToTouch);

		float deltaX = screenCoords.x;
		float deltaY = screenCoords.y;


		float heightChangeRelative = -deltaY/totalPixelHeightToDistribute;

		LayoutItem bottom = rows.get(idx);
		LayoutItem top = rows.get(idx + 1);

		top.setRelativeHeight(startRelativeHeightTop - heightChangeRelative);
		bottom.setRelativeHeight(startRelativeHeightBottom + heightChangeRelative);

		invalidateHierarchy();


		super.draggedResizeWidget(layoutResizeWidget, event, x, y, pointer);
	}

	private void takeThirtyPercent (LayoutItem newLayoutContent, int thisItemIdx, int otherItemIdx) {
		if (otherItemIdx < 0 || otherItemIdx >= rows.size) {
			//Just give it full width
			rows.get(thisItemIdx).setRelativeHeight(1f);
		} else {
			LayoutItem layoutItem = rows.get(otherItemIdx);
			float totalRelativeHeight = layoutItem.getRelativeHeight();
			float totalRelativeSub = totalRelativeHeight * 0.3f;


			newLayoutContent.setRelativeHeight(totalRelativeSub);
			layoutItem.setRelativeHeight(totalRelativeHeight - totalRelativeSub);
		}

	}


	@Override
	public void layout () {
		super.layout();

		int size = rows.size;
		if (size == 0) return;


		float counterY = 0;

		for (int i = 0; i < rows.size; i++) {
			LayoutItem row = rows.get(i);
			float heightForItem = row.getRelativeHeight() * getHeight();
			row.setBounds(0, counterY, getWidth(), heightForItem);
			counterY += heightForItem;
		}

		counterY = 0;
		for (int i = 0; i < resizeWidgets.size; i++) {

			//Draw them at the end of each one

			LayoutItem row = rows.get(i);
			float heightForItem = row.getRelativeHeight() * getHeight();
			counterY+= heightForItem;

			float x = 0;
			float y = counterY;
			LayoutResizeWidget layoutResizeWidget = resizeWidgets.get(i);
			layoutResizeWidget.toFront();
			layoutResizeWidget.setBounds(x, y, getWidth(), 5);

		}
	}

	@Override
	public void removeItem (LayoutItem content) {
		if (rows.size > 1) {
			removeActor(resizeWidgets.pop());
		}
		int idxOfItem = rows.indexOf(content, true);

		//Lets see if there is item below, or above to give back some width
		if (idxOfItem >= 1) {
			LayoutItem itemToGiveWidthTo = rows.get(idxOfItem - 1);
			itemToGiveWidthTo.setRelativeHeight(itemToGiveWidthTo.getRelativeHeight() + content.getRelativeHeight());
		} else if (idxOfItem == 0 && rows.size >= 2) {
			LayoutItem itemToGiveWidthTo = rows.get(1);
			itemToGiveWidthTo.setRelativeHeight(itemToGiveWidthTo.getRelativeHeight() + content.getRelativeHeight());
		} else {
			if (rows.size != 1) {
				throw new GdxRuntimeException("Invalid state");
			}
		}


		rows.removeValue(content, true);
		removeActor(content);
		invalidate();
	}

	@Override
	public void exchangeItem (LayoutItem target, LayoutItem newColumn) {
		removeActor(target);

		int idx = rows.indexOf(target, true);

		rows.set(idx, newColumn);
		addActor(newColumn);
	}

	@Override
	public boolean isEmpty () {
		return rows.isEmpty();
	}



}
