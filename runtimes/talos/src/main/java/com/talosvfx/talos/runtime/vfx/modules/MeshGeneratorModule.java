package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.vfx.ParticlePointGroup;
import com.talosvfx.talos.runtime.vfx.render.ParticleRenderer;

public abstract class MeshGeneratorModule extends AbstractModule {

	public static final int MODULE = 0;

	public abstract void render (ParticleRenderer particleRenderer, MaterialModule materialModule, Array<ParticlePointGroup> pointGroups);

	public abstract void setRenderMode (boolean is3D);
}
