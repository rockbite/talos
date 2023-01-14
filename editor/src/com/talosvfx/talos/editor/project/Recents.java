package com.talosvfx.talos.editor.project;

import com.badlogic.gdx.utils.Array;
import lombok.Data;

@Data
public class Recents {

	private Array<RecentsEntry> recents = new Array<>();

	public Recents () {

	}

}
