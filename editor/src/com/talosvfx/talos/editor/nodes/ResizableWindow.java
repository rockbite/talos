package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.util.function.Consumer;

public class ResizableWindow extends Window {


	private boolean wasDragging = false;

	private boolean collapsed;
	private float collapseHeight = getPadTop();
	private float expandHeight;

	private final Vector2 tempPos = new Vector2();
	private final Vector2 tempSize = new Vector2();

	private Consumer<ResizableWindow> sizeChangeConsumer;

	public ResizableWindow (Skin skin) {
		super("", skin, "resizable");


		setModal(false);
		setResizable(true);
		setTouchable(Touchable.enabled);
		setKeepWithinStage(false);

		clearListeners();

		int MOVE = (1 << 5);

		addListener(new ClickListener() {
			float startX, startY, lastX, lastY;

			private void updateEdge (float x, float y) {
				float border = 16 / 2f;
				float width = getWidth(), height = getHeight();
				float left = border, right = width - border, bottom = border, top = height - border;
				edge = 0;

				if (isResizable() && x >= left - border && x <= right + border && y >= bottom - border) {
					if (x < left + border) edge |= Align.left;
					if (x > right - border) edge |= Align.right;
					if (y < bottom + border) edge |= Align.bottom;
					if (y > top - border) edge |= Align.top;
//					if (edge != 0) border += 25;
//					if (x < left + border) edge |= Align.left;
//					if (x > right - border) edge |= Align.right;
//					if (y < bottom + border) edge |= Align.bottom;
					//10100
					//00100
					boolean isOnXBorder = ((edge & Align.left) != 0);
					boolean isOnXWideBorder = ((edge & Align.right) != 0);
					boolean isOnYBorder = ((edge & Align.bottom) != 0);
					boolean isOnYTopBorder = ((edge & Align.top) != 0);

					if (isOnXBorder && isOnYBorder) {
//						Editor.getInstance().getCursors().setCursorTemp(Cursors.CursorType.LEFT_DOWN_UP);
					} else if (isOnXWideBorder && isOnYBorder) {
//						Editor.getInstance().getCursors().setCursorTemp(Cursors.CursorType.RIGHT_DOWN_UP);
					} else if (isOnXBorder && isOnYTopBorder) {
//						Editor.getInstance().getCursors().setCursorTemp(Cursors.CursorType.RIGHT_DOWN_UP);
					} else if (isOnXWideBorder && isOnYTopBorder) {
//						Editor.getInstance().getCursors().setCursorTemp(Cursors.CursorType.LEFT_DOWN_UP);
					} else if (isOnYBorder || isOnYTopBorder) {
//						Editor.getInstance().getCursors().setCursorTemp(Cursors.CursorType.UP_DOWN);
					} else if (isOnXBorder || isOnXWideBorder) {
//						Editor.getInstance().getCursors().setCursorTemp(Cursors.CursorType.LEFT_RIGHT);
					} else {
						if (isMovable()) {
							edge = MOVE;
						}
//						Editor.getInstance().getCursors().revert();
					}
				}
			}

			@Override
			public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
//				Editor.getInstance().getCursors().revert();
			}

			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				super.touchDown(event, x, y, pointer, button);

				if (event.isHandled()) return false;


				if (button == 0) {
					updateEdge(x, y);
					dragging = edge != 0;
					startX = x;
					startY = y;
					lastX = x - getWidth();
					lastY = y - getHeight();
				}
				return edge != 0 || isModal();
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);

				dragging = false;
			}

			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				if (getTapCount() == 2){
//					toggleCollapsed();
				}
			}


			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (!dragging) return;
				float width = getWidth(), height = getHeight();
				float windowX = getX(), windowY = getY();

				float minWidth = getMinWidth(), maxWidth = getMaxWidth();
				float minHeight = getMinHeight(), maxHeight = getMaxHeight();
				Stage stage = getStage();
				boolean clampPosition = false && getParent() == stage.getRoot();

				if ((edge & MOVE) != 0) {
					float amountX = x - startX, amountY = y - startY;
					windowX += amountX;
					windowY += amountY;
				}
				if ((edge & Align.left) != 0) {
					float amountX = x - startX;
					if (width - amountX < minWidth) amountX = -(minWidth - width);
					if (clampPosition && windowX + amountX < 0) amountX = -windowX;
					width -= amountX;
					windowX += amountX;
				}
				if ((edge & Align.bottom) != 0) {
					float amountY = y - startY;
					if (height - amountY < minHeight) amountY = -(minHeight - height);
					if (clampPosition && windowY + amountY < 0) amountY = -windowY;
					height -= amountY;
					windowY += amountY;
				}
				if ((edge & Align.right) != 0) {
					float amountX = x - lastX - width;
					if (width + amountX < minWidth) amountX = minWidth - width;
					if (clampPosition && windowX + width + amountX > stage.getWidth()) amountX = stage.getWidth() - windowX - width;
					width += amountX;
				}
				if ((edge & Align.top) != 0) {
					float amountY = y - lastY - height;
					if (height + amountY < minHeight) amountY = minHeight - height;
					if (clampPosition && windowY + height + amountY > stage.getHeight())
						amountY = stage.getHeight() - windowY - height;
					height += amountY;
				}
				setBounds(Math.round(windowX), Math.round(windowY), Math.round(width), Math.round(height));
			}

			public boolean mouseMoved (InputEvent event, float x, float y) {
				updateEdge(x, y);
				return isModal();
			}

			public boolean scrolled (InputEvent event, float x, float y, int amount) {
				return isModal();
			}

			public boolean keyDown (InputEvent event, int keycode) {
				return isModal();
			}

			public boolean keyUp (InputEvent event, int keycode) {
				return isModal();
			}

			public boolean keyTyped (InputEvent event, char character) {
				return isModal();
			}

		});


	}

	@Override
	public void act (float delta) {
		super.act(delta);

		if (wasDragging != isDragging()) {
			wasDragging = isDragging();
		}

		if (sizeChangeConsumer != null) {
			sizeChangeConsumer.accept(this);
		}
	}

//	private void expand () {
//		if (!collapsed) return;
//		setResizable(true);
//		setHeight(expandHeight);
//		collapsed = false;
//	}
//
//	private void collapse () {
//		if (collapsed) return;
//		setResizable(false);
//		expandHeight = getHeight();
//		setHeight(collapseHeight);
//		collapsed = true;
//	}
//
//	private void toggleCollapsed () {
//		if (collapsed){
//			expand();
//		} else {
//			collapse();
//		}
//	}

}
