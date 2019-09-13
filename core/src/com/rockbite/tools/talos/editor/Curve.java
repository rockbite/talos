package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.math.Vector2;

public class Curve {

    private Vector2 from = new Vector2();
    private Vector2 to = new Vector2();

    boolean reverse = false;

    public Curve(float x, float y, float toX, float toY, boolean reverse) {
        this.reverse = reverse;
        if(reverse) {
            to.set(x, y);
            from.set(toX, toY);
        } else {
            from.set(x, y);
            to.set(toX, toY);
        }
    }

    public void setTo(float toX, float toY) {
        if(reverse) {
            from.set(toX, toY);
        } else {
            to.set(toX, toY);
        }
    }

    public void setFrom(float toX, float toY) {
        if(reverse) {
            to.set(toX, toY);
        } else {
            from.set(toX, toY);
        }
    }

    public Vector2 getFrom() {
        return from;
    }

    public Vector2 getTo() {
        return to;
    }
}
