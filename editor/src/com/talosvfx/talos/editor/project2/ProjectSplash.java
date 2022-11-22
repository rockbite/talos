package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

import java.io.File;

import static com.talosvfx.talos.editor.project2.TalosProjectData.TALOS_PROJECT_EXTENSION;

public class ProjectSplash extends VisDialog {


	public ProjectSplash (String title) {
		super(title);

		Table contentTable = getContentTable();



		VisTable openTable = new VisTable(true);
		VisTable createTable = new VisTable(true);

		openTable.top().defaults().top();
		createTable.top().defaults().top();

		buildOpenTable(openTable);
		buildCreateTable(createTable);


		contentTable.add(openTable).grow();
		contentTable.add(createTable).grow();
	}

	private void buildCreateTable (VisTable createTable) {
		VisLabel label = new VisLabel("Create Project");
		Table spacer = new Table();

		createTable.defaults().left();
		createTable.defaults().pad(10);

		VisTable fieldsTable = new VisTable();
		fieldsTable.defaults().left().pad(5);

		VisLabel projectNameLabel = new VisLabel("Project Name");
		VisTextField projectName = new VisTextField("untitled_project");
		VisLabel projectDirectoryLabel = new VisLabel("Project directory");
		VisTextField projectDirectory = new VisTextField(Gdx.files.absolute(System.getProperty("user.home")).file().getAbsolutePath());
		VisTextButton selectDirectory = new VisTextButton("Select");
		selectDirectory.addListener(new ClickListener(){
			@Override
			public void clicked (InputEvent event, float x, float y) {
				selectDirectoryForCreatingProject(projectDirectory);
			}
		});


		fieldsTable.add(projectNameLabel);
		fieldsTable.row();
		fieldsTable.add(projectName).growX();
		fieldsTable.row();

		fieldsTable.add(projectDirectoryLabel).padTop(20);
		fieldsTable.row();
		fieldsTable.add(projectDirectory).growX();
		fieldsTable.row();
		fieldsTable.add(selectDirectory);
		fieldsTable.row();

		createTable.add(label);
		createTable.row();

		createTable.add(spacer).size(256, 128);
		createTable.row();

		createTable.add(fieldsTable).grow();
		createTable.row();

		VisTextButton createButton = new VisTextButton("Create");
		createButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				validateAndCreateProject(projectName, projectDirectory);
			}
		});

		createTable.add(createButton);
	}

	private void validateAndCreateProject (VisTextField projectName, VisTextField projectDirectory) {
		String projectNameText = projectName.getText();
		String directory = projectDirectory.getText();

		if (projectNameText.isEmpty()) {
			Dialogs.showErrorDialog(SharedResources.stage, "Project name cannot be empty");
			return;
		}


		FileHandle dirHandle = Gdx.files.absolute(directory);
		if (!dirHandle.exists()) {
			dirHandle.mkdirs();
		}

		TalosProjectData talosProjectData = TalosProjectData.newDefaultProject(projectNameText, dirHandle);
		SharedResources.projectLoader.loadProject(talosProjectData);
	}

	private void buildOpenTable (VisTable openTable) {
		VisLabel label = new VisLabel("Recent Projects");

		Table spacer = new Table();

		VisTextButton open = new VisTextButton("Open");
		open.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				openProject();
			}
		});

		openTable.defaults().left();
		openTable.defaults().pad(10);

		Table recentItems = new Table();

		Array<RecentProject> recentProjects = TalosLocalPrefs.Instance().getRecentProjects();

		for (RecentProject recentProject : recentProjects) {
			VisLabel recentProjectLabel = new VisLabel(recentProject.getProjectPath());
			recentProjectLabel.addListener(new ClickListener() {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					boolean success = validateAndOpenProject(Gdx.files.absolute(recentProject.getProjectPath()));
					if (success) {
						ProjectSplash.this.hide();
					}
				}
			});
			recentItems.add(recentProjectLabel);
		}

		openTable.add(label);
		openTable.row();
		openTable.add(spacer).size(256, 128);
		openTable.row();

		openTable.add(recentItems);
		openTable.row();
		openTable.add(open).padTop(20);
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

}
