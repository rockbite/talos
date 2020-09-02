package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

import java.util.Comparator;

public abstract class AbstractList<T extends BasicRow<U>, U> extends Table {

    private Pool<T> itemPool;
    private Array<T> items = new Array<>();
    private ObjectMap<U, T> itemMap = new ObjectMap<>();

    private ListItem<U> selectedItem;

    protected TimelineWidget<U> timeline;

    protected boolean rebuildFlag = false;

    private Comparator<T> itemComparator;

    public AbstractList(TimelineWidget<U> timeline) {
        setSkin(timeline.getSkin());

        this.timeline = timeline;

        itemPool = new Pool<T>(20) {
            @Override
            protected T newObject() {
                return createNewItem();
            }
        };

        itemComparator = new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getIndex() - o2.getIndex();
            }
        };
    }

    protected abstract T createNewItem();

    protected void addItem(T item) {
        items.add(item);
        itemMap.put(item.getIdentifier(), item);
    }

    public void clearItems() {
        for (T item : items) {
            itemPool.free(item);
        }
        items.clear();
        itemMap.clear();
    }



    public void updateItemData(Array<? extends TimelineItemDataProvider<U>> dataArray) {
        for(TimelineItemDataProvider<U> data: dataArray) {
            U identifier = data.getIdentifier();
            T item = itemMap.get(identifier);
            if(item != null) {
                item.setFrom(data);
            }
        }
    }

    public void setData(Array<? extends  TimelineItemDataProvider<U>> dataArray) {
        clearItems();

        for(TimelineItemDataProvider<U> data: dataArray) {
            T item = itemPool.obtain();
            item.setFrom(data);
            addItem(item);
        }

        sortAndRebuild();
    }

    public void addItem(TimelineItemDataProvider<U> data) {
        T item = itemPool.obtain();
        item.setFrom(data);

        addItem(item);

        rebuildFlag = true;
    }

    public Array<T> getItems() {
        return items;
    }

    public void setSelected(U identifier) {

        T item = itemMap.get(identifier);

        if(item == null) return;

        if(selectedItem != null) {
            // we must un-select it
            selectedItem.setSelected(false);
        }

        selectedItem = item;

        selectedItem.setSelected(true);
    }

    public T getItem(U identifier) {
        return itemMap.get(identifier);
    }

    public void removeItem(U identifier) {
        T item = getItem(identifier);

        if (item == null) {
            // trying to remove items that is already removed, or wrong identifier provided
            return;
        }

        itemMap.remove(identifier);
        items.removeValue(item, true);

        if(selectedItem == item) {
            selectedItem = null;
        }

        itemPool.free(item);

        rebuildFlag = true;
    }

    private void sortItems() {
        items.sort(itemComparator);
    }

    public void sortAndRebuild() {
        sortItems();
        rebuildFromData();
    }

    @Override
    public void act(float delta) {
        if(rebuildFlag) {
            sortItems();
            rebuildFromData();
            rebuildFlag = false;
        }
        super.act(delta);
    }

    protected abstract void rebuildFromData();

    public U getSelected() {
        if(selectedItem != null) {
            return selectedItem.getIdentifier();
        }

        return null;
    }

    public void setComparator (Comparator<T> comparator) {
        this.itemComparator = comparator;
    }
}
