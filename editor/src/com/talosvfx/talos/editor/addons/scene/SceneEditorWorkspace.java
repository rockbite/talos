package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.logic.*;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.FileWatching;
import com.talosvfx.talos.editor.addons.scene.widgets.AssetListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.TemplateListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.TransformGizmo;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SceneEditorWorkspace extends ViewportWidget implements Json.Serializable, Notifications.Observer {

    private static SceneEditorWorkspace instance;
    public final TemplateListPopup templateListPopup;

    private SceneEditorAddon sceneEditorAddon;
    private String projectPath;

    private SavableContainer currentContainer;
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

    public Array<String> layers = new Array<>();


    // selections
    private Image selectionRect;

    public SceneEditorWorkspace() {

        layers.clear();
        layers.add("Default");
        layers.add("UI");
        layers.add("Misc");

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

        Stage stage = TalosMain.Instance().UIStage().getStage();
        Skin skin = TalosMain.Instance().getSkin();
        selectionRect = new Image(skin.getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        addActor(selectionRect);
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

        component.path = AssetImporter.relative(importedAsset);
        component.reloadTexture();

        TextureRegion texture = component.texture;
        float aspect = (float)texture.getRegionWidth() / texture.getRegionHeight();
        TransformComponent transformComponent = spriteObject.getComponent(TransformComponent.class);
        transformComponent.scale.x *= aspect;

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

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run () {
                selectGameObjectExternally(gameObject);
            }
        });

        return gameObject;
    }

    public GameObject createFromPrefab (Prefab prefab, Vector2 position, GameObject parent) {
        GameObject gameObject = prefab.root;
        String name = getUniqueGOName(prefab.name, true);
        gameObject.setName(name);

        if(position != null && gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
            transformComponent.position.set(position.x, position.y);
            transformComponent.rotation = 0;
            transformComponent.scale.set(1, 1);
        }

        if(parent == null) {
            currentContainer.addGameObject(gameObject);
        } else {
            parent.addGameObject(gameObject);
        }

        initGizmos(gameObject);

        Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));

        selectGameObjectExternally(gameObject);

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

            // selection stuff
            boolean dragged = false;
            Vector2 startPos = new Vector2();
            Rectangle rectangle = new Rectangle();
            boolean upWillClear = true;

            GameObject selectedGameObject;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

                upWillClear = true;
                dragged = false;
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

                selectedGameObject = null;

                if (gizmo != null) {
                    touchedGizmo = gizmo;
                    GameObject gameObject = touchedGizmo.getGameObject();
                    upWillClear = false;
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        // toggling
                        if(selection.contains(gameObject, true)) {
                            removeFromSelection(gameObject);
                        } else {
                            addToSelection(gameObject);
                            selectedGameObject = gameObject;
                        }
                    } else {
                        if(!selection.contains(gameObject, true)) {
                            selectGameObject(gameObject);
                            selectedGameObject = gameObject;
                        }
                    }

                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));

                    touchedGizmo.touchDown(hitCords.x, hitCords.y, button);
                    // also tell all other selected gizmos about this touchdown
                    for(int i = 0; i < gizmoList.size; i++) {
                        Gizmo item = gizmoList.get(i);
                        if(item.isSelected() && item.getClass().equals(touchedGizmo.getClass()) && item != touchedGizmo) {
                            item.touchDown(hitCords.x, hitCords.y, button);
                        }
                    }


                    getStage().setKeyboardFocus(SceneEditorWorkspace.this);

                    event.handle();

                    return true;
                } else {
                    touchedGizmo = null;
                }

                if(button == 2 || ctrlPressed()) {
                    selectionRect.setVisible(true);
                    selectionRect.setSize(0, 0);
                    startPos.set(x, y);

                    return true;
                }

                clearSelection();
                Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                getStage().setKeyboardFocus(SceneEditorWorkspace.this);


                return false;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                dragged = true;

                Vector2 hitCords = getWorldFromLocal(x, y);

                if(touchedGizmo != null) {
                    touchedGizmo.touchDragged(hitCords.x, hitCords.y);
                    for(int i = 0; i < gizmoList.size; i++) {
                        Gizmo item = gizmoList.get(i);
                        if(item.isSelected() && item.getClass().equals(touchedGizmo.getClass()) && item != touchedGizmo) {
                            item.touchDragged(hitCords.x, hitCords.y);
                        }
                    }
                }

                if(selectionRect.isVisible()) {
                    vec.set(x, y);
                    vec.sub(startPos);
                    if(vec.x < 0) {
                        rectangle.setX(x);
                    } else {
                        rectangle.setX(startPos.x);
                    }
                    if(vec.y < 0) {
                        rectangle.setY(y);
                    } else {
                        rectangle.setY(startPos.y);
                    }
                    rectangle.setWidth(Math.abs(vec.x));
                    rectangle.setHeight(Math.abs(vec.y));

                    selectionRect.setPosition(rectangle.x, rectangle.y);
                    selectionRect.setSize(rectangle.getWidth(), rectangle.getHeight());
                }
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                Vector2 hitCords = getWorldFromLocal(x, y);

                Gizmo gizmo = hitGizmo(hitCords.x, hitCords.y);

                if(touchedGizmo != null) {
                    touchedGizmo.touchUp(hitCords.x, hitCords.y);
                    for(int i = 0; i < gizmoList.size; i++) {
                        Gizmo item = gizmoList.get(i);
                        if(item.isSelected() && item.getClass().equals(touchedGizmo.getClass()) && item != touchedGizmo) {
                            item.touchUp(hitCords.x, hitCords.y);
                        }
                    }
                }

                touchedGizmo = null;

                if(selectionRect.isVisible()) {
                    upWillClear = false;
                    selectByRect(rectangle);
                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                } else if(upWillClear) {
                    FocusManager.resetFocus(getStage());
                    clearSelection();
                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                } else {
                    if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        // deselect all others, if they are selected
                        if(deselectOthers(selectedGameObject)) {
                            Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                        }
                    }
                }

                getStage().setKeyboardFocus(SceneEditorWorkspace.this);

                selectionRect.setVisible(false);
            }

            @Override
            public boolean keyDown (InputEvent event, int keycode) {

                if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                    Array<GameObject> deleteList = new Array<>();
                    deleteList.addAll(selection);
                    clearSelection();
                    deleteGameObjects(deleteList);
                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                }

                if(keycode == Input.Keys.C && ctrlPressed()) {
                    copySelected();
                }

                if(keycode == Input.Keys.V && ctrlPressed()) {
                    pasteFromClipboard();
                }

                if(keycode == Input.Keys.A && ctrlPressed()) {
                    selectAll();
                    Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
                }

                if(keycode == Input.Keys.Z && ctrlPressed() && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    TalosMain.Instance().ProjectController().undo();
                }

                if(keycode == Input.Keys.Z && ctrlPressed() && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
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

    public static boolean isRenamePressed(int keycode) {
        if(TalosMain.Instance().isOsX()) {
            return keycode == Input.Keys.ENTER;
        } else {
            return keycode == Input.Keys.F2;
        }
    }

    public static boolean ctrlPressed() {
        if(TalosMain.Instance().isOsX()) {
            return Gdx.input.isKeyPressed(Input.Keys.SYM) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        } else {
            return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        }
    }

    public void openPrefab (FileHandle fileHandle) {
        if(currentContainer != null) {
            currentContainer.save();
        }
        Prefab scene = new Prefab();
        scene.path = fileHandle.path();
        scene.loadFromPath();
        openSavableContainer(scene);
    }

    public void openScene (FileHandle fileHandle) {
        if(currentContainer != null) {
            currentContainer.save();
        }
        Scene scene = new Scene();
        scene.path = fileHandle.path();
        scene.loadFromPath();
        openSavableContainer(scene);
    }

    public void convertToPrefab (GameObject gameObject) {
        String name = gameObject.getName();

        String path = getProjectPath() + File.separator + "assets";
        if(SceneEditorAddon.get().projectExplorer.getCurrentFolder() != null) {
            path = SceneEditorAddon.get().projectExplorer.getCurrentFolder().path();
        }

        FileHandle handle = AssetImporter.suggestNewName(path, name, "prefab");
        if(handle != null) {
            Prefab prefab = new Prefab();
            prefab.path = handle.path();
            prefab.root = gameObject;
            prefab.save();
            SceneEditorAddon.get().projectExplorer.reload();
        }
    }

    public static class ClipboardPayload {
        public Array<GameObject> objects = new Array<>();
        public Vector2 cameraPositionAtCopy = new Vector2(0, 0);
    }

    private void copySelected () {
        ClipboardPayload payload = new ClipboardPayload();
        payload.objects.addAll(selection);
        Vector3 camPos = getCamera().position;
        payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

        Json json = new Json();
        String clipboard = json.toJson(payload);
        Gdx.app.getClipboard().setContents(clipboard);
    }

    private void pasteFromClipboard () {
        String clipboard = Gdx.app.getClipboard().getContents();

        Json json = new Json();

        try {
            ClipboardPayload payload = json.fromJson(ClipboardPayload.class, clipboard);
            Vector3 camPosAtPaste = getCamera().position;
            Vector2 offset = new Vector2(camPosAtPaste.x, camPosAtPaste.y);
            offset.sub(payload.cameraPositionAtCopy);

            clearSelection();
            for(GameObject gameObject: payload.objects) {
                String name = getUniqueGOName(gameObject.getName(), false);
                gameObject.setName(name);
                currentContainer.addGameObject(gameObject);
                TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
                transformComponent.position.add(offset);
                initGizmos(gameObject);
                Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));
                addToSelection(gameObject);
            }
            Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
        } catch (Exception e) {

        }
    }

    @Override
    public void write (Json json) {

        changeVersion = UUID.randomUUID().toString();

        if(projectPath != null) {
            json.writeValue("projectPath", projectPath);

            json.writeArrayStart("layers");
            for (String layer : layers) {
                json.writeValue(layer);
            }
            json.writeArrayEnd();

            json.writeValue("currentScene", AssetImporter.relative(currentContainer.path));

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

        JsonValue layersJson = jsonData.get("layers");
        if(layersJson != null) {
            layers.clear();
            for (JsonValue layerJson : layersJson) {
                layers.add(layerJson.asString());
            }
        }

        String path = jsonData.getString("currentScene", "");
        FileHandle sceneFileHandle = AssetImporter.get(path);
        if(sceneFileHandle.exists()) {
            SavableContainer container;
            if(sceneFileHandle.extension().equals("prefab")) {
                container = new Prefab();
            } else {
                container = new Scene();
            }
            container.path = sceneFileHandle.path();
            container.loadFromPath();
            openSavableContainer(container);
        }

        SceneEditorAddon.get().assetImporter.housekeep(projectPath);
    }

    @Override
    public void act (float delta) {
        if(!(TalosMain.Instance().Project() instanceof SceneEditorProject)) return;
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
        if(currentContainer == null) return;

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

    public void openSavableContainer (SavableContainer mainScene) {
        if(mainScene == null) return;
        sceneEditorAddon.hierarchy.loadEntityContainer(mainScene);
        currentContainer = mainScene;

        // process all game objects
        removeGizmos();
        initGizmos(mainScene);

        clearSelection();

        selectPropertyHolder(mainScene);

        if(mainScene instanceof Scene) {
            bgColor.set(Color.BLACK);
        } else {

            bgColor.set(Color.valueOf("#241a00"));
        }
    }

    private void removeGizmos () {
        gizmoList.clear();
        gizmoMap.clear();
    }

    private void removeGizmos (GameObject gameObject) {
        Array<Gizmo> list = gizmoMap.get(gameObject);
        for(Gizmo gizmo : list) {
            gizmo.notifyRemove();
        }
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

    public void selectGameObjectExternally(GameObject gameObject) {
        selectGameObject(gameObject);
        Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(selection));
    }

    private void selectGameObject (GameObject gameObject) {
        if(gameObject == null) return;
        Array<GameObject> tmp = new Array<>();
        tmp.add(gameObject);

        setSelection(tmp);
    }

    private void removeFromSelection (GameObject gameObject) {
        selection.removeValue(gameObject, true);
    }

    private void addToSelection (GameObject gameObject) {
        if(!selection.contains(gameObject, true)) {
            selection.add(gameObject);
        }
    }

    private void setSelection(Array<GameObject> gameObjects) {
        selection.clear();

        selection.addAll(gameObjects);
    }

    private void selectAll() {
        selection.clear();
        Array<GameObject> gameObjects = currentContainer.getGameObjects();
        if(gameObjects != null) {
            for(int i = 0; i < gameObjects.size; i++) {
                selectGameObjectAndChildren(gameObjects.get(i));
            }
        }
    }

    private void selectGameObjectAndChildren(GameObject gameObject) {
        selection.add(gameObject);

        Array<GameObject> children = gameObject.getGameObjects();

        if(children != null) {
            for(int i = 0; i < children.size; i++) {
                selectGameObjectAndChildren(children.get(i));
            }
        }
    }

    private boolean deselectOthers(GameObject exceptThis) {
        if(selection.size > 1 && selection.contains(exceptThis, true)) {
            selection.clear();
            selection.add(exceptThis);

            return true;
        }

        return false;
    }

    private void clearSelection() {
        selection.clear();
    }

    private void selectByRect(Rectangle rectangle) {
        if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            clearSelection();
        }
        for(int i = 0; i < gizmoList.size; i++) {
            Gizmo gizmo = gizmoList.get(i);
            if(gizmo instanceof TransformGizmo) {
                TransformGizmo transformGizmo = (TransformGizmo) gizmo;
                Vector2 worldPos = transformGizmo.getWorldPos();
                Vector2 local = getLocalFromWorld(worldPos.x, worldPos.y);

                if(rectangle.contains(local)) {
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        addToSelection(gizmo.getGameObject());
                    } else {
                        addToSelection(gizmo.getGameObject());
                    }

                }
            }
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

        if(gameObjects.size == 1) {
            Array<Gizmo> gizmos = gizmoMap.get(gameObjects.get(0));
            for(Gizmo gizmo: gizmos) {
                gizmo.setSelected(true);
            }
        } else {
            for (GameObject gameObject : gameObjects) {
                Array<Gizmo> gizmos = gizmoMap.get(gameObject);
                if(gizmos != null) {
                    for (Gizmo gizmo : gizmos) {
                        if (gizmo.isMultiSelect()) {
                            gizmo.setSelected(true);
                        }
                    }
                }
            }
        }

        // now for properties

        if(selection.size == 0) {
            // we select the main container then
            if(currentContainer instanceof Scene) {
                Scene scene = (Scene) currentContainer;
                selectPropertyHolder(scene);
            }
        } else {
            if(selection.size == 1) {
                selectPropertyHolder(gameObjects.first());
            } else {
                MultiPropertyHolder multiPropertyHolder = new MultiPropertyHolder(gameObjects);
                selectPropertyHolder(multiPropertyHolder);
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

        SavableContainer savableContainer = currentContainer;
        if(savableContainer != null) {
            if (toMemory) {
                snapshotService.saveSnapshot(changeVersion, AssetImporter.relative(savableContainer.path), savableContainer.getAsString());
            } else {
                savableContainer.save();
            }
        }

        return data;
    }

    public void loadFromData (Json json, JsonValue jsonData, boolean fromMemory) {
        read(json, jsonData);

        String path = jsonData.getString("currentScene", "");
        FileHandle sceneFileHandle = AssetImporter.get(path);
        if(sceneFileHandle.exists()) {
            SavableContainer container;
            if(sceneFileHandle.extension().equals("prefab")) {
                container = new Prefab();
            } else {
                container = new Scene();
            }
            container.path = sceneFileHandle.path();
            if(fromMemory) {
                container.load(snapshotService.getSnapshot(changeVersion, AssetImporter.relative(container.path)));
            } else {
                container.loadFromPath();
                snapshotService.saveSnapshot(changeVersion, AssetImporter.relative(container.path), container.getAsString());
            }

            openSavableContainer(container);
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
        return layers;
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

    public void clearMetadata (String assetPath) {
        if(metadataCache.containsKey(assetPath)) {
            metadataCache.remove(assetPath);
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
                //reloadScheduled = 0.5f;
            }
        }
    }

    @EventHandler
    public void onPropertyHolderEdited(PropertyHolderEdited event) {
        IPropertyHolder currentHolder = SceneEditorAddon.get().propertyPanel.getCurrentHolder();
        if(currentHolder != null) {
            if(currentHolder instanceof MultiPropertyHolder) {
                Array<IPropertyHolder> holders = ((MultiPropertyHolder) currentHolder).getHolders();
                boolean setDirty = false;
                for(IPropertyHolder holder: holders) {
                    if(holder instanceof AMetadata) {
                        AssetImporter.saveMetadata((AMetadata) holder);
                    } else {
                        setDirty = true;
                    }
                }
                if(setDirty) {
                    TalosMain.Instance().ProjectController().setDirty();
                }
            } else {
                if (currentHolder instanceof AMetadata) {
                    AssetImporter.saveMetadata((AMetadata) currentHolder);
                } else {
                    TalosMain.Instance().ProjectController().setDirty();
                }
            }
        }
    }

    public void dispose () {
        fileWatching.shutdown();
    }
}
