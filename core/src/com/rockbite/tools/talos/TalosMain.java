package com.rockbite.tools.talos;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.editor.MainStage;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class TalosMain extends ApplicationAdapter implements DropTargetListener {
	MainStage mainStage;

	@Override
	public void create () {
		mainStage = new MainStage();
	}


	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mainStage.act();
		mainStage.draw();
	}

	public void resize (int width, int height) {
		mainStage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		mainStage.dispose();
	}




	@Override
	public void dragEnter(DropTargetDragEvent dtde) {

	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {

	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {

	}

	@Override
	public void dragExit(DropTargetEvent dte) {

	}

	@Override
	public void drop(final DropTargetDropEvent dtde) {
		Vector2 pos = null;
		String[] paths = null;

		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		Transferable t = dtde.getTransferable();
		if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				List<File> list = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				paths = new String[list.size()];
				for(int i = 0; i < list.size(); i++) {
					paths[i] = list.get(i).getAbsolutePath();
				}

				if(paths.length == 1) {
					dtde.dropComplete(true);

					pos = new Vector2();
					pos.set(dtde.getLocation().x, dtde.getLocation().y);



				} else {
					dtde.dropComplete(false);
				}
			}
			catch (Exception ufe) {
				dtde.dropComplete(false);
			}
		}

		if(pos != null) {
			final String[] finalPaths = paths;
			final Vector2 finalPos = pos;
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					mainStage.fileDrop(finalPaths, finalPos.x, finalPos.y);
				}
			});
		}


	}
}
