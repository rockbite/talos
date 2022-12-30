package com.talosvfx.talos.editor.project2.apps.preferences;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ContainerOfPrefs<T extends AppPrefs.AppPreference> {

    Supplier<T> getPreferences();

    Consumer<T> applyPreferences();
}
