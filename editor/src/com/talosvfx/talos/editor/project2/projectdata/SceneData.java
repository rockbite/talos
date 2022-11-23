package com.talosvfx.talos.editor.project2.projectdata;

import com.badlogic.gdx.utils.Array;
import lombok.Data;

@Data
public class SceneData {

	private Array<String> renderLayers = new Array<>(new String[]{"Default", "UI", "Misc"});

}
