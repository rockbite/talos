package com.talosvfx.talos.editor.project2.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class InputHandling {

	private ObjectSet<InputProcessor> permanentInputProcessors = new ObjectSet<>();

	private ObjectSet<InputProcessor> temporaryInputProcessors = new ObjectSet<>();
	private ObjectSet<InputProcessor> priorityInputProcessors = new ObjectSet<>();

	private InputMultiplexer inputMultiplexer = new InputMultiplexer();

	public void clearPermanentInputProcessors () {
		permanentInputProcessors.clear();
	}

	public void clearTemporaryInputProcessors () {
		temporaryInputProcessors.clear();
	}

	public void addPermanentInputProcessor (InputProcessor inputProcessor) {
		permanentInputProcessors.add(inputProcessor);
	}

	public void removePermanentInputProcessor (InputProcessor inputProcessor) {
		permanentInputProcessors.remove(inputProcessor);
	}

	public void addTemporaryInputProcessor (InputProcessor inputProcessor) {
		temporaryInputProcessors.add(inputProcessor);
	}
	public void removeTemporaryInputProcessor (InputProcessor inputProcessor) {
		temporaryInputProcessors.remove(inputProcessor);
	}
	public void setGDXMultiPlexer () {
		inputMultiplexer.clear();

		for (InputProcessor priorityInputProcessor : priorityInputProcessors) {
			inputMultiplexer.addProcessor(priorityInputProcessor);
		}

		for (InputProcessor permanentInputProcessor : permanentInputProcessors) {
			inputMultiplexer.addProcessor(permanentInputProcessor);
		}

		for (InputProcessor temporaryInputProcessor : temporaryInputProcessors) {
			inputMultiplexer.addProcessor(temporaryInputProcessor);
		}

		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	public void addPriorityInputProcessor (InputProcessor inputProcessor) {
		priorityInputProcessors.add(inputProcessor);
	}

	public void removePriorityInputProcessor (InputProcessor inputProcessor) {
		priorityInputProcessors.remove(inputProcessor);
	}
}
