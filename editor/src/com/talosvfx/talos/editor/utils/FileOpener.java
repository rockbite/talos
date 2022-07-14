package com.talosvfx.talos.editor.utils;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.macosx.ObjCRuntime;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.macosx.CoreFoundation.*;
import static org.lwjgl.system.macosx.ObjCRuntime.objc_getClass;
import static org.lwjgl.system.macosx.ObjCRuntime.sel_getUid;

public class FileOpener {

	public static void open (File file) {

//		if (true) {
//			openHackMac(file);
//			return;
//		}

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run () {
				if (!openDESKTOP(file) && !openSystem(file.getPath()))
					System.err.println("unable to open file " + System.getProperty("os.name"));
			}
		});
		thread.setName("OpenFile");
		thread.start();
	}
	private static void openHackMac (File file) {
		long objc_msgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");

		try (MemoryStack stack = stackPush()) {
			long fullPath = CFStringCreateWithCStringNoCopy(kCFAllocatorDefault, stack.UTF8(file.getAbsolutePath()), kCFStringEncodingUTF8, kCFAllocatorNull);

			long sharedWorkspace = invokePPP(objc_getClass("NSWorkspace"), sel_getUid("sharedWorkspace"), objc_msgSend);
			int result = invokePPPI(sharedWorkspace, sel_getUid("openFile:"), fullPath, objc_msgSend);
			System.out.println("SUCCESS: " + (result != 0 ? "YES" : "NO"));

			CFRelease(fullPath);
		}
	}

	private static boolean openSystem (String what) {

		String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

		if (os.contains("win")) {
			return (run("explorer", "%s", what));
		}

		if (os.contains("mac")) {
			return (run("open", "%s", what));
		}

		return run("kde-open", "%s", what) || run("gnome-open", "%s", what) || run("xdg-open", "%s", what);

	}

	private static boolean openDESKTOP (File file) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
			try {
				Desktop.getDesktop().open(file);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;

	}

	private static boolean run (String command, String arg, String file) {

		String[] args = arg.split(" ");
		String[] parts = new String[args.length + 1];
		parts[0] = command;
		for (int i = 0; i < args.length; i++) {
			parts[i + 1] = String.format(args[0], file).trim();
		}

		new Thread(new Runnable() {
			@Override
			public void run () {
				try {
					ProcessBuilder builder = new ProcessBuilder(parts);
					Process process = builder.start();
					process.waitFor();

					try {
						if (process.exitValue() == 0) {
						}
					} catch (IllegalThreadStateException itse) {
						itse.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {

				}
			}
		}).start();



		return true;
	}

}
