package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.simulation.TinyEmitter;

import java.util.Comparator;

public abstract class TimelineWidget<U> extends Table {

    TimelineLeft<U> leftList;
    TimelineRight<U> rightList;

    Array<U> selector = new Array<>();

    private float leftScrollTracker;
    private float rightScrollTracker;

    protected boolean enforceSelection = true;

    public TimelineWidget(Skin skin) {
        setSkin(skin);

        leftList = new TimelineLeft<U>(this);
        rightList = new TimelineRight<U>(this);

        leftScrollTracker = leftList.getScrollPos();
        rightScrollTracker = rightList.getScrollPos();

        leftList.setTypeName(getItemTypeName());

        SplitPane splitPane = new SplitPane(leftList, rightList, false, skin, "timeline");
        splitPane.setSplitAmount(0.3f);

        add(splitPane).grow();
    }

    public void updateItemData(Array<? extends TimelineItemDataProvider<U>> items) {
        if(items == null) return;

        leftList.updateItemData(items);
        rightList.updateItemData(items);
    }

    public void setData (Array<? extends TimelineItemDataProvider<U>> items) {
        if(items == null) return;

        leftList.setData(items);
        rightList.setData(items);

        leftList.rebuildFromData();

        if (enforceSelection && items.size > 0) {
            setSelected(items.first().getIdentifier());
        }
    }

    public void setSelected(U identifier) {
        leftList.setSelected(identifier);
        rightList.setSelected(identifier);
    }

    private TimelineListener.TimelineEvent obtainEvent() {
        return Pools.obtain(TimelineListener.TimelineEvent.class);
    }

    protected abstract String getItemTypeName();

    public Array<U> getSelector() {
        Array<U> copy = new Array<>();
        copy.addAll(selector);
        return copy;
    }

    public void addNewItem(TimelineItemDataProvider<U> dataProvider) {
        leftList.addItem(dataProvider);
        rightList.addItem(dataProvider);

        if (enforceSelection && leftList.getSelected() == null) {
            setSelected(leftList.getItems().first().getIdentifier());
        }
    }

    public void removeItems(Array<U> identifierList) {
        for(U identifier: identifierList) {
            selector.removeValue(identifier, true);
            leftList.removeItem(identifier);
            rightList.removeItem(identifier);
        }

        if (enforceSelection) {
            if (getSelectedItem() == null) {
                Array<ActionRow<U>> items = leftList.getItems();
                if(items.size > 0) {
                    setSelected(items.first().getIdentifier());
                }
            }
        }
    }

    public U getSelectedItem() {
        return leftList.getSelected();
    }

    public TimelineLeft<U> getActionWidget() {
        return leftList;
    }

    public boolean isItemVisible (U identifier) {
        ActionRow<U> item = leftList.getItem(identifier);

        return item.isItemVisible();
    }


    public void onRowClicked(ListItem<U> item) {
        U identifier = item.getIdentifier();

        rightList.setSelected(identifier);
        leftList.setSelected(identifier);

        fire(obtainEvent().as(TimelineListener.Type.itemSelected).target(item));
    }

    public void onVisibilityToggled(ActionRow<U> item) {
        fire(obtainEvent().as(TimelineListener.Type.visibilityChanged).target(item).payload(item.isItemVisible()));
    }

    public void onSelectorToggled(ActionRow<U> item) {

        U identifier = item.getIdentifier();
        boolean isChecked = item.isSelectorChecked();
        boolean contains = selector.contains(identifier, true);

        if(isChecked && !contains) {
            selector.add(identifier);
        } else if (!isChecked && contains) {
            selector.removeValue(identifier, true);
        }

        fire(obtainEvent().as(TimelineListener.Type.selectorUpdated));
    }

    public void onItemNameChange(ActionRow<U> item, String newName) {
        fire(obtainEvent().as(TimelineListener.Type.rename).payload(newName).target(item));
    }

    public void onActionButtonClicked(TimelineListener.Type type) {
        TimelineListener.TimelineEvent event = obtainEvent().as(type);

        if(type == TimelineListener.Type.toggleLoop) {
            event.payload(leftList.isLoopEnabled());
        }

        fire(event);
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        // do some scroll tracking
        if(leftScrollTracker != leftList.getScrollPos()) {
            rightList.setScrollPos(leftList.getScrollPos());
        }

        if(rightScrollTracker != rightList.getScrollPos()) {
            leftList.setScrollPos(rightList.getScrollPos());
        }

        // sync trackers
        leftScrollTracker = leftList.getScrollPos();
        rightScrollTracker = rightList.getScrollPos();
    }

    public void setTimeCursor (float time) {
        rightList.setTimeCursor(time);
    }

    protected void setSortComparator(Comparator comparator) {
        leftList.setComparator(comparator);
        rightList.setComparator(comparator);
    }
}
