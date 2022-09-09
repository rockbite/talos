package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class LayoutGrid extends WidgetGroup {
	private final DragAndDrop dragAndDrop;

	//Start with a 2x2 grid with a 'root' container


	//Todo force to rows, always root is rows
	LayoutItem root;

	LayoutItem overItem;
	boolean highlightEdge = true;
	private Skin skin;
	private Drawable edgeBackground;

	public LayoutGrid (Skin skin) {
		this.skin = skin;

		edgeBackground = skin.newDrawable("white", 1f, 1f, 1f, 0.5f);
		dragAndDrop = new DragAndDrop();
	}


	public enum LayoutDirection {
		UP,RIGHT,DOWN,LEFT
	}


	public void addContent (LayoutItem content) {
		if (root == null) {
			root = content;
			addActor(root);
		} else {
			if (root instanceof LayoutRow) {
				((LayoutRow)root).addColumnContainer(content);
			} else {
				//Exchange root
				LayoutItem oldRoot = root;
				removeActor(oldRoot);

				LayoutRow newRow = new LayoutRow(skin, this);
				newRow.addColumnContainer(oldRoot);
				newRow.addColumnContainer(content);

				root = newRow;
				addActor(root);

			}
		}
	}

	//Add each LayoutContent for drag and drop as a source
	//Add each LayoutContent for drag and drop as a target
	void registerForDragAndDrop () {

	}

	@Override
	public void layout () {
		super.layout();
		if (root == null) return;

		root.setBounds(0, 0, getWidth(), getHeight());
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (root == null) return;

		int x = Gdx.input.getX();
		int y = Gdx.graphics.getHeight() - Gdx.input.getY();
		Vector2 screenCoords = new Vector2(x, y);
		screenToLocalCoordinates(screenCoords);

		Actor hit = hit(x, y, true);
		if (hit instanceof LayoutItem) {
			overItem = (LayoutItem)hit;
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (highlightEdge && overItem != null) {

			//Detect the edge
			int x = Gdx.input.getX();
			int y = Gdx.input.getY();
			Vector2 coords = new Vector2(x, y);
			overItem.screenToLocalCoordinates(coords);


			float horizontalPercent = 0.3f;
			float verticalPercent = 0.3f;

			coords.scl(1f/overItem.getWidth(), 1f/overItem.getHeight());

			//UNiversal coordinates


			float distanceFromMiddleX = Math.abs(0.5f - coords.x);
			float distanceFromMiddleY = Math.abs(0.5f - coords.y);

			if (distanceFromMiddleX >= distanceFromMiddleY) {
				//Its going to be an X if it exists

				if (coords.x < horizontalPercent) {
					//Left edge
					edgeBackground.draw(batch, overItem.getX(), overItem.getY(), horizontalPercent * overItem.getWidth(), overItem.getHeight());
				} else if (coords.x > (1-horizontalPercent)) {
					//Right edge
					edgeBackground.draw(batch, overItem.getX() + ((1-horizontalPercent) * overItem.getWidth()), overItem.getY(), horizontalPercent * overItem.getWidth(), overItem.getHeight());
				}

			} else {
				//its going to be Y if it exists
				if (coords.y < verticalPercent) {
					//bottom edge
					edgeBackground.draw(batch, overItem.getX(), overItem.getY(),  overItem.getWidth(), verticalPercent * overItem.getHeight());
				} else if (coords.y > (1-verticalPercent)) {
					//top edge

					float pixelAmount = overItem.getHeight() - (coords.y * overItem.getHeight());

					if (pixelAmount < LayoutItem.HEADER_SIZE) {
						edgeBackground.draw(batch, overItem.getX(), overItem.getY() + overItem.getHeight() - LayoutItem.HEADER_SIZE, overItem.getWidth(), LayoutItem.HEADER_SIZE);

					} else {
						edgeBackground.draw(batch, overItem.getX(), overItem.getY() + ((1-verticalPercent) * overItem.getHeight()), overItem.getWidth(), verticalPercent * overItem.getHeight());
					}

				}
			}




		}
	}
}
