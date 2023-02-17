package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractContextRequiredEvent<T> implements ContextRequiredEvent<T> {
    @Getter@Setter
    T context;

    @Override
    public void reset() {
        context = null;
    }
}
