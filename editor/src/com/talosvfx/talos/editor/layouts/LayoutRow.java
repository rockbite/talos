package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;

public class LayoutRow extends LayoutItem {

	private Array<LayoutItem> columns = new Array<>();
	Array<LayoutResizeWidget> resizeWidgets = new Array<>();

	public LayoutRow (Skin skin, LayoutGrid grid) {
		super(skin, grid);
	}

	public void addColumnContainer (LayoutItem newLayoutContent, boolean left) {
		addColumnContainer(newLayoutContent, left, null, true);
	}
	public void addColumnContainer (LayoutItem newLayoutContent, boolean left, boolean resize) {
		addColumnContainer(newLayoutContent, left, null, resize);
	}

	public void addColumnContainer (LayoutItem newLayoutContent, boolean left, @Null LayoutItem relative) {
		addColumnContainer(newLayoutContent, left, relative, true);
	}

	public void addColumnContainer (LayoutItem newLayoutContent, boolean left, @Null LayoutItem relative, boolean resize) {

		if (relative != null) {
			//We are relative to some other actor
			int idxRelative = columns.indexOf(relative, true);
			if (left) {
//				idxRelative -= 0; Don't move it
			} else {
				idxRelative += 1;
			}

			columns.insert(idxRelative, newLayoutContent);
			if (resize) {
				handleSize(newLayoutContent, idxRelative, left ? idxRelative + 1 : idxRelative - 1);
			}

		} else {
			if (left) {
				columns.insert(0, newLayoutContent);

				//Take 30% from index 1 width
				if (columns.size == 1) {
					if (resize) {
						newLayoutContent.setRelativeWidth(1f);
						newLayoutContent.setRelativeHeight(1f);
					}
				} else {
					if (resize) {
						handleSize(newLayoutContent, 0, 1);
					}
				}

			} else {
				columns.add(newLayoutContent);
				if (columns.size == 1) {
					if (resize) {
						newLayoutContent.setRelativeWidth(1f);
						newLayoutContent.setRelativeHeight(1f);
					}
				} else {
					if (resize) {
						handleSize(newLayoutContent, 1, 0);
					}
				}
			}
		}


		addActor(newLayoutContent);

		if (columns.size > 1) {
			LayoutResizeWidget resizeWidget = new LayoutResizeWidget(skin, grid, this);
			resizeWidgets.add(resizeWidget);
			addActor(resizeWidget);
		}
	}

	private void handleSize (LayoutItem newLayoutContent, int thisItemIdx, int otherItemIdx) {
		if (otherItemIdx < 0 || otherItemIdx >= columns.size) {
			//Just give it full width
			columns.get(thisItemIdx).setRelativeWidth(1f);
		} else if (newLayoutContent instanceof LayoutContent) {
			LayoutContent layoutContent = (LayoutContent) newLayoutContent;
			if (layoutContent.getActiveApp().hasPreferredWidth()) {
				float preferredWidth = layoutContent.getContentTable().getPrefWidth();
				LayoutItem otherItem = columns.get(otherItemIdx);
				if (preferredWidth < totalPixelWidthToDistribute) {
					float currentRelativePercent = preferredWidth / totalPixelWidthToDistribute;
					newLayoutContent.setRelativeWidth(currentRelativePercent);
					otherItem.setRelativeWidth(otherItem.getRelativeWidth() - currentRelativePercent);
				} else {
					takeThirtyPercent(newLayoutContent, otherItemIdx);
				}
			} else {
				takeThirtyPercent(newLayoutContent, otherItemIdx);
			}
		} else {
			takeThirtyPercent(newLayoutContent, otherItemIdx);
		}
	}

	private void takeThirtyPercent (LayoutItem newLayoutContent, int otherItemIdx) {
		LayoutItem layoutItem = columns.get(otherItemIdx);
		float totalRelativeWidth = layoutItem.getRelativeWidth();
		float totalRelativeSub = totalRelativeWidth * 0.3f;

		newLayoutContent.setRelativeWidth(totalRelativeSub);
		layoutItem.setRelativeWidth(totalRelativeWidth - totalRelativeSub);
	}

	@Override
	public void layout () {
		super.layout();

		int size = columns.size;
		if (size == 0)
			return;


		float debugBuffer = 0;

		float counterX = 0;

		for (int i = 0; i < columns.size; i++) {
			LayoutItem row = columns.get(i);
			float widthForItem = row.getRelativeWidth() * getWidth();

			if(row.getRelativeWidth() <= 0f) {
				System.out.println("Negative width, should restrict at some point");
			}


			row.setBounds(counterX + debugBuffer, debugBuffer, widthForItem - (debugBuffer * 2), getHeight() - (debugBuffer * 2f));
			counterX += widthForItem;
		}

		counterX = 0;
		for (int i = 0; i < resizeWidgets.size; i++) {

			//Draw them at the end of each one

			LayoutItem row = columns.get(i);
			float widthForItem = row.getRelativeWidth() * getWidth();
			counterX+= widthForItem;

			float x = counterX;
			float y = 0;
			LayoutResizeWidget layoutResizeWidget = resizeWidgets.get(i);
			layoutResizeWidget.toFront();
			layoutResizeWidget.setBounds(x, y, 5, getHeight() - 30);

		}
	}


	float startTouchX;
	float startTouchY;

	float totalPixelWidthToDistribute;
	float startRelativeLeftWidth;
	float startRelativeRightWidth;
	float uiToTouch;

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

		LayoutItem left = columns.get(idx);
		LayoutItem right = columns.get(idx + 1);

		startRelativeLeftWidth = left.getRelativeWidth();
		startRelativeRightWidth = right.getRelativeWidth();

		totalPixelWidthToDistribute = getWidth();

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



		float widthChangeRelative = -deltaX/totalPixelWidthToDistribute;

		LayoutItem left = columns.get(idx);
		LayoutItem right = columns.get(idx + 1);

		float leftNewHeight = startRelativeLeftWidth - widthChangeRelative;
		float rightNewHeight = startRelativeRightWidth + widthChangeRelative;

		float minPixelSize = 50;

		float startTotalRelative = startRelativeLeftWidth + startRelativeRightWidth;

		if (leftNewHeight < 0 || (leftNewHeight * totalPixelWidthToDistribute) < minPixelSize) {
			leftNewHeight = minPixelSize / totalPixelWidthToDistribute;
			rightNewHeight = startTotalRelative - leftNewHeight;
		} else if (rightNewHeight < 0 || (rightNewHeight * totalPixelWidthToDistribute) < minPixelSize) {
			rightNewHeight = minPixelSize / totalPixelWidthToDistribute;
			leftNewHeight = startTotalRelative - rightNewHeight;
		}

		left.setRelativeWidth(leftNewHeight);
		right.setRelativeWidth(rightNewHeight);


		invalidateHierarchy();


		super.draggedResizeWidget(layoutResizeWidget, event, x, y, pointer);
	}

	@Override
	public void removeItem (LayoutItem content) {
		if (columns.size > 1) {
			LayoutResizeWidget resizeToPop = resizeWidgets.pop();
			removeActor(resizeToPop);
		}

		int idxOfItem = columns.indexOf(content, true);

		//Lets see if there is item below, or above to give back some width
		if (idxOfItem >= 1) {
			LayoutItem itemToGiveWidthTo = columns.get(idxOfItem - 1);
			itemToGiveWidthTo.setRelativeWidth(itemToGiveWidthTo.getRelativeWidth() + content.getRelativeWidth());
		} else if (idxOfItem == 0 && columns.size >= 2) {
			LayoutItem itemToGiveWidthTo = columns.get(1);
			itemToGiveWidthTo.setRelativeWidth(itemToGiveWidthTo.getRelativeWidth() + content.getRelativeWidth());
		} else {
			if (columns.size != 1) {
				throw new GdxRuntimeException("Invalid state");
			}
		}

//		content.setRelativeWidth(0); //reset it for now


		columns.removeValue(content,true);
		removeActor(content);
		invalidate();
	}

	@Override
	public void exchangeItem (LayoutItem target, LayoutItem newColumn) {
		removeActor(target);

		int idx = columns.indexOf(target, true);

		columns.set(idx, newColumn);
		addActor(newColumn);
	}

	@Override
	public boolean isEmpty () {
		return columns.isEmpty();
	}

	public Array<LayoutItem> getColumns () {
		return columns;
	}
}
