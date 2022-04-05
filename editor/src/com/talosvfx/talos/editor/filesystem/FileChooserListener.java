package com.talosvfx.talos.editor.filesystem;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public interface FileChooserListener {
	void selected (Array<FileHandle> files);
}
