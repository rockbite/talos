package com.talosvfx.talos.editor.notifications.events;

import lombok.Getter;
import lombok.Setter;

public class AbstractContextBasedEvent implements TalosContextBasedEvent{

    @Getter@Setter
    Object context;

    @Override
    public void reset() {
        context = null;
    }

    @Override
    public Object getContext() {
        return context;
    }
}
