package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.MenuPopupOpenCommand;
import com.talosvfx.talos.editor.notifications.events.assets.MenuItemClickedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;

public class MenuRow extends Table {

    private final Label label;
    private final Label shortcut;
    private final String id;
    private final MainMenu mainMenu;

    private Image icon;
    private Image collapseImage;

    Drawable selectedDrawable;

    ClickListener clickListener;

    private float overTimer = 0;
    private boolean openTriggered = false;

    private boolean hasSubMenu = false;

    public MenuRow(MainMenu mainMenu, String id) {
        this.mainMenu = mainMenu;
        this.id = id;

        icon = new Image();
        label = new Label("", SharedResources.skin);

        shortcut = new Label("Ctrl+C", SharedResources.skin);
        shortcut.setColor(Color.GRAY);

        collapseImage = new Image(SharedResources.skin.getDrawable("panel-collapse-right"));
        collapseImage.setColor(Color.valueOf("aaaaaaff"));

        add(icon).padLeft(7).left().size(20);

        add(label).pad(4).padLeft(7).padRight(16).left().expandX();

        add(shortcut).padRight(2).right().expandX();

        add(collapseImage).right().padRight(3).padLeft(3).size(10).padTop(2);

        selectedDrawable = SharedResources.skin.getDrawable("menu-selection-bg-blue");

        clickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                if(!hasSubMenu) {
                    Notifications.fireEvent(Notifications.obtainEvent(MenuItemClickedEvent.class).set(id));
                    mainMenu.collapseAll();
                }
            }
        };
        addListener(clickListener);

        setTouchable(Touchable.enabled);
    }

    public void buildFrom(XmlReader.Element item) {
        String title = item.getAttribute("title");

        label.setText(title);

        if(item.getChildCount() > 0) {
            collapseImage.setVisible(true);
            hasSubMenu = true;
        } else {
            collapseImage.setVisible(false);
        }

        if(item.hasAttribute("icon")) {
            String iconName = item.getAttribute("icon");
            icon.setDrawable(SharedResources.skin.getDrawable(iconName));
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(clickListener.isOver()) {
            setBackground(selectedDrawable);
        } else {
            if(mainMenu.isPathOpen(id)) {
                setBackground(selectedDrawable);
            } else {
                setBackground((Drawable) null);
            }
        }

        if(clickListener.isOver()) {
            overTimer+= Gdx.graphics.getDeltaTime();

            if(!openTriggered) {
                openTriggered = true;

                triggerOpen();
            }

            mainMenu.collapseUnrelatedHierarchies(id);
        } else {
            openTriggered = false;
            overTimer = 0;
        }
    }

    private void triggerOpen() {
        if(hasSubMenu) {
            Notifications.fireEvent(Notifications.obtainEvent(MenuPopupOpenCommand.class).set(id, getPreferredPopupPosition()));
        }
    }

    private Vector2 getPreferredPopupPosition() {
        Vector2 vec = new Vector2(getWidth(), getHeight());

        localToStageCoordinates(vec);

        return vec;
    }
}
