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
import com.talosvfx.talos.editor.project2.localprefs.PrefKeys;
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
				SharedResources.talosControl.openProjectByChoosingFile(new Runnable() {
					@Override
					public void run() {
						ProjectSplash.this.hide();
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
		fileOpener.setPath(TalosLocalPrefs.Instance().getGlobalData(PrefKeys.FILE_PATHS.GENERAL.DEFAULT_PROJECT_PATH));
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
		talosProjectData.save();
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
					boolean success = SharedResources.talosControl.validateAndOpenProject(Gdx.files.absolute(recentProject.getProjectPath()));
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


	public void show(Stage stage) {

		stage.addActor(this);
		setPosition(stage.getWidth()/2f - getWidth()/2f, stage.getHeight()/2f - getHeight()/2f);

		SharedResources.mainMenu.hide();
	}

	public void hide() {
		remove();
	}

}
