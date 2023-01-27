package com.talosvfx.talos.editor.notifications;

import lombok.Getter;
import lombok.Setter;

public class ContextRequiredEvent<T> implements TalosEvent {

    @Getter@Setter
    private T context;

    @Override
    public void reset () {
        context = null;
    }
}
