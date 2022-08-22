package com.talosvfx.talos.editor.addons.scene.widgets.directoryview;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;

class ItemGroup extends WidgetGroup {
	private float prefHeight;
	private float cellWidth, cellHeight;

	private float space, wrapSpace, padTop, padLeft, padBottom, padRight;

	public ItemGroup () {
		setTouchable(Touchable.childrenOnly);
	}

	@Override
	public void invalidate () {
		super.invalidate();
	}

    @Override
	public void layout () {
		float padLeft = this.padLeft, padRight = this.padRight, pad = padLeft + padRight, space = this.space, wrapSpace = this.wrapSpace;
		float width = cellWidth, height = cellHeight;
		float maxWidth = getWidth() - pad;
		SnapshotArray<Actor> children = getChildren();
		int n = children.size;
		if (n < 1) {
			return;
		}

		if (n * width + (n - 1) * space <= maxWidth) { // strategy 1 - align.topLeft and fixed space
			float rowHeight = height, x = padLeft;

			float startY = getHeight() - padTop - rowHeight;
			int i = 0, incr = 1;
			for (; i != n; i += incr) {
				Actor child = children.get(i);
				Layout layout = null;
				if (child instanceof Layout) {
					layout = (Layout)child;
				}
				float y = startY;
				y += rowHeight - height;
				child.setBounds(x, y, width, height);
				x += width + space;
				if (layout != null)
					layout.validate();
			}
			prefHeight = padTop + padBottom + cellHeight;
		} else {
			float rowY = getHeight() - padTop, groupWidth = getWidth(), xStart = padLeft, x = 0, rowHeight = 0, rowDir = -1;

			int canFit = (int)((maxWidth - space) / (width + space));
			if (canFit > 1 && n > 1) { // strategy 2 - align.topCenter and dynamic space between
				maxWidth += pad;
				space = (maxWidth - canFit * width) / (canFit + 1);
				xStart = space;

				int i = 0, incr = 1;
				for (int r = 0; i != n; i += incr) {
					Actor child = children.get(i);

					Layout layout = null;
					if (child instanceof Layout) {
						layout = (Layout)child;
					}

					if ((i % canFit) == 0 || r == 0) {
						x = xStart;
						rowHeight = height;
						if (r > 0)
							rowY += wrapSpace * rowDir;
						rowY += rowHeight * rowDir;
						r += 1;
					}

					float y = rowY;
					y += rowHeight - height;

					child.setBounds(x, y, width, height);
					prefHeight = r * height + (r - 1) * wrapSpace + padBottom + padTop;

					x += width + space;

					if (layout != null)
						layout.validate();
				}
			} else { // strategy 3 - one item per row
				int i = 0, incr = 1;
				for (int r = 0; i != n; i += incr) {
					Actor child = children.get(r);

					Layout layout = null;
					if (child instanceof Layout) {
						layout = (Layout)child;
					}

					x = xStart;
					x += (maxWidth - width) / 2;
					rowHeight = height;
					if (r > 0)
						rowY += wrapSpace * rowDir;
					rowY += rowHeight * rowDir;
					r += 1;

					float y = rowY;
					y += rowHeight - height;

					child.setBounds(x, y, width, height);

					if (layout != null)
						layout.validate();
				}

				prefHeight = padTop + padBottom + n * cellHeight + (n - 1) * wrapSpace;
			}
		}
	}

	/**
	 * Sets the padTop, padLeft, padBottom, and padRight to the specified value.
	 */
	public ItemGroup pad (float pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		return this;
	}

	/**
	 * Sets the horizontal space between children.
	 */
	public ItemGroup space (float space) {
		this.space = space;
		return this;
	}

	public void setCellWidth (float width) {
		this.cellWidth = width;
	}

	public void setCellHeight (float height) {
		this.cellHeight = height;
	}

	public void setCellSize (float size) {
		setCellSize(size, size);
	}

	public void setCellSize (float width, float height) {
		setCellWidth(width);
		setCellHeight(height);
	}

	/**
	 * Sets the vertical space between rows when wrap is enabled.
	 */
	public ItemGroup wrapSpace (float wrapSpace) {
		this.wrapSpace = wrapSpace;
		return this;
	}

	protected void drawDebugBounds (ShapeRenderer shapes) {
		super.drawDebugBounds(shapes);
		if (!getDebug())
			return;
		shapes.set(ShapeRenderer.ShapeType.Line);
		if (getStage() != null)
			shapes.setColor(getStage().getDebugColor());
		shapes.rect(getX() + padLeft, getY() + padBottom, getOriginX(), getOriginY(), getWidth() - padLeft - padRight, getHeight() - padBottom - padTop, getScaleX(), getScaleY(), getRotation());
	}

	@Override
	public float getMinWidth () {
		return padLeft + cellWidth + padRight;
	}

	@Override
	public float getPrefHeight () {
		return prefHeight;
	}
}
