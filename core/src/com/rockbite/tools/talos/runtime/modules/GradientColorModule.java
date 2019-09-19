package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.tools.ColorPoint;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

import java.util.Comparator;

public class GradientColorModule extends Module {

    public static final int ALPHA = 0;
    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue output;

    private Array<ColorPoint> points = new Array();

    private Color tmpColor = new Color();

    Comparator<ColorPoint> comparator = new Comparator<ColorPoint>() {
        @Override
        public int compare(ColorPoint o1, ColorPoint o2) {
            if(o1.pos < o2.pos) return -1;
            if(o1.pos > o2.pos) return 1;

            return 0;
        }
    };

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
        resetPoints();
    }

    @Override
    protected void defineSlots() {
        alpha = createInputSlot(ALPHA);

        output = createOutputSlot(OUTPUT);
    }

    protected void processAlphaDefaults() {
        if(alpha.isEmpty()) {
            // as default we are going to fetch the lifetime or duration depending on context
            float requester = getScope().getFloat(ScopePayload.REQUESTER_ID);
            if(requester < 1) {
                // particle
                alpha.set(getScope().get(ScopePayload.PARTICLE_ALPHA));
                alpha.setEmpty(false);
            } else if(requester > 1) {
                // emitter
                alpha.set(getScope().get(ScopePayload.EMITTER_ALPHA));
                alpha.setEmpty(false);
            } else {
                // whaat?
                alpha.set(0);
            }
        }
    }

    @Override
    public void processValues() {
        processAlphaDefaults();
        interpolate(alpha.getFloat(), output);
    }

    private void interpolate(float alpha, NumericalValue output) {
        Color color = getPosColor(alpha);
        output.set(color.r, color.g, color.b);
    }

    public Array<ColorPoint> getPoints() {
        return points;
    }

    private void resetPoints() {
        // need to guarantee at least one point
        points.clear();
        ColorPoint colorPoint = new ColorPoint();
        colorPoint.pos = 0;
        colorPoint.color.set(255/255f, 68/255f, 26/255f, 1f);
        points.add(colorPoint);
    }


    public ColorPoint createPoint(Color color, float pos) {
        ColorPoint colorPoint = new ColorPoint();
        colorPoint.pos = pos;
        colorPoint.color.set(color);
        points.add(colorPoint);

        points.sort(comparator);

        return colorPoint;
    }

    public void removePoint(int hitIndex) {
        if(points.size <= 1) return;
        points.removeIndex(hitIndex);
    }

    public Color getPosColor(float pos) {

        if(pos <= points.get(0).pos) {
            tmpColor.set(points.get(0).color);
        }

        if(pos >= points.get(points.size-1).pos) {
            tmpColor.set(points.get(points.size-1).color);
        }

        for(int i = 0; i < points.size - 1; i++) {
            if(points.get(i).pos < pos && points.get(i+1).pos > pos) {
                // found it

                if(points.get(i+1).pos == points.get(i).pos) {
                    tmpColor.set(points.get(i).color);
                } else {
                    float localAlpha = (pos - points.get(i).pos) / (points.get(i + 1).pos - points.get(i).pos);
                    tmpColor.r = Interpolation.linear.apply(points.get(i).color.r, points.get(i + 1).color.r, localAlpha);
                    tmpColor.g = Interpolation.linear.apply(points.get(i).color.g, points.get(i + 1).color.g, localAlpha);
                    tmpColor.b = Interpolation.linear.apply(points.get(i).color.b, points.get(i + 1).color.b, localAlpha);
                }
                break;
            }
        }

        return tmpColor;
    }

    public void setPoints(Array<ColorPoint> from) {
        points.clear();
        for(ColorPoint fromPoint: from) {
            ColorPoint point = new ColorPoint(fromPoint.color, fromPoint.pos);
            points.add(point);
        }
    }
}
