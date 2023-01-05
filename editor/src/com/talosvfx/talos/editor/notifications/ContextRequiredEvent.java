package com.talosvfx.talos.editor.notifications;

import lombok.Getter;
import lombok.Setter;

public class ContextRequiredEvent<T extends ContextRequiredEvent.Context> implements TalosEvent {

    @Getter@Setter
    private T context;

    @Override
    public void reset () {

    }

    /** Tag interface */
    public interface Context {
    }
}
