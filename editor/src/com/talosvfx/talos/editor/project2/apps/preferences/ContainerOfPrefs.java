package com.talosvfx.talos.editor.project2.apps.preferences;

import java.util.function.Supplier;

public interface ContainerOfPrefs<T extends AppPrefs.AppPreference> {

    T getPreferences();

    void applyPreferences(T preferences);
}
