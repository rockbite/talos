package com.talosvfx.talos.runtime;

import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import lombok.Getter;
import lombok.Setter;

public class RuntimeContext {
	private static RuntimeContext context;

	public static RuntimeContext getInstance () {
		if (context == null) {
			context = new RuntimeContext();
		}
		return context;
	}

	private boolean isEditor = true;

	@Getter@Setter
	public BaseAssetRepository AssetRepository;

}
