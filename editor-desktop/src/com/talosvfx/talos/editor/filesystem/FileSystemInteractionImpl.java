package com.talosvfx.talos.editor.filesystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.project2.SharedResources;

import java.io.File;
import java.io.FileFilter;

public class FileSystemInteractionImpl extends FileSystemInteraction {

	private final FileChooser fileChooser;

	public FileSystemInteractionImpl () {
		Skin skin = SharedResources.skin;
		fileChooser = new FileChooser(FileChooser.Mode.SAVE);
		fileChooser.setBackground(skin.getDrawable("window-noborder"));

	}

	@Override
	public void openProject (IProject projectType) {
		String defaultLocation = TalosMain.Instance().ProjectController().getLastDir("Open", projectType);
		if(defaultLocation.equals("")) {
			TalosMain.Instance().ProjectController().getLastDir("Save", projectType);
		}
		fileChooser.setDirectory(defaultLocation);

		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(projectType.getExtension());
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				TalosMain.Instance().ProjectController().setProject(projectType);
				TalosMain.Instance().ProjectController().loadProject(Gdx.files.absolute(path));
			}
		});

		TalosMain.Instance().UIStage().getStage().addActor(fileChooser.fadeIn());
	}

	@Override
	public void export () {
		IProject projectType = TalosMain.Instance().ProjectController().getProject();
		String defaultLocation = TalosMain.Instance().ProjectController().getLastDir("Export", projectType);
		fileChooser.setDirectory(defaultLocation);

		final String ext = projectType.getExportExtension();

		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(ext);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				if(!path.endsWith(ext)) {
					if(path.indexOf(".") > 0) {
						path = path.substring(0, path.indexOf("."));
					}
					path += ext;
				}
				FileHandle handle = Gdx.files.absolute(path);
				TalosMain.Instance().ProjectController().exportProject(handle);
			}
		});

		fileChooser.setDefaultFileName(TalosMain.Instance().ProjectController().getCurrentExportNameSuggestion());

		TalosMain.Instance().UIStage().getStage().addActor(fileChooser.fadeIn());
	}

	@Override
	public void save () {
		IProject projectType = TalosMain.Instance().ProjectController().getProject();
		String defaultLocation = TalosMain.Instance().ProjectController().getLastDir("Save", projectType);
		fileChooser.setDirectory(defaultLocation);

		final String ext = projectType.getExtension();
		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(ext);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				String path = file.first().file().getAbsolutePath();
				if(!path.endsWith(ext)) {
					if(path.indexOf(".") > 0) {
						path = path.substring(0, path.indexOf("."));
					}
					path += ext;
				}
				FileHandle handle = Gdx.files.absolute(path);
				TalosMain.Instance().ProjectController().saveProject(handle);
			}
		});

//		fileChooser.setDefaultFileName(TalosMain.Instance().ProjectController().currentTab.fileName);

		TalosMain.Instance().UIStage().getStage().addActor(fileChooser.fadeIn());
	}

	@Override
	public void showSaveFileChooser (String extension, FileChooserListener listener) {
		fileChooser.setMode(FileChooser.Mode.SAVE);
		fileChooser.setMultiSelectionEnabled(false);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(extension);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> files) {
				super.selected(files);
				listener.selected(files);
			}
		});

		TalosMain.Instance().UIStage().getStage().addActor(fileChooser.fadeIn());
	}

	@Override
	public void showFileChooser (String extension, FileChooserListener listener) {
		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(extension);
			}
		});
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		fileChooser.setListener(new FileChooserAdapter(){
			@Override
			public void selected (Array<FileHandle> files) {
				super.selected(files);
				listener.selected(files);
			}
		});

		SharedResources.stage.addActor(fileChooser.fadeIn());
	}

	@Override
	public void showFolderChooser (FileChooserListener listener) {

		fileChooser.setMode(FileChooser.Mode.OPEN);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> files) {
				listener.selected(files);
			}
		});

		SharedResources.stage.addActor(fileChooser.fadeIn());
	}
}
