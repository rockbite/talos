package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Pool;

import java.util.Objects;

public abstract class ListItem<U> extends Table implements Pool.Poolable {

    protected boolean isSelected;

    private U identifier;

    private int index =  -1;

    public void setFrom(TimelineItemDataProvider<U> dataProvider) {
        identifier = dataProvider.getIdentifier();
        index = dataProvider.getIndex();
    }

    public boolean isSelected () {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = true;
    }

    @Override
    public void reset() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItem listItem = (ListItem) o;
        return identifier.equals(listItem.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    public U getIdentifier () {
        return identifier;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex (int index) {
        this.index = index;
    }
}