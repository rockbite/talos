package com.talosvfx.talos.editor.widgets.ui.menu;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader;
import com.rockbite.bongo.engine.collections.SerializableObjectIntMap;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.MenuPopupOpenCommand;
import com.talosvfx.talos.editor.project2.SharedResources;

public class MainMenu extends Table implements Observer {

    private Array<MenuTab> tabs = new Array<>();

    private MenuTab currentlyActive = null;
    private ObjectMap<String, MenuPopup> popupMap = new ObjectMap<>();

    private Array<MenuPopup> openStack = new Array<>();

    public MainMenu() {
        Notifications.registerObserver(this);
    }

    public void buildFrom(FileHandle file) {
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(file);

        buildFrom(root);
    }

    private void buildFrom(XmlReader.Element root) {
        clearChildren();

        int count = root.getChildCount();
        for(int i = 0; i < count; i++) {
            XmlReader.Element item = root.getChild(i);

            MenuTab menuTab = new MenuTab(this);
            menuTab.buildFrom(item);

            add(menuTab).left().pad(3).padLeft(0).padRight(0);
            tabs.add(menuTab);

            processHierarchy(item, "", true);

            menuTab.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                    if(menuTab == currentlyActive) {
                        collapseAll();
                    } else {
                        openMenu(menuTab);
                    }


                    return super.touchDown(event, x, y, pointer, button);
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);

                    if(currentlyActive != null) {
                        if(currentlyActive != menuTab) {
                            openMenu(menuTab);
                        }
                    }
                }
            });
        }

        add().growX();
    }

    private void processHierarchy(XmlReader.Element parent, String path, boolean isPrimary) {
        if(parent.getName().equals("menu")) {
            String newPath = path + parent.getAttribute("name");
            MenuPopup menuPopup = new MenuPopup(this, newPath);
            menuPopup.buildFrom(parent, isPrimary);
            popupMap.put(menuPopup.getId(), menuPopup);

            if (parent.getChildCount() > 0) {
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    XmlReader.Element item = parent.getChild(i);
                    processHierarchy(item, newPath + "/", false);
                }
            }
        }
    }

    public void openMenu(MenuTab menuTab) {
        if(currentlyActive != null) {
            if(currentlyActive != menuTab) {
                currentlyActive.collapse();
            }
        }

        menuTab.open();

        currentlyActive = menuTab;
    }

    @EventHandler
    public void menuPopupOpenCommand(MenuPopupOpenCommand command) {

        collapseUnrelatedHierarchies(command.getPath());

        MenuPopup menuPopup = popupMap.get(command.getPath());

        Stage stage = SharedResources.stage;

        stage.addActor(menuPopup);

        menuPopup.setPosition(command.getPreferredPos().x, command.getPreferredPos().y - menuPopup.getHeight());

        openStack.add(menuPopup);
    }

    public void collapseUnrelatedHierarchies(String path) {
        for(int i = openStack.size - 1; i >= 0; i--) {
            MenuPopup popup = openStack.get(i);
            if(!path.startsWith(popup.getId())) {
                popup.remove();
                openStack.removeIndex(i);
            }
        }
    }

    public void collapseHierarchyOf(String id) {

        for(int i = openStack.size - 1; i >= 0; i--) {
            MenuPopup popup = openStack.get(i);
            if(popup.getId().startsWith(id)) {
                popup.remove();
                openStack.removeIndex(i);
            }
        }
    }

    public boolean isPathOpen(String id) {
        for(MenuPopup popup : openStack) {
            if(popup.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public void collapseAll() {
        if(currentlyActive != null) {
            currentlyActive.collapse();
        } else {
            return;
        }
        for(MenuPopup popup : openStack) {
            popup.remove();
        }
        openStack.clear();
        currentlyActive = null;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if(stage == null) return;

        final Vector2 tmp = new Vector2();

        stage.addListener(new ClickListener() {

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {

                if(currentlyActive == null) super.mouseMoved(event, x, y);

                float distance = findDistanceFromClosestPopup(x, y);
                System.out.println(distance);

                if(distance > 80) {
                    collapseAll();
                }

                return super.mouseMoved(event, x, y);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                boolean anyOfOurs = false;
                for(MenuPopup popup : openStack) {
                    tmp.set(x, y);
                    popup.stageToLocalCoordinates(tmp);
                    if(popup.hit(tmp.x, tmp.y, true) != null) {
                        anyOfOurs = true;
                        break;
                    }
                }

                for(MenuTab tab : tabs) {
                    tmp.set(x, y);
                    tab.stageToLocalCoordinates(tmp);
                    if(tab.hit(tmp.x, tmp.y, true) != null) {
                        anyOfOurs = true;
                        break;
                    }
                }

                if(!anyOfOurs) {
                    collapseAll();
                }

                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    private float findDistanceFromClosestPopup(float x, float y) {
        float min = Float.MAX_VALUE;
        for(MenuPopup popup : openStack) {
            Rectangle rectangle = Pools.obtain(Rectangle.class);

            rectangle.set(popup.getX(), popup.getY(), popup.getWidth(), popup.getHeight());

            if(rectangle.contains(x, y)) return 0;

            float dx = Float.MAX_VALUE;
            float dy = Float.MAX_VALUE;
            if(x < rectangle.x) {
                dx = rectangle.x - x;
            } else if(x > rectangle.x + rectangle.width) {
                dx = x - (rectangle.x + rectangle.width);
            }

            if(y < rectangle.y) {
                dy = rectangle.y - y;
            } else if(y > rectangle.y + rectangle.height) {
                dy = y - (rectangle.y + rectangle.height);
            }

            float diff = Math.min(dy, dx);

            if(min > diff) {
                min = diff;
            }

            Pools.free(rectangle);
        }

        return min;
    }
}
