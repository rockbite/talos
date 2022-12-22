package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.lang.reflect.Field;


public abstract class AbstractGenericRoutineNode extends AbstractRoutineNode {

    boolean running = false;

    private Vector2 vec = new Vector2();
    private Vector2 vec2 = new Vector2();

    private MicroNodeView microNodeView;

    private boolean isMicroView = false;

    private ObjectMap<String, Object> payload;

    private Array<String> removeList = new Array<>();
    protected ObjectMap<String, GenericTweenData> dataMap = new ObjectMap<>();
    private boolean minimized = false;
    private SelectBox interpolationSelectBox;

    protected class GenericTweenData {
        public float chunkIndex;
        public GameObject target;
        public float alpha;
        public float duration;
        public boolean yoyo = false;

        public Interpolation interpolation = Interpolation.linear;
        public int direction = 1;
        public boolean complete = false;
        public ObjectMap<String, Object> misc;
        public Array<GameObject> neighbours = new Array<>();
    }

    @Override
    public void reset() {
        running = false;
        dataMap.clear();
        removeList.clear();

        microNodeView.progressMap.clear();
    }

    @Override
    protected void onSignalReceived(String command, ObjectMap<String, Object> payload) {
        if(command.equals("execute")) {
            runGenericTween(payload);
        }
    }

    protected ObjectMap<String, Object> buildParams(GameObject targetObject) {
        String exactString = getGameObjectPath(targetObject);
        return buildParams(exactString);
    }

    protected ObjectMap<String, Object> buildParams(String target) {
        params.clear();
        params.put("targetGO", dataMap.get(target).target);
        params.put("target", dataMap.get(target));
        params.put("chunkIndex", dataMap.get(target).chunkIndex);
        params.put("neighbours", dataMap.get(target).neighbours);

        return params;
    }

    public void runGenericTween(ObjectMap<String, Object> payload) {
        this.payload = payload;
        String targetString = (String)payload.get("target");
        Array<GameObject> list = new Array<>();
        targetString = targetString.replaceAll(" ", "");
        String[] split = targetString.split(",");
        for(String row: split) {
            fetchGameObjects(list, row);
        }

        running = true;

        for(GameObject target: list) {
            GenericTweenData data = new GenericTweenData();
            String exactString = getGameObjectPath(target);
            dataMap.put(exactString, data);
        }

        int iter = 0;
        for(GameObject target: list) {
            String targetPath = getGameObjectPath(target);
            GenericTweenData data = dataMap.get(targetPath);
            data.alpha = 0;
            data.target = target;

            if(list.size > 1) {
                data.chunkIndex = (iter++)/(float)dataMap.size;
                data.neighbours.clear();
                data.neighbours.addAll(list);
            } else {
                if (payload.containsKey("chunkIndex")) {
                    data.chunkIndex = (float) payload.get("chunkIndex");
                    data.neighbours = (Array<GameObject>) payload.get("neighbours");
                } else {
                    data.chunkIndex = 0f;
                    data.neighbours.clear();
                }
            }

            data.duration = getWidgetFloatValue("duration", buildParams(targetPath));
            data.yoyo = getWidgetBooleanValue("yoyo");
            data.interpolation = getSelectedInterpolation();
            startTween(target, data);
        }

        if(isMicroView) {
            microNodeView.showProgressDisc();
        }
    }

    private Interpolation getSelectedInterpolation() {
        String name = (String) interpolationSelectBox.getSelection().first();

        Field[] declaredFields = Interpolation.class.getDeclaredFields();
        for(Field field: declaredFields) {
            if(field.getName().equals(name)) {
                try {
                    return (Interpolation) field.get(null);
                } catch (Exception e) {
                    return Interpolation.linear;
                }
            }
        }

        return Interpolation.linear;
    }


    private String getGameObjectPath(GameObject gameObject, String path) {
        if(path.isEmpty()) {
            path = gameObject.getName();
        } else {
            path = gameObject.getName() + "." + path;
        }
        if(gameObject.parent != null) {
            path = getGameObjectPath(gameObject.parent, path);
        }

        return path;
    }

    private String getGameObjectPath(GameObject gameObject) {
        String path = getGameObjectPath(gameObject, "");

        // remove last child
        path = path.substring(path.indexOf(".") + 1);

        return path;
    }

    private void fetchGameObjects(Array<GameObject> list, String targetString) {
//        SceneEditorAddon sceneEditorAddon = ((SceneEditorProject) TalosMain.Instance().ProjectController().getProject()).sceneEditorAddon;
//        RoutineEditor routineEditor = sceneEditorAddon.routineEditor;
//
//        GameObject root = routineEditor.scenePreviewStage.currentScene.root;
//
//        findGameObjects(list, root, targetString);
    }

    private void findGameObjects(Array<GameObject> list, GameObject parent, String targetString) {

        int dotIndex = targetString.indexOf(".");
        String lastPart = "";

        String levelName = targetString;
        if(dotIndex >= 0) {
            levelName = targetString.substring(0, targetString.indexOf("."));

            if(targetString.length() > dotIndex + 1) {
                lastPart = targetString.substring(targetString.indexOf(".") + 1);
            } else {
                //irrelevant dot
                lastPart = "";
            }
        }

        Array<GameObject> gameObjects = parent.getGameObjects();

        for(GameObject gameObject : gameObjects) {

            boolean matchCriteria = false;
            if(levelName.contains("*")) {
                String expression = levelName.replaceAll("\\*", ".*");
                matchCriteria = gameObject.getName().matches(expression);
            } else {
                matchCriteria = gameObject.getName().equals(levelName);
            }

            if(matchCriteria) {
                if(lastPart.length() == 0) {
                    list.add(gameObject);
                } else {
                    findGameObjects(list, gameObject, lastPart);
                }
            }
        }
    }

    protected void startTween(GameObject target, GenericTweenData data) {

    }

    @Override
    public void notifyRemoved() {
        microNodeView.remove();
    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        InterpolationTimeline widget = new InterpolationTimeline(getSkin());
        Table timeline = getCustomContainer("timeline");

        Field[] declaredFields = Interpolation.class.getDeclaredFields();
        Array<String> interpolationList = new Array<>();
        for(Field field: declaredFields) {
            if(field.getType().isAssignableFrom(Interpolation.class)) {
                interpolationList.add(field.getName());
            }
        }

        interpolationSelectBox = new SelectBox(getSkin(), "propertyValue");
        interpolationSelectBox.setItems(interpolationList);
        timeline.add(interpolationSelectBox).growX().row();


        timeline.add(widget).growX().height(58).row();

        microNodeView = new MicroNodeView();
        microNodeView.setTouchable(Touchable.enabled);

        microNodeView.addListener(new InputListener() {

            long clickTime = 0;

            Vector2 prevPos = new Vector2();
            Vector2 tmp = new Vector2();
            Vector2 tmp2 = new Vector2();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                long now = TimeUtils.millis();

                if(now - clickTime < 200 && button == 0) {
                    doubleClick();
                }
                clickTime = now;

                tmp.set(x, y);
                event.getTarget().localToStageCoordinates(tmp);
                prevPos.set(tmp);

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                tmp.set(x, y);
                event.getTarget().localToStageCoordinates(tmp);
                tmp2.set(tmp);
                tmp.sub(prevPos);
                prevPos.set(tmp2);

                setPosition(getX() + tmp.x, getY() + tmp.y);
                microNodeView.setPosition(microNodeView.getX() + tmp.x, microNodeView.getY() + tmp.y);

            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                nodeBoard.selectNode(AbstractGenericRoutineNode.this);
                event.cancel();
                super.touchUp(event, x, y, pointer, button);
            }

            private void doubleClick() {
                animateShow();
            }
        });

        headerTable.setTouchable(Touchable.enabled);
        headerTable.addListener(new ClickListener() {

            long clickTime = 0;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                long now = TimeUtils.millis();
                
                if(now - clickTime < 200 && button == 0) {
                    doubleClick();
                }
                clickTime = now;

                event.handle();
                return false;
            }

            private void doubleClick() {
                animateHide();
            }
        });
    }

    @Override
    public void getOutputSlotPos(String id, Vector2 tmp) {
        super.getOutputSlotPos(id, tmp);

        vec.set(getWidth()/2f, getHeight()/2f);
        localToStageCoordinates(vec);

        vec2.set(tmp).sub(vec).scl(getColor().a);
        vec2.add(vec);

        tmp.set(vec2);
    }

    @Override
    public void getInputSlotPos(String id, Vector2 tmp) {
        super.getInputSlotPos(id, tmp);

        vec.set(getWidth()/2f, getHeight()/2f);
        localToStageCoordinates(vec);

        vec2.set(tmp).sub(vec).scl(getColor().a);
        vec2.add(vec);

        tmp.set(vec2);
    }

    public void animateShow() {

        minimized = false;

        setTransform(true);
        setVisible(true);
        clearActions();
        setScale(0f);
        getColor().a = 0;

        setOrigin(Align.center);

        addAction(Actions.fadeIn(0.2f));

        vec.set(getWidth()/2f, getHeight()/2f);
        localToStageCoordinates(vec);

        // hide disk
        microNodeView.hide();
        isMicroView = false;

        addAction(Actions.sequence(
                Actions.scaleTo(1f, 1f, 0.22f, Interpolation.swingOut),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        setTransform(false);
                        setVisible(true);
                    }
                })
        ));

    }

    public void animateHide() {

        minimized = true;

        setTransform(true);
        setVisible(true);
        clearActions();
        setScale(1f);
        getColor().a = 1;

        setOrigin(Align.center);

        addAction(Actions.fadeOut(0.1f));

        vec.set(getWidth()/2f, getHeight()/2f);
        localToStageCoordinates(vec);

        // show disk
        microNodeView.setVisible(false);
        nodeBoard.getStage().addActor(microNodeView);
        microNodeView.setPosition(vec.x, vec.y);
        microNodeView.show();

        microNodeView.setLabel(((int)(getWidgetFloatValue("duration")*100f)/100f) + "");
        isMicroView = true;

        addAction(Actions.sequence(
                Actions.scaleTo(0.8f, 0.8f, 0.22f, Interpolation.swingIn),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        setTransform(false);
                        setVisible(false);
                    }
                })
        ));

    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    public void setMini() {
        setVisible(false);
        setTransform(false);
        clearActions();
        getColor().a = 0;
        setOrigin(Align.center);

        vec.set(getWidth()/2f, getHeight()/2f);
        localToStageCoordinates(vec);

        // show disk
        microNodeView.setVisible(false);
        nodeBoard.getStage().addActor(microNodeView);
        microNodeView.setPosition(vec.x, vec.y);
        microNodeView.show();
        isMicroView = true;
    }

    public boolean isRunning() {
        return running;
    }

    class InterpolationTimeline extends Table {

        private Group container = new Group();
        private ObjectMap<String, Image> progressMap = new ObjectMap<>();

        public InterpolationTimeline(Skin skin) {
            super(skin);
            setBackground(getSkin().getDrawable("timelinebg"));



            addActor(container);
        }

        protected void fireOnComplete(String target) {
            payload.put("target", target);
            payload.put("targetGO", dataMap.get(target).target);
            payload.put("chunkIndex", dataMap.get(target).chunkIndex);
            payload.put("neighbours", dataMap.get(target).neighbours);

            boolean sent = sendSignal("onComplete", "execute", payload);
        }

        public void actTarget(String target, GenericTweenData data, float delta) {
            data.alpha += data.direction * delta/data.duration;

            boolean complete = false;

            if(data.yoyo) {
                if(data.direction == 1 && data.alpha >= 1) {
                    data.alpha = 0.99f;
                    data.direction = -1;
                } else if(data.direction == -1 && data.alpha <= 0) {
                    data.alpha = 0;
                    complete = true;
                }
            } else {
                if(data.direction == 1 && data.alpha >= 1) {
                    data.alpha = 1;
                    complete = true;
                }
            }

            //todo: add interpolation of alpha here

            tick(target, data, data.alpha);

            if(complete) {
                data.complete = true;
                fireOnComplete(target);
            }

            if(isMicroView) {
                microNodeView.setProgress(target, data.alpha);
                microNodeView.setLabel(((int)(data.duration * 100))/100f + "");
            }
        }

        private void positionTargetProgress(String target) {
            if(!progressMap.containsKey(target)) {
                Image image = new Image(getSkin().getDrawable("white"));
                image.setColor(Color.valueOf("#37574a"));
                progressMap.put(target, image);
                container.addActor(image);
            }

            Image image = progressMap.get(target);

            image.getColor().a = 0.4f;

            float alpha = dataMap.get(target).alpha;

            image.setPosition(1, 1);

            if(alpha * (getWidth() - 1) > 0) {
                float width = alpha * (getWidth() - 1);
                if(width > getWidth() - 2) width = getWidth() - 2;
                image.setSize(width, getHeight() - 2);
                image.setVisible(true);
            } else {
                image.setVisible(false);
            }

            if(alpha == 0 || alpha == 1) {
                image.setVisible(false);
            } else {
                image.setVisible(true);
            }
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            if(running) {
                for(String target: dataMap.keys()) {
                    actTarget(target, dataMap.get(target), getDelta());
                    positionTargetProgress(target);
                }

                running = false;
                removeList.clear();
                for(String item: dataMap.keys()) {
                    if(!dataMap.get(item).complete) {
                        running = true;
                    } else {
                        removeList.add(item);
                    }
                }

                for(String item: removeList) {
                    dataMap.remove(item);
                }

                if(!running) {
                    ((RoutineStage) nodeBoard.getNodeStage()).nodeReportedComplete();

                    if(isMicroView) {
                        microNodeView.hideProgressDisc();
                    }
                }
            }
        }

        public void setTimeValue(float alpha) {
            //this.alpha = alpha;
        }

        @Override
        public float getPrefHeight() {
            return 58;
        }
    }

    protected void tick(String target, GenericTweenData data, float alpha) {

    }

    class ProgressWidget extends Table {

        private final TextureAtlas.AtlasRegion region;
        private final Image progressImage;

        private ObjectFloatMap<String> progressMap = new ObjectFloatMap<>();

        private ShaderProgram shaderProgram;

        public ProgressWidget() {
            super(SharedResources.skin);

            region = getSkin().getAtlas().findRegion("mini-node-bg");

            progressImage = new Image(ColorLibrary.obtainBackground(getSkin(), "mini-node-bg", ColorLibrary.BackgroundColor.GREEN));
            progressImage.setSize(110, 110);
            progressImage.setPosition(-progressImage.getWidth()/2, -progressImage.getHeight()/2);
            progressImage.setRotation(180);
            progressImage.setOrigin(Align.center);
            addActor(progressImage);

            shaderProgram = new ShaderProgram(Gdx.files.internal("addons/scene/shaders/default.vert.glsl"), Gdx.files.internal("addons/scene/shaders/circularbar.frag.glsl"));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {

            ShaderProgram prevShader = null;

            if(progressMap.size > 0) {
                // enable shader
                prevShader = batch.getShader();

                batch.setShader(shaderProgram);
                shaderProgram.setUniformf("regionU", region.getU());
                shaderProgram.setUniformf("regionV", region.getV());
                shaderProgram.setUniformf("regionU2", region.getU2());
                shaderProgram.setUniformf("regionV2", region.getV2());

                int i = 0;
                for(ObjectFloatMap.Entry<String> entry : progressMap) {
                    float progress = entry.value;
                    shaderProgram.setUniformf("alpha[" + (i++) + "]", progress);
                }
                shaderProgram.setUniformi("alphaCount", progressMap.size);
            }

            super.draw(batch, parentAlpha);

            if(prevShader != null) {
                // change shader back
                batch.setShader(prevShader);
            }
        }

        public void setProgress(ObjectFloatMap<String> progressMap) {
            this.progressMap = progressMap;

            float min = 1;
            for(ObjectFloatMap.Entry<String> entry : progressMap) {
                float progress = entry.value;
                if(min > progress) min = progress;
            }

            progressImage.setRotation(180 - min * 90);
        }
    }

    class MicroNodeView extends Table {


        private Image shadow;
        private ProgressWidget progressContainer;
        private Image bg;

        private Label label;

        private ObjectFloatMap<String> progressMap = new ObjectFloatMap<>();

        public MicroNodeView() {
            super(SharedResources.skin);

            shadow = new Image(ColorLibrary.obtainBackground(getSkin(), "mini-node-bg-shadow", ColorLibrary.BackgroundColor.DARK_GRAY));
            shadow.getColor().a = 0.4f;

            progressContainer = new ProgressWidget();
            progressContainer.setVisible(false);

            bg = new Image(ColorLibrary.obtainBackground(getSkin(), "mini-node-bg", ColorLibrary.BackgroundColor.DARK_GRAY));
            bg.getColor().a = 1f;

            addActor(shadow);
            addActor(progressContainer);
            addActor(bg);

            shadow.setOrigin(Align.center);
            shadow.setPosition(-shadow.getWidth()/2, -shadow.getHeight()/2);
            bg.setPosition(-bg.getWidth()/2, -bg.getHeight()/2);

            label = new Label("1.0", SharedResources.skin);
            add(label).expand().center();
        }

        public void showProgressDisc() {
            progressContainer.setTransform(true);
            progressContainer.setVisible(true);
            progressContainer.clearActions();
            progressContainer.setScale(0.8f);
            progressContainer.getColor().a = 0;

            progressContainer.addAction(Actions.fadeIn(0.2f));

            progressContainer.addAction(Actions.sequence(
                    Actions.scaleTo(1.1f, 1.1f, 0.18f, Interpolation.pow2Out),
                    Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2In),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            progressContainer.setTransform(false);
                        }
                    })
            ));
        }

        public void show() {
            setTransform(true);
            setVisible(true);
            getColor().a = 0;
            setScale(0);
            clearActions();

            addAction(Actions.fadeIn(0.2f));

            shadow.clearActions();
            shadow.addAction(Actions.alpha(0.4f, 0.1f));
            shadow.setScale(1);

            addAction(Actions.sequence(
                    Actions.scaleTo(1.2f, 1.2f, 0.18f, Interpolation.pow2Out),
                    Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2In),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            setTransform(false);
                        }
                    })
            ));
        }

        public void hide() {
            setTransform(true);
            setVisible(true);
            clearActions();

            shadow.clearActions();
            shadow.addAction(Actions.sequence(
                    Actions.scaleTo(1.1f, 1.1f, 0.18f, Interpolation.pow2Out),
                    Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out)
            ));
            shadow.addAction(Actions.sequence(
                    Actions.delay(0.18f),
                    Actions.fadeOut(0.1f, Interpolation.pow2Out)
            ));

            addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.fadeOut(0.05f),
                        Actions.scaleTo(0.2f, 0.2f, 0.05f, Interpolation.pow2In)
                    ),

                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            setTransform(false);
                            setVisible(false);
                            remove();
                        }
                    })
            ));
        }

        public void hideProgressDisc() {
            progressContainer.setTransform(true);
            progressContainer.setVisible(true);
            progressContainer.clearActions();
            progressContainer.setScale(1f);
            progressContainer.getColor().a = 1;

            progressContainer.addAction(Actions.sequence(
                    Actions.scaleTo(1.2f, 1.2f, 0.18f, Interpolation.pow2Out),
                    Actions.scaleTo(0.8f, 0.8f, 0.1f, Interpolation.pow2In),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            progressContainer.setTransform(false);
                            progressContainer.setVisible(false);
                        }
                    })
            ));
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            progressContainer.setProgress(progressMap);
        }

        public void setProgress(String target, float alpha) {
            progressMap.put(target, alpha);
        }

        public void setLabel(String string) {
            label.setText(string);
        }
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        super.read(json, jsonValue);

        minimized = jsonValue.getBoolean("minimized", false);

        if(minimized) {
            setMini();
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);

        json.writeValue("minimized", minimized);
    }
}

