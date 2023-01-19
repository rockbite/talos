package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;

public class ParticleComponentProvider extends RendererComponentProvider<ParticleComponent<? extends BaseVFXProjectData>> {

	public ParticleComponentProvider (ParticleComponent<? extends BaseVFXProjectData> component) {
		super(component);
	}
}
