package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;

public class LayoutRow extends LayoutItem {

	private Array<LayoutItem> columns = new Array<>();

	public LayoutRow (Skin skin, LayoutGrid grid) {
		super(skin, grid);

		System.out.println("NEw layout row created");
	}

	public void addColumnContainer (LayoutItem newLayoutContent, boolean left) {
		addColumnContainer(newLayoutContent, left, null);
	}

	public void addColumnContainer (LayoutItem newLayoutContent, boolean left, @Null LayoutItem relative) {

		if (relative != null) {
			//We are relative to some other actor
			int idxRelative = columns.indexOf(relative, true);
			if (left) {
//				idxRelative -= 0; Don't move it
			} else {
				idxRelative += 1;
			}

			columns.insert(idxRelative, newLayoutContent);
			takeThirtyPercent(newLayoutContent, idxRelative, left ? idxRelative + 1 : idxRelative - 1);


		} else {
			if (left) {
				columns.insert(0, newLayoutContent);

				//Take 30% from index 1 width
				takeThirtyPercent(newLayoutContent, 0, 1);

			} else {
				columns.add(newLayoutContent);
				takeThirtyPercent(newLayoutContent, columns.size - 1, columns.size - 2);
			}
		}


		addActor(newLayoutContent);
	}

	private void takeThirtyPercent (LayoutItem newLayoutContent, int thisItemIdx, int otherItemIdx) {
		if (otherItemIdx < 0 || otherItemIdx >= columns.size) {
			//Just give it full width
			columns.get(thisItemIdx).setRelativeWidth(1f);
		} else {
			LayoutItem layoutItem = columns.get(otherItemIdx);
			float totalRelativeWidth = layoutItem.getRelativeWidth();
			float totalRelativeSub = totalRelativeWidth * 0.3f;


			newLayoutContent.setRelativeWidth(totalRelativeSub);
			layoutItem.setRelativeWidth(totalRelativeWidth - totalRelativeSub);
		}

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
				System.out.println();
			}


			row.setBounds(counterX + debugBuffer, debugBuffer, widthForItem - (debugBuffer * 2), getHeight() - (debugBuffer * 2f));
			counterX += widthForItem;
		}
	}

	@Override
	public void removeItem (LayoutItem content) {
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
}
