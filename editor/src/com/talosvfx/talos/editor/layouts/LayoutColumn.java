package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;

public class LayoutColumn extends LayoutItem {

	Array<LayoutItem> rows = new Array<>();

	public LayoutColumn (Skin skin, LayoutGrid grid) {
		super(skin, grid);
	}


	public enum LayoutDirection {
		UP,RIGHT,DOWN,LEFT
	}

	public void addRowContainer (LayoutItem newLayoutContent, boolean up) {
		addRowContainer(newLayoutContent, up, null);
	}

	public void addRowContainer (LayoutItem newLayoutContent, boolean up, @Null LayoutContent relative) {

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
				takeThirtyPercent(newLayoutContent, 0, 1);

			} else {
				rows.add(newLayoutContent);
				takeThirtyPercent(newLayoutContent, 0, 1);
			}
		}

		addActor(newLayoutContent);
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
	}

	@Override
	public void removeItem (LayoutItem content) {
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
