package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.ParticleEmitterWrapper;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedWidget;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.vfx.VFXEditorActivated;
import com.talosvfx.talos.editor.data.ModuleWrapperGroup;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.project2.vfxui.GenericStageWrappedViewportWidget;
import com.talosvfx.talos.editor.serialization.EmitterData;
import com.talosvfx.talos.editor.serialization.GroupData;
import com.talosvfx.talos.editor.serialization.VFXEditorState;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.widgets.ui.ModuleBoardWidget;
import com.talosvfx.talos.editor.wrappers.ModuleWrapper;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.vfx.assets.AssetProvider;
import lombok.Getter;

import java.util.Comparator;

public class ParticleNodeEditorApp extends AppManager.BaseApp<VFXProjectData> implements ContainerOfPrefs<ViewportPreferences>, GameAsset.GameAssetUpdateListener {

	@Getter
	private final ModuleBoardWidget moduleBoardWidget;

	private final GenericStageWrappedViewportWidget moduleGraphStageWrapper;
	private final GenericStageWrappedWidget moduleGraphUIWrapper;

	public Table uiContent;

	private Comparator<ParticleEmitterWrapper> emitterComparator = new Comparator<ParticleEmitterWrapper>() {
		@Override
		public int compare(ParticleEmitterWrapper o1, ParticleEmitterWrapper o2) {
			return (int)( 10f * (o1.getPosition() - o2.getPosition()));
		}
	};

	ParticleEffectDescriptor particleEffectDescriptor;
	ParticleEffectInstance particleEffect;
	@Getter
	private VFXEditorState editorState;

	public ParticleNodeEditorApp () {
		this.singleton = false;

		moduleBoardWidget = new ModuleBoardWidget(this);

		moduleGraphStageWrapper = new GenericStageWrappedViewportWidget(moduleBoardWidget) {
			@Override
			protected boolean canMoveAround() {
				return true;
			}
		};
		moduleGraphStageWrapper.disableListeners();
		moduleGraphStageWrapper.panRequiresSpace(false);

		uiContent = new Table();
		moduleGraphUIWrapper = new GenericStageWrappedWidget(uiContent);
		moduleBoardWidget.sendInUIStage(moduleGraphUIWrapper.getStage());

		final Table content = new Table();
		Stack stack = new Stack(moduleGraphStageWrapper, moduleGraphUIWrapper);
		content.add(stack).grow();

		this.gridAppReference = new DummyLayoutApp<VFXProjectData>(SharedResources.skin, this, getAppName()) {
			@Override
			public Actor getMainContent() {
				return content;
			}

			@Override
			public void onInputProcessorAdded () {
				super.onInputProcessorAdded();
				moduleGraphStageWrapper.restoreListeners();
				SharedResources.stage.setScrollFocus(moduleGraphStageWrapper);
				SharedResources.inputHandling.addPriorityInputProcessor(moduleGraphUIWrapper.getStage());
				SharedResources.inputHandling.addPriorityInputProcessor(moduleGraphStageWrapper.getStage());
				SharedResources.inputHandling.setGDXMultiPlexer();
			}

			@Override
			public void onInputProcessorRemoved () {
				super.onInputProcessorRemoved();
				moduleGraphStageWrapper.disableListeners();
				SharedResources.inputHandling.removePriorityInputProcessor(moduleGraphUIWrapper.getStage());
				SharedResources.inputHandling.removePriorityInputProcessor(moduleGraphStageWrapper.getStage());
				SharedResources.inputHandling.setGDXMultiPlexer();
			}
		};
	}

	private void saveProjectToData (VFXProjectData projectData) {
		projectData.setFrom(moduleBoardWidget);
	}

	public void loadProject (VFXProjectData projectData) {
		particleEffectDescriptor = new ParticleEffectDescriptor();
		particleEffect = new ParticleEffectInstance(particleEffectDescriptor);

		particleEffectDescriptor.setAssetProvider(new AssetProvider() {
			@Override
			public <T> T findAsset (String assetName, Class<T> clazz) {

				if (Sprite.class.isAssignableFrom(clazz)) {
					GameAsset<Texture> gameAsset = AssetRepository.getInstance().getAssetForIdentifier(assetName, GameAssetType.SPRITE);
					if(gameAsset.getResource() == null) {
						gameAsset = AssetRepository.getInstance().getAssetForIdentifier("white", GameAssetType.SPRITE);
					}
					return (T)new Sprite(gameAsset.getResource());
				}

				throw new GdxRuntimeException("Couldn't find asset " + assetName + " for type " + clazz);
			}
		});


		editorState = projectData.getEditorState();

		//Set it up every time we load it
		editorState.reset();
		moduleBoardWidget.clearAll();

		projectData.setDescriptor(particleEffectDescriptor);

		ParticleEmitterWrapper firstEmitter = null;

		for (EmitterData emitterData : projectData.getEmitters()) {
			IntMap<ModuleWrapper> map = new IntMap<>();

			ParticleEmitterWrapper emitterWrapper = loadEmitter(emitterData.name, emitterData.sortPosition);

			moduleBoardWidget.loadEmitterToBoard(emitterWrapper, emitterData);

			final ParticleEmitterDescriptor graph = emitterWrapper.getGraph();
			for (ModuleWrapper module : emitterData.modules) {
				map.put(module.getId(), module);

				graph.addModule(module.getModule());
				module.getModule().setModuleGraph(graph);
			}


			particleEffectDescriptor.setEffectReference(particleEffect); // important
			particleEffectDescriptor.addEmitter(graph);
			particleEffect.init();
			// configure emitter visibility
			emitterWrapper.isMuted = emitterData.isMuted;
			particleEffect.getEmitter(emitterWrapper.getGraph()).setVisible(!emitterData.isMuted);
			// time to load groups here
			for (GroupData group : emitterData.groups) {
				ObjectSet<ModuleWrapper> childWrappers = new ObjectSet<>();
				for (Integer id : group.modules) {
					if (map.get(id) != null) {
						childWrappers.add(map.get(id));
					}
				}

				ModuleWrapperGroup moduleWrapperGroup = moduleBoardWidget.createGroupForWrappers(childWrappers);
				Color clr = new Color();
				Color.abgr8888ToColor(clr, group.color);
				moduleWrapperGroup.setData(group.text, clr);

			}
		}

		sortEmitters();

		if (editorState.activeWrappers.size > 0) {
			firstEmitter = editorState.activeWrappers.first();
		}

		if (firstEmitter != null) {
			moduleBoardWidget.setCurrentEmitter(firstEmitter);
		}

	}

	public ParticleEmitterWrapper initEmitter (String emitterName) {
		ParticleEmitterWrapper emitterWrapper = new ParticleEmitterWrapper();
		emitterWrapper.setName(emitterName);

		ParticleEmitterDescriptor moduleGraph = particleEffectDescriptor.createEmitterDescriptor();
		emitterWrapper.setModuleGraph(moduleGraph);

		particleEffect.addEmitter(moduleGraph);

		return emitterWrapper;
	}


	private ParticleEmitterWrapper loadEmitter(String emitterName, int sortPosition) {
		ParticleEmitterWrapper emitterWrapper = initEmitter(emitterName);
		editorState.activeWrappers.add(emitterWrapper);

		emitterWrapper.getEmitter().setSortPosition(sortPosition);
		emitterWrapper.setPosition(sortPosition);

		moduleBoardWidget.setCurrentEmitter(emitterWrapper);

		return emitterWrapper;
	}

	public void sortEmitters () {
		editorState.activeWrappers.sort(emitterComparator);

		// fix for older projects
//		if (activeWrappers.size > 1 && activeWrappers.get(0).getEmitter().getSortPosition() == 0 && activeWrappers.get(1).getEmitter().getSortPosition() == 0) {
//			activeWrappers.reverse();
//		}

		// re-normalize position numbers
		int index = 0;
		for (ParticleEmitterWrapper wrapper : editorState.activeWrappers) {
			wrapper.getEmitter().setSortPosition(index++);
			wrapper.setPosition(wrapper.getEmitter().getSortPosition());
		}
		particleEffect.sortEmitters();

	}

	@Override
	public void updateForGameAsset (GameAsset<VFXProjectData> gameAsset) {
		if (this.gameAsset != null) {
			this.gameAsset.listeners.removeValue(this, true);
		}

		if (!gameAsset.listeners.contains(this, true)) {
			gameAsset.listeners.add(this);
		}


		super.updateForGameAsset(gameAsset);

		loadProject(gameAsset.getResource());

		TalosLocalPrefs.getAppPrefs(gameAsset, this);

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Notifications.fireEvent(Notifications.obtainEvent(VFXEditorActivated.class).set(gameAsset));
			}
		});
	}

	@Override
	public String getAppName () {
		if (gameAsset != null) {
			return "VFX - " + gameAsset.nameIdentifier;
		} else {
			return "VFX - ";
		}
	}

	@Override
	public void onRemove () {
		// remove listeners and stuff somehow
		moduleGraphStageWrapper.disableListeners();
		SharedResources.inputHandling.removePriorityInputProcessor(moduleGraphUIWrapper.getStage());
		SharedResources.inputHandling.removePriorityInputProcessor(moduleGraphStageWrapper.getStage());
		SharedResources.inputHandling.setGDXMultiPlexer();

		if (this.gameAsset != null) {
			this.gameAsset.listeners.removeValue(this, true);
		}
	}

	@Override
	public void applyFromPreferences(ViewportPreferences prefs) {
		moduleGraphStageWrapper.setCameraPos(prefs.cameraPos);
		moduleGraphStageWrapper.setCameraZoom(prefs.cameraZoom);
	}

	@Override
	public ViewportPreferences getPrefs() {
		ViewportPreferences prefs = new ViewportPreferences();
		prefs.cameraPos = moduleGraphStageWrapper.getCameraPos();
		prefs.cameraZoom = moduleGraphStageWrapper.getCameraZoom();
		return prefs;
	}

	public void dataModified() {
		saveProjectToData(gameAsset.getResource());
		AssetRepository.getInstance().assetChanged(gameAsset);
	}

	public void resetToNew() {
		// ?
	}

	@Override
	public void onUpdate () {
		loadProject(gameAsset.getResource());
	}
}

