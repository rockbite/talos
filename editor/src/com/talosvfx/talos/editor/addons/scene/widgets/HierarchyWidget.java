package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.SkeletonData;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.layouts.LayoutApp;
import com.talosvfx.talos.editor.project2.GlobalDragAndDrop;
import com.talosvfx.talos.editor.project2.apps.SceneEditorApp;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.widgets.ui.SearchFilteredTree;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.events.scene.AddToSelectionEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.DeSelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.RemoveFromSelectionEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.RequestSelectionClearEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.SelectGameObjectExternallyEvent;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.EventContextProvider;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.assets.GameAssetOpenEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.Scene;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchyWidget extends Table implements Observer, EventContextProvider<GameObjectContainer> {

    private static final Logger logger = LoggerFactory.getLogger(HierarchyWidget.class);

    private final ScrollPane scrollPane;

    @Getter
    private FilteredTree<GameObject> tree;
    private SearchFilteredTree<GameObject> searchFilteredTree;

    private ObjectMap<String, GameObject> objectMap = new ObjectMap<>();
    private GameObjectContainer currentContainer;
    private ObjectMap<GameObject, FilteredTree.Node<GameObject>> nodeMap = new ObjectMap<>();

    private ContextualMenu contextualMenu;

    private GameAsset<Scene> gameAsset;

    public HierarchyWidget() {
        tree = new FilteredTree<>(SharedResources.skin, "modern");
        tree.draggable = true;
        searchFilteredTree = new SearchFilteredTree<>(SharedResources.skin, tree, null);
        searchFilteredTree.setPad(0, 20, 0, 0);

        setBackground(SharedResources.skin.newDrawable("white", Color.valueOf("#252525ff")));

        top();
        defaults().top();

        scrollPane = new ScrollPane(searchFilteredTree);

        add(scrollPane).height(0).grow().pad(5).padRight(0);

        contextualMenu = new ContextualMenu();

        tree.addItemListener(new FilteredTree.ItemListener<GameObject>() {
            @Override
            public void selected (FilteredTree.Node<GameObject> node) {
                super.selected(node);

                SharedResources.stage.setKeyboardFocus(tree);

                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());

                SelectGameObjectExternallyEvent selectGameObjectExternallyEvent = Notifications.obtainEvent(SelectGameObjectExternallyEvent.class);
                selectGameObjectExternallyEvent.setGameObject(gameObject);
                Notifications.fireEvent(selectGameObjectExternallyEvent);
            }

            @Override
            public void addedIntoSelection (FilteredTree.Node<GameObject> node) {
                super.addedIntoSelection(node);

                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());

                AddToSelectionEvent addToSelectionEvent = Notifications.obtainEvent(AddToSelectionEvent.class);
                addToSelectionEvent.set(currentContainer, gameObject);
                Notifications.fireEvent(addToSelectionEvent);

                SceneUtils.shouldPasteTo(currentContainer, getSelection().first());
            }

            @Override
            public void removedFromSelection (FilteredTree.Node<GameObject> node) {
                super.removedFromSelection(node);
                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());

                RemoveFromSelectionEvent removeFromSelectionEvent = Notifications.obtainEvent(RemoveFromSelectionEvent.class);
                removeFromSelectionEvent.setGameObject(gameObject);
                Notifications.fireEvent(removeFromSelectionEvent);

                if (getSelection().isEmpty()) {
                    SceneUtils.shouldPasteToRoot(currentContainer);
                }
            }

            @Override
            public void clearSelection () {
                super.clearSelection();
                RequestSelectionClearEvent requestSelectionClearEvent = Notifications.obtainEvent(RequestSelectionClearEvent.class);
                Notifications.fireEvent(requestSelectionClearEvent);

                SceneUtils.shouldPasteToRoot(currentContainer);
            }

            @Override
            public void rightClick (FilteredTree.Node<GameObject> node) {
                if (node == null) {
                    return;
                }

                GameObject gameObject = objectMap.get(node.getObject().uuid.toString());
                if(!tree.getSelection().contains(node)) {
                    SelectGameObjectExternallyEvent selectGameObjectExternallyEvent = Notifications.obtainEvent(SelectGameObjectExternallyEvent.class);
                    selectGameObjectExternallyEvent.setGameObject(gameObject);
                    Notifications.fireEvent(selectGameObjectExternallyEvent);
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

                        SceneUtils.deleteGameObject(currentContainer, gameObject);
                    }
                }
            }

            @Override
            public void onNodeMove (FilteredTree.Node<GameObject> parentToMoveTo, FilteredTree.Node<GameObject> childThatHasMoved, int indexInParent, int indexOfPayloadInPayloadBefore) {
                if (parentToMoveTo != null) {
                    GameObject parent = objectMap.get(parentToMoveTo.getObject().uuid.toString());
                    GameObject child = objectMap.get(childThatHasMoved.getObject().uuid.toString());

                    SceneUtils.repositionGameObject(currentContainer, parent, child);
                }
            }

            @Override
            public void mouseMoved(FilteredTree.Node<GameObject> node) {

            }
        });

        Notifications.registerObserver(this);

        SharedResources.globalDragAndDrop.addTarget(new DragAndDrop.Target(HierarchyWidget.this) {
			@Override
			public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				if (currentContainer == null) return false;

				GlobalDragAndDrop.BaseDragAndDropPayload object = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();

				if (object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
					//We support single game asset drops

					return true;
				}

				return false;
			}

			@Override
			public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				GlobalDragAndDrop.BaseDragAndDropPayload object = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();
				// TODO: this needs a nicer system

                Selection<FilteredTree.Node<GameObject>> selection = tree.getSelection();
                Vector2 vec = new Vector2();
                GameObject parent;
                if (selection.size() == 1) {
                    // add payload as child
                    parent = selection.first().getObject();
                } else {
                    // add payload to root
                    parent = currentContainer.getSelfObject();
                }

                if (object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
					//We support single game asset drops
					GlobalDragAndDrop.GameAssetDragAndDropPayload gameAssetPayload = (GlobalDragAndDrop.GameAssetDragAndDropPayload)object;
					if (gameAssetPayload.getGameAsset().type == GameAssetType.SPRITE) {
						GameAsset<AtlasSprite> gameAsset = (GameAsset<AtlasSprite>) gameAssetPayload.getGameAsset();


						SceneUtils.createSpriteObject(currentContainer, gameAsset, vec, parent);

						//forcefully make active if we aren't active
                        SceneEditorApp sceneEditorApp = SharedResources.appManager.getAppForAsset(SceneEditorApp.class, HierarchyWidget.this.gameAsset);
                        LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
						SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());

					} else if (gameAssetPayload.getGameAsset().type == GameAssetType.PREFAB) {
						GameAsset<Prefab> gameAsset = (GameAsset<Prefab>)gameAssetPayload.getGameAsset();

						SceneUtils.createFromPrefab(currentContainer, gameAsset, vec, parent);

                        //forcefully make active if we aren't active
                        SceneEditorApp sceneEditorApp = SharedResources.appManager.getAppForAsset(SceneEditorApp.class, HierarchyWidget.this.gameAsset);
                        LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
                        SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());

					} else if (gameAssetPayload.getGameAsset().type == GameAssetType.SKELETON) {
						GameAsset<SkeletonData> gameAsset = (GameAsset<SkeletonData>)gameAssetPayload.getGameAsset();

						SceneUtils.createSpineObject(currentContainer, gameAsset, vec, parent);

                        //forcefully make active if we aren't active
                        SceneEditorApp sceneEditorApp = SharedResources.appManager.getAppForAsset(SceneEditorApp.class, HierarchyWidget.this.gameAsset);
                        LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
                        SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());

					} else if (gameAssetPayload.getGameAsset().type == GameAssetType.VFX) {
						GameAsset<VFXProjectData> gameAsset = (GameAsset<VFXProjectData>)gameAssetPayload.getGameAsset();

						SceneUtils.createParticle(currentContainer, gameAsset, vec, parent);

                        //forcefully make active if we aren't active
                        SceneEditorApp sceneEditorApp = SharedResources.appManager.getAppForAsset(SceneEditorApp.class, HierarchyWidget.this.gameAsset);
                        LayoutApp gridAppReference = sceneEditorApp.getGridAppReference();
                        SharedResources.currentProject.getLayoutGrid().setLayoutActive(gridAppReference.getLayoutContent());

					}
					return;
				}
				logger.info("TODO other implementations of drag drop payloads");

			}
		});
    }

    public void copySelected () {
        OrderedSet<GameObject> selection = getSelection();
        SceneUtils.copy(gameAsset, selection);
    }

    public void cutSelected () {
        OrderedSet<GameObject> selection = getSelection();
        SceneUtils.cut(gameAsset, selection);
    }

    public OrderedSet<GameObject> getSelection () {
        final Selection<FilteredTree.Node<GameObject>> selection = tree.getSelection();
        final OrderedSet<GameObject> arraySelection = new OrderedSet<>();
        for (FilteredTree.Node<GameObject> gameObjectNode : selection) {
            arraySelection.add(gameObjectNode.getObject());
        }
        return arraySelection;
    }

    public void deleteSelected () {
        ObjectSet<GameObject> selection = new ObjectSet<>();
        for(FilteredTree.Node<GameObject> nodeObject: tree.getSelection()) {
            if(objectMap.containsKey(nodeObject.getObject().uuid.toString())) {
                GameObject gameObject = objectMap.get(nodeObject.getObject().uuid.toString());
                selection.add(gameObject);
            }
        }

        ObjectSet<GameObject> deleteList = new ObjectSet<>();
        deleteList.addAll(selection);

        if (currentContainer != null) {
            SceneUtils.deleteGameObjects(currentContainer, deleteList);
        }
    }

    public void pasteFromClipboard () {
        SceneUtils.paste(gameAsset);
    }

    private Actor createToolsForNode (FilteredTree.Node<GameObject> node) {
        GameObject gameObject = node.getObject();
        Table toolsWidget;
        ImageButton eyeButton;
        ImageButton handButton;

        Drawable openEyeDrawable = SharedResources.skin.getDrawable("timeline-icon-eye");
        Drawable closedEyeDrawable = SharedResources.skin.getDrawable("timeline-icon-eye-closed");

        eyeButton = new ImageButton(openEyeDrawable);
        eyeButton.setColor(new Color(Color.WHITE));

        handButton = new ImageButton(SharedResources.skin.getDrawable("hand-cursor"));
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
                gameObject.setEditorVisible(!gameObject.isEditorVisible());
                SceneUtils.visibilityUpdated(currentContainer, gameObject);
                // stop proceeding to parent touch down
                event.cancel();
                return true;
            }
        });

        handButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                gameObject.setEditorTransformLocked(!gameObject.isEditorTransformLocked());
                SceneUtils.lockUpdated(currentContainer, gameObject);

                Selection<FilteredTree.Node<GameObject>> selection = tree.getSelection();
                for (FilteredTree.Node<GameObject> gameObjectNode : selection) {
                    if (gameObjectNode.getObject() == gameObject) {
                        DeSelectGameObjectExternallyEvent deSelectGameObjectExternallyEvent = Notifications.obtainEvent(DeSelectGameObjectExternallyEvent.class);
                        deSelectGameObjectExternallyEvent.setGameObject(gameObject);
                        Notifications.fireEvent(deSelectGameObjectExternallyEvent);
                        tree.removeNodeFromSelection(gameObjectNode);
                    }
                }
                // stop proceeding to parent touch down
                event.cancel();
                return true;
            }
        });

        toolsWidget.pack();

        return toolsWidget;
    }

    private void showContextMenu (@Null GameObject gameObject) {
        contextualMenu.clearItems();

        boolean areWeRootOfPrefab = areWeRootOfPrefab(gameObject);

        // if multiple objects are selected disable convert to prefab functionality
        final boolean multipleObjectSelected = tree.getSelection().size() != 1;
        final MenuItem convertToPrefab = contextualMenu.addItem("Convert to Prefab", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (multipleObjectSelected) return;
                FilteredTree.Node<GameObject> item = tree.getSelection().first();
                GameObject gameObject = objectMap.get(item.getObject().uuid.toString());
                SceneUtils.convertToPrefab(gameObject);
            }
        });

        contextualMenu.addSeparator();
        MenuItem cut = contextualMenu.addItem("Cut", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        MenuItem copy = contextualMenu.addItem("Copy", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                copySelected();
            }
        });
        MenuItem paste = contextualMenu.addItem("Paste", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                pasteFromClipboard();
            }
        });

        contextualMenu.addSeparator();
        MenuItem rename = contextualMenu.addItem("Rename", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (tree.getSelection().size() == 1) {
                    FilteredTree.Node<GameObject> node = tree.findNode(tree.getSelection().first().getObject());
                    if (node != null) {
                        if (node.getActor() instanceof HierarchyWrapper) {
                            HierarchyWrapper wrapper = (HierarchyWrapper)node.getActor();
                            if (wrapper.label instanceof EditableLabel) {
                                ((EditableLabel)wrapper.label).setEditMode();
                            }
                        }
                    }
                }
            }
        });
        MenuItem duplicate = contextualMenu.addItem("Duplicate", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

            }
        });
        MenuItem delete = contextualMenu.addItem("Delete", new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                deleteSelected();
            }
        });
        contextualMenu.addSeparator();

        PopupMenu popupMenu = new PopupMenu();

        ObjectMap<String, XmlReader.Element> confMap = RuntimeContext.getInstance().configData.getGameObjectConfigurationMap();
        for(String key: confMap.keys()) {
            XmlReader.Element element = confMap.get(key);

            MenuItem item = new MenuItem(element.getAttribute("title"));
            final String name = element.getAttribute("name");
            item.addListener(new ClickListener() {
                @Override
                public void clicked (InputEvent event, float x, float y) {
                    final GameObject newObjectInstance = SceneUtils.createObjectByTypeName(currentContainer, name, new Vector2(), gameObject, name);
                    Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(newObjectInstance));
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

        //cut copy  duplicate delete
        convertToPrefab.setDisabled(multipleObjectSelected || areWeRootOfPrefab);

        cut.setDisabled(areWeRootOfPrefab);
        copy.setDisabled(areWeRootOfPrefab);
        duplicate.setDisabled(areWeRootOfPrefab);
        delete.setDisabled(areWeRootOfPrefab);


        contextualMenu.show(getStage());
    }

    private boolean areWeRootOfPrefab (@Null GameObject gameObject) {
        if (gameObject == null) {
            //we are a dummy root that doesnt have real game object, this is for dummy root of scene, not required to be valid game object
            return false;
        }
        if (gameObject.getGameObjectContainerRoot() instanceof Prefab) {
            if (gameObject.parent == null) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    public void gameActiveChanged (GameObjectActiveChanged event) {
        updateColourForActive(event.target);
    }

    @EventHandler
    public void gameObjectsRestructured (GameObjectsRestructured event) {
        restructureGameObjects(event.targets);
    }

    @EventHandler
    public void gameObjectNameChanged (GameObjectNameChanged event) {
        FilteredTree.Node<GameObject> node = tree.findNode(event.target);
        if (node != null) {
            if (node.getActor() instanceof HierarchyWrapper) {
                HierarchyWrapper wrapper = (HierarchyWrapper)node.getActor();
                if (wrapper.label instanceof EditableLabel) {
                    ((EditableLabel)wrapper.label).setText(event.newName);
                }
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
                //Just add it

                FilteredTree.Node<GameObject> newNode = createNodeForGameObject(gameObject);
                processNewNode(newNode);

                tree.addSource(newNode);
            }
        }

    }

    private void processNewNode (FilteredTree.Node<GameObject> newNode) {
        GameObject gameObject = newNode.getObject();

        //If our parent doesn't have a parent, our parent is the fake root
        if (gameObject.getParent() != null && gameObject.getParent().getParent() != null) {
            FilteredTree.Node<GameObject> parent = tree.findNode(gameObject.getParent());
            if (parent != null) {

                objectMap.put(gameObject.uuid.toString(), gameObject);
                nodeMap.put(gameObject, newNode);

                parent.add(newNode);
            } else {
                System.out.println("No parent found to add to node");
            }
        } else {
            //Add it to the fake root

            objectMap.put(gameObject.uuid.toString(), gameObject);
            nodeMap.put(gameObject, newNode);

            tree.getRootNodes().first().add(newNode);
        }

        if (gameObject.getGameObjects() != null) {
            Array<GameObject> children = gameObject.getGameObjects();
            for (int i = 0; i < children.size; i++) {
                GameObject child = children.get(i);

                FilteredTree.Node<GameObject> childNode = createNodeForGameObject(child);
                processNewNode(childNode);
            }
        }
    }

    @EventHandler
    public void onGameObjectDeleted(GameObjectDeleted event) {
        FilteredTree.Node node = nodeMap.get(event.getTarget());
        logger.warn("there can be 2 hierarchy widgets open (like prefab and scene), handle to find right instance");
        if (node != null) {
            tree.remove(node);
            nodeMap.remove(event.getTarget());
        }
    }

    @EventHandler
    public void onGameObjectSelectionChanged(GameObjectSelectionChanged event) {
        if (currentContainer != null) {
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

            if (!nodes.isEmpty()) {
                //Focus on first one
                FilteredTree.Node<GameObject> first = nodes.first();
                //Focus on first one

                first.expandTo();
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run () {
                        //Need to do it frame layer after layut
                        float topY = scrollPane.getScrollY();
                        float scrollHeight = scrollPane.getScrollHeight();

                        float positionInParent = tree.getHeight() - first.getActor().getY();

                        if (positionInParent < topY || positionInParent > (topY + scrollHeight)) {
                            scrollPane.setScrollY(positionInParent - scrollHeight/2f);
                        }
                    }
                });

            }
        }
    }

    private void focusKeyboard(GameObject gameObject){
        Actor actor = nodeMap.get(gameObject).getActor();
        if(actor instanceof HierarchyWrapper) {
            Actor label = ((HierarchyWrapper)actor).label;
            if (label instanceof EditableLabel) {
                EditableLabel editableLabel = (EditableLabel) label;
                if(!editableLabel.isEditMode()) {
                    getStage().setKeyboardFocus(nodeMap.get(gameObject).getActor());
                }
            }
        } else {
            getStage().setKeyboardFocus(nodeMap.get(gameObject).getActor());
        }
    }

    public void loadEntityContainer (GameAsset<Scene> gameAsset) {
        this.gameAsset = gameAsset;

        currentContainer = gameAsset.getResource();

        tree.clearChildren();
        objectMap.clear();
        nodeMap.clear();

        FilteredTree.Node<GameObject> parent = new FilteredTree.Node<>("root", makeHierarchyWidgetActor( new Label(currentContainer.getName(), SharedResources.skin), currentContainer.getSelfObject()));

        boolean shouldBeSelectable = currentContainer instanceof Prefab;

        parent.setSelectable(shouldBeSelectable);
        if (shouldBeSelectable) {
            //set the root object
            parent.setObject(currentContainer.getSelfObject());
            objectMap.put(currentContainer.getSelfObject().uuid.toString(), currentContainer.getSelfObject());
            nodeMap.put(currentContainer.getSelfObject(), parent);
        } else {
            parent.setObject(new GameObject());
        }

        parent.setCompanionActor(createToolsForNode(parent));

        traverseEntityContainer(currentContainer, parent);

        tree.add(parent);

        tree.expandAll();
    }

    private FilteredTree.Node<GameObject> createNodeForGameObject (GameObject gameObject) {
        EditableLabel editableLabel = new EditableLabel(gameObject.getName(), SharedResources.skin);
        editableLabel.setStage(getStage());

        editableLabel.setListener(new EditableLabel.EditableLabelChangeListener() {
            @Override
            public void editModeStarted () {

            }

            @Override
            public void changed (String newText) {
                String oldName = gameObject.getName();

                gameObject.setName(newText);

                GameObjectNameChanged gameObjectNameChanged = Notifications.obtainEvent(GameObjectNameChanged.class);
                gameObjectNameChanged.target = gameObject;
                gameObjectNameChanged.newName = newText;
                gameObjectNameChanged.oldName = oldName;
                Notifications.fireEvent(gameObjectNameChanged);

                SceneUtils.markContainerChanged(currentContainer);
            }
        });

        FilteredTree.Node<GameObject> newNode = new FilteredTree.Node<>(gameObject.getName(), makeHierarchyWidgetActor(editableLabel, gameObject));
        newNode.setObject(gameObject);
        newNode.setCompanionActor(createToolsForNode(newNode));

        newNode.draggable = true;

        return newNode;
    }

    private void traverseEntityContainer(GameObjectContainer entityContainer, FilteredTree.Node<GameObject> node) {
        Array<GameObject> gameObjects = entityContainer.getGameObjects();

        if(gameObjects == null) return;

        for(int i = 0; i < gameObjects.size; i++) {
            final GameObject gameObject = gameObjects.get(i);
            FilteredTree.Node<GameObject> newNode = createNodeForGameObject(gameObject);
            node.add(newNode);

            objectMap.put(gameObject.uuid.toString(), gameObject);
            nodeMap.put(gameObject, newNode);

            if(gameObject.getGameObjects() != null) {
                traverseEntityContainer(gameObject, newNode);
            }
        }
    }

    public void restructureGameObjects (Array<GameObject> selectedObjects) {
        for (GameObject gameObject : selectedObjects) {
            FilteredTree.Node<GameObject> node = tree.findNode(gameObject);

            if (node != null) {

                if (gameObject.getParent() != null && gameObject.getParent().getParent() != null) {
                    //Its not a root
                    GameObject parent = gameObject.getParent();
                    FilteredTree.Node<GameObject> parentNode = tree.findNode(parent);
                    if (parentNode != null) {

                        tree.remove(node);
                        parentNode.add(node);
                    } else {
                        System.out.println("Couldn't find new parent");
                    }
                } else {
                    tree.remove(node);
                    //Its somehow moved into the root
                    tree.getRootNodes().first().add(node);
                }

            }

        }
    }

    public ScrollPane getScrollPane () {
        return scrollPane;
    }

    @Override
    public GameObjectContainer getContext() {
        return currentContainer;
    }

    private static class HierarchyWrapper extends Table {

        private final Actor label;

        HierarchyWrapper (Actor editableLabel) {
            this.label = editableLabel;
        }

        @Override
        public void setColor (Color color) {
//                super.setColor(color);//Don't set colour for multiplied alpha
            label.setColor(color);
        }

        @Override
        public void setColor (float r, float g, float b, float a) {
//                super.setColor(r, g, b, a);  //DOn't set colour for multplied alpha
            label.setColor(r, g, b, a);

        }
    }
    private HierarchyWrapper makeHierarchyWidgetActor(Actor editableLabel, GameObject gameObject){
        HierarchyWrapper objectTable = new HierarchyWrapper(editableLabel);

        if (!gameObject.active) {
            editableLabel.setColor(0.5f, 0.5f, 0.5f, 1f);
        }

        objectTable.add(editableLabel);
        return objectTable;
    }

    @EventHandler
    public void onGameAssetOpen (GameAssetOpenEvent gameAssetOpenEvent) {
        GameAsset<?> gameAsset = gameAssetOpenEvent.getGameAsset();
        if (gameAsset.type == GameAssetType.SCENE) {
            loadEntityContainer((GameAsset<Scene>) gameAsset);
        }
    }
}
