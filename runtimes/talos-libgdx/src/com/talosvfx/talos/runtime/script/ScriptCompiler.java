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

package com.talosvfx.talos.runtime.script;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.scripts.SimpleReturnScript;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

public class ScriptCompiler {

	private static ScriptCompiler instance;

	public static ScriptCompiler instance () {
		if (instance == null) {
			instance = new ScriptCompiler();
		}
		return instance;
	}

	private final JavaCompiler compiler;

	public ScriptCompiler () {
		compiler = ToolProvider.getSystemJavaCompiler();
	}

	public SimpleReturnScript compile (String javaString) {
		JavaSourceFromString file = new JavaSourceFromString("SimpleRunIm", javaString);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		DynamicClassesFileManager manager = new DynamicClassesFileManager(compiler.getStandardFileManager(null, null, null));

		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
		JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null, compilationUnits);

		boolean success = task.call();
		for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
			System.err.print(String.format("Script compilation error: Line: %d - %s%n", diagnostic.getLineNumber(), diagnostic.getMessage(null)));
		}
		if (success) {
			try {

				System.out.println("Compiled");
				Class clazz = manager.loader.findClass("SimpleRunIm");

				return (SimpleReturnScript)ClassReflection.newInstance(clazz);
			} catch (ReflectionException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static class ByteClassLoader extends ClassLoader {

		private ObjectMap<String, JavaSourceFromString> cache = new ObjectMap<>();

		public ByteClassLoader () {
			super(ByteClassLoader.class.getClassLoader());
		}

		public void put (String name, JavaSourceFromString obj) {
			cache.put(name, obj);
		}

		@Override
		protected Class<?> findClass (String name) throws ClassNotFoundException {
			if (cache.containsKey(name)) {
				final JavaSourceFromString javaSourceFromString = cache.get(name);
				final byte[] classBytes = javaSourceFromString.getClassBytes();
				return defineClass(name, classBytes, 0, classBytes.length);
			}

			throw new GdxRuntimeException("Woopsy");
		}
	}


	public static class JavaSourceFromString extends SimpleJavaFileObject {

		private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		String code;

		JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),Kind.SOURCE);
			this.code = code;
		}

		JavaSourceFromString(String name, Kind kind) {
			super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}

		public byte[] getClassBytes () {
			return bos.toByteArray();
		}

		@Override
		public OutputStream openOutputStream () throws IOException {
			return bos;
		}
	}

	public static class DynamicClassesFileManager<FileManager> extends ForwardingJavaFileManager<JavaFileManager> {

		private ByteClassLoader loader = null;

		protected DynamicClassesFileManager (StandardJavaFileManager fileManager) {
			super(fileManager);
			try {
				loader = new ByteClassLoader();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JavaFileObject getJavaFileForOutput (Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
			JavaSourceFromString obj = new JavaSourceFromString(className, kind);
			loader.put(className, obj);
			return obj;
		}

		@Override
		public ClassLoader getClassLoader (Location location) {
			return loader;
		}
	}
}
