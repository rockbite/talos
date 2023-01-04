package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.notifications.TalosEvent;

public interface TalosContextBasedEvent extends TalosEvent {
    Object getContext();
}
