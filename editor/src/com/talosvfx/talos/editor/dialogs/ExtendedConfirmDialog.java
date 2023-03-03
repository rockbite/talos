package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * Alternative to {@link com.kotcrab.vis.ui.util.dialog.Dialogs.ConfirmDialog}, because later could crash when rendering
 * long messages. Former one can use several labels for rendering long messages and overcome the problem.
 */
public class ExtendedConfirmDialog<T> extends VisDialog {
    private ConfirmDialogListener<T> listener;

    /** Use for short text. */
    public ExtendedConfirmDialog (String title, String text, String[] buttons, T[] returns, ConfirmDialogListener<T> listener) {
        super(title);

        if (buttons.length != returns.length) {
            throw new IllegalStateException("buttons.length must be equal to returns.length");
        }

        this.listener = listener;

        text(new VisLabel(text, Align.center));
        defaults().padBottom(3);

        for (int i = 0; i < buttons.length; i++) {
            button(buttons[i], returns[i]);
        }

        padBottom(3);
        pack();
        centerWindow();
    }

    /** Use for long text. */
    public ExtendedConfirmDialog (String title, String[] text, String[] buttons, T[] returns, ConfirmDialogListener<T> listener) {
        super(title);

        if (buttons.length != returns.length) {
            throw new IllegalStateException("buttons.length must be equal to returns.length");
        }

        this.listener = listener;

        for (String chunk : text) {
            text(new VisLabel(chunk, Align.left));
        }
        defaults().padBottom(3);

        for (int i = 0; i < buttons.length; i++) {
            button(buttons[i], returns[i]);
        }
        padTop(30);
        padBottom(3);
        pack();
        centerWindow();
    }

    @Override
    protected void result (Object object) {
        listener.result((T) object);
    }

    @Override
    public VisDialog text (Label label) {
        Table contentTable = getContentTable();
        contentTable.add(label).growX().row();
        return this;
    }
}
