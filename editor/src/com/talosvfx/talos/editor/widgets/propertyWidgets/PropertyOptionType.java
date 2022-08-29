package com.talosvfx.talos.editor.widgets.propertyWidgets;

public enum PropertyOptionType {
    RESET("Reset"),
    REMOVE("Remove Component");

    public final String label;
    public static final PropertyOptionType[] RESET_REMOVE_OPTION = {RESET, REMOVE};
    public static final PropertyOptionType[] RESET_OPTION = {RESET};
    public static final PropertyOptionType[] REMOVE_OPTION = {REMOVE};

    PropertyOptionType(String label){
        this.label = label;
    }
}
