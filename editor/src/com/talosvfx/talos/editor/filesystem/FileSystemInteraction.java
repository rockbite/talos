package com.talosvfx.talos.editor.filesystem;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.project.IProject;

public abstract class FileSystemInteraction {

	private static FileSystemInteraction instance;

	public static FileSystemInteraction instance () {
		if (instance == null) {
			try {
				instance = (FileSystemInteraction)ClassReflection.newInstance(ClassReflection.forName("com.talosvfx.talos.editor.filesystem.FileSystemInteractionImpl"));
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public abstract void openProject (IProject projectType);

	public abstract void export ();

	public abstract void save ();

	public abstract void showSaveFileChooser (String extension, FileChooserListener listener);
	public abstract void showFileChooser (String extension, FileChooserListener listener);

	public abstract void showFolderChooser (FileChooserListener listener);
}
