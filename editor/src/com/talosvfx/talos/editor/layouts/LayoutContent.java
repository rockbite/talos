package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;

public class LayoutContent extends LayoutItem {

	private final Table innerContents;
	private final Table tabBar;
	private final Drawable headerDrawable;
	private Array<String> contents = new Array<>();
	private Array<Actor> barActors = new Array<>();

	public LayoutContent (Skin skin, LayoutGrid grid) {
		super(skin, grid);


		innerContents = new Table();

		innerContents.top().left();
		innerContents.defaults();

		innerContents.setFillParent(true);
		addActor(innerContents);


		tabBar = new Table();
		tabBar.setTouchable(Touchable.enabled);

		tabBar.defaults().pad(5);
		tabBar.left();

		headerDrawable = skin.newDrawable("white", 1f, 0, 0, 1f);
		tabBar.setBackground(headerDrawable);

		innerContents.add(tabBar).growX();

	}

	public void addContent (String content) {
		contents.add(content);

		VisLabel label = new VisLabel(content);
		tabBar.add(label);

		barActors.add(label);

		grid.registerDragSource(label);


		//Register drag and drop with the Grid
	}

	public void removeContent (String content) {
		contents.removeValue(content, true);

		tabBar.clearChildren();
		for (String c : contents) {
			VisLabel label = new VisLabel(c);
			tabBar.add(label);
		}

		//Remove drag and drop with the grid
	}

	public Actor hitTabTable (Vector2 localCoords) {
		Actor hit = hit(localCoords.x, localCoords.y, true);

		if (hit == tabBar) {
			return tabBar;
		}

		if (barActors.contains(hit, true)) {
			return tabBar;
		}

		return null;
	}

	public Table getTabTable () {
		return tabBar;
	}
}
