package com.talosvfx.talos.editor.dialogs;

import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.talosvfx.talos.TalosMain;

public class ErrorReporting {

    public boolean enabled = true;

    public void reportException(Throwable e) {
        if(enabled) {
            Dialogs.showErrorDialog(TalosMain.Instance().UIStage().getStage(), "Talos just encountered an error, click details, then copy and send error developers if you dare", e);
        }
    }
}
