package com.talosvfx.talos.editor.widgets.propertyWidgets;

public class ItemData {
        public String id;
        public String text;

        public boolean canDelete = true;

        public ItemData(String id, String text) {
            this.id = id;
            this.text = text;
        }

        public ItemData(String text) {
            this.id = text;
            this.text = text;
        }

    @Override
    public String toString () {
        return text;
    }
}
