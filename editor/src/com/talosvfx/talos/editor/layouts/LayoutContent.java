package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;

public class LayoutContent extends LayoutItem {

	private final Table innerContents;
	private final Table tabBar;
	private final Table contentTable;
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


		contentTable = new Table();

		innerContents.add(tabBar).growX();
		innerContents.row();
		innerContents.add(contentTable).grow();

		Vector2 temp = new Vector2();
		tabBar.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				temp.set(x, y);
				tabBar.localToActorCoordinates(LayoutContent.this, temp);

				LayoutApp result = getAppForHit(temp);
				if (result != null) {
					swapToApp(result);
				}

			}
		});

		contentTable.debugAll();

	}

	private void swapToApp (LayoutApp result) {
		for (ObjectMap.Entry<String, LayoutApp> app : apps) {
			app.value.setTabActive(false);
		}
		contentTable.clearChildren();
		contentTable.add(result.getMainContent()).grow();
		result.setTabActive(true);
	}

	public void addContent (LayoutApp layoutApp) {
		addContent(layoutApp, false);
		layoutApp.setDestroyCallback(new DestroyCallback() {
			@Override
			public void onDestroyRequest () {
				removeContent(layoutApp);
			}
		});
	}

	public void addContent (LayoutApp layoutApp, boolean copy) {
		apps.put(layoutApp.getUniqueIdentifier(), layoutApp);

		if (copy) {
			tabBar.add(layoutApp.copyTabWidget()).growY();

			contentTable.clearChildren();
			contentTable.add(layoutApp.getCopyMainContent()).grow();
		} else {
			tabBar.add(layoutApp.getTabWidget()).growY();

			grid.registerDragSource(this, layoutApp, layoutApp.getTabWidget());

			//The state of content depends on the tabs

			swapToApp(layoutApp);
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

		if (!apps.isEmpty()) {
			for (ObjectMap.Entry<String, LayoutApp> first : apps) {
				swapToApp(first.value);
				break;
			}
		} else {
			contentTable.clearChildren();
			grid.removeContent(this);
		}

	}

	public LayoutApp getAppForHit (Vector2 localCoords) {
		Actor hit = hit(localCoords.x, localCoords.y, true);
		if (hit == null) return null;

		if (hit == tabBar) {
			return null;
		}


		for (LayoutApp value : apps.values()) {
			Actor tabWidget = value.getTabWidget();
			if (tabWidget == hit || hit.isDescendantOf(tabWidget)) {
				return value;
			}
		}

		return null;
	}

	public Actor hitTabTable (Vector2 localCoords) {
		Actor hit = hit(localCoords.x, localCoords.y, true);
		if (hit == null) return null;

		if (hit == tabBar) {
			return tabBar;
		}


		for (LayoutApp value : apps.values()) {
			Actor tabWidget = value.getTabWidget();
			if (tabWidget == hit || hit.isDescendantOf(tabWidget)) {
				return tabWidget;
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

	public ObjectMap<String, LayoutApp> getApps () {
		return apps;
	}
}
