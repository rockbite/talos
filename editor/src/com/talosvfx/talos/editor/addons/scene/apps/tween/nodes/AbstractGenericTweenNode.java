package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;


public abstract class AbstractGenericTweenNode extends AbstractTweenNode {

    boolean running = false;
    float time = 0;

    @Override
    protected void onSignalReceived(String command, Object[] payload) {
        if(command.equals("execute")) {
            runGenericTween();
        }
    }

    public void runGenericTween() {
        running = true;
        time = 0;
    }


    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        InterpolationTimeline widget = new InterpolationTimeline(getSkin());
        getCustomContainer("timeline").add(widget).growX().height(58);
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
            sendSignal("onComplete", "execute", null);
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            if(running) {
                float duration = (float) getWidget("duration").getValue();
                time += delta;

                if(time >= duration) {
                    time = duration;
                    running = false;
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            fireOnComplete();
                        }
                    });
                }

                alpha = time/duration;
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
}
