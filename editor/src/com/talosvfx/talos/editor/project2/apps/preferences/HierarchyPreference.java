package com.talosvfx.talos.editor.project2.apps.preferences;

import com.badlogic.gdx.utils.Array;
import lombok.Data;

@Data
public class HierarchyPreference implements AppPrefs.AppPreference {
    // hack for now, because new root node is generated every time with different uuid and serialization
    // doesn't really help :/
    // TODO: 12.01.23 get rid off flag
    boolean rootOpen;

    Array<String> UUUIDsOfExpandedObjects;
}
