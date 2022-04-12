package com.talosvfx.talos.editor.dialogs;

import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.talosvfx.talos.TalosMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorReporting {

    private static final Logger logger = LoggerFactory.getLogger(ErrorReporting.class);


    public void reportException(Throwable e) {
        logger.error("Error", e);
        Dialogs.showErrorDialog(TalosMain.Instance().UIStage().getStage(), "Talos just encountered an error, click details, then copy and send error developers if you dare", e);
    }
}
