package com.talosvfx.talos.runtime.assets.meta;

import com.talosvfx.talos.runtime.utils.Supplier;

public class DefaultConstants {

	public static Supplier<Float> defaultPixelPerUnitProvider = new Supplier<Float>() {
		@Override
		public Float get () {
			return defaultPixelPerUnitProvider.get();
		}
	};

}
