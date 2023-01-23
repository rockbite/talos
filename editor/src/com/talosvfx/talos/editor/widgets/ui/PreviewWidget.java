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

package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import com.talosvfx.talos.editor.wrappers.IDragPointProvider;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.function.Supplier;

public abstract class PreviewWidget extends ViewportWidget {

	private static final Logger logger = LoggerFactory.getLogger(PreviewWidget.class);

	Vector2 mid = new Vector2();

	Vector2 temp = new Vector2();

	private String countStr = "Particles: ";
	private String trisCountStr = "Triangles: ";
	private String nodeCallsStr = "Node Calls: ";
	private String gpuTimeStr = "GPU: ";
	private String cpuTimeStr = "CPU: ";
	private String msStr = "ms";

	private Label countLbl;
	private Label trisCountLbl;
	private Label nodeCallsLbl;
	private Label gpuTimeLbl;
	private Label cpuTimeLbl;

	private Image previewImage = new Image();

	protected StringBuilder stringBuilder = new StringBuilder();
	protected int trisCount = 0;
	protected FloatCounter renderTime = new FloatCounter(100);
	protected FloatCounter cpuTime = new FloatCounter(100);
	protected float fps = 0;

	private GLProfiler glProfiler = new GLProfiler(Gdx.graphics);
	private FPSLogger fpsLogger = new FPSLogger();

	private Array<DragPoint> dragPoints = new Array<>();
	private IDragPointProvider dragPointProvider = null;

	private String backgroundImagePath = "";
	private float gridSize;
	private VFXProjectData vfxProjectData;
	@Getter
	private ParticleEffectDescriptor descriptor;

	@Getter
	protected ParticleEffectInstance effectInstance;

	public PreviewWidget () {
		super();

		countLbl = new Label(countStr, SharedResources.skin);
		trisCountLbl = new Label(trisCountStr, SharedResources.skin);
		nodeCallsLbl = new Label(nodeCallsStr, SharedResources.skin);
		gpuTimeLbl = new Label(gpuTimeStr, SharedResources.skin);
		cpuTimeLbl = new Label(cpuTimeStr, SharedResources.skin);

		countLbl.setColor(Color.GRAY);
		trisCountLbl.setColor(Color.GRAY);
		nodeCallsLbl.setColor(Color.GRAY);
		gpuTimeLbl.setColor(Color.GRAY);
		cpuTimeLbl.setColor(Color.GRAY);

		addActor(rulerRenderer);

		add(countLbl).left().top().padLeft(22).padTop(20).row();
		add(trisCountLbl).left().top().padLeft(22).row();
		add(nodeCallsLbl).left().top().padLeft(22).row();
		add(cpuTimeLbl).left().top().padLeft(22).row();
		add(gpuTimeLbl).left().top().padLeft(22).row();
		add().expand();
		row();

//		cameraController.scrollOnly = true; // camera controller can't operate in this shitty custom conditions

		initListeners();

	}

	@Override
	protected boolean canMoveAround() {
		return true;
	}

	private void initListeners () {
		inputListener = new InputListener() {

			boolean moving = false;
			private Vector3 tmp = new Vector3();
			private Vector3 tmp2 = new Vector3();
			private Vector2 prevPos = new Vector2();
			private Vector2 pos = new Vector2();

			private DragPoint currentlyDragging = null;

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				moving = false;
				getWorldFromLocal(tmp.set(x, y, 0));
				pos.set(tmp.x, tmp.y);
				prevPos.set(x, y);

				//detect drag points
//                for(DragPoint point: dragPoints) {
//                    if(pos.dst(point.position) < 0.2f * camera.zoom) {
//                        // dragging a point
//                        currentlyDragging = point;
//                        return true;
//                    }
//                }

				if (button == 1) {
					moving = true;
					return true;
				}
				return true;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				super.touchDragged(event, x, y, pointer);

				getWorldFromLocal(tmp.set(x, y, 0)); // I can't really explain
				pos.set(tmp.x, tmp.y);
//
//                if(moving) {
//                    final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
//                    particleEffect.setPosition(tmp.x, tmp.y);
//                } else {
//                    getWorldFromLocal(tmp.set(prevPos.x, prevPos.y, 0));
//                    getWorldFromLocal(tmp2.set(x, y, 0));
//
//                    if(currentlyDragging == null) {
//                        // panning
//
//                        camera.position.sub(tmp2.x-tmp.x, tmp2.y-tmp.y, 0);
//                    } else {
//                        // dragging a point
//                        currentlyDragging.position.add(tmp2.x-tmp.x, tmp2.y-tmp.y);
//                        dragPointProvider.dragPointChanged(currentlyDragging);
//                    }
//                }

				prevPos.set(x, y);
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				currentlyDragging = null;
			}

			@Override
			public boolean keyUp (InputEvent event, int keycode) {
				return super.keyUp(event, keycode);
			}
		};

		addListener(inputListener);
	}

	public void fileDrop (float x, float y, String[] paths) {
		temp.set(x, y);
		(getStage().getViewport()).unproject(temp);
		stageToLocalCoordinates(temp);
		if (this.hit(temp.x, temp.y, false) != null) {
			addPreviewImage(paths);
		}
	}

	public void addPreviewImage (String[] paths) {
		if (paths.length == 1) {

			String resourcePath = paths[0];
			FileHandle fileHandle = Gdx.files.absolute(resourcePath);

			final String extension = fileHandle.extension();

			if (GameAssetType.SPRITE.getExtensions().contains(extension.toLowerCase(Locale.US))) {
				fileHandle = TalosMain.Instance().ProjectController().findFile(fileHandle);
				if (fileHandle != null && fileHandle.exists()) {
					final TextureRegion textureRegion = new TextureRegion(new Texture(fileHandle));

					if (textureRegion != null) {
						previewImage.setDrawable(new TextureRegionDrawable(textureRegion));

						backgroundImagePath = fileHandle.path();

						logger.error("Should be saving and using undo system");

//						TalosMain.Instance().ProjectController().setDirty();
					}
				}
			}
		}
	}

	@Override
	public void act (float delta) {
		super.act(delta);

		ParticleEffectDescriptor dataDescriptor = vfxProjectData.getDescriptorSupplier().get();

		if (this.descriptor != dataDescriptor || vfxNeedsUpdate()) {
			this.descriptor = dataDescriptor;
			this.effectInstance = this.descriptor.createEffectInstance();
			effectInstance.loopable = true;
		}

		long timeBefore = TimeUtils.nanoTime();
		cpuTime.put(TimeUtils.timeSinceNanos(timeBefore));

		if (effectInstance != null) {
			stringBuilder.clear();

			stringBuilder.append(countStr).append(effectInstance.getParticleCount());
			countLbl.setText(stringBuilder.toString());

			stringBuilder.clear();
			stringBuilder.append(trisCountStr).append(trisCount);
			trisCountLbl.setText(stringBuilder.toString());

			stringBuilder.clear();
			stringBuilder.append(nodeCallsStr).append(effectInstance.getNodeCalls());
			nodeCallsLbl.setText(stringBuilder.toString());

			float rt = renderTime.value / 1000000f;
			float cp = cpuTime.value / 1000000f;

			rt = (float)Math.round(rt * 10000f) / 10000f;
			cp = (float)Math.round(cp * 10000f) / 10000f;

			stringBuilder.clear();
			stringBuilder.append(gpuTimeStr).append(rt).append(msStr);
			gpuTimeLbl.setText(stringBuilder.toString());

			stringBuilder.clear();
			stringBuilder.append(cpuTimeStr).append(cp).append(msStr);
			cpuTimeLbl.setText(stringBuilder.toString());

		}

	}

	private boolean vfxNeedsUpdate () {
		if (effectInstance == null) return true;

		if (effectInstance.getEmitters().size != descriptor.emitterModuleGraphs.size) return true;
		if (effectInstance.getEmitters().size != vfxProjectData.getEditorState().activeWrappers.size) return true;

		return false;
	}

	@Override
	public void drawContent (PolygonBatch batch, float parentAlpha) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		if (viewportViewSettings.isShowGrid()) {
			batch.end();
			gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
			((DynamicGridPropertyProvider)gridPropertyProvider).distanceThatLinesShouldBe = pixelToWorld(150);
			if (camera instanceof OrthographicCamera) {
				gridPropertyProvider.update((OrthographicCamera)camera, parentAlpha);
			} //todo make it compatible with perspective camera if needs
			gridRenderer.drawGrid(batch, shapeRenderer);
			batch.begin();
		}

		mid.set(0, 0);

		float imagePrefWidth = previewImage.getPrefWidth();
		float imagePrefHeight = previewImage.getPrefHeight();
		float scale = imagePrefHeight / imagePrefWidth;

		//todo preview image
//		float imageWidth = previewController.getImageWidth();
//		float imageHeight = imageWidth * scale;
//		previewController.getPreviewBoxWidth();
//
//		previewImage.setPosition(mid.x - imageWidth / 2, mid.y - imageHeight / 2);
//		previewImage.setSize(imageWidth, imageHeight);
//		if (previewController.isBackground()) {
//			previewImage.draw(batch, parentAlpha);
//		}

//        spriteBatchParticleRenderer.setBatch(batch);

		batch.flush();
		glProfiler.enable();

		long timeBefore = TimeUtils.nanoTime();

//        particleEffect.render(particleRenderer);

		batch.flush();
		renderTime.put(TimeUtils.timeSinceNanos(timeBefore));
		trisCount = (int)(glProfiler.getVertexCount().value / 3f);
		glProfiler.disable();
//
//		if (!previewController.isBackground()) {
//			previewImage.draw(batch, parentAlpha);
//		}

		// now for the drag points
		if (dragPoints.size > 0) {
			batch.end();
//            tmpColor.set(Color.ORANGE);
//            tmpColor.a = 0.8f;
			Gdx.gl.glLineWidth(1f);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//            shapeRenderer.setColor(tmpColor);

			for (DragPoint point : dragPoints) {
				shapeRenderer.circle(point.position.x, point.position.y, 0.1f * viewportViewSettings.getZoom(), 15);
			}

			shapeRenderer.end();
			batch.begin();
		}
	}

	@Override
	public void initializeGridPropertyProvider () {
		gridPropertyProvider = new DynamicGridPropertyProvider();
		gridPropertyProvider.getBackgroundColor().set(0.1f, 0.1f, 0.1f, 1f);
	}

	public GLProfiler getGLProfiler () {
		return glProfiler;
	}

	public void registerForDragPoints (IDragPointProvider dragPointProvider) {
		this.dragPointProvider = dragPointProvider;
		DragPoint[] arr = dragPointProvider.fetchDragPoints();
		dragPoints.clear();
		for (int i = 0; i < arr.length; i++) {
			dragPoints.add(arr[i]);
		}
	}

	public void unregisterDragPoints () {
		this.dragPointProvider = null;
		dragPoints.clear();
	}

	public void unregisterDragPoints (IDragPointProvider dragPointProvider) {
		if (this.dragPointProvider == dragPointProvider) {
			this.dragPointProvider = null;
			dragPoints.clear();
		}
	}

	public abstract void removePreviewImage ();

	public abstract void gridSizeChanged (float size);

	public void setParticleEffect (VFXProjectData vfxProjectData) {
		this.vfxProjectData = vfxProjectData;

	}

	@Override
	protected VFXProjectData getEventContext() {
		return vfxProjectData;
	}
}
