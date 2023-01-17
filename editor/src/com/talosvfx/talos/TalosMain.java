/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.talosvfx.talos.editor.UIStage;
import com.talosvfx.talos.editor.WorkplaceStage;
import com.talosvfx.talos.editor.dialogs.ErrorReporting;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.project.ProjectController;
import com.talosvfx.talos.editor.render.Render;
import com.talosvfx.talos.editor.socket.SocketServer;
import com.talosvfx.talos.editor.utils.CameraController;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.editor.utils.ScreenshotService;
import com.talosvfx.talos.runtime.ScopePayload;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class TalosMain extends ApplicationAdapter {

	private static boolean focused = true;

	private UIStage uiStage;


	private WorkplaceStage currentWorkplaceStage;

	private ProjectController projectController;

	private Skin skin;

	private static TalosMain instance;


//	public ObjectMap<Class, String> moduleNames = new ObjectMap<>();

	public static TalosMain Instance () {
		return instance;
	}

	public ScopePayload globalScope = new ScopePayload();

	public UIStage UIStage () {
		return uiStage;
	}

	public ErrorReporting errorReporting;

	private Preferences preferences;

	private FileTracker fileTracker = new FileTracker();

	private InputMultiplexer inputMultiplexer;

	public IProject Project() {
		return projectController.getProject();
	}

	public ProjectController ProjectController () {
		return projectController;
	}

	public Preferences Prefs() {
		return preferences;
	}

	public ScreenshotService Screeshot() {
		return screenshotService;
	}

	private ScreenshotService screenshotService;

	private Array<InputProcessor> inputProcessors = new Array<>();
	private Array<InputProcessor> customInputProcessors = new Array<>();

	public TalosMain () {

	}

	public static boolean isOsX() {
		String osName = System.getProperty("os.name").toLowerCase();
		boolean isMacOs = osName.startsWith("mac os x");
		return isMacOs;
	}

	@Override
	public void create () {

		TalosMain.instance = this;

		screenshotService = new ScreenshotService();


		preferences = Gdx.app.getPreferences("talos-preferences");

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		skin.addRegions(atlas);

		VisUI.load(skin);

		uiStage = new UIStage(skin);

		errorReporting = new ErrorReporting();


		projectController = new ProjectController();


//		inputProcessors.add(uiStage.getStage(), currentWorkplaceStage.getStage());

		inputMultiplexer = new InputMultiplexer();
		setInputProcessors();

		uiStage.init();


		Gdx.input.setInputProcessor(inputMultiplexer);

		// final init after all is done
	}

	private void setInputProcessors() {
		inputMultiplexer.clear();
		for(InputProcessor processor: inputProcessors) {
			inputMultiplexer.addProcessor(processor);
		}
		for(InputProcessor processor: customInputProcessors) {
			inputMultiplexer.addProcessor(processor);
		}
	}

	public void addCustomInputProcessor(InputProcessor inputProcessor) {
		customInputProcessors.add(inputProcessor);
		setInputProcessors();
	}

	public void setPerFrameCursorTypeEnabled () {

	}

	public void disableNodeStage() {
		currentWorkplaceStage = null;
	}

	public void setThirdPartyStage(WorkplaceStage stage) {
		currentWorkplaceStage = stage;
		inputProcessors.clear();
		inputProcessors.add(uiStage.getStage());
//		inputProcessors.add(currentWorkplaceStage.getStage());
		setInputProcessors();
	}

	public void enableNodeStage() {
		inputProcessors.clear();
		inputProcessors.add(uiStage.getStage());
//		inputProcessors.add(currentWorkplaceStage.getStage());
		setInputProcessors();
	}

	@Override
	public void render () {
		CursorUtil.checkAndReset();


		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (currentWorkplaceStage != null) {
//			Gdx.gl.glClearColor(currentWorkplaceStage.getBgColor().r, currentWorkplaceStage.getBgColor().g, currentWorkplaceStage.getBgColor().b, 1);
		} else {
			Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
		}
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (currentWorkplaceStage != null) {
			currentWorkplaceStage.act();
//			currentWorkplaceStage.getStage().act();
//			currentWorkplaceStage.getStage().draw();
		}

		fileTracker.update();

		uiStage.getStage().act();
		uiStage.getStage().draw();

		screenshotService.postRender();
	}

	public void reportException(Throwable e) {
		errorReporting.reportException(e);
	}

	public void resize (int width, int height) {
		if(currentWorkplaceStage != null) {
			currentWorkplaceStage.resize(width, height);
		}
		uiStage.resize(width, height);
	}

	@Override
	public void dispose () {
//		if(currentWorkplaceStage != null && currentWorkplaceStage.getStage() != null) {
//			currentWorkplaceStage.getStage().dispose();
//		}
		uiStage.getStage().dispose();
		Render.instance().dispose();
		SocketServer.dispose();
	}

	public Skin getSkin() {
		return skin;
	}

	public CameraController getCameraController() {
		if(currentWorkplaceStage == null) return null;

		return null;
//		return currentWorkplaceStage.getCameraController();
	}

	public FileTracker FileTracker() {
		return fileTracker;
	}

	public WorkplaceStage getNodeStage () {
		return currentWorkplaceStage;
	}
}
