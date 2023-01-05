package com.talosvfx.talos.editor.notifications;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;
import lombok.Setter;

public interface ContextRequiredEvent<T> extends TalosEvent {

    T getContext();

    void setContext(T context);

}
