package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;

public class DummyLayoutApp implements LayoutApp {

	private String tabName;

	private transient Actor tabWidget;
	private transient Actor mainContent;
	private transient Skin skin;

	public DummyLayoutApp (Skin skin, String tabName) {
		this.tabName = tabName;
		build(skin);
	}

	public void build (Skin skin) {
		this.skin = skin;

		tabWidget = createTab(tabName);
		mainContent = createMainContent();
	}

	private Table createTab (String tabName) {
		Table tab = new Table();
		tab.setTouchable(Touchable.enabled);
		tab.setBackground(skin.getDrawable("tab-bg"));

		tab.padLeft(10);
		tab.padRight(10);
		VisLabel visLabel = new VisLabel(tabName.substring(0, Math.min(10, tabName.length())));
		tab.add(visLabel);

		return tab;
	}

	private Actor createMainContent () {
		Table table = new Table();
		table.setBackground(skin.newDrawable("white", 0.2f, 0.2f, 0.2f, 1f));
		return table;
	}

	@Override
	public String getUniqueIdentifier () {
		return tabName;
	}

	@Override
	public Actor getTabWidget () {
		return tabWidget;
	}

	@Override
	public Actor copyTabWidget () {
		return createTab(tabName);
	}

	@Override
	public Actor getMainContent () {
		return mainContent;
	}

	@Override
	public Actor getCopyMainContent () {
		Table table = new Table();
		table.setBackground(skin.newDrawable("white", 0.5f, 0.5f, 0.5f, 1f));
		return table;
	}
}
