package com.talosvfx.talos.editor.dialogs;

import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.talosvfx.talos.TalosMain;

public class ErrorReporting {


    public void reportException(Throwable e) {
        Dialogs.showErrorDialog(TalosMain.Instance().UIStage().getStage(), "Talos just encountered an error, click details, then copy and send error developers if you dare", e);
    }
}
