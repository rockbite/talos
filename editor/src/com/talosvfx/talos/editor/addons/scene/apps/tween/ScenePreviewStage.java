package com.talosvfx.talos.editor.addons.scene.apps.tween;


import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;

import com.talosvfx.talos.editor.addons.scene.events.TweenFinishedEvent;
import com.talosvfx.talos.editor.addons.scene.events.TweenPlayedEvent;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;

import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import java.awt.*;

public class ScenePreviewStage extends ViewportWidget implements Observer {

    public Scene currentScene;

    private MainRenderer renderer;

    private boolean isPlaying = false;

    public ScenePreviewStage () {
        setSkin(SharedResources.skin);
        setWorldSize(10);
        renderer = new MainRenderer();
        addActor(rulerRenderer);
        Notifications.registerObserver(this);
        updateWorkspaceState(false);
    }

    @Override
    public void drawContent (PolygonBatch batch, float parentAlpha) {
        batch.end();

        gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
        ((DynamicGridPropertyProvider) gridPropertyProvider).distanceThatLinesShouldBe = pixelToWorld(150);

        gridPropertyProvider.update(camera, parentAlpha);
        gridRenderer.drawGrid(batch, shapeRenderer);
        renderer.setRenderParentTiles(false);
        batch.begin();

        renderer.skipUpdates = !isPlaying;
        renderer.setCamera(camera);
        drawMainRenderer(batch, parentAlpha);
        renderer.skipUpdates = !isPlaying;
    }

    @Override
    protected boolean canMoveAround () {
        return isInViewPort || isDragging;
    }

    private void drawMainRenderer (PolygonBatch batch, float parentAlpha) {
        if (currentScene == null)
            return;

        renderer.update(currentScene.getSelfObject());
        renderer.render(batch, new MainRenderer.RenderState(), currentScene.getSelfObject());
    }

    @Override
    public void initializeGridPropertyProvider () {
        gridPropertyProvider = new DynamicGridPropertyProvider();
    }

    @EventHandler
    public void onTweenPlay (TweenPlayedEvent event) {
        updateWorkspaceState(true);
        isPlaying = true;
    }

    @EventHandler
    public void onTweenFinish (TweenFinishedEvent event) {
        updateWorkspaceState(false);
        isPlaying = false;
    }

    public void updateWorkspaceState (boolean copy) {
        SavableContainer currentContainer = SceneEditorWorkspace.getInstance().getCurrentContainer();

        if (copy) {
            Scene scene = new Scene();
            scene.load(currentContainer.getAsString());
            currentScene = scene;
        } else {
            currentScene = ((Scene) currentContainer);
        }
    }
}
