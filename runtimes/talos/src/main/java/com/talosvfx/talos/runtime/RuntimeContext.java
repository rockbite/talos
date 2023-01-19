package com.talosvfx.talos.runtime;

import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.scene.SceneData;
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

	@Getter@Setter
	public BaseAssetRepository AssetRepository;

	@Getter@Setter
	public SceneData sceneData;

}
