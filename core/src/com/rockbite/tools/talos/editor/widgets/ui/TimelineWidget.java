package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class TimelineWidget extends Table {

    public TimelineWidget() {
        //TODO: add top bar here.

        for(int i = 0; i < 5; i++) {
            EmitterRow emitterRow = new EmitterRow();
            add(emitterRow);
            row();
        }

    }


    class EmitterRow extends Table {

    }
}
