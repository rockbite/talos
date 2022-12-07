package com.talosvfx.talos.editor.addons.scene.widgets.directoryview;

import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

public class KeepStopReplaceDialog extends Dialogs.ConfirmDialog<Runnable> {
    public KeepStopReplaceDialog(String title, String text, Runnable keep, Runnable stop, Runnable replace) {
        super(title, text, new String[]{"Keep", "Stop", "Replace"}, new Runnable[] {keep, stop, replace}, new ConfirmDialogListener<Runnable>() {
            @Override
            public void result (Runnable result) {
                Gdx.app.postRunnable(result);
            }
        });
    }
}
