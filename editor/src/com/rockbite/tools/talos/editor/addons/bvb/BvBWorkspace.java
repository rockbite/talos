package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.rockbite.tools.talos.editor.widgets.ui.ViewportWidget;

public class BvBWorkspace extends ViewportWidget {

    BvBWorkspace() {
        setModeUI();

        setCameraPos(0, 0);
        bgColor.set(0.1f, 0.1f, 0.1f, 1f);
    }

    public void setModeUI() {
        setWorldSize(1280f);
    }

    public void setModeGame() {
        setWorldSize(10f);
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();

        drawGrid(batch, parentAlpha);

        batch.begin();
    }
}
