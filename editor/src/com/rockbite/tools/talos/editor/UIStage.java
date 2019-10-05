package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.dialogs.BatchConvertDialog;
import com.rockbite.tools.talos.editor.dialogs.SettingsDialog;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.editor.widgets.ui.TimelineWidget;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class UIStage {

	private final Stage stage;
	private final Skin skin;

	Table fullScreenTable;

	private TimelineWidget timelineWidget;
	private PreviewWidget previewWidget;

	FileChooser fileChooser;
	BatchConvertDialog batchConvertDialog;
	SettingsDialog settingsDialog;

	ColorPicker colorPicker;

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

		initFileChoosers();

		batchConvertDialog = new BatchConvertDialog();
		settingsDialog = new SettingsDialog();

		colorPicker = new ColorPicker();
		colorPicker.padTop(32);
		colorPicker.padLeft(16);
		colorPicker.setHeight(330);
		colorPicker.setWidth(430);
		colorPicker.padRight(26);
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
		Menu modulesMenu = new Menu("Modules");
		menuBar.addMenu(modulesMenu);
		Menu helpMenu = new Menu("Help");
		MenuItem about = new MenuItem("About");
		helpMenu.addItem(about);
		menuBar.addMenu(helpMenu);

		about.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				VisDialog dialog = Dialogs.showOKDialog(stage, "About Talos 1.0.4", " - ");
			}
		});

		MenuItem createModule = new MenuItem("Create Module");
		PopupMenu createPopup = createModuleListPopup(null);
		createModule.setSubMenu(createPopup);
		MenuItem removeSelectedModules = new MenuItem("Remove Selected").setShortcut(Input.Keys.DEL);
		MenuItem groupSelectedModules = new MenuItem("Group Selected").setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.G);
		MenuItem ungroupSelectedModules = new MenuItem("Ungroup Selected").setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.U);
		modulesMenu.addItem(createModule);
		modulesMenu.addItem(removeSelectedModules);
		modulesMenu.addItem(groupSelectedModules);

		MenuItem newProject = new MenuItem("New Project");
		MenuItem openProject = new MenuItem("Open Project");
		MenuItem saveProject = new MenuItem("Save Project");
		MenuItem export = new MenuItem("Export");
		MenuItem examples = new MenuItem("Examples");

		MenuItem legacy = new MenuItem("Legacy");
		PopupMenu legacyPopup = new PopupMenu();
		MenuItem legacyImportItem = new MenuItem("Import");
		MenuItem legacyBatchImportItem = new MenuItem("Batch Convert");
		legacyPopup.addItem(legacyImportItem);
		legacyPopup.addItem(legacyBatchImportItem);
		legacy.setSubMenu(legacyPopup);

		MenuItem settings = new MenuItem("Preferences");

		PopupMenu examplesPopup = new PopupMenu();
		examples.setSubMenu(examplesPopup);
		initExampleList(examplesPopup);
		MenuItem saveAsProject = new MenuItem("Save As Project");
		MenuItem exitApp = new MenuItem("Exit");

		projectMenu.addItem(newProject);
		projectMenu.addItem(openProject);
		projectMenu.addItem(saveProject);
		projectMenu.addItem(saveAsProject);
		projectMenu.addItem(export);
		projectMenu.addSeparator();
		projectMenu.addItem(examples);
		projectMenu.addItem(legacy);
		projectMenu.addSeparator();
		projectMenu.addItem(settings);
		projectMenu.addSeparator();
		projectMenu.addItem(exitApp);

		removeSelectedModules.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				TalosMain.Instance().NodeStage().moduleBoardWidget.deleteSelectedWrappers();
			}
		});

		groupSelectedModules.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				TalosMain.Instance().NodeStage().moduleBoardWidget.createGroupFromSelectedWrappers();
			}
		});

		ungroupSelectedModules.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				TalosMain.Instance().NodeStage().moduleBoardWidget.ungroupSelectedWrappers();
			}
		});

		newProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				newProjectAction();
			}
		});

		openProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				openProjectAction();
			}
		});

		saveProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				saveProjectAction();
			}
		});

		export.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				exportAction();
			}
		});

		saveAsProject.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				saveAsProjectAction();
			}
		});

		legacyImportItem.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				legacyImportAction();
			}
		});

		legacyBatchImportItem.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				legacyBatchConvertAction();
			}
		});

		exitApp.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				System.exit(0);
			}
		});
		settings.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				stage.addActor(settingsDialog.fadeIn());
			}
		});

		topTable.add(menuBar.getTable()).left().grow();

		fullScreenTable.add(topTable).growX();

		// adding key listeners for menu items
		stage.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Input.Keys.N && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
					TalosMain.Instance().Project().newProject();
				}
				if(keycode == Input.Keys.O && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
					openProjectAction();
				}
				if(keycode == Input.Keys.S && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
					saveProjectAction();
				}

				return super.keyDown(event, keycode);
			}

		});
	}

	public PopupMenu createModuleListPopup(Vector2 location) {
		if(location == null) {
			OrthographicCamera cam = (OrthographicCamera) TalosMain.Instance().NodeStage().getStage().getCamera();
			location = new Vector2(cam.position.x, cam.position.y);
		}
		PopupMenu menu = new PopupMenu();
		Array<Class> temp = new Array<>();
		for (Class registeredModule : ParticleEmitterDescriptor.getRegisteredModules()) {
			temp.add(registeredModule);
		}
		temp.sort(new Comparator<Class>() {
			@Override
			public int compare (Class o1, Class o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});
		for(final Class clazz : temp) {
			String className = clazz.getSimpleName();
			MenuItem menuItem = new MenuItem(className);
			menu.addItem(menuItem);

			final Vector2 finalLocation = location;
			menuItem.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					TalosMain.Instance().NodeStage().moduleBoardWidget.createModule( clazz, finalLocation.x, finalLocation.y);
				}
			});
		}

		return menu;
	}

	private void initFileChoosers() {
		fileChooser = new FileChooser(FileChooser.Mode.SAVE);
		fileChooser.setBackground(skin.getDrawable("window-noborder"));
	}


	private void newProjectAction() {
		TalosMain.Instance().Project().newProject();
	}


	private void openProjectAction() {
		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".tls");
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				TalosMain.Instance().Project().loadProject(Gdx.files.absolute(file.first().file().getAbsolutePath()));
			}
		});

		stage.addActor(fileChooser.fadeIn());
	}

	private void saveProjectAction() {
		if(!TalosMain.Instance().Project().isBoundToFile()) {
			saveAsProjectAction();
		} else {
			TalosMain.Instance().Project().saveProject();
		}
	}

	private void exportAction() {
		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".p");
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				if(!path.endsWith(".p")) {
					if(path.indexOf(".") > 0) {
						path = path.substring(0, path.indexOf("."));
					}
					path += ".p";
				}
				FileHandle handle = Gdx.files.absolute(path);
				TalosMain.Instance().Project().exportProject(handle);
			}
		});

		stage.addActor(fileChooser.fadeIn());
	}

	private void saveAsProjectAction() {
		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".tls");
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				if(!path.endsWith(".tls")) {
					if(path.indexOf(".") > 0) {
						path = path.substring(0, path.indexOf("."));
					}
					path += ".tls";
				}
				FileHandle handle = Gdx.files.absolute(path);
				TalosMain.Instance().Project().saveProject(handle);
			}
		});

		stage.addActor(fileChooser.fadeIn());
	}


	public void legacyImportAction() {
		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileChooser.DefaultFileFilter(fileChooser));
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				TalosMain.Instance().Project().importFromLegacyFormat(file.get(0));
			}
		});

		stage.addActor(fileChooser.fadeIn());
	}

	public void legacyBatchConvertAction() {
		// show dialog for batch convert
		stage.addActor((batchConvertDialog.fadeIn()));
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
			final String fileName = sample.getAttribute("file");
			MenuItem item = new MenuItem(name);
			examples.addItem(item);

			item.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					//openProject(fileName);
					TalosMain.Instance().Project().loadProject(Gdx.files.internal("samples/" + fileName));
					TalosMain.Instance().Project().resetCurrentProjectPath();
				}
			});
		}
	}

	public TimelineWidget Timeline() {
		return timelineWidget;
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public void setEmitters (Array<ParticleEmitterWrapper> emitterWrappers) {
		timelineWidget.setEmitters(emitterWrappers);
	}

	public void showColorPicker(ColorPickerListener listener) {
		colorPicker.setListener(listener);
		TalosMain.Instance().UIStage().getStage().addActor(colorPicker.fadeIn());
	}
}
