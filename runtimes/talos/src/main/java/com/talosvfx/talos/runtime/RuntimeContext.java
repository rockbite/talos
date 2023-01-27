package com.talosvfx.talos.runtime;

import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.runtime.utils.ConfigData;
import lombok.Getter;
import lombok.Setter;

public class RuntimeContext {
	private static RuntimeContext context;

	public static RuntimeContext getInstance () {
		if (context == null) {
			context = new RuntimeContext();
			context.configData = new ConfigData();
		}
		return context;
	}

	@Getter@Setter
	public BaseAssetRepository AssetRepository;

	@Getter
	public ConfigData configData;

	@Getter@Setter
	public SceneData sceneData;

}
