package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.render.ParticleRenderer;

public abstract class MeshGeneratorModule extends AbstractModule {

	public static final int MODULE = 0;

	public abstract void render (ParticleRenderer particleRenderer, MaterialModule materialModule, Array<ParticlePointGroup> pointGroups);

	public abstract void setRenderMode (boolean is3D);
}
