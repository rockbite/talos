package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/** Listener for {@link TimelineEvent}.
 * @author Avetis Zakharyan */
public class TimelineListener implements EventListener {

    public boolean handle (Event event) {
        if (event instanceof TimelineEvent) {
            TimelineEvent timelineEvent = (TimelineEvent) event;
            switch (timelineEvent.getType()) {
                case itemSelected:
                    onItemSelect(timelineEvent.getTargetIdentifier());
                    break;
                case visibilityChanged:
                    onItemVisibilityChange(timelineEvent.getTargetIdentifier(), (Boolean) timelineEvent.payload);
                    break;
                case selectorUpdated:
                    onSelectorUpdate();
                    break;
                case play:
                    onPlayClicked();
                    break;
                case rewind:
                    onRewindClicked();
                    break;
                case skipToStart:
                    onSkipToStartClicked();
                    break;
                case skipToEnd:
                    onSkipToEndClicked();
                    break;
                case newItem:
                    onNewClicked();
                    break;
                case deleteSelection:
                    onDeleteClicked();
                    break;
                case toggleLoop:
                    onToggleLoop((Boolean) timelineEvent.payload);
                    break;
                case rename:
                    onItemRename(timelineEvent.getTargetIdentifier(), (String) timelineEvent.payload);
                    break;
                case up:
                    onUp();
                    break;
                case down:
                    onDown();
                    break;
            }
        }

        return false;
    }

    static public class TimelineEvent extends Event {
        private Type type;

        private Object target;
        private Object payload;

        public Type getType() {
            return type;
        }

        public TimelineEvent as(Type type) {
            this.type = type;

            return this;
        }

        public TimelineEvent payload(Object payload) {
            this.payload = payload;

            return this;
        }

        public TimelineEvent target(ListItem target) {
            this.target = target.getIdentifier();

            return this;
        }

        public Object getTargetIdentifier () {
            return target;
        }
    }

    protected void onItemSelect(Object identifier) {

    }


    protected void onItemVisibilityChange(Object identifier, boolean isVisible) {}


    protected void onSelectorUpdate() {}

    protected void onPlayClicked() {}
    protected void onRewindClicked() {}
    protected void onSkipToStartClicked() {}
    protected void onSkipToEndClicked() {}
    protected void onNewClicked() {}
    protected void onDeleteClicked() {}
    protected void onToggleLoop(boolean loopEnabled) {}
    protected void onDown () {}
    protected void onUp () {}

    protected void onItemRename(Object identifier, String newName) {}

    static public enum Type {
        itemSelected,
        visibilityChanged,
        selectorUpdated,
        skipToStart,
        skipToEnd,
        play,
        rewind,
        newItem,
        deleteSelection,
        toggleLoop,
        rename,
        up,
        down
    }
}
