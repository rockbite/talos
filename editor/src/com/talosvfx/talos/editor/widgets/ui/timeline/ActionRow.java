package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class ActionRow<U> extends BasicRow<U> {

    private boolean isItemVisible = true;

    private final Image eye;
    private final EditableLabel label;
    private final Cell<Button> actionCell;
    private final Button selectorBox;

    public ActionRow(TimelineWidget timeline) {
        super(timeline);

        eye = new Image(getSkin().getDrawable("timeline-icon-eye"));
        eye.setColor(new Color(Color.WHITE));
        label = new EditableLabel("Default Emitter", getSkin());
        label.setColor(ColorLibrary.FONT_WHITE);
        selectorBox = new Button(getSkin(), "miniSelector");

        add(eye).left().pad(3);
        add(label).padLeft(7).padBottom(4).left().expandX();
        actionCell = add(selectorBox).right().pad(4);

        eye.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                event.cancel();

                isItemVisible = !isItemVisible;

                if (isItemVisible) {
                    eye.getColor().a = 1f;
                } else {
                    eye.getColor().a = 0.3f;
                }

                onVisibilityToggled();
            }
        });

        label.setListener(new EditableLabel.EditableLabelChangeListener() {
            @Override
            public void editModeStarted () {
            }

            @Override
            public void changed(String newText) {
                onItemNameChange(newText);
            }
        });

        selectorBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSelectorToggled();
            }
        });
    }

    private void onItemNameChange(String newText) {
        timeline.onItemNameChange(this, newText);
    }

    private void onVisibilityToggled() {
        timeline.onVisibilityToggled(this);
    }

    private void onSelectorToggled() {
        timeline.onSelectorToggled(this);
    }

    @Override
    public void reset() {
        super.reset();

        selectorBox.setChecked(false);

        clearActions();
    }

    public float getActionCellWidth () {
        return actionCell.getPrefWidth() + actionCell.getPadLeft() + actionCell.getPadRight();
    }

    public boolean isItemVisible() {
        return isItemVisible;
    }

    public boolean isChecked() {
        return selectorBox.isChecked();
    }

    @Override
    public void setFrom(TimelineItemDataProvider<U> dataProvider) {
        super.setFrom(dataProvider);

        label.setText(dataProvider.getItemName());
        boolean isVisible = dataProvider.isItemVisible();
        if (isItemVisible) {
            eye.getColor().a = 1f;
        } else {
            eye.getColor().a = 0.3f;
        }
        this.isItemVisible = isVisible;
    }

    public boolean isSelectorChecked() {
        return selectorBox.isChecked();
    }
}
