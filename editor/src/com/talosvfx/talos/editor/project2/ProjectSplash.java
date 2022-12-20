package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.filesystem.FileSystemInteraction;
import com.talosvfx.talos.editor.nodes.widgets.ButtonWidget;
import com.talosvfx.talos.editor.nodes.widgets.LabelWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.widgets.propertyWidgets.EditableLabelWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ButtonLabel;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.FileOpenField;
import com.talosvfx.talos.editor.widgets.ui.common.RoundedFlatButton;

import java.io.File;

import static com.talosvfx.talos.editor.project2.TalosProjectData.TALOS_PROJECT_EXTENSION;

public class ProjectSplash extends Table {


	public ProjectSplash () {
		setBackground(SharedResources.skin.getDrawable("splash-window"));

		Image splash = new Image(new Texture(Gdx.files.internal("splash.png")));

		add(splash).width(990).pad(4);
		row();

		Table content = new Table();

		Table left = new Table();
		Table right = new Table();

		content.add(left).top().left().expand().grow().pad(10).padLeft(50).padBottom(20);
		content.add(right).top().right().expand().grow().pad(10).padRight(50).padBottom(20);

		Table createTable = new Table();
		Table openTable = new Table();
		buildCreateTable(createTable);
		buildOpenTable(openTable);

		ButtonLabel openBtn = new ButtonLabel(SharedResources.skin.getDrawable("ic-folder"), "Open...");
		openBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				// open
				FileSystemInteraction.instance().showFileChooser("tlsprj", new FileChooserListener() {
					@Override
					public void selected(Array<FileHandle> files) {
						boolean success = validateAndOpenProject(files.first());
						if (success) {
							ProjectSplash.this.hide();
						}
					}
				});
			}
		});

		left.add(createTable).grow();
		left.row();
		left.add(openBtn).padTop(30).left().expand();

		right.add(openTable).grow().top().expand();

		add(content).grow().padBottom(20);

		pack();
	}

	private void buildCreateTable (Table table) {


		VisLabel label = new VisLabel("New Project");
		label.setColor(Color.GRAY);

		table.add(label).left().expand();
		table.row();

		TextValueWidget projectName = new TextValueWidget(SharedResources.skin);
		projectName.setLabel("Project Name"); projectName.setValue("");
		table.add(projectName).width(300).left().expand().padTop(15);
		table.row();

		VisLabel projectDirectoryLabel = new VisLabel("Parent Directory");
		table.add(projectDirectoryLabel).left().expand().padTop(15);
		table.row();

		FileOpenField fileOpener = new FileOpenField();
		fileOpener.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {

			}
		});
		table.add(fileOpener).left().expand().padTop(15).width(300);
		table.row();

		RoundedFlatButton createBtn = new RoundedFlatButton(); createBtn.make("Create");
		createBtn.getStyle().up = ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.LIGHT_BLUE);
		table.add(createBtn).left().expand().padTop(15).width(100).padLeft(200);
		table.row();

		table.add().grow();

		createBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				validateAndCreateProject(projectName.getValue(), fileOpener.getPath());
			}
		});
	}

	private void validateAndCreateProject (String name, String parentDir) {

		if (name.isEmpty()) {
			Dialogs.showErrorDialog(SharedResources.stage, "Project name cannot be empty");
			return;
		}


		FileHandle dirHandle = Gdx.files.absolute(parentDir + File.separator + name);
		if (!dirHandle.exists()) {
			dirHandle.mkdirs();
		}

		TalosProjectData talosProjectData = TalosProjectData.newDefaultProject(name, dirHandle);
		SharedResources.projectLoader.loadProject(talosProjectData);

		hide();
	}

	private void buildOpenTable (Table table) {
		VisLabel label = new VisLabel("Open Recent");
		label.setColor(Color.GRAY);

		table.add(label).left().expandX().top();
		table.row();

		Table recentItems = new Table();

		Array<RecentProject> recentProjects = TalosLocalPrefs.Instance().getRecentProjects();

		int iter = 0;
		for (RecentProject recentProject : recentProjects) {
			if(iter++ > 5) break;

			FileHandle handle = Gdx.files.absolute(recentProject.getProjectPath());
			ButtonLabel recentProjectLabel = new ButtonLabel(SharedResources.skin.getDrawable("ic-file-blank"), handle.name());
			recentProjectLabel.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					boolean success = validateAndOpenProject(Gdx.files.absolute(recentProject.getProjectPath()));
					if (success) {
						ProjectSplash.this.hide();
					}
				}
			});
			recentItems.add(recentProjectLabel).left().expandX().top().padBottom(3);
			recentItems.row();
		}
		recentItems.add().grow();
		recentItems.row();

		table.add(recentItems).grow().top().expand().padTop(10);
		table.row();


		table.add().grow().expand();
		table.row();
	}

	private void selectDirectoryForCreatingProject (VisTextField projectDirectory) {
		FileChooser fileChooser = new FileChooser(Gdx.files.absolute(System.getProperty("user.home")), FileChooser.Mode.OPEN);
		fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> files) {
				FileHandle first = files.first();
				if (first.list().length > 0) {
					Dialogs.showConfirmDialog(SharedResources.stage, "Confirm project creation", "The directory is not empty\n"
						+ "do you want to generate a project in this directory?", new String[] {"Cancel", "Yes"}, new Object[] {false, true}, new ConfirmDialogListener<Object>() {
						@Override
						public void result (Object result) {
							if (result instanceof Boolean) {
								if (result == Boolean.TRUE) {
									projectDirectory.setText(first.path());
								}
							}
						}
					});
				} else {
					projectDirectory.setText(first.path());
				}
			}

			@Override
			public void canceled () {
				super.canceled();
			}
		});
		SharedResources.stage.addActor(fileChooser.fadeIn());
	}

	private void openProject () {
		FileChooser fileChooser = new FileChooser(Gdx.files.absolute(System.getProperty("user.home")), FileChooser.Mode.OPEN);
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES_AND_DIRECTORIES);
		fileChooser.setFileFilter(new FileChooser.DefaultFileFilter(fileChooser) {
			@Override
			public boolean accept (File f) {

				if (f.isDirectory()) {
					//Find any file inside the directory that has a tlsprj file
					FileHandle absolute = Gdx.files.absolute(f.getAbsolutePath());
					boolean hasOtherDirectory = false;
					if (absolute.isDirectory()) {
						FileHandle[] list = absolute.list();
						for (FileHandle handle : list) {
							if (handle.isDirectory()) hasOtherDirectory = true;
							if (handle.extension().equals(TALOS_PROJECT_EXTENSION)) {
								return true;
							}
						}
						if (hasOtherDirectory) {
							return true;
						}
						//None found, if it has another ddirectory its chill though
					}
				} else {
					String extension = Gdx.files.absolute(f.getAbsolutePath()).extension();
					if (extension.equals(TALOS_PROJECT_EXTENSION)) {
						return true;
					}
				}

				return false;
			}
		});
		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> files) {
				FileHandle first = files.first();
				//Only ever one

				boolean success = validateAndOpenProject(first);

				if (success) {
					ProjectSplash.this.hide();
				}
			}

			@Override
			public void canceled () {

				//Dont need to do anthing
			}
		});

		Stage stage = SharedResources.stage;
		stage.addActor(fileChooser.fadeIn());
	}

	private boolean validateAndOpenProject (FileHandle first) {
		FileHandle projectToTryToLoad = null;
		if (first.isDirectory()) {
			FileHandle[] list = first.list();
			for (FileHandle handle : list) {
				if (handle.extension().equals(TALOS_PROJECT_EXTENSION)) {
					projectToTryToLoad = handle;
					break;
				}
			}
		} else {
			if (first.extension().equals(TALOS_PROJECT_EXTENSION)) {
				projectToTryToLoad = first;
			}
		}

		if (projectToTryToLoad != null) {
			TalosProjectData talosProjectData = TalosProjectData.loadFromFile(projectToTryToLoad);
			if (talosProjectData != null) {
				try {
					SharedResources.projectLoader.loadProject(talosProjectData);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				Dialogs.showErrorDialog(SharedResources.stage, "No valid project found to load");
			}
		} else {
			Dialogs.showErrorDialog(SharedResources.stage, "No valid project found to load");
		}

		return false;

	}

	public void show(Stage stage) {
		stage.addActor(this);
		setPosition(stage.getWidth()/2f - getWidth()/2f, stage.getHeight()/2f - getHeight()/2f);
	}

	public void hide() {
		remove();
	}

}
