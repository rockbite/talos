package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.AsyncRoutineNodeWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;

public class MicroNodeView extends Table {


    private final AsyncRoutineNodeWidget asyncRoutineNodeWidget;
    private Image shadow;
    private ProgressWidget progressContainer;
    private Image bg;

    private Label label;

    private ObjectFloatMap<Object> progressMap = new ObjectFloatMap<>();

    public MicroNodeView(AsyncRoutineNodeWidget asyncRoutineNodeWidget) {
        super(SharedResources.skin);

        this.asyncRoutineNodeWidget = asyncRoutineNodeWidget;

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

        label = new LabelWithZoom("1.0", SharedResources.skin);
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void setProgress(Object target, float alpha) {
        progressMap.put(target, alpha);
    }

    public void setLabel(String string) {
        label.setText(string);
    }

    public void clearMap() {
        progressMap.clear();
    }
}