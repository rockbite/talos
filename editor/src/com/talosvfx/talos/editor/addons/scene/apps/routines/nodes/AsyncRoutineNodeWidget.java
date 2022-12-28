package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc.InterpolationTimeline;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc.MicroNodeView;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.AsyncRoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import lombok.Getter;

import java.lang.reflect.Field;

public class AsyncRoutineNodeWidget extends AbstractRoutineNodeWidget {

    @Getter
    private MicroNodeView microNodeView;

    private boolean isMicroView = false;

    private boolean minimized = false;

    private Vector2 vec = new Vector2();
    private Vector2 vec2 = new Vector2();

    private SelectBox interpolationSelectBox;
    private InterpolationTimeline timelineWidget;

    private boolean runningFlag = false;

    public AsyncRoutineNodeWidget() {

    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        timelineWidget = new InterpolationTimeline(this, getSkin());
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


        timeline.add(timelineWidget).growX().height(58).row();

        microNodeView = new MicroNodeView(this);
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
                nodeBoard.selectNode(AsyncRoutineNodeWidget.this);
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

    @Override
    public void notifyRemoved() {
        microNodeView.remove();
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        AsyncRoutineNode<?, ?> node = getNodeInstance();
        if(node != null) {
            Array<? extends AsyncRoutineNodeState<?>> states = node.getStates();

            if (states.size > 0) {
                if(!runningFlag) {
                    runningFlag = true;
                    onRunStart();
                }
                for (AsyncRoutineNodeState<?> state : states) {
                    Object target = state.getTarget();
                    timelineWidget.setProgress(target, state.alpha);
                    microNodeView.setProgress(target, state.alpha);
                }
            } else {
                if(runningFlag) {
                    runningFlag = false;
                    onRunStop();
                }
            }
        }
    }

    private void onRunStart() {
        if(isMicroView) {
            microNodeView.showProgressDisc();
        }
    }

    private void onRunStop() {
        if(isMicroView) {
            microNodeView.hideProgressDisc();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }
}

