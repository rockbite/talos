package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.project2.SharedResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Toast extends Table {

    private static final Logger logger = LoggerFactory.getLogger(Toast.class);

    public static float LENGTH_LONG = 3f;
    public static float LENGTH_SHORT = 1f;

    private static Toast instance;
    private final Label messageLabel;
    private final Label.LabelStyle labelStyle;

    private float duration = LENGTH_SHORT;

    public static Toast getInstance() {
        if (instance == null) {
            instance = new Toast();
        }
        return instance;
    }

    private Toast(){
        messageLabel = new Label("", SharedResources.skin);
        labelStyle = new Label.LabelStyle();
        labelStyle.font = messageLabel.getStyle().font;
        labelStyle.fontColor = Color.WHITE;
        messageLabel.setStyle(labelStyle);
        messageLabel.setAlignment(Align.right);
        add(messageLabel);
        messageLabel.getColor().a = 0f;

        logger.info("Toast");
//        SceneEditorWorkspace.getInstance().add(this).grow().pad(30);
    }

    private void setProperties(String message, float duration, int align){
        this.duration = duration;
        messageLabel.setText(message);
        align(align);
    }

    public static Toast makeToast(String message, float duration, int align) {
        getInstance().setProperties(message, duration, align);
        return getInstance();
    }

    public static Toast makeToast(String message) {
        makeToast(message, LENGTH_SHORT, Align.bottomRight);
        return getInstance();
    }

    public void showWarn(){
        labelStyle.fontColor = Color.YELLOW;
        show();
    }

    public void showError(){
        labelStyle.fontColor = Color.RED;
        show();
    }

    public void show(){
        messageLabel.clearActions();
        messageLabel.getColor().a = 0f;
        messageLabel.addAction(Actions.sequence(
                Actions.alpha(0.7f, 0.1f),
                Actions.delay(duration),
                Actions.alpha(0f, 0.1f)
        ));
        toFront();
    }
}
