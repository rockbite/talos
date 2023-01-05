package com.talosvfx.talos.editor.notifications;

public interface EventContextProvider<T extends ContextRequiredEvent.Context> {
    T getContext ();

}
