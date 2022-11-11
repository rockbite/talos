package com.talosvfx.talos.runtime.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Matrix4;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import lombok.Data;

@Data
public class Particle extends Component {

	private Matrix4 transform = new Matrix4();

	private ParticleEffectInstance particleEffectInstance;

}
