package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.HierarchyWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.TemplateListPopup;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.GizmoRegister;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class SceneEditorWorkspace extends ViewportWidget implements Json.Serializable {

    private static SceneEditorWorkspace instance;
    private final TemplateListPopup templateListPopup;

    private SceneEditorAddon sceneEditorAddon;
    private String projectPath;

    private Array<Scene> scenes = new Array<>();

    private GameObjectContainer currentContainer;
    private Array<Gizmo> gizmoList = new Array<>();

    public SceneEditorWorkspace() {
        setSkin(TalosMain.Instance().getSkin());
        setWorldSize(10);

        GizmoRegister.init();

        FileHandle list = Gdx.files.internal("addons/scene/go-templates.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        templateListPopup = new TemplateListPopup(root);
        templateListPopup.setListener(new TemplateListPopup.ListListener() {
            @Override
            public void chosen (XmlReader.Element template, float x, float y) {
                GameObject gameObject = new GameObject();
                String nameTemplate = template.getAttribute("nameTemplate", "gameObject");
                String name = getUniqueGOName(nameTemplate);
                gameObject.setName(name);
                initComponentsFromTemplate(gameObject, template);

                currentContainer.addGameObject(gameObject);
                initGizmos(gameObject);

                //todo: change this to events
                sceneEditorAddon.hierarchy.loadEntityContainer(currentContainer);
            }
        });

        initListeners();
    }

    private String getUniqueGOName (String nameTemplate) {
        int number = 0;
        String name = nameTemplate + number;

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

    protected void initListeners () {
        addListener(new InputListener() {

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {

                if (button == 1 && !event.isCancelled()) {
                    final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                    (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);

                    Vector2 location = new Vector2(vec);
                    Vector2 createLocation = new Vector2(vec);
                    templateListPopup.showPopup(getStage(), location, createLocation);
                }
            }

            @Override
            public boolean keyDown (InputEvent event, int keycode) {
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
    }

    private void removeGizmos () {
        gizmoList.clear();
    }

    private void initGizmos (GameObject gameObject) {
        makeGizmosFor(gameObject);
    }

    private void initGizmos (GameObjectContainer gameObjectContainer) {
        Array<GameObject> gameObjects = gameObjectContainer.getGameObjects();
        if(gameObjects != null) {
            for (GameObject gameObject : gameObjects) {
                makeGizmosFor(gameObject);
            }
        }
    }

    private void makeGizmosFor (GameObject gameObject) {
        Iterable<IComponent> components = gameObject.getComponents();
        for(IComponent component: components) {
            Gizmo gizmo = GizmoRegister.makeGizmoFor(component);
            gizmo.setGameObject(gameObject);
            if(gizmo != null) {
                gizmoList.add(gizmo);
            }
        }
    }

    public void selectPropertyHolder (IPropertyHolder propertyHolder) {
        if(propertyHolder == null) return;
        sceneEditorAddon.propertyPanel.showPanel(propertyHolder.getPropertyProviders());
    }
}
