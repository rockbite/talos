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

package com.talosvfx.talos.runtime.values;

import com.badlogic.gdx.math.MathUtils;

import java.util.Arrays;

public class NumericalValue extends Value {

	private float[] elements = new float[4];

	private int currentElementCount;

	public enum Flavour {
		REGULAR,
		ANGLE,
		NORMALIZED
	}

	private Flavour flavour = Flavour.REGULAR;

	public void setFlavour(Flavour flavour) {
		this.flavour = flavour;
	}

	public Flavour getFlavour() {
		return flavour;
	}


	public void configure (int elementCount) {
		//Depeneding on the values

		//If we are 2 element count vector, and other is a 3 vector, we increase currentElementCount and init with 0
		currentElementCount = elementCount;

	}

	public void sum (NumericalValue other, NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = elements[i] + other.elements[i];
		}
		out.setElementsCount(elementsCount());
	}

	public void sub (NumericalValue other, NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = elements[i] - other.elements[i];
		}
		out.setElementsCount(elementsCount());
	}

	public void mul (float val, NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = elements[i] * val;
		}
		out.setElementsCount(elementsCount());
	}

	public void div (NumericalValue other, NumericalValue out) {


		for (int i = 0; i < currentElementCount; i++) {
			float d = other.getFloat();
			if(d==0)d=1;
			out.elements[i] = elements[i] / d;
		}
		out.setElementsCount(elementsCount());
	}

	public void mod (NumericalValue other, NumericalValue out) {

		for (int i = 0; i < currentElementCount; i++) {
			float d = other.getFloat();
			if(d==0)d=1;
			out.elements[i] = elements[i] % d;
		}
		out.setElementsCount(elementsCount());
	}


	public void mul (NumericalValue other, NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = elements[i] * other.elements[i];
		}
		out.setElementsCount(elementsCount());
	}

	public void sin (NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = MathUtils.sinDeg(elements[i]);
		}
		out.setElementsCount(elementsCount());
	}

	public void cos (NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = MathUtils.cosDeg(elements[i]);
		}
		out.setElementsCount(elementsCount());
	}


	@Override
	public void set(Value value) {
		set((NumericalValue) value);
	}

	public void set (NumericalValue other) {
		if(other.elementsCount() > currentElementCount) {
			currentElementCount = other.elementsCount();
		}
		for (int i = 0; i < 4; i++) {
			if (i < currentElementCount) {
				elements[i] = other.elements[i];
			} else {
				elements[i] = 0;
			}
		}
	}

	public void set(int index, float value) {
		elements[index] = value;
	}

	public void set(float value) {
		currentElementCount = 1;
		elements[0] = value;
	}

	public void set(float val1, float val2) {
		currentElementCount = 2;
		elements[0] = val1;
		elements[1] = val2;
	}

	public void set(NumericalValue val1, NumericalValue val2) {
		currentElementCount = 2;
		elements[0] = val1.getFloat();
		elements[1] = val2.getFloat();
	}

	public void set(NumericalValue val1, NumericalValue val2, NumericalValue val3) {
		currentElementCount = 3;
		elements[0] = val1.getFloat();
		elements[1] = val2.getFloat();
		elements[2] = val3.getFloat();
	}

	public void set(float val1, float val2, float val3) {
		currentElementCount = 3;
		elements[0] = val1;
		elements[1] = val2;
		elements[2] = val3;
	}

	public void set(float val1, float val2, float val3, float val4) {
		currentElementCount = 4;
		elements[0] = val1;
		elements[1] = val2;
		elements[2] = val3;
		elements[3] = val4;
	}

	public float getFloat() {
		return elements[0];
	}

	public int elementsCount() {
		return currentElementCount;
	}

	public void setElementsCount(int elementsCount) {
		currentElementCount = elementsCount;
	}

	public float[] getElements() {
		return elements;
	}

	public float get(int index) {
		return elements[index];
	}

	public void pow(NumericalValue b, NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = (float) Math.pow(elements[i], b.getFloat());
		}
		out.setElementsCount(elementsCount());
	}

	public void abs(NumericalValue out) {
		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = Math.abs(elements[i]);
		}
		out.setElementsCount(elementsCount());
	}

	@Override
	public String toString () {
		return Arrays.toString(elements);
	}

	@Override
	public void setEmpty(boolean isEmpty) {
		super.setEmpty(isEmpty);
		if(isEmpty) {
			currentElementCount = 0;
		}
	}
}
