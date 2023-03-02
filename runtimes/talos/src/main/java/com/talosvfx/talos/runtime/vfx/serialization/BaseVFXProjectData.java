package com.talosvfx.talos.runtime.vfx.serialization;

import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ScopePayload;

import java.util.function.Supplier;

public abstract class BaseVFXProjectData {

	public BaseVFXProjectData () {}

	public abstract Supplier<ParticleEffectDescriptor> getDescriptorSupplier ();
}
