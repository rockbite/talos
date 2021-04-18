package com.talosvfx.talos.editor.addons.uieditor.runtime;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;
import com.talosvfx.talos.editor.addons.uieditor.runtime.anchors.Anchor;

public class DynamicGroup extends BackgroundGroup {

    private ObjectMap<Actor, Anchor> anchorMap = new ObjectMap<>();

    public DynamicGroup() {

    }

    @Override
    public void act (float delta) {
        // apply anchor data with outside-in approach
        invalidateHierarchy(this);

        super.act(delta);
    }

    public void invalidateHierarchy(Actor actor) {
        if(actor instanceof Group) {
            SnapshotArray<Actor> children = ((Group)actor).getChildren();
            invalidateActors(children);
        } else {
            invalidate(actor);
        }
    }

    private void invalidateActors(SnapshotArray<Actor> array) {
        Actor[] actors = array.begin();
        for (int i = 0, n = array.size; i < n; i++) {
            invalidate(actors[i]);
            if(actors[i] instanceof Group) {
                invalidateActors(((Group)actors[i]).getChildren());
            }
        }
    }

    private void invalidate(Actor actor) {
        Anchor anchor = anchorMap.get(actor);
        if(anchor == null) return;

        invalidateSize(actor, anchor);
        invalidatePosition(actor, anchor);
    }

    private void invalidateSize (Actor actor, Anchor anchor) {
        actor.setWidth(anchor.computeWidth());
        actor.setHeight(anchor.computeHeight());
    }

    private void invalidatePosition (Actor actor, Anchor anchor) {
        actor.setX(anchor.getX(actor.getWidth()));
        actor.setY(anchor.getY(actor.getHeight()));
    }

    @Override
    public void addActor (Actor actor) {
        super.addActor(actor);

        Anchor anchor = Pools.get(Anchor.class).obtain();
        anchorMap.put(actor, anchor);
    }

    public void addActorTo (Actor actor, Group parent) {
        parent.addActor(actor);

        Anchor anchor = Pools.get(Anchor.class).obtain();
        anchorMap.put(actor, anchor);
    }

    @Override
    public boolean removeActor (Actor actor) {
        boolean wasRemoved = super.removeActor(actor);

        if(wasRemoved) {
            Anchor anchor = anchorMap.get(actor);
            Pools.get(Anchor.class).free(anchor);
        }

        return wasRemoved;
    }

    public Anchor getAnchor (Actor actor) {
        return anchorMap.get(actor);
    }
}
