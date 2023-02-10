package com.talosvfx.talos;

import javafx.application.Platform;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Start {

	BlockingQueue<Runnable> mainThreadRunnables = new LinkedBlockingQueue<>();


	public static void main (String[] args) {
		Start start = new Start();
		Bootstrap.main(args, start);


		//Wait for the booter to die and then start our lwjgl app
		start.exec();
		System.exit(0);
	}

	private void exec () {
		if (!mainThreadRunnables.isEmpty()) {
			try {
				mainThreadRunnables.take().run();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
