package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisLabel;

public class LayoutContent extends LayoutItem {

	private final Table innerContents;
	private final Table tabBar;
	private final Drawable headerDrawable;
	private ObjectMap<String,Actor> barActors = new ObjectMap<>();

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

		headerDrawable = skin.newDrawable("white", 0.2f, 0.2f, 0.2f, 1f);
		tabBar.setBackground(headerDrawable);

		innerContents.add(tabBar).growX();

	}

	void addSpacer () {
		tabBar.add(new VisLabel("|"));
	}

	public void addContent (String content) {
		addSpacer();

		VisLabel label = new VisLabel(content);
		tabBar.add(label);

		barActors.put(content, label);

		grid.registerDragSource(this, label);


		//Register drag and drop with the Grid
	}

	public void removeContent (String content) {
		Actor remove = barActors.remove(content);

		tabBar.clearChildren();
		for (ObjectMap.Entry<String, Actor> barActor : barActors) {
			addSpacer();
			tabBar.add(barActor.value);
		}

		invalidate();

		System.out.println("Remove content " + content);
	}

	public Actor hitTabTable (Vector2 localCoords) {
		Actor hit = hit(localCoords.x, localCoords.y, true);

		if (hit == tabBar) {
			return tabBar;
		}

		if (barActors.containsValue(hit, true)) {
			return tabBar;
		}

		return null;
	}

	public Table getTabTable () {
		return tabBar;
	}

	@Override
	public boolean isEmpty () {
		return barActors.isEmpty();
	}

	@Override
	public void removeItem (LayoutContent content) {

	}

	@Override
	public void exchange (LayoutContent target, LayoutItem newColumn) {
		throw new UnsupportedOperationException("SHouldn't be allowed");
	}
}
