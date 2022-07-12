package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectCreated;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectDeleted;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectNameChanged;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

public class HierarchyWidget extends Table implements Notifications.Observer {

    private FilteredTree tree;

    private ObjectMap<String, GameObject> objectMap = new ObjectMap<>();
    private GameObjectContainer currentContainer;
    private ObjectMap<GameObject, FilteredTree.Node> nodeMap = new ObjectMap<>();

    private ContextualMenu contextualMenu;

    public HierarchyWidget() {
        tree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");
        tree.draggable = true;
        //tree.getSelection().setMultiple(true);

        add(tree).grow().pad(5).padRight(0);

        contextualMenu = new ContextualMenu();

        tree.setItemListener(new FilteredTree.ItemListener() {
            @Override
            public void chosen (FilteredTree.Node node) {
                GameObject gameObject = objectMap.get(node.getName());
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
                if(SceneEditorWorkspace.ctrlPressed()){
                    addToSelection(gameObject);
                    sceneEditorAddon.workspace.addToSelection(gameObject);
                }else{
                    select(gameObject);
                    sceneEditorAddon.workspace.selectGameObjectExternally(gameObject);
                }
            }

            @Override
            public void deselect(FilteredTree.Node node){
                GameObject gameObject = objectMap.get(node.getName());
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
                sceneEditorAddon.workspace.removeFromSelection(gameObject);
            }

            @Override
            public void rightClick (FilteredTree.Node node) {
                if (node == null) {
                    return;
                }
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();

                GameObject gameObject = objectMap.get(node.getName());

                if(!tree.getSelection().contains(node)) {
                    select(gameObject);
                    sceneEditorAddon.workspace.selectGameObjectExternally(gameObject);
                }

                showContextMenu(gameObject);
            }

            @Override
            public void delete (Array<FilteredTree.Node> nodes) {
                Array<GameObject> gameObjects = new Array<>();
                for(FilteredTree.Node node: nodes) {
                    if(objectMap.containsKey(node.getName())) {
                        GameObject gameObject = objectMap.get(node.getName());
                        gameObjects.add(gameObject);
                    }

                }
                SceneEditorAddon.get().workspace.deleteGameObjects(gameObjects);
            }

            @Override
            public void onNodeMove (FilteredTree.Node parentToMoveTo, FilteredTree.Node childThatHasMoved, int indexInParent, int indexOfPayloadInPayloadBefore) {
                if(parentToMoveTo != null) {
                    GameObject parent = objectMap.get(parentToMoveTo.getName());
                    GameObject child = objectMap.get(childThatHasMoved.getName());
                    SceneEditorAddon.get().workspace.repositionGameObject(parent, child);
                }
            }
        });

        Notifications.registerObserver(this);
    }

    private void showContextMenu (GameObject gameObject) {
        contextualMenu.clearItems();
        contextualMenu.addItem("Convert to Prefab", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                FilteredTree.Node item = (FilteredTree.Node) tree.getSelection().first();
                GameObject gameObject = objectMap.get(item.getName());
                SceneEditorAddon.get().workspace.convertToPrefab(gameObject);
            }
        });
        contextualMenu.addSeparator();
        contextualMenu.addItem("Cut", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addItem("Copy", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addItem("Paste", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addSeparator();
        contextualMenu.addItem("Rename", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addItem("Duplicate", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        contextualMenu.addItem("Delete", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                Array<GameObject> gameObjects= new Array<>();
                for(Object nodeObject: tree.getSelection()) {
                    FilteredTree.Node node = (FilteredTree.Node) nodeObject;
                    if(objectMap.containsKey(node.getName())) {
                        GameObject gameObject = objectMap.get(node.getName());
                        gameObjects.add(gameObject);
                    }

                }
                SceneEditorAddon.get().workspace.deleteGameObjects(gameObjects);
            }
        });
        contextualMenu.addSeparator();

        PopupMenu popupMenu = new PopupMenu();
        ObjectMap<String, XmlReader.Element> confMap = SceneEditorAddon.get().workspace.templateListPopup.getConfMap();
        for(String key: confMap.keys()) {
            XmlReader.Element element = confMap.get(key);

            MenuItem item = new MenuItem(element.getAttribute("title"));
            final String name = element.getAttribute("name");
            item.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    SceneEditorAddon.get().workspace.createObjectByTypeName(name, new Vector2(), gameObject);
                }
            });
            popupMenu.addItem(item);
        }

        MenuItem createMenu = contextualMenu.addItem("Create", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        });

        createMenu.setSubMenu(popupMenu);

        contextualMenu.show(getStage());
    }

    @EventHandler
    public void onGameObjectCreated(GameObjectCreated event) {
        GameObject gameObject = event.getTarget();
        if(currentContainer != null) {
            if(currentContainer.hasGOWithName(gameObject.getName())) {
                loadEntityContainer(currentContainer);
            }
        }
    }

    @EventHandler
    public void onGameObjectDeleted(GameObjectDeleted event) {
        FilteredTree.Node node = nodeMap.get(event.getTarget());
        tree.remove(node);
        nodeMap.remove(event.getTarget());
    }

    @EventHandler
    public void onGameObjectSelectionChanged(GameObjectSelectionChanged event) {
        if(currentContainer != null) {
            Array<GameObject> gameObjects = event.get();
            Array<FilteredTree.Node> nodes = new Array<>();
            for(GameObject gameObject: gameObjects) {
                nodes.add(nodeMap.get(gameObject));
            }
            tree.getSelection().clear();
            tree.getSelection().addAll(nodes);

        }
    }

    @EventHandler
    public void onGameObjectNameChanged(GameObjectNameChanged event) {
        GameObject gameObject = objectMap.get(event.oldName);
        objectMap.remove(event.oldName);
        objectMap.put(event.newName, gameObject);

        nodeMap.get(gameObject).name = event.newName;
    }

    private void select (GameObject gameObject) {
        if(gameObject == null) return;

        tree.getSelection().clear();
        tree.getSelection().add(nodeMap.get(gameObject));
        focusKeyboard(gameObject);
    }

    private void addToSelection (GameObject gameObject) {
        if(gameObject == null) return;

        tree.getSelection().add(nodeMap.get(gameObject));
        focusKeyboard(gameObject);
    }

    private void focusKeyboard(GameObject gameObject){
        Actor actor = nodeMap.get(gameObject).getActor();
        if(actor instanceof EditableLabel) {
            EditableLabel editableLabel = (EditableLabel) actor;
            if(!editableLabel.isEditMode()) {
                getStage().setKeyboardFocus(nodeMap.get(gameObject).getActor());
            }
        } else {
            getStage().setKeyboardFocus(nodeMap.get(gameObject).getActor());
        }
    }

    public void loadEntityContainer(GameObjectContainer entityContainer) {
        tree.clearChildren();
        objectMap.clear();
        nodeMap.clear();

        FilteredTree.Node parent = new FilteredTree.Node("root", new Label(entityContainer.getName(), TalosMain.Instance().getSkin()));

        traverseEntityContainer(entityContainer, parent);

        tree.add(parent);

        tree.expandAll();

        currentContainer = entityContainer;
    }

    private void traverseEntityContainer(GameObjectContainer entityContainer, FilteredTree.Node node) {
        Array<GameObject> gameObjects = entityContainer.getGameObjects();

        if(gameObjects == null) return;

        for(int i = 0; i < gameObjects.size; i++) {
            final GameObject gameObject = gameObjects.get(i);
            EditableLabel editableLabel = new EditableLabel(gameObject.getName(), TalosMain.Instance().getSkin());
            FilteredTree.Node newNode = new FilteredTree.Node(gameObject.getName(), editableLabel);
            newNode.draggable = true;
            node.add(newNode);

            editableLabel.setListener(new EditableLabel.EditableLabelChangeListener() {
                @Override
                public void changed (String newText) {
                    SceneEditorAddon.get().workspace.changeGOName(gameObject, newText);
                }
            });

            objectMap.put(gameObject.getName(), gameObject);
            nodeMap.put(gameObject, newNode);

            if(gameObject.getGameObjects() != null) {
                traverseEntityContainer(gameObject, newNode);
            }
        }
    }
}
