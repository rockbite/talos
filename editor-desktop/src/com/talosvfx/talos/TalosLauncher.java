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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.talosvfx.talos.editor.UIStage;
import com.talosvfx.talos.editor.WorkplaceStage;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWDropCallback;

import static org.lwjgl.glfw.GLFW.glfwSetDropCallback;
import static org.lwjgl.system.MemoryUtil.*;

public class TalosLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1200, 900);
		config.setTitle("Talos");
		config.useVsync(true);
		config.setHdpiMode(HdpiMode.Pixels);
		config.setBackBufferConfig(1,1,1,1,8,8, 0);
		config.setWindowIcon("icon/talos-64x64.png");

		TalosMain talos = new TalosMain() {
			@Override
			public void create () {
				super.create();
				afterCreated();
			}
		};

		boolean gl3 = false;

		if (gl3) {
			config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 2);

			ShaderProgram.prependVertexCode = "#version 330 core\n";
			ShaderProgram.prependFragmentCode = "#version 330 core\n";
		}
		



//		GLFWWindowFocusCallback glfwWindowFocusCallback = GLFWWindowFocusCallback.create(new GLFWWindowFocusCallback() {
//			@Override
//			public void invoke (long window, boolean focused) {
//				Gdx.app.postRunnable(new Runnable() {
//					@Override
//					public void run () {
//						TalosMain.focused = focused;
//					}
//				});
//			}
//		});
//		GLFW.glfwSetWindowFocusCallback(((Lwjgl3Graphics)Gdx.graphics).getWindow().getWindowHandle(), glfwWindowFocusCallback);

		new Lwjgl3Application(talos, config);
	}

	private static void afterCreated () {
		final Lwjgl3Graphics graphics = (Lwjgl3Graphics)Gdx.graphics;
		glfwSetDropCallback(graphics.getWindow().getWindowHandle(), new GLFWDropCallback() {
			@Override
			public void invoke (long window, int count, long names) {

				PointerBuffer namebuffer = memPointerBuffer(names, count);
				final String[] filesPaths = new String[count];
				for (int i = 0; i < count; i++) {
					String pathToObject = memUTF8(memByteBufferNT1(namebuffer.get(i)));
					filesPaths[i] = pathToObject;
				}

				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run () {
						final int x = Gdx.input.getX();
						final int y = Gdx.input.getY();

						try {
							final UIStage uiStage = TalosMain.Instance().UIStage();
							final WorkplaceStage nodeStage = TalosMain.Instance().getNodeStage();
							nodeStage.fileDrop(filesPaths, x, y);
							uiStage.fileDrop(filesPaths, x, y);
						}  catch (Exception e) {
							TalosMain.Instance().reportException(e);
						}
					}
				});
			}
		});
	}
}
