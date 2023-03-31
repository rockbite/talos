package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.utils.Array;
import lombok.Data;

@Data
public class SceneData {
	private Array<SceneLayer> renderLayers = new Array<>(new SceneLayer[]{
			new SceneLayer("Default", 0),
			new SceneLayer("UI", 1),
	new SceneLayer("Misc", 2)});

	private SceneLayer preferredSceneLayer;


	public SceneLayer getSceneLayerByName (String layerName) {
		for (SceneLayer renderLayer : this.renderLayers) {
			if (renderLayer.getName().equals(layerName)) {
				return renderLayer;
			}
		}

		return null;
	}


	public void setPreferredSceneLayer (String sceneLayer){
		preferredSceneLayer = getSceneLayerByName(sceneLayer);
	}

	public SceneLayer getPreferredSceneLayer(){
		if (preferredSceneLayer == null) {
			SceneLayer defaultLayer = getSceneLayerByName("Default");
			if (defaultLayer == null) {
				return renderLayers.first();
			} else {
				return defaultLayer;
			}
		}

		return preferredSceneLayer;
	}
}
