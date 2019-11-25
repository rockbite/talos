package com.talosvfx.talos.editor.widgets.propertyWidgets;

public class TruncatedString {
    public String originalString = "";
    public int maxSize = 3;

    public TruncatedString(String originalString, int maxSize) {
        this.originalString = originalString;
        this.maxSize = maxSize;
    }

    @Override
    public String toString() {
        String result = originalString;
        if(result.length() > maxSize) {
            result = result.substring(0, maxSize-3) + "...";
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        return originalString.equals(((TruncatedString)obj).originalString);
    }
}
