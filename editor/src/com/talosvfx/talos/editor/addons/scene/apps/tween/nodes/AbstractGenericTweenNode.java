package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorProject;
import com.talosvfx.talos.editor.addons.scene.apps.tween.TweenStage;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;


public abstract class AbstractGenericTweenNode extends AbstractTweenNode {

    boolean running = false;
    float time = 0;
    float duration;

    private Vector2 vec = new Vector2();
    private Vector2 vec2 = new Vector2();

    private MicroNodeView microNodeView;

    private boolean isMicroView = false;

    private ObjectMap<String, Object> payload;

    @Override
    protected void onSignalReceived(String command, ObjectMap<String, Object> payload) {
        if(command.equals("execute")) {
            runGenericTween(payload);
        }
    }

    public void runGenericTween(ObjectMap<String, Object> payload) {
        this.payload = payload;
        String targetString = (String)payload.get("target");
        GameObject gameObject = fetchGameObject(targetString);

        running = true;
        time = 0;

        duration = getWidgetFloatValue("duration");

        startTween(gameObject);

        if(isMicroView) {
            microNodeView.showProgressDisc();
        }
    }

    private GameObject fetchGameObject(String targetString) {

        SceneEditorAddon sceneEditorAddon = ((SceneEditorProject) TalosMain.Instance().ProjectController().getProject()).sceneEditorAddon;
        GameObject root = sceneEditorAddon.workspace.getCurrentContainer().root;

        return findGameObject(root, targetString);
    }

    private GameObject findGameObject(GameObject parent, String targetString) {

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
            if(gameObject.getName().equals(levelName)) {
                if(lastPart.length() == 0) {
                    return gameObject;
                } else {
                    return findGameObject(gameObject, lastPart);
                }
            }
        }

        return null;
    }

    protected void startTween(GameObject target) {

    }

    @Override
    public void notifyRemoved() {
        microNodeView.remove();
    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        InterpolationTimeline widget = new InterpolationTimeline(getSkin());
        getCustomContainer("timeline").add(widget).growX().height(58);

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
                nodeBoard.selectNode(AbstractGenericTweenNode.this);
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

        private Image tracker;

        private Image bgFill;
        private Image line;

        private float alpha = 0;

        public InterpolationTimeline(Skin skin) {
            super(skin);
            setBackground(getSkin().getDrawable("timelinebg"));

            tracker = new Image(getSkin().getDrawable("time-selector-green"));
            bgFill = new Image(getSkin().getDrawable("white"));
            line = new Image(getSkin().getDrawable("white"));

            line.setColor(Color.valueOf("#3e7561"));
            bgFill.setColor(Color.valueOf("#37574a"));

            addActor(bgFill);
            addActor(line);
            addActor(tracker);
        }

        protected void fireOnComplete() {
            boolean sent = sendSignal("onComplete", "execute", payload);

            ((TweenStage)nodeBoard.getNodeStage()).nodeReportedComplete();

            if(isMicroView) {
                microNodeView.hideProgressDisc();
            }
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            if(running) {
                time += delta;

                if(time >= duration) {
                    time = duration;
                    running = false;
                    fireOnComplete();
                }

                alpha = time/duration;

                //todo: add interpolation of alpha here

                tick(alpha);

                if(isMicroView) {
                    microNodeView.setProgress(alpha);
                    float val = time;
                    if(val == 0) val =  duration;
                    microNodeView.setLabel(((int)(val * 100))/100f + "");
                }

            }

            tracker.setY(getHeight() - 15);
            tracker.setX(alpha * (getWidth()-1) - 4);

            line.setX(tracker.getX() + 4);
            line.setSize(1, getHeight());

            bgFill.setPosition(1, 1);

            if(tracker.getX() + 4 > 0) {
                float width = tracker.getX() + 4;
                if(width > getWidth() - 2) width = getWidth() - 2;
                bgFill.setSize(width, getHeight() - 2);
                bgFill.setVisible(true);
            } else {
                bgFill.setVisible(false);
            }

            if(alpha == 0 || alpha == 1) {
                line.setVisible(false);
            } else {
                line.setVisible(true);
            }
        }

        public void setTimeValue(float alpha) {
            this.alpha = alpha;
        }

        @Override
        public float getPrefHeight() {
            return 58;
        }
    }

    protected void tick(float alpha) {

    }

    class ProgressWidget extends Table {

        private final TextureAtlas.AtlasRegion region;
        private final Image progressImage;
        private float progress = 0;

        private ShaderProgram shaderProgram;

        public ProgressWidget() {
            super(TalosMain.Instance().getSkin());

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

            if(progress > 0 && progress < 1) {
                // enable shader
                prevShader = batch.getShader();

                batch.setShader(shaderProgram);
                shaderProgram.setUniformf("regionU", region.getU());
                shaderProgram.setUniformf("regionV", region.getV());
                shaderProgram.setUniformf("regionU2", region.getU2());
                shaderProgram.setUniformf("regionV2", region.getV2());

                shaderProgram.setUniformf("alpha", progress);
            }

            super.draw(batch, parentAlpha);

            if(prevShader != null) {
                // change shader back
                batch.setShader(prevShader);
            }
        }

        public void setProgress(float progress) {
            this.progress = progress;

            progressImage.setRotation(180 - progress * 90);
        }
    }

    class MicroNodeView extends Table {


        private Image shadow;
        private ProgressWidget progressContainer;
        private Image bg;

        private Label label;

        private float progress = 0;

        public MicroNodeView() {
            super(TalosMain.Instance().getSkin());

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

            label = new Label("1.0", TalosMain.Instance().getSkin());
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

            progressContainer.setProgress(progress);
        }

        public void setProgress(float alpha) {
            progress = alpha;
        }

        public void setLabel(String string) {
            label.setText(string);
        }
    }
}

