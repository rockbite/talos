package com.rockbite.tools.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

public abstract class DropTargetListenerAdapter extends DropTargetAdapter {

	@Override
	public void drop (DropTargetDropEvent dtde) {
		Vector2 pos = null;
		String[] paths = null;

		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		Transferable t = dtde.getTransferable();
		if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				List<File> list = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				paths = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					paths[i] = list.get(i).getAbsolutePath();
				}

				if (paths.length == 1) {
					dtde.dropComplete(true);

					pos = new Vector2();
					pos.set(dtde.getLocation().x, dtde.getLocation().y);

				} else {
					dtde.dropComplete(false);
				}
			} catch (Exception ufe) {
				dtde.dropComplete(false);
			}
		}

		if (pos != null) {
			final String[] finalPaths = paths;
			final Vector2 finalPos = pos;
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run () {

					doDrop(finalPaths, finalPos.x, finalPos.y);
				}
			});
		}
	}

	protected abstract void doDrop (String[] finalPaths, float x, float y);
}
