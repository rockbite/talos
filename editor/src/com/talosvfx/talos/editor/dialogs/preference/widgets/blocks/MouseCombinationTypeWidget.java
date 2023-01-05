package com.talosvfx.talos.editor.dialogs.preference.widgets.blocks;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.talosvfx.talos.editor.notifications.commands.CombinationType;
import com.talosvfx.talos.editor.notifications.commands.MouseCommand;
import com.talosvfx.talos.editor.widgets.ui.Styles;

public class MouseCombinationTypeWidget extends InputCombinationTypeWidget {
    private SelectBox selectBox;
    public MouseCombinationTypeWidget() {
        super(CombinationType.MOUSE);
    }

    @Override
    protected void construct() {
        super.construct();

        selectBox = new SelectBox<>(Styles.keyInputWidgetSelectBoxStyle);
        selectBox.setItems(MouseCommand.values());

        add(selectBox).expandX().left();
    }
}
