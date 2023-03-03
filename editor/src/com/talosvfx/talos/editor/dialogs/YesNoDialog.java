package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;

public class YesNoDialog extends ExtendedConfirmDialog<Runnable> {

	/** Use for short messages. */
	public YesNoDialog (String title, String message, Runnable yes, Runnable no) {
		super(title, message, new String[] {"Yes", "No"}, new Runnable[] {yes, no}, new ConfirmDialogListener<Runnable>() {
			@Override
			public void result (Runnable result) {
				Gdx.app.postRunnable(result);
			}
		});
	}

	/** Use for long messages. */
	public YesNoDialog (String title, String[] messages, Runnable yes, Runnable no) {
		super(title, messages, new String[] {"Yes", "No"}, new Runnable[] {yes, no}, new ConfirmDialogListener<Runnable>() {
			@Override
			public void result (Runnable result) {
				Gdx.app.postRunnable(result);
			}
		});
	}

}
