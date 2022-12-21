package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.MenuPopupOpenCommand;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Getter;

public class MenuTab extends Table {

    private final MainMenu mainMenu;
    private ClickListener clickListener;

    @Getter
    private boolean isActive = false;
    private String id;

    public MenuTab(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        setTouchable(Touchable.enabled);
    }

    public void buildFrom(XmlReader.Element item) {
        String title = item.getAttribute("title");

        id = item.getAttribute("name");

        setBackground((Drawable) null);

        Label label = new Label(title, SharedResources.skin);
        add(label).pad(2).padLeft(10).padRight(10);

        clickListener = new ClickListener();
        addListener(clickListener);
    }

    @Override
    public void act(float delta) {

        if(isActive) {
            setBackground(SharedResources.skin.getDrawable("top-menu-selected"));
        } else {
            if (clickListener.isOver()) {
                setBackground(SharedResources.skin.getDrawable("top-menu"));
            } else {
                setBackground((Drawable) null);
            }
        }

        super.act(delta);
    }

    public void open() {
        if(!isActive) {
            isActive = true;

            Notifications.fireEvent(Notifications.obtainEvent(MenuPopupOpenCommand.class).set(id, getPreferredPopupPosition()));
        }
    }

    public void collapse() {
        if(isActive) {
            isActive = false;

            mainMenu.collapseHierarchyOf(id);
        }
    }

    public Vector2 getPreferredPopupPosition() {
        Vector2 vec = new Vector2(-4, 4);

        localToStageCoordinates(vec);

        return vec;
    }
}
