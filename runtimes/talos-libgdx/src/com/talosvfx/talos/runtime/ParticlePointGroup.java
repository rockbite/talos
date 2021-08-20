package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.modules.ParticleModule;

public class ParticlePointGroup {

	public Array<ParticlePointData> pointDataArray = new Array<>();

	public int requester;
	public float seed;
}
