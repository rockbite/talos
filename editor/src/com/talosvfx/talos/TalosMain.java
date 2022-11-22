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
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.talosvfx.talos.editor.NodeStage;
import com.talosvfx.talos.editor.TalosInputProcessor;
import com.talosvfx.talos.editor.UIStage;
import com.talosvfx.talos.editor.WorkplaceStage;
import com.talosvfx.talos.editor.addons.AddonController;
import com.talosvfx.talos.editor.dialogs.ErrorReporting;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.project.TalosProject;
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

	private NodeStage nodeStage;

	private WorkplaceStage currentWorkplaceStage;

	private ProjectController projectController;

	private Skin skin;

	private static TalosMain instance;

	private AddonController addonController;

//	public ObjectMap<Class, String> moduleNames = new ObjectMap<>();

	public static TalosMain Instance () {
		return instance;
	}

	public ScopePayload globalScope = new ScopePayload();

	public UIStage UIStage () {
		return uiStage;
	}

	public ErrorReporting errorReporting;

	public NodeStage NodeStage () {
		return nodeStage;
	}

	private Preferences preferences;

	private FileTracker fileTracker = new FileTracker();

	private InputMultiplexer inputMultiplexer;

	public TalosProject TalosProject() {
		return ProjectController.TLS;
	}

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

	private TalosInputProcessor talosInputProcessor;

	private Array<InputProcessor> inputProcessors = new Array<>();
	private Array<InputProcessor> customInputProcessors = new Array<>();

	public TalosMain () {

	}

	public boolean isOsX() {
		String osName = System.getProperty("os.name").toLowerCase();
		boolean isMacOs = osName.startsWith("mac os x");
		return isMacOs;
	}

	@Override
	public void create () {

		TalosMain.instance = this;

		screenshotService = new ScreenshotService();

		addonController = new AddonController();

		preferences = Gdx.app.getPreferences("talos-preferences");

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		skin.addRegions(atlas);

		VisUI.load(skin);

		uiStage = new UIStage(skin);

		errorReporting = new ErrorReporting();

		nodeStage = new NodeStage(skin);
		currentWorkplaceStage = nodeStage;

		projectController = new ProjectController();


		inputProcessors.add(uiStage.getStage(), currentWorkplaceStage.getStage());

		inputMultiplexer = new InputMultiplexer();
		setInputProcessors();

		uiStage.init();
		nodeStage.init();

		addonController.initAll();

		Gdx.input.setInputProcessor(inputMultiplexer);

		// final init after all is done
		TalosMain.Instance().ProjectController().newProject(ProjectController.TLS);
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

	private void loadFromProperties () {
		FileHandle properties = Gdx.files.internal("talos-version.properties");
		if (properties.exists()) {
			Properties props = new Properties();
			try {
				props.load(properties.read());

				String title = "Talos";
				//buildHash=cac0e98
				//buildTime=1660634881583
				//version=1.4.2-SNAPSHOT

				if (props.containsKey("version")) {
					title = props.getProperty("version");
				}
				if (props.containsKey("buildTime")) {
					String buildTime = props.getProperty("buildTime");
					Date date = new Date(Long.parseLong(buildTime));

					SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

					title += " " + sdf.format(date);
				}
				if (props.containsKey("buildHash")) {
					title += " " + props.getProperty("buildHash");
				}
				Gdx.graphics.setTitle(title);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setPerFrameCursorTypeEnabled () {

	}

	public void disableNodeStage() {
		currentWorkplaceStage = null;
		inputProcessors.removeValue(nodeStage.getStage(), true);
	}

	public void setThirdPartyStage(WorkplaceStage stage) {
		currentWorkplaceStage = stage;
		inputProcessors.clear();
		inputProcessors.add(uiStage.getStage());
		inputProcessors.add(currentWorkplaceStage.getStage());
		setInputProcessors();
	}

	public void enableNodeStage() {
		currentWorkplaceStage = nodeStage;
		inputProcessors.clear();
		inputProcessors.add(uiStage.getStage());
		inputProcessors.add(currentWorkplaceStage.getStage());
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
			Gdx.gl.glClearColor(currentWorkplaceStage.getBgColor().r, currentWorkplaceStage.getBgColor().g, currentWorkplaceStage.getBgColor().b, 1);
		} else {
			Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
		}
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (currentWorkplaceStage != null) {
			currentWorkplaceStage.act();
			currentWorkplaceStage.getStage().act();
			currentWorkplaceStage.getStage().draw();
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
		addonController.dispose();
		if(currentWorkplaceStage != null && currentWorkplaceStage.getStage() != null) {
			currentWorkplaceStage.getStage().dispose();
		}
		uiStage.getStage().dispose();
		Render.instance().dispose();
		SocketServer.dispose();
	}

	public Skin getSkin() {
		return skin;
	}

	public CameraController getCameraController() {
		if(currentWorkplaceStage == null) return null;

		return currentWorkplaceStage.getCameraController();
	}

	public AddonController Addons() {
		return addonController;
	}

	public FileTracker FileTracker() {
		return fileTracker;
	}

	public WorkplaceStage getNodeStage () {
		return currentWorkplaceStage;
	}
}
