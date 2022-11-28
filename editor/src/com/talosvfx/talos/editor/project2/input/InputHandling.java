package com.talosvfx.talos.editor.project2.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class InputHandling {

	private Array<InputProcessor> permanentInputProcessors = new Array<>();

	private Array<InputProcessor> temporaryInputProcessors = new Array<>();
	private Array<InputProcessor> priorityInputProcessors = new Array<>();

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

	public void addTemporaryInputProcessor (InputProcessor inputProcessor) {
		temporaryInputProcessors.add(inputProcessor);
	}
	public void removeTemporaryInputProcessor (InputProcessor inputProcessor) {
		temporaryInputProcessors.removeValue(inputProcessor, true);
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
		priorityInputProcessors.removeValue(inputProcessor, true);
	}
}
