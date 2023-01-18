package com.talosvfx.talos.editor.project2.projectdata;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import lombok.Data;

@Data
public class SceneData {

	private Array<SceneLayer> renderLayers = new Array<>(new SceneLayer[]{
			new SceneLayer("Default", 0),
			new SceneLayer("UI", 1),
	new SceneLayer("Misc", 2)});


	public SceneLayer getSceneLayerByName (String layerName) {
		for (SceneLayer renderLayer : this.renderLayers) {
			if (renderLayer.getName().equals(layerName)) {
				return renderLayer;
			}
		}

		return null;
	}

}
