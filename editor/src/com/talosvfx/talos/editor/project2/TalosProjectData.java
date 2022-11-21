package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.layouts.LayoutContent;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import lombok.Getter;

import java.util.UUID;

public class TalosProjectData implements Json.Serializable {

	//Store any specific data here for the specific project settings that should be shared with anyone that
	//is loading the same project

	@Getter
	private LayoutGrid layoutGrid;


	private transient TalosSourceProject projectsOpened;

	public TalosProjectData () {
		layoutGrid = new LayoutGrid(SharedResources.skin);

		test();
	}


	public void test () {
		layoutGrid.reset();

		LayoutContent content = new LayoutContent(SharedResources.skin, layoutGrid);
		content.addContent(new DummyLayoutApp(SharedResources.skin, UUID.randomUUID().toString()));
		layoutGrid.addContent(content);

		{
			LayoutContent newContent = new LayoutContent(SharedResources.skin, layoutGrid);

			int random = MathUtils.random(1,3);
			for (int i = 0; i < random; i++) {
				String uuid = UUID.randomUUID().toString();

				newContent.addContent(new DummyLayoutApp(SharedResources.skin, uuid));

			}

			layoutGrid.addContent(newContent);
		}
	}



	@Override
	public void write (Json json) {

	}

	@Override
	public void read (Json json, JsonValue jsonData) {

	}
}
