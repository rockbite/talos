package com.talosvfx.talos.editor.project2.apps.preferences;

import com.badlogic.gdx.utils.Array;
import lombok.Data;

@Data
public class HierarchyPreference implements AppPrefs.AppPreference {
    Array<String> UUUIDsOfExpandedObjects;
}
