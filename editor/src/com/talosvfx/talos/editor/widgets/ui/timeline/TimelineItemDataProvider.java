package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;

public interface TimelineItemDataProvider<U> {

    Array<Button> registerSecondaryActionButtons();

    Array<Button> registerMainActionButtons();

    String getItemName();

    U getIdentifier();

    int getIndex();

    boolean isFull();

    float getDurationOne ();

    float getDurationTwo ();

    float getTimePosition ();

    boolean isItemVisible ();

    void setTimePosition (float time);
}
