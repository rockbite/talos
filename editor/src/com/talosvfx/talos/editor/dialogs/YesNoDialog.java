package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

public class YesNoDialog extends Dialogs.ConfirmDialog<Runnable> {

	public YesNoDialog (String title, String message, Runnable yes, Runnable no) {
		super(title, message, new String[] {"Yes", "No"}, new Runnable[] {yes, no}, new ConfirmDialogListener<Runnable>() {
			@Override
			public void result (Runnable result) {
				Gdx.app.postRunnable(result);
			}
		});

	}

}
