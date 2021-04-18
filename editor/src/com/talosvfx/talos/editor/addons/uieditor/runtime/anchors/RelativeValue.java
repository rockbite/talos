package com.talosvfx.talos.editor.addons.uieditor.runtime.anchors;

public class RelativeValue {

    public float sourceValue;

    public float percent;

    public float offset;

    public boolean isAttached = false;

    public float getValue() {
        if (isAttached) {
            return sourceValue * percent + offset;
        } else {
            return offset;
        }
    }

    public void setAttached(boolean isAttached) {
        if(isAttached != this.isAttached) {
            if (this.isAttached) {
                offset = sourceValue * percent + offset;
            } else {
                offset = sourceValue * percent - offset;
            }

            this.isAttached = isAttached;
        }
    }

    public void setValue(float value) {
        offset = value;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public void setPercent(float percent) {
        this.percent = percent;
        setAttached(true);
    }

    public void updateSourceValue(float value) {
        sourceValue = value;
    }

    public float getOffset () {
        return offset;
    }
}
