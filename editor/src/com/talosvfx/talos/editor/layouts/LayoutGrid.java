package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisLabel;

import java.util.Objects;

public class LayoutGrid extends WidgetGroup {
	private final DragAndDrop dragAndDrop;

	//Start with a 2x2 grid with a 'root' container

	//Todo force to rows, always root is rows
	LayoutItem root;

	LayoutContent overItem;
	LayoutContent startItem;
	boolean highlightEdge = true;
	private Skin skin;
	private Drawable edgeBackground;

	private DragHitResult dragHitResult = new DragHitResult();

	private ObjectMap<LayoutContentApp, DragAndDrop.Source> sources = new ObjectMap<>();
	private ObjectMap<LayoutContent, DragAndDrop.Target> targets = new ObjectMap<>();

	float horizontalPercent = 0.3f;
	float verticalPercent = 0.3f;

	public LayoutGrid (Skin skin) {
		this.skin = skin;

		edgeBackground = skin.newDrawable("white", 1f, 1f, 1f, 0.5f);
		dragAndDrop = new DragAndDrop();
		dragAndDrop.setKeepWithinStage(false);
	}

	public void removeContent (LayoutContent content) {
		removeContent(content, true);
	}

	public void removeContent (LayoutContent content, boolean removeEmptyParent) {
		removeDragTarget(content);

		if (content == root) {
			root = null;
			removeActor(content);
		} else {
			removeRecursive(content, removeEmptyParent);
		}
	}
	private void removeRecursive (LayoutItem content, boolean removeEmpty) {
		LayoutItem parent = (LayoutItem)content.getParent(); //Its always going to be a LayoutItem

		parent.removeItem(content);

		if (removeEmpty && parent.isEmpty()) {
			removeRecursive(parent, removeEmpty);
		}


	}

	public enum LayoutDirection {
		UP,
		RIGHT,
		DOWN,
		LEFT,
		TAB
	}

	public void addContent (LayoutItem content) {
		if (root == null) {
			root = content;
			addActor(root);
		} else {
			if (root instanceof LayoutRow) {
				((LayoutRow)root).addColumnContainer(content, false);
			} else {
				//Exchange root
				LayoutItem oldRoot = root;
				removeActor(oldRoot);

				LayoutRow newRow = new LayoutRow(skin, this);
				newRow.addColumnContainer(oldRoot, false);
				newRow.addColumnContainer(content, false);

				root = newRow;
				addActor(root);
			}
		}

		if (content instanceof LayoutContent) {
			registerDragTarget((LayoutContent)content);
		}

	}

	void registerDragTarget (LayoutContent layoutContent) {
		DragAndDrop.Target target = new DragAndDrop.Target(layoutContent) {
			@Override
			public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

				//should just always be true

				return true;
			}

			@Override
			public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				DragHitResult hitResult = dragHitResult;
				if (hitResult.hit == null)
					return;
				if (hitResult.hit == startItem)
					return;

				dropContainer((LayoutContentApp)payload.getObject(), dragHitResult.hit, dragHitResult.direction);
			}
		};
		dragAndDrop.addTarget(target);
		targets.put(layoutContent, target);
	}

	public void removeDragTarget (LayoutContent content) {
		DragAndDrop.Target remove = targets.remove(content);
		dragAndDrop.removeTarget(remove);
	}

	private void dropContainer (LayoutContentApp source, LayoutContent target, LayoutDirection direction) {
		//Here comes the logic

		LayoutContent parent = source.layoutContent;
		String app = source.app;

		//Source
		//Remove drag and drop
		DragAndDrop.Source dragAndDropSource = sources.remove(source);
		dragAndDrop.removeSource(dragAndDropSource);

		System.out.println("Removed source");

		//Potentially removes all the shit from hierarchy if its last one
		parent.removeContent(app);

		if (parent.isEmpty()) {
			System.out.println("Parent is empty, removing content");
			removeRecursive(parent, true);
		}

		//Target
		switch (direction) {

		//If its up or down we get the parent of the target and wrap it in a row, then add our content to the top or bottom
		case UP:
		case DOWN: {
			LayoutItem parentItem = (LayoutItem)target.getParent();

			//Check the parent item for the target. If its already a layout row, we can just add at top or bottom

			LayoutColumn colTarget;

			boolean isExistingColumn = false;
			if (parentItem instanceof LayoutColumn) {
				colTarget = (LayoutColumn)parentItem;
				isExistingColumn = true;
			} else {
				//Its TIME TO WRAP

				LayoutColumn newColumn = new LayoutColumn(skin, this);

				//Remove the target
				exchangeAndWrapToColumn(newColumn, target);

				colTarget = newColumn;

			}

			LayoutContent newLayoutContent = new LayoutContent(skin, this);
			newLayoutContent.setRandomColour(parent.getRandomColour());
			registerDragTarget(newLayoutContent);
			newLayoutContent.addContent(app);
			colTarget.addRowContainer(newLayoutContent, direction == LayoutDirection.UP, isExistingColumn ? target : null);
		}
		break;

		case RIGHT:
		case LEFT: {

			LayoutItem parentItem = (LayoutItem)target.getParent();

			//Check the parent item for the target. If its already a layout row, we can just add at top or bottom

			LayoutRow rowTarget;

			boolean isExistingRow = false;
			if (parentItem instanceof LayoutRow) {
				rowTarget = (LayoutRow)parentItem;
				isExistingRow = true;
			} else {
				//Its TIME TO WRAP

				LayoutRow newRow = new LayoutRow(skin, this);

				//Remove the target
				exchangeAndWrapToRow(newRow, target);

				rowTarget = newRow;

			}

			LayoutContent newLayoutContent = new LayoutContent(skin, this);
			newLayoutContent.setRandomColour(parent.getRandomColour());
			registerDragTarget(newLayoutContent);
			newLayoutContent.addContent(app);
			rowTarget.addColumnContainer(newLayoutContent, direction == LayoutDirection.LEFT, isExistingRow ? target : null);
		}

		break;

		case TAB:

			//If its a tab, we add it to the target
			target.addContent(app);
			break;
		default:
			throw new IllegalStateException("Unexpected value: " + direction);
		}

	}

	private void exchangeAndWrapToColumn (LayoutColumn newColumn, LayoutContent target) {

		//We need to swap this column with the parent
		LayoutItem parent = (LayoutItem)target.getParent();
		parent.exchangeItem(target, newColumn);
		newColumn.setRelativeWidth(target.getRelativeWidth());
		newColumn.setRelativeHeight(target.getRelativeHeight());

		target.setRelativeWidth(1f);
		target.setRelativeHeight(1f);

		newColumn.addRowContainer(target, false);
	}

	private void exchangeAndWrapToRow (LayoutRow newRow, LayoutContent target) {

		//We need to swap this column with the parent
		LayoutItem parent = (LayoutItem)target.getParent();
		parent.exchangeItem(target, newRow);

		newRow.setRelativeWidth(target.getRelativeWidth());
		newRow.setRelativeHeight(target.getRelativeHeight());

		target.setRelativeWidth(1f);
		target.setRelativeHeight(1f);

		newRow.addColumnContainer(target, false);
	}

	//Add each LayoutContent for drag and drop as a source
	//Add each LayoutContent for drag and drop as a target
	void registerDragSource (LayoutContent parent, VisLabel actorToDrag) {

		LayoutContentApp userObject = new LayoutContentApp(parent, actorToDrag.getText().toString());

		DragAndDrop.Source source = new DragAndDrop.Source(actorToDrag) {
			@Override
			public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
				DragAndDrop.Payload payload = new DragAndDrop.Payload();

				LayoutContent dummy = new LayoutContent(skin, LayoutGrid.this);
				dummy.setRandomColour(parent.getRandomColour());
				dummy.setSize(200, 200);
				dummy.addContent(actorToDrag.getText().toString());

				payload.setDragActor(dummy);
				payload.setObject(userObject);

				startItem = parent;

				return payload;
			}

			@Override
			public void drag (InputEvent event, float x, float y, int pointer) {
				super.drag(event, x, y, pointer);

				float unhitSize = 200;

				Actor dragActor = dragAndDrop.getDragActor();
				if (dragActor != null) {

					//Find out if we hit something, and if so what side

					getDragHit(dragHitResult);

					Vector2 vector2 = new Vector2();
					float hitInStageX = vector2.x;
					float hitInStageY = vector2.y;

					LayoutContent hitResult = dragHitResult.hit;
					if (hitResult != null) {
						if (hitResult == startItem) {
							return;
						}

						switch (dragHitResult.direction) {
						case UP:

							dragActor.setSize(hitResult.getWidth(), hitResult.getHeight() * verticalPercent);

							//The offset needs to be the difference between the drag x and y and the target x and y
							vector2.setZero();
							dragHitResult.hit.localToStageCoordinates(vector2);
							vector2.sub(x, y);

							hitInStageX = vector2.x;
							hitInStageY = vector2.y;

							vector2.set(Gdx.input.getX(), Gdx.input.getY());
							screenToLocalCoordinates(vector2);

							dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + hitResult.getWidth(), y - (vector2.y - hitInStageY) + hitResult.getHeight() - dragActor.getHeight());

							break;

						case DOWN:
							dragActor.setSize(hitResult.getWidth(), hitResult.getHeight() * verticalPercent);

							//The offset needs to be the difference between the drag x and y and the target x and y
							vector2.setZero();
							dragHitResult.hit.localToStageCoordinates(vector2);
							vector2.sub(x, y);

							hitInStageX = vector2.x;
							hitInStageY = vector2.y;

							vector2.set(Gdx.input.getX(), Gdx.input.getY());
							screenToLocalCoordinates(vector2);

							dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + hitResult.getWidth(), y - (vector2.y - hitInStageY));
							break;

						case RIGHT:
							dragActor.setSize(horizontalPercent * hitResult.getWidth(), hitResult.getHeight());

							//The offset needs to be the difference between the drag x and y and the target x and y
							vector2.setZero();
							dragHitResult.hit.localToStageCoordinates(vector2);
							vector2.sub(x, y);

							hitInStageX = vector2.x;
							hitInStageY = vector2.y;

							vector2.set(Gdx.input.getX(), Gdx.input.getY());
							screenToLocalCoordinates(vector2);

							dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + hitResult.getWidth(), y - (vector2.y - hitInStageY));
							break;

						case LEFT:
							dragActor.setSize(horizontalPercent * hitResult.getWidth(), hitResult.getHeight());

							//The offset needs to be the difference between the drag x and y and the target x and y
							vector2.setZero();
							dragHitResult.hit.localToStageCoordinates(vector2);
							vector2.sub(x, y);

							hitInStageX = vector2.x;
							hitInStageY = vector2.y;

							vector2.set(Gdx.input.getX(), Gdx.input.getY());
							screenToLocalCoordinates(vector2);

							dragAndDrop.setDragActorPosition(x - (vector2.x - hitInStageX) + dragActor.getWidth(), y - (vector2.y - hitInStageY));
							break;

						case TAB:
							Table tabTable = hitResult.getTabTable();
							dragActor.setSize(200, tabTable.getHeight());

							vector2.setZero();
							dragHitResult.hit.localToStageCoordinates(vector2);
							vector2.sub(x, y);

							hitInStageX = vector2.x;
							hitInStageY = vector2.y;

							vector2.set(Gdx.input.getX(), Gdx.input.getY());
							screenToLocalCoordinates(vector2);

							dragAndDrop.setDragActorPosition(+dragActor.getWidth() / 2f, y - (vector2.y - hitInStageY) + hitResult.getHeight() - dragActor.getHeight());

							break;
						}
					} else {
						dragActor.setSize(unhitSize, unhitSize);
						dragAndDrop.setDragActorPosition(dragActor.getWidth() / 2f, -dragActor.getHeight() / 2f);
					}

				}

			}

			@Override
			public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
				super.dragStop(event, x, y, pointer, payload, target);
				startItem = null;
				dragHitResult.hit = null;
				dragHitResult.direction = null;
			}
		};
		dragAndDrop.addSource(source);

		sources.put(userObject, source);
	}

	private void getDragHit (DragHitResult dragHitResult) {
		dragHitResult.direction = null;
		dragHitResult.hit = null;

		if (overItem == null)
			return;

		//Detect the edge
		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		Vector2 coords = new Vector2(x, y);
		overItem.screenToLocalCoordinates(coords);

		Vector2 localCoords = new Vector2(coords.x, coords.y);

		coords.scl(1f / overItem.getWidth(), 1f / overItem.getHeight());

		//UNiversal coordinates

		float distanceFromMiddleX = Math.abs(0.5f - coords.x);
		float distanceFromMiddleY = Math.abs(0.5f - coords.y);

		if (distanceFromMiddleX >= distanceFromMiddleY) {
			//Its going to be an X if it exists

			if (coords.x < horizontalPercent) {
				//Left edge
				dragHitResult.hit = overItem;
				dragHitResult.direction = LayoutDirection.LEFT;
			} else if (coords.x > (1 - horizontalPercent)) {
				//Right edge
				dragHitResult.hit = overItem;
				dragHitResult.direction = LayoutDirection.RIGHT;
			}

		} else {
			//its going to be Y if it exists
			if (coords.y < verticalPercent) {

				dragHitResult.hit = overItem;
				dragHitResult.direction = LayoutDirection.DOWN;
			} else if (coords.y > (1 - verticalPercent)) {
				//top edge

				boolean hitLabel = false;

				Actor hit = null;
				if ((hit = ((LayoutContent)overItem).hitTabTable(localCoords)) != null) {
					dragHitResult.hit = overItem;
					dragHitResult.direction = LayoutDirection.TAB;
					hitLabel = true;
				}

				if (!hitLabel) {
					dragHitResult.hit = overItem;
					dragHitResult.direction = LayoutDirection.UP;
				}
			}
		}

	}

	static class LayoutContentApp {
		public LayoutContent layoutContent;
		public String app;

		public LayoutContentApp (LayoutContent parent, String app) {
			this.layoutContent = parent;
			this.app = app;
		}

		@Override
		public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			LayoutContentApp that = (LayoutContentApp)o;
			return Objects.equals(layoutContent, that.layoutContent) && Objects.equals(app, that.app);
		}

		@Override
		public int hashCode () {
			return Objects.hash(layoutContent, app);
		}
	}

	static class DragHitResult {
		public LayoutContent hit;
		public LayoutDirection direction;
	}

	@Override
	public void layout () {
		super.layout();
		if (root == null)
			return;

		root.setBounds(0, 0, getWidth(), getHeight());
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (root == null)
			return;

		int x = Gdx.input.getX();
		int y = Gdx.graphics.getHeight() - Gdx.input.getY();
		Vector2 screenCoords = new Vector2(x, y);
		screenToLocalCoordinates(screenCoords);

		Actor hit = hit(x, y, true);
		if (hit instanceof LayoutContent) {
			overItem = (LayoutContent)hit;
		} else if (hit != null) {
			if (hit.getParent() instanceof LayoutContent) {
				overItem = (LayoutContent)hit.getParent();
			} else if (hit.getParent() != null) {
				if (hit.getParent().getParent() instanceof LayoutContent) {
					overItem = (LayoutContent)hit.getParent().getParent();
				}
			}
		} else {
			overItem = null;
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (highlightEdge && overItem != null && dragHitResult.hit != null) {

			int x = Gdx.input.getX();
			int y = Gdx.input.getY();
			Vector2 coords = new Vector2(x, y);
			overItem.screenToLocalCoordinates(coords);

			Vector2 localCoords = new Vector2(coords.x, coords.y);

			switch (dragHitResult.direction) {
			case LEFT:
//				edgeBackground.draw(batch, overItem.getX(), overItem.getY(), horizontalPercent * overItem.getWidth(), overItem.getHeight());

				break;
			case RIGHT:
//				edgeBackground.draw(batch, overItem.getX() + ((1 - horizontalPercent) * overItem.getWidth()), overItem.getY(), horizontalPercent * overItem.getWidth(), overItem.getHeight());

				break;

			case UP:
//				edgeBackground.draw(batch, overItem.getX(), overItem.getY() + ((1 - verticalPercent) * overItem.getHeight()), overItem.getWidth(), verticalPercent * overItem.getHeight());

				break;
			case DOWN:
//				edgeBackground.draw(batch, overItem.getX(), overItem.getY(), overItem.getWidth(), verticalPercent * overItem.getHeight());

				break;

			case TAB:

				Actor hit = null;
				if ((hit = ((LayoutContent)overItem).hitTabTable(localCoords)) != null) {
					Vector2 out = new Vector2(0, 0);
					hit.localToAscendantCoordinates(this, out);
//					edgeBackground.draw(batch, out.x, out.y, hit.getWidth(), hit.getHeight());
				}

				break;
			}

		}
	}
}
