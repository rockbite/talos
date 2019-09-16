package com.rockbite.tools.talos.runtime.values;

import com.badlogic.gdx.math.MathUtils;

public class NumericalValue extends Value {

	private float[] elements = new float[4];

	private int currentElementCount;

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
		elements[3] = val3;
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
}
