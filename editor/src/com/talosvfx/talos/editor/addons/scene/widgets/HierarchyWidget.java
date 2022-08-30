package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectActiveChanged;
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

    private FilteredTree<GameObject> tree;

    private ObjectMap<String, GameObject> objectMap = new ObjectMap<>();
    private GameObjectContainer currentContainer;
    private ObjectMap<GameObject, FilteredTree.Node<GameObject>> nodeMap = new ObjectMap<>();

    private ContextualMenu contextualMenu;


    public HierarchyWidget() {
        tree = new FilteredTree<>(TalosMain.Instance().getSkin(), "modern");
        tree.draggable = true;
        //tree.getSelection().setMultiple(true);

        top();
        defaults().top();

        ScrollPane scrollPane= new ScrollPane(tree);

        add(scrollPane).height(0).grow().pad(5).padRight(0);

        contextualMenu = new ContextualMenu();

        tree.addItemListener(new FilteredTree.ItemListener<GameObject>() {
            @Override
            public void selected (FilteredTree.Node<GameObject> node) {
                super.selected(node);
                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
                focusKeyboard(gameObject);
                sceneEditorAddon.workspace.selectGameObjectExternally(gameObject);
            }

            @Override
            public void addedIntoSelection (FilteredTree.Node<GameObject> node) {
                super.addedIntoSelection(node);
                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
                sceneEditorAddon.workspace.addToSelection(gameObject);
            }

            @Override
            public void removedFromSelection (FilteredTree.Node<GameObject> node) {
                super.removedFromSelection(node);
                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
                sceneEditorAddon.workspace.removeFromSelection(gameObject);
            }

            @Override
            public void clearSelection () {
                super.clearSelection();
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();
                sceneEditorAddon.workspace.requestSelectionClear();
            }

            @Override
            public void rightClick (FilteredTree.Node<GameObject> node) {
                if (node == null) {
                    return;
                }
                SceneEditorAddon sceneEditorAddon = SceneEditorAddon.get();

                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());

                if(!tree.getSelection().contains(node)) {
                    sceneEditorAddon.workspace.selectGameObjectExternally(gameObject);
                }

                showContextMenu(gameObject);
            }

            @Override
            public void delete (Array<FilteredTree.Node<GameObject>> nodes) {
                ObjectSet<GameObject> gameObjects = new ObjectSet<>();
                for(FilteredTree.Node<GameObject> node: nodes) {
                    if(objectMap.containsKey(node.getObject().uuid.toString())) {
                        GameObject gameObject = objectMap.get(node.getObject().uuid.toString());
                        gameObjects.add(gameObject);
                    }

                }
                SceneEditorAddon.get().workspace.deleteGameObjects(gameObjects);
            }

            @Override
            public void onNodeMove (FilteredTree.Node<GameObject> parentToMoveTo, FilteredTree.Node<GameObject> childThatHasMoved, int indexInParent, int indexOfPayloadInPayloadBefore) {
                if(parentToMoveTo != null) {
                    GameObject parent = objectMap.get(parentToMoveTo.getObject().uuid.toString());
                    GameObject child = objectMap.get(childThatHasMoved.getObject().uuid.toString());
                    SceneEditorAddon.get().workspace.repositionGameObject(parent, child);
                }
            }

            @Override
            public void mouseMoved(FilteredTree.Node<GameObject> node) {

            }
        });

        Notifications.registerObserver(this);


    }

    private Actor createToolsForNode (FilteredTree.Node<GameObject> node) {
        GameObject gameObject = node.getObject();
        Table toolsWidget;
        ImageButton eyeButton;
        ImageButton handButton;

        Drawable openEyeDrawable = TalosMain.Instance().getSkin().getDrawable("timeline-icon-eye");
        Drawable closedEyeDrawable = TalosMain.Instance().getSkin().getDrawable("timeline-icon-eye-closed");

        eyeButton = new ImageButton(openEyeDrawable);
        eyeButton.setColor(new Color(Color.WHITE));

        handButton = new ImageButton(TalosMain.Instance().getSkin().getDrawable("hand-cursor"));
        handButton.setColor(new Color(Color.WHITE));

        toolsWidget = new Table() {
            @Override
            public void act (float delta) {
                super.act(delta);
                //Update from game object

                boolean hovered = node.over;

                if (!gameObject.isEditorVisible()) {
                    eyeButton.getStyle().imageUp = closedEyeDrawable;

                    if (eyeButton.isOver()) {
                        eyeButton.getColor().a = 1;
                    } else {
                        eyeButton.getColor().a = 1;
                    }
                } else {
                    if (eyeButton.isOver() || hovered) {
                        eyeButton.getStyle().imageUp = openEyeDrawable;
                        eyeButton.getColor().a = 1f;
                    } else {
                        eyeButton.getColor().a = 0;
                    }
                }

                if (gameObject.isEditorTransformLocked()) {
                    if (handButton.isOver() || hovered) {
                        handButton.getColor().a = 0.6f;
                    } else {
                        handButton.getColor().a = 0.3f;
                    }
                } else {
                    if (handButton.isOver() || hovered) {
                        handButton.getColor().a = 1f;
                    } else {
                        handButton.getColor().a = 0;
                    }
                }


            }
        };

        toolsWidget.add(eyeButton).size(15,15).padRight(2);
        toolsWidget.add(handButton).size(15,15).padRight(5);


        eyeButton.addListener(new ClickListener() {

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                gameObject.setEditorVisible(!gameObject.isEditorVisible());

                return true;
            }


        });

        handButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                gameObject.setEditorTransformLocked(!gameObject.isEditorTransformLocked());

                return true;
            }
        });

        toolsWidget.pack();

        return toolsWidget;
    }

    private void showContextMenu (GameObject gameObject) {
        contextualMenu.clearItems();
        contextualMenu.addItem("Convert to Prefab", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                FilteredTree.Node<GameObject> item = tree.getSelection().first();
                GameObject gameObject = objectMap.get(item.getObject().uuid.toString());
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
                SceneEditorWorkspace.getInstance().copySelected();
            }
        });
        contextualMenu.addItem("Paste", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                SceneEditorWorkspace.getInstance().pasteFromClipboard();
            }
        });
        contextualMenu.addSeparator();
        contextualMenu.addItem("Rename", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (tree.getSelection().size() == 1) {
                    FilteredTree.Node<GameObject> node = tree.findNode(tree.getSelection().first().getObject());
                    if (node != null) {
                        if (node.getActor() instanceof EditableLabel) {
                            EditableLabel actor = (EditableLabel)node.getActor();
                            actor.setEditMode();
                        }
                    }
                }
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
                ObjectSet<GameObject> gameObjects= new ObjectSet<>();
                for(FilteredTree.Node<GameObject> nodeObject: tree.getSelection()) {
                    if(objectMap.containsKey(nodeObject.getObject().uuid.toString())) {
                        GameObject gameObject = objectMap.get(nodeObject.getObject().uuid.toString());
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
    public void gameActiveChanged (GameObjectActiveChanged event) {
        updateColourForActive(event.target);
    }

    @EventHandler
    public void gameObjectNameChanged (GameObjectNameChanged event) {
        FilteredTree.Node<GameObject> node = tree.findNode(event.target);
        if (node != null) {
            if (node.getActor() instanceof EditableLabel) {
                EditableLabel actor = (EditableLabel)node.getActor();
                actor.setText(event.newName);
            }
        }
    }

    private void updateColourForActive (GameObject gameObject) {
        FilteredTree.Node<GameObject> node = tree.findNode(gameObject);
        if (node != null) {
            if (gameObject.active) {
                node.getActor().setColor(1, 1, 1, 1);
            } else {
                node.getActor().setColor(0.5f, 0.5f, 0.5f, 1f);
            }
        }
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
            ObjectSet<GameObject> gameObjects = event.get();
            Array<FilteredTree.Node<GameObject>> nodes = new Array<>();
            for(GameObject gameObject: gameObjects) {
                boolean hasNode = nodeMap.containsKey(gameObject);
                if (hasNode) {
                    nodes.add(nodeMap.get(gameObject));
                }

            }

            tree.clearSelection(false);
            tree.addNodesToSelection(nodes, false);
        }
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

        FilteredTree.Node<GameObject> parent = new FilteredTree.Node<>("root", makeHierarchyWidgetActor( new Label(entityContainer.getName(), TalosMain.Instance().getSkin()), entityContainer.getSelfObject()));
        parent.setObject(new GameObject());
        parent.setCompanionActor(createToolsForNode(parent));
        parent.setSelectable(false);

        traverseEntityContainer(entityContainer, parent);

        tree.add(parent);

        tree.expandAll();

        currentContainer = entityContainer;
    }

    private void traverseEntityContainer(GameObjectContainer entityContainer, FilteredTree.Node<GameObject> node) {
        Array<GameObject> gameObjects = entityContainer.getGameObjects();

        if(gameObjects == null) return;

        for(int i = 0; i < gameObjects.size; i++) {
            final GameObject gameObject = gameObjects.get(i);
            EditableLabel editableLabel = new EditableLabel(gameObject.getName(), TalosMain.Instance().getSkin());
            editableLabel.setStage(getStage());

            FilteredTree.Node<GameObject> newNode = new FilteredTree.Node<>(gameObject.getName(), makeHierarchyWidgetActor(editableLabel, gameObject));
            newNode.setObject(gameObject);
            newNode.setCompanionActor(createToolsForNode(newNode));

            newNode.draggable = true;
            node.add(newNode);

            editableLabel.setListener(new EditableLabel.EditableLabelChangeListener() {
                @Override
                public void changed (String newText) {
                    SceneEditorAddon.get().workspace.changeGOName(gameObject, newText);
                }
            });

            objectMap.put(gameObject.uuid.toString(), gameObject);
            nodeMap.put(gameObject, newNode);

            if(gameObject.getGameObjects() != null) {
                traverseEntityContainer(gameObject, newNode);
            }
        }
    }

    private Table makeHierarchyWidgetActor(Actor editableLabel, GameObject gameObject){
        Table objectTable = new Table() {
            @Override
            public void setColor (Color color) {
//                super.setColor(color);//Don't set colour for multiplied alpha
                editableLabel.setColor(color);
            }

            @Override
            public void setColor (float r, float g, float b, float a) {
//                super.setColor(r, g, b, a);  //DOn't set colour for multplied alpha
                editableLabel.setColor(r, g, b, a);

            }
        };

        if (!gameObject.active) {
            editableLabel.setColor(0.5f, 0.5f, 0.5f, 1f);
        }

        objectTable.add(editableLabel);
        return  objectTable;
    }
}
