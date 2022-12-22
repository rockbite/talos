package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.talosvfx.talos.editor.nodes.widgets.GameAssetWidget;
import com.talosvfx.talos.editor.project2.SharedResources;

public class CustomAssetWidget extends ATypeWidget {
    private final GameAssetWidget gameAssetWidget;

    @Override
    public String getTypeName() {
        return "asset";
    }

    public CustomAssetWidget() {
        gameAssetWidget = new GameAssetWidget();
        gameAssetWidget.init(SharedResources.skin);

        add(gameAssetWidget).padLeft(4).padRight(4).width(220);
    }
}
