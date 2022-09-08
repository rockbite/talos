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
	private ObjectMap<String, LayoutApp> apps = new ObjectMap<>();

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

	public void addContent (LayoutApp layoutApp) {
		addContent(layoutApp, false);
	}

	public void addContent (LayoutApp layoutApp, boolean copy) {
		addSpacer();

		apps.put(layoutApp.getUniqueIdentifier(), layoutApp);

		if (copy) {
			tabBar.add(layoutApp.copyTabWidget());
		} else {
			tabBar.add(layoutApp.getTabWidget());

			grid.registerDragSource(this, layoutApp, layoutApp.getTabWidget());
		}
	}

	public void removeContent (String uniqueID) {
		removeContent(apps.get(uniqueID));
	}
	public void removeContent (LayoutApp app) {
		apps.remove(app.getUniqueIdentifier());

		tabBar.clearChildren();
		for (ObjectMap.Entry<String, LayoutApp> barActor : apps) {
			addSpacer();
			tabBar.add(barActor.value.getTabWidget());
		}

		invalidate();

	}

	public Actor hitTabTable (Vector2 localCoords) {
		Actor hit = hit(localCoords.x, localCoords.y, true);

		if (hit == tabBar) {
			return tabBar;
		}

		for (LayoutApp value : apps.values()) {
			if (value.getTabWidget() == hit) {
				return tabBar;
			}
		}

		return null;
	}

	public Table getTabTable () {
		return tabBar;
	}

	@Override
	public boolean isEmpty () {
		return apps.isEmpty();
	}

	@Override
	public void removeItem (LayoutItem item) {

	}

	@Override
	public void exchangeItem (LayoutItem target, LayoutItem newItem) {
		throw new UnsupportedOperationException("Not supported operation");

	}

}
