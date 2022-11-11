/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.values.ColorPoint;
import com.talosvfx.talos.runtime.values.NumericalValue;

import java.util.Comparator;

public class GradientColorModule extends AbstractModule {

	public static final int ALPHA = 0;
	public static final int OUTPUT = 0;

	NumericalValue alpha;
	NumericalValue output;

	private Array<ColorPoint> points;

	private Color tmpColor = new Color();

	Comparator<ColorPoint> comparator = new Comparator<ColorPoint>() {
		@Override
		public int compare (ColorPoint o1, ColorPoint o2) {
			if (o1.pos < o2.pos)
				return -1;
			if (o1.pos > o2.pos)
				return 1;

			return 0;
		}
	};

	@Override
	public void init () {
		super.init();
		resetPoints();
	}

	@Override
	protected void defineSlots () {
		alpha = createAlphaInputSlot(ALPHA);

		output = createOutputSlot(OUTPUT);
	}

	@Override
	public void processCustomValues () {
		interpolate(alpha.getFloat(), output);
	}

	private void interpolate (float alpha, NumericalValue output) {
		Color color = getPosColor(alpha);
		output.set(color.r, color.g, color.b, 1f);
	}

	public Array<ColorPoint> getPoints () {
		return points;
	}

	private void resetPoints () {
		// need to guarantee at least one point
		points = new Array<>();
		ColorPoint colorPoint = new ColorPoint();
		colorPoint.pos = 0;
		colorPoint.color.set(255 / 255f, 68 / 255f, 26 / 255f, 1f);
		points.add(colorPoint);
	}

	public ColorPoint createPoint (Color color, float pos) {
		ColorPoint colorPoint = new ColorPoint();
		colorPoint.pos = pos;
		colorPoint.color.set(color);
		points.add(colorPoint);

		points.sort(comparator);

		return colorPoint;
	}

	public void removePoint (int hitIndex) {
		if (points.size <= 1)
			return;
		points.removeIndex(hitIndex);
	}

	public Color getPosColor (float pos) {

		if (pos <= points.get(0).pos) {
			tmpColor.set(points.get(0).color);
		}

		if (pos >= points.get(points.size - 1).pos) {
			tmpColor.set(points.get(points.size - 1).color);
		}

		for (int i = 0; i < points.size - 1; i++) {
			if (points.get(i).pos < pos && points.get(i + 1).pos > pos) {
				// found it

				if (points.get(i + 1).pos == points.get(i).pos) {
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

	public void setPoints (Array<ColorPoint> from) {
		points.clear();
		for (ColorPoint fromPoint : from) {
			ColorPoint point = new ColorPoint(fromPoint.color, fromPoint.pos);
			points.add(point);
		}
	}

	@Override
	public void write (Json json) {
		super.write(json);
		Array<ColorPoint> points = getPoints();
		json.writeArrayStart("points");
		for (ColorPoint point : points) {
            json.writeObjectStart();
            json.writeValue("r", point.color.r);
            json.writeValue("g", point.color.g);
            json.writeValue("b", point.color.b);
            json.writeValue("pos", point.pos);
            json.writeObjectEnd();
		}
		json.writeArrayEnd();
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
        points.clear();
        final JsonValue jsonPpoints = jsonData.get("points");
        for (JsonValue point : jsonPpoints) {
            createPoint(new Color(point.getFloat("r"), point.getFloat("g"), point.getFloat("b"), 1f), point.getFloat("pos"));
        }
    }
}
