package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.FileWatching;
import com.talosvfx.talos.editor.addons.scene.widgets.AssetListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.TemplateListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
    private MainRenderer renderer;

    private String changeVersion = "";
    private SnapshotService snapshotService;

    private AssetListPopup assetListPopup;

    private ObjectMap<String, AMetadata> metadataCache = new ObjectMap<>();

    private FileTracker fileTracker = new FileTracker();
    private FileWatching fileWatching = new FileWatching();
    private float reloadScheduled = -1;

    public SceneEditorWorkspace() {
        setSkin(TalosMain.Instance().getSkin());
        setWorldSize(10);

        snapshotService = new SnapshotService();

        Notifications.registerObserver(this);

        FileHandle list = Gdx.files.internal("addons/scene/go-templates.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        GizmoRegister.init(root);

        assetListPopup = new AssetListPopup();
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

        renderer = new MainRenderer();
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


    public GameObject createSpriteObject (FileHandle importedAsset, Vector2 sceneCords) {
        return createSpriteObject(importedAsset, sceneCords, null);
    }

    public GameObject createSpriteObject (FileHandle importedAsset, Vector2 sceneCords, GameObject parent) {
        GameObject spriteObject = createObjectByTypeName("sprite", sceneCords, parent);
        SpriteRendererComponent component = spriteObject.getComponent(SpriteRendererComponent.class);

        component.path = importedAsset.path();
        component.reloadTexture();

        return spriteObject;
    }

    public GameObject createObjectByTypeName (String idName, Vector2 position, GameObject parent) {
        GameObject gameObject = new GameObject();
        XmlReader.Element template = templateListPopup.getTemplate(idName);

        String name = getUniqueGOName(template.getAttribute("nameTemplate", "gameObject"), true);
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

        return gameObject;
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
        XmlReader.Element container = template.getChildByName("components");
        Array<XmlReader.Element> componentsXMLArray = container.getChildrenByName("component");
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

                if(touchedGizmo != null) {
                    touchedGizmo.touchUp(hitCords.x, hitCords.y);
                }

                touchedGizmo = null;
            }

            @Override
            public boolean keyDown (InputEvent event, int keycode) {

                if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                    Array<GameObject> deleteList = new Array<>();
                    deleteList.addAll(selection);
                    clearSelection();
                    deleteGameObjects(deleteList);
                }

                if(keycode == Input.Keys.Z && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    TalosMain.Instance().ProjectController().undo();
                }

                if(keycode == Input.Keys.Z && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    TalosMain.Instance().ProjectController().redo();
                }

                for(Gizmo gizmo: gizmoList) {
                    if(gizmo.isSelected()) {
                        gizmo.keyDown(event, keycode);
                    }
                }

                return super.keyDown(event, keycode);
            }
        });
    }

    @Override
    public void write (Json json) {

        changeVersion = UUID.randomUUID().toString();

        if(projectPath != null) {
            json.writeValue("projectPath", projectPath);
            json.writeArrayStart("scenes");
            for(Scene scene: scenes) {
                json.writeValue(scene);
            }
            json.writeArrayEnd();
            json.writeValue("changeVersion", changeVersion);
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
        changeVersion = jsonData.getString("changeVersion", "");

        ProjectExplorerWidget projectExplorer = sceneEditorAddon.projectExplorer;

        if(projectPath == null || projectPath.isEmpty()) {
            projectPath = jsonData.getString("projectPath", "");
        }
        projectExplorer.loadDirectoryTree(projectPath);
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        for(int i = 0; i < gizmoList.size; i++) {
            Gizmo gizmo = gizmoList.get(i);
            gizmo.act(delta);
        }

        if(reloadScheduled > 0) {
            reloadScheduled -= delta;
            if(reloadScheduled <= 0) {
                reloadScheduled = -1;
                reloadProjectExplorer();
            }
        }
    }

    @Override
    public void drawContent (Batch batch, float parentAlpha) {
        if(!(TalosMain.Instance().Project() instanceof SceneEditorProject)) return;
        batch.end();
        drawGrid(batch, parentAlpha);
        batch.begin();

        drawMainRenderer(batch, parentAlpha);

        for(int i = 0; i < gizmoList.size; i++) {
            Gizmo gizmo = gizmoList.get(i);
            gizmo.setWoldWidth(getWorldWidth() * camera.zoom);
            gizmo.draw(batch, parentAlpha);
        }
    }

    private void drawMainRenderer (Batch batch, float parentAlpha) {
        renderer.render(batch, currentContainer.getSelfObject());
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

    // if asset is updated externally, do something about it maybe
    public void updateAsset (FileHandle handle) {

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
            Array<Gizmo> gizmos = GizmoRegister.makeGizmosFor(component);

            for(Gizmo gizmo: gizmos) {
                if (gizmo != null) {
                    gizmo.setGameObject(gameObject);

                    Array<Gizmo> list = gizmoMap.get(gameObject);
                    if (list == null) list = new Array<>();

                    gizmoMap.put(gameObject, list);

                    if (gizmo != null) {
                        gizmoList.add(gizmo);
                        list.add(gizmo);
                    }
                }
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

        Stage stage = TalosMain.Instance().UIStage().getStage();
        stage.setKeyboardFocus(this);
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

        if(!event.wasRapid()) {
            TalosMain.Instance().ProjectController().setDirty();
        }
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

    public Vector2 getMouseCordsOnScene () {
        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        this.screenToLocalCoordinates(vec);
        Vector2 local = getWorldFromLocal(vec.x, vec.y);
        return local;
    }

    public String saveData (boolean toMemory) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(sceneEditorAddon.workspace);

        if(toMemory){
            for (Scene scene : scenes) {
                String sceneData = scene.getAsString();
                String scenePath = scene.path;

                snapshotService.saveSnapshot(changeVersion, scenePath, sceneData);
            }
        } else {
            for (Scene scene : scenes) {
                scene.save();
            }
        }

        return data;
    }

    public void loadFromData (Json json, JsonValue jsonData, boolean fromMemory) {
        read(json, jsonData);

        JsonValue scenesJson = jsonData.get("scenes");
        scenes.clear();
        for(JsonValue sceneJson : scenesJson) {
            Scene scene = json.readValue(Scene.class, sceneJson);
            addScene(scene);
            if(fromMemory) {
                scene.loadFromJson(snapshotService.getSnapshot(changeVersion, scene.path));
            } else {
                scene.loadFromPath();
                // save it as snapshot
                snapshotService.saveSnapshot(changeVersion, scene.path, scene.getAsString());
            }
        }
        ProjectExplorerWidget projectExplorer = sceneEditorAddon.projectExplorer;
        if(!scenes.isEmpty()) {
            Scene scene = scenes.first();
            projectExplorer.select(scene.path);
            openScene(scene);
        }

        if(!fromMemory) {
            Notifications.fireEvent(Notifications.obtainEvent(ProjectOpened.class));
        }
    }

    public void repositionGameObject (GameObject parentToMoveTo, GameObject childThatHasMoved) {
        if(parentToMoveTo == null) {
            parentToMoveTo = currentContainer.getSelfObject();
        }

        if(childThatHasMoved.parent != null) {
            childThatHasMoved.parent.removeObject(childThatHasMoved);
        }

        parentToMoveTo.addGameObject(childThatHasMoved);

        TalosMain.Instance().ProjectController().setDirty();
    }

    public Array<String> getLayerList () {
        if(currentContainer instanceof Scene) {
            Scene scene = (Scene) currentContainer;
            return scene.layers;
        }
        return new Array<>();
    }

    public GameObject getRootGO () {
        return currentContainer.getSelfObject();
    }

    @EventHandler
    public void onLayerListUpdated(LayerListUpdated event) {
        Array<String> layerList = getLayerList();
        // find all game objects and if any of them is on layer that does not exist, change its layer to default
        Array<GameObject> list = new Array<>();
        list = currentContainer.getSelfObject().getChildrenByComponent(RendererComponent.class, list);

        for(GameObject gameObject: list) {
            RendererComponent component = gameObject.getComponentSlow(RendererComponent.class);
            String sortingLayer = component.getSortingLayer();
            if(!layerList.contains(sortingLayer, false)) {
                component.setSortingLayer("Default");
            }
        }
    }

    public MainRenderer getRenderer () {
        return renderer;
    }

    public String getProjectPath () {
        return projectPath;
    }

    public AssetListPopup getAssetListPopup() {
        return assetListPopup;
    }

    public <T extends AMetadata> T getMetadata (String assetPath, Class<? extends T> clazz) {
        if(metadataCache.containsKey(assetPath)) {
            return (T) metadataCache.get(assetPath);
        } else {
            FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(assetPath);
            if(metadataHandle.exists()) {
                T t = SceneEditorAddon.get().assetImporter.readMetadata(metadataHandle, clazz);
                metadataCache.put(assetPath, t);
                return t;
            }
            return null;
        }

    }

    public FileHandle getProjectFolder() {
        return Gdx.files.absolute(projectPath);
    }

    public FileHandle getAssetsFolder() {
        return Gdx.files.absolute(projectPath + File.separator + "assets");
    }

    @EventHandler
    public void onProjectOpened(ProjectOpened event) {
        // setup file tracker
        try {
            fileWatching.startWatchingCurrentProject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onProjectDirectoryContentsChanged(ProjectDirectoryContentsChanged event) {
        if(event.getChanges().directoryStructureChange()) {
            boolean nonMeta = false;
            for(FileHandle added: event.getChanges().added) {
                if(!added.extension().equals("meta")) {
                    nonMeta = true;
                }
            }
            if(nonMeta) {
                reloadScheduled = 0.5f;
            }
        }
    }
}
