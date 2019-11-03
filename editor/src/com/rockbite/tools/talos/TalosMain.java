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

package com.rockbite.tools.talos;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.rockbite.tools.talos.editor.NodeStage;
import com.rockbite.tools.talos.editor.UIStage;
import com.rockbite.tools.talos.editor.WorkplaceStage;
import com.rockbite.tools.talos.editor.addons.AddonController;
import com.rockbite.tools.talos.editor.addons.bvb.FileTracker;
import com.rockbite.tools.talos.editor.project.IProject;
import com.rockbite.tools.talos.editor.project.TalosProject;
import com.rockbite.tools.talos.editor.project.ProjectController;
import com.rockbite.tools.talos.editor.utils.CameraController;
import com.rockbite.tools.talos.editor.utils.DropTargetListenerAdapter;
import com.rockbite.tools.talos.runtime.ScopePayload;

import java.awt.dnd.*;

public class TalosMain extends ApplicationAdapter {

	private UIStage uiStage;

	private NodeStage nodeStage;

	private WorkplaceStage currentWorkplaceStage;

	private ProjectController projectController;

	private Skin skin;

	private DropTargetListener dropTargetListener;

	private static TalosMain instance;

	private AddonController addonController;

	public ObjectMap<Class, String> moduleNames = new ObjectMap<>();

	public static TalosMain Instance () {
		return instance;
	}

	public ScopePayload globalScope = new ScopePayload();

	public UIStage UIStage () {
		return uiStage;
	}

	public NodeStage NodeStage () {
		return nodeStage;
	}

	private Preferences preferences;

	private FileTracker fileTracker = new FileTracker();

	private InputMultiplexer inputMultiplexer;

	public TalosProject TalosProject() {
		return (TalosProject) projectController.getProject();
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

	public DropTargetListener getDropTargetListener () {
		return dropTargetListener;
	}

	public TalosMain () {
		dropTargetListener = new DropTargetListenerAdapter() {
			@Override
			protected void doDrop (String[] finalPaths, float x, float y) {
				nodeStage.fileDrop(finalPaths, x, y);
				uiStage.fileDrop(finalPaths, x, y);
			}
		};
	}

	@Override
	public void create () {
		TalosMain.instance = this;

		addonController = new AddonController();

		preferences = Gdx.app.getPreferences("talos-preferences");

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		skin.addRegions(atlas);

		VisUI.load(skin);

		uiStage = new UIStage(skin);
		nodeStage = new NodeStage(skin);
		currentWorkplaceStage = nodeStage;

		projectController = new ProjectController();

		uiStage.init();
		nodeStage.init();

		addonController.initAll();

		inputMultiplexer = new InputMultiplexer(uiStage.getStage(), currentWorkplaceStage.getStage());

		Gdx.input.setInputProcessor(inputMultiplexer);

		// final init after all is done
		TalosMain.Instance().ProjectController().newProject(ProjectController.TLS);
	}

	public void disableNodeStage() {
		currentWorkplaceStage = null;
		inputMultiplexer.removeProcessor(nodeStage.getStage());
	}

	public void setThirdPartyStage(WorkplaceStage stage) {
		currentWorkplaceStage = stage;
		inputMultiplexer.setProcessors(uiStage.getStage(), currentWorkplaceStage.getStage());
	}

	public void enableNodeStage() {
		currentWorkplaceStage = nodeStage;
		inputMultiplexer.setProcessors(uiStage.getStage(), currentWorkplaceStage.getStage());
	}

	@Override
	public void render () {
		if(currentWorkplaceStage != null) {
			Gdx.gl.glClearColor(currentWorkplaceStage.getBgColor().r, currentWorkplaceStage.getBgColor().g, currentWorkplaceStage.getBgColor().b, 1);
		} else {
			Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
		}
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if(currentWorkplaceStage != null) {
			currentWorkplaceStage.getStage().act();
			currentWorkplaceStage.getStage().draw();
		}

		fileTracker.update();

		uiStage.getStage().act();
		uiStage.getStage().draw();
	}

	public void resize (int width, int height) {
		if(currentWorkplaceStage != null) {
			currentWorkplaceStage.resize(width, height);
		}
		uiStage.resize(width, height);
	}

	@Override
	public void dispose () {
		currentWorkplaceStage.getStage().dispose();
		uiStage.getStage().dispose();
	}

	public Skin getSkin() {
		return skin;
	}

	public CameraController getCameraController() {
		return currentWorkplaceStage.getCameraController();
	}

	public AddonController Addons() {
		return addonController;
	}

	public FileTracker FileTracker() {
		return fileTracker;
	}
}
