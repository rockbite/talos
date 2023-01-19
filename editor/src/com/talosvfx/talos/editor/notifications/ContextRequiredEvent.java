package com.talosvfx.talos.editor.notifications;

import lombok.Getter;
import lombok.Setter;

public interface ContextRequiredEvent<T> extends TalosEvent {

    T getContext();

    void setContext(T context);

}
