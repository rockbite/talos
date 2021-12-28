package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.HierarchyWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.TemplateListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class SceneEditorWorkspace extends ViewportWidget implements Json.Serializable, Notifications.Observer {

    private static SceneEditorWorkspace instance;
    private final TemplateListPopup templateListPopup;

    private SceneEditorAddon sceneEditorAddon;
    private String projectPath;

    private Array<Scene> scenes = new Array<>();

    private GameObjectContainer currentContainer;
    private Array<Gizmo> gizmoList = new Array<>();
    private ObjectMap<GameObject, Array<Gizmo>> gizmoMap = new ObjectMap<>();

    private Array<GameObject> selection = new Array<>();

    public SceneEditorWorkspace() {
        setSkin(TalosMain.Instance().getSkin());
        setWorldSize(10);

        GizmoRegister.init();

        Notifications.registerObserver(this);

        FileHandle list = Gdx.files.internal("addons/scene/go-templates.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        templateListPopup = new TemplateListPopup(root);
        templateListPopup.setListener(new TemplateListPopup.ListListener() {
            @Override
            public void chosen (XmlReader.Element template, float x, float y) {
                Vector2 pos = new Vector2(x, y);
                createObjectByTypeName(template.getAttribute("name"), pos, null);
            }
        });

        clearListeners();
        initListeners();
        addPanListener();
    }

    public void createEmpty (Vector2 position) {
        createObjectByTypeName("empty", position, null);
    }

    public void createEmpty (GameObject parent) {
        createObjectByTypeName("empty", null, parent);
    }

    public void createEmpty (Vector2 position, GameObject parent) {
        createObjectByTypeName("empty", position, parent);
    }


    public void createObjectByTypeName (String idName, Vector2 position, GameObject parent) {
        GameObject gameObject = new GameObject();
        XmlReader.Element template = templateListPopup.getTemplate(idName);

        String name = getUniqueGOName(template.getAttribute("nameTemplate", "gameObject"));
        gameObject.setName(name);
        initComponentsFromTemplate(gameObject, templateListPopup.getTemplate(idName));

        if(position != null && gameObject.hasComponent(TransformComponent.class)) {
            // oh boi always special case with this fuckers
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
            transformComponent.position.set(position.x, position.y);
        }

        if(parent == null) {
            currentContainer.addGameObject(gameObject);
        } else {
            parent.addGameObject(gameObject);
        }

        initGizmos(gameObject);

        Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));

        selectGameObject(gameObject);
    }

    private String getUniqueGOName (String nameTemplate) {
        return getUniqueGOName(nameTemplate, false);
    }

    private String getUniqueGOName (String nameTemplate, boolean keepOriginal) {
        int number = 0;

        String name = nameTemplate;

        if(!keepOriginal) {
            name = nameTemplate + number;
        }

        while(currentContainer.hasGOWithName(name)) {
            number++;
            name = nameTemplate + number;
        }

        return name;
    }

    private void initComponentsFromTemplate (GameObject gameObject, XmlReader.Element template) {
        Array<XmlReader.Element> componentsXMLArray = template.getChildrenByName("component");
        for(XmlReader.Element componentXML: componentsXMLArray) {
            String className = componentXML.getAttribute("className");
            String classPath = templateListPopup.componentClassPath;

            try {
                Class clazz = ClassReflection.forName(classPath + "." + className);
                Object instance = ClassReflection.newInstance(clazz);
                IComponent component = (IComponent) instance;
                gameObject.addComponent(component);
            } catch (Exception e) {

            }
        }
    }

    private Gizmo hitGizmo(float x, float y) {
        for(Gizmo gizmo: gizmoList) {
            if(gizmo.hit(x, y)) return gizmo;
        }

        return null;
    }

    protected void initListeners () {
        addListener(new InputListener() {

            Vector2 vec = new Vector2();

            Gizmo touchedGizmo = null;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

                touchedGizmo = null;

                Vector2 hitCords = getWorldFromLocal(x, y);

                if (button == 1 && !event.isCancelled()) {
                    final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                    (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);

                    Vector2 location = new Vector2(vec);
                    Vector2 createLocation = new Vector2(hitCords);
                    templateListPopup.showPopup(getStage(), location, createLocation);

                    return true;
                }

                Gizmo gizmo = hitGizmo(hitCords.x, hitCords.y);

                if (gizmo != null) {
                    touchedGizmo = gizmo;
                    GameObject gameObject = touchedGizmo.getGameObject();
                    selectGameObject(gameObject);

                    touchedGizmo.touchDown(hitCords.x, hitCords.y, button);

                    getStage().setKeyboardFocus(SceneEditorWorkspace.this);

                    event.handle();

                    return true;
                } else {
                    touchedGizmo = null;
                }

                clearSelection();

                return false;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                Vector2 hitCords = getWorldFromLocal(x, y);

                if(touchedGizmo != null) {
                    touchedGizmo.touchDragged(hitCords.x, hitCords.y);
                }
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                Vector2 hitCords = getWorldFromLocal(x, y);

                Gizmo gizmo = hitGizmo(hitCords.x, hitCords.y);

                if(gizmo == touchedGizmo && touchedGizmo != null) {
                    touchedGizmo.touchUp(hitCords.x, hitCords.y);
                }

                touchedGizmo = null;
            }

            @Override
            public boolean keyDown (InputEvent event, int keycode) {

                if(keycode == Input.Keys.DEL) {
                    Array<GameObject> deleteList = new Array<>();
                    deleteList.addAll(selection);
                    clearSelection();
                    deleteGameObjects(deleteList);
                }

                return super.keyDown(event, keycode);
            }
        });
    }

    @Override
    public void write (Json json) {
        if(projectPath != null) {
            json.writeValue("projectPath", projectPath);
            json.writeArrayStart("scenes");
            for(Scene scene: scenes) {
                json.writeValue(scene);

                // also let's save scene data, although not sure about that
                scene.save();
            }
            json.writeArrayEnd();
        }
    }

    public void readProjectPath (FileHandle projectFileHandle, JsonValue jsonValue) {
        String path = "";
        if(projectFileHandle != null) {
            path = projectFileHandle.parent().path();
        } else {
            jsonValue.getString("projectPath", "");
        }
        projectPath = path;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        HierarchyWidget hierarchy = sceneEditorAddon.hierarchy;
        ProjectExplorerWidget projectExplorer = sceneEditorAddon.projectExplorer;

        if(projectPath == null || projectPath.isEmpty()) {
            projectPath = jsonData.getString("projectPath", "");
        }
        projectExplorer.loadDirectoryTree(projectPath);

        JsonValue scenesJson = jsonData.get("scenes");
        scenes.clear();
        for(JsonValue sceneJson : scenesJson) {
            Scene scene = json.readValue(Scene.class, sceneJson);
            addScene(scene);
            scene.loadFromPath();
        }

        if(!scenes.isEmpty()) {
            Scene scene = scenes.first();
            projectExplorer.select(scene.path);
            openScene(scene);
        }
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        for(int i = 0; i < gizmoList.size; i++) {
            Gizmo gizmo = gizmoList.get(i);
            gizmo.act(delta);
        }
    }

    @Override
    public void drawContent (Batch batch, float parentAlpha) {
        batch.end();
        drawGrid(batch, parentAlpha);
        batch.begin();


        for(int i = 0; i < gizmoList.size; i++) {
            Gizmo gizmo = gizmoList.get(i);
            gizmo.setWoldWidth(getWorldWidth() * camera.zoom);
            gizmo.draw(batch, parentAlpha);
        }
    }

    public void setAddon (SceneEditorAddon sceneEditorAddon) {
        this.sceneEditorAddon = sceneEditorAddon;
    }

    public static SceneEditorWorkspace getInstance() {
        if(instance == null) {
            instance = new SceneEditorWorkspace();
        }
        return instance;
    }

    public void cleanWorkspace () {

    }

    public String writeExport () {
        return "";
    }

    public void setProjectPath (String path) {
        projectPath = path;
    }

    public void reloadProjectExplorer() {
        ProjectExplorerWidget projectExplorer = sceneEditorAddon.projectExplorer;
        projectExplorer.loadDirectoryTree(projectPath);
    }

    public void addScene (Scene scene) {
        scenes.add(scene);
    }

    public void openScene (Scene mainScene) {
        sceneEditorAddon.hierarchy.loadEntityContainer(mainScene);
        currentContainer = mainScene;

        // process all game objects
        removeGizmos();
        initGizmos(mainScene);

        clearSelection();
    }

    private void removeGizmos () {
        gizmoList.clear();
        gizmoMap.clear();
    }

    private void removeGizmos (GameObject gameObject) {
        Array<Gizmo> list = gizmoMap.get(gameObject);

        gizmoList.removeAll(list, true);
        gizmoMap.remove(gameObject);
    }

    private void initGizmos (GameObject gameObject) {
        makeGizmosFor(gameObject);
        Array<GameObject> childObjects = gameObject.getGameObjects();
        if(childObjects != null) {
            for (GameObject childObject : childObjects) {
                makeGizmosFor(childObject);
                initGizmos(childObject);
            }
        }
    }

    private void initGizmos (GameObjectContainer gameObjectContainer) {
        Array<GameObject> childObjects = gameObjectContainer.getGameObjects();
        if(childObjects != null) {
            for (GameObject childObject : childObjects) {
                makeGizmosFor(childObject);
                initGizmos(childObject);
            }
        }
    }

    private void makeGizmosFor (GameObject gameObject) {

        if(gizmoMap.containsKey(gameObject)) return;

        Iterable<IComponent> components = gameObject.getComponents();
        for(IComponent component: components) {
            Gizmo gizmo = GizmoRegister.makeGizmoFor(component);
            gizmo.setGameObject(gameObject);
            Array<Gizmo> list = new Array<>();
            gizmoMap.put(gameObject, list);

            if(gizmo != null) {
                gizmoList.add(gizmo);
                list.add(gizmo);

            }
        }
    }

    public void selectPropertyHolder (IPropertyHolder propertyHolder) {
        if(propertyHolder == null) return;

        Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderSelected.class).setTarget(propertyHolder));
    }


    public void selectGameObject (GameObject gameObject) {
        if(gameObject == null) return;
        Array<GameObject> tmp = new Array<>();
        tmp.add(gameObject);

        setSelection(tmp);
    }

    public void addToSelection (GameObject gameObject) {
        selection.add(gameObject);
        selectPropertyHolder(gameObject);
        Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
    }

    public void setSelection(Array<GameObject> gameObjects) {
        selection.clear();

        selection.addAll(gameObjects);

        selectPropertyHolder(gameObjects.first());

        Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
    }

    private void clearSelection() {
        selection.clear();
        Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));

        // we select the main container then
        if(currentContainer instanceof Scene) {
            Scene scene = (Scene) currentContainer;
            selectPropertyHolder(scene);
        }
    }

    public void deleteGameObjects (Array<GameObject> gameObjects) {
        if(currentContainer != null) {
            for (GameObject gameObject : gameObjects) {

                if(gameObject == null) continue;

                GameObject parent = gameObject.getParent();
                if(parent != null) {

                    Array<GameObject> deletedObjects = null;

                    if (parent.hasGOWithName(gameObject.getName())) {
                        deletedObjects = parent.deleteGameObject(gameObject);
                    }

                    if (deletedObjects != null) {
                        for (GameObject deletedObject : deletedObjects) {
                            Notifications.fireEvent(Notifications.obtainEvent(GameObjectDeleted.class).setTarget(deletedObject));
                        }
                    }

                } else {
                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectDeleted.class).setTarget(gameObject));
                }

            }
        }
    }

    @EventHandler
    public void onGameObjectCreated(GameObjectCreated event) {

        TalosMain.Instance().ProjectController().setDirty();
    }

    @EventHandler
    public void onComponentUpdated(ComponentUpdated event) {
        IComponent component = event.getComponent();
        sceneEditorAddon.propertyPanel.propertyProviderUpdated(component);

        TalosMain.Instance().ProjectController().setDirty();
    }

    @EventHandler
    public void onGameObjectDeleted(GameObjectDeleted event) {
        GameObject target = event.getTarget();
        sceneEditorAddon.propertyPanel.notifyPropertyHolderRemoved(target);

        // remove gizmos
        removeGizmos(target);

        TalosMain.Instance().ProjectController().setDirty();
    }

    @EventHandler
    public void onGameObjectNameChanged(GameObjectNameChanged event) {
        TalosMain.Instance().ProjectController().setDirty();
    }

    @EventHandler
    public void onGameObjectSelectionChanged(GameObjectSelectionChanged event) {
        Array<GameObject> gameObjects = event.get();

        for(Gizmo gizmo: gizmoList) {
            gizmo.setSelected(false);
        }

        for(GameObject gameObject: gameObjects) {
            Array<Gizmo> gizmos = gizmoMap.get(gameObject);
            for(Gizmo gizmo: gizmos) {
                gizmo.setSelected(true);
            }
        }
    }


    public void changeGOName (GameObject gameObject, String suggestedName) {
        if(suggestedName.equals(gameObject.getName())) return;

        String finalName = getUniqueGOName(suggestedName, true);

        String oldName = gameObject.getName();

        gameObject.setName(finalName);

        GameObjectNameChanged event = Notifications.obtainEvent(GameObjectNameChanged.class);
        event.target = gameObject;
        event.oldName = oldName;
        event.newName = finalName;

        Notifications.fireEvent(event);
    }
}
