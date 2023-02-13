package com.talosvfx.talos;

import javafx.application.Platform;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Start {

	String[] args;
	BlockingQueue<Runnable> mainThreadRunnables = new LinkedBlockingQueue<>();

	private boolean shutdown = false;

	public Start (String[] args) {
		this.args = args;
	}

	public static void main (String[] args) {
		Start start = new Start(args);
		Bootstrap.main(args, start);


		//Wait for the booter to die and then start our lwjgl app
		start.exec();
	}

	private void exec () {
		while (!shutdown) {
			if (!mainThreadRunnables.isEmpty()) {
				try {
					mainThreadRunnables.take().run();

					//Its blocking, so we can wait until first task which is the talos app to ocmplete
					shutdown = true;
					System.exit(0);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

	}

}
