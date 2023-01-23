package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.talosvfx.talos.editor.notifications.commands.MouseCombination;
import com.talosvfx.talos.editor.notifications.commands.MouseCommand;
import com.talosvfx.talos.editor.widgets.ui.Styles;

public class MouseCombinationTypeWidget extends InputCombinationTypeWidget<MouseCombination> {
    private SelectBox<MouseCommand> selectBox;
    public MouseCombinationTypeWidget(MouseCombination currentCombination) {
        super(currentCombination);
    }

    public MouseCombinationTypeWidget() {
        super();
    }

    @Override
    protected void createEmpty() {
        currentCombination = new MouseCombination(MouseCommand.LEFT);
    }

    @Override
    protected void construct(MouseCombination mouseCombination) {
        selectBox = new SelectBox<>(Styles.keyInputWidgetSelectBoxStyle);
        selectBox.setItems(MouseCommand.values());
        selectBox.setSelected(mouseCombination.getMouseCommand());

        add(selectBox).expandX().left();
    }
}
