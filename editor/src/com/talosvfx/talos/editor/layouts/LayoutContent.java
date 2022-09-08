package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;

public class LayoutContent extends LayoutItem {

	private final Table innerContents;
	private final Table tabBar;
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

		tabBar.defaults().padLeft(5).padRight(5);
		tabBar.left();

		tabBar.debug();

		innerContents.add(tabBar).growX();

	}


	public void addContent (LayoutApp layoutApp) {
		addContent(layoutApp, false);
	}

	public void addContent (LayoutApp layoutApp, boolean copy) {
		apps.put(layoutApp.getUniqueIdentifier(), layoutApp);

		if (copy) {
			tabBar.add(layoutApp.copyTabWidget()).growY();
		} else {
			tabBar.add(layoutApp.getTabWidget()).growY();

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
