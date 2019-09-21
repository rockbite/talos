package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.editor.widgets.ui.TimelineWidget;

public class UIStage {

	private final Stage stage;
	private final Skin skin;

	Table fullScreenTable;

	private TimelineWidget timelineWidget;
	private PreviewWidget previewWidget;

	public UIStage (Skin skin) {
		stage = new Stage(new ScreenViewport());
		this.skin = skin;
	}

	public void init () {
		fullScreenTable = new Table();
		fullScreenTable.setFillParent(true);

		stage.addActor(fullScreenTable);

		defaults();
		constructMenu();
		constructSplitPanes();
	}

	public Stage getStage () {
		return stage;
	}

	private void defaults () {
		fullScreenTable.top().left();
	}

	private void constructMenu () {
		Table topTable = new Table();
		topTable.setBackground(skin.getDrawable("button-main-menu"));

		MenuBar menuBar = new MenuBar();
		Menu projectMenu = new Menu("File");
		menuBar.addMenu(projectMenu);
		Menu helpMenu = new Menu("Help");
		MenuItem about = new MenuItem("About");
		helpMenu.addItem(about);
		menuBar.addMenu(helpMenu);

		about.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				VisDialog dialog = Dialogs.showOKDialog(stage, "About Talos 1.0.2", "Talos Particle Editor 1.0.2");
			}
		});

		MenuItem newProject = new MenuItem("New Project");
		MenuItem openProject = new MenuItem("Open Project");
		MenuItem saveProject = new MenuItem("Save Project");
		MenuItem examples = new MenuItem("Examples");
		MenuItem importItem = new MenuItem("Legacy Import");
		PopupMenu examplesPopup = new PopupMenu();
		examples.setSubMenu(examplesPopup);
		initExampleList(examplesPopup);
		MenuItem saveAsProject = new MenuItem("Save As Project");
		MenuItem exitApp = new MenuItem("Exit");

		projectMenu.addItem(newProject);
		projectMenu.addItem(openProject);
		projectMenu.addItem(saveProject);
		projectMenu.addItem(saveAsProject);
		projectMenu.addSeparator();
		projectMenu.addItem(examples);
		projectMenu.addItem(importItem);
		projectMenu.addSeparator();
		projectMenu.addItem(exitApp);

		newProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				//newProjectAction();
			}
		});

		openProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				//openProjectAction();
			}
		});

		saveProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				//saveProjectAction();
			}
		});

		saveAsProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				//saveAsProjectAction();
			}
		});

		importItem.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				//legacyImport();
			}
		});

		exitApp.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				System.exit(0);
			}
		});

		topTable.add(menuBar.getTable()).left().grow();

		fullScreenTable.add(topTable).growX();
	}


	private void constructSplitPanes () {
		previewWidget = new PreviewWidget();

		timelineWidget = new TimelineWidget(skin);

		Table midTable = new Table();
		Table bottomTable = new Table();
		bottomTable.setSkin(skin);
		bottomTable.setBackground(skin.getDrawable("button-main-menu"));

		Table timelineContainer = new Table();
		Table libraryContainer = new Table();

		libraryContainer.addListener(new ClickListener(0) { //Quick hack for library container intercepting touch as its an empty table currently
			@Override
			public void clicked (InputEvent event, float x, float y) {
			}
		});
		libraryContainer.addListener(new ClickListener(1) { //Quick hack for library container intercepting touch as its an empty table currently
			@Override
			public void clicked (InputEvent event, float x, float y) {
			}
		});
		libraryContainer.setTouchable(Touchable.enabled);
		VisSplitPane bottomPane = new VisSplitPane(timelineContainer, libraryContainer, false);

		timelineContainer.add(timelineWidget).grow().expand().fill();
		bottomTable.add(bottomPane).expand().grow();

		VisSplitPane verticalPane = new VisSplitPane(midTable, bottomTable, true);
		verticalPane.setMaxSplitAmount(0.8f);
		verticalPane.setMinSplitAmount(0.2f);
		verticalPane.setSplitAmount(0.7f);

		Table leftTable = new Table(); leftTable.setSkin(skin);
		leftTable.add(previewWidget).grow();
		Table rightTable = new Table(); rightTable.setSkin(skin);
		rightTable.add().grow();
		VisSplitPane horizontalPane = new VisSplitPane(leftTable, rightTable, false);
		midTable.add(horizontalPane).expand().grow().fill();
		horizontalPane.setMaxSplitAmount(0.8f);
		horizontalPane.setMinSplitAmount(0.2f);
		horizontalPane.setSplitAmount(0.3f);

		fullScreenTable.row();
		fullScreenTable.add(verticalPane).grow();
	}


	private void initExampleList (PopupMenu examples) {
		FileHandle list = Gdx.files.internal("samples/list.xml");
		XmlReader xmlReader = new XmlReader();
		XmlReader.Element root = xmlReader.parse(list);
		Array<XmlReader.Element> samples = root.getChildrenByName("sample");
		for (XmlReader.Element sample : samples) {
			String name = sample.getAttribute("name");
			String fileName = sample.getAttribute("file");
			MenuItem item = new MenuItem(name);
			examples.addItem(item);

			item.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					//openProject(fileName);
					//currentProjectPath = null;
				}
			});
		}
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public void setEmitters (Array<EmitterWrapper> emitterWrappers) {
		timelineWidget.setEmitters(emitterWrappers);
	}
}
