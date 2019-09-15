package com.rockbite.tools.talos.runtime.values;

public class NumericalValue {


	float[] elements = new float[4];

	int currentElementCount;

	public void configure (int elementCount) {
		//Depeneding on the values

		//If we are 2 element count vector, and other is a 3 vector, we increase currentElementCount and init with 0

	}

	public void sum (NumericalValue other, NumericalValue out) {



		for (int i = 0; i < currentElementCount; i++) {
			out.elements[i] = elements[i] + other.elements[i];
		}


	}

	public void fetchFrom (NumericalValue other) {
		for (int i = 0; i < 4; i++) {
			if (i < currentElementCount) {
				elements[i] = other.elements[i];
			} else {
				elements[i] = 0;
			}
		}
	}
}
