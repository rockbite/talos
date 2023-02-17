package com.talosvfx.talos;

import javafx.application.Platform;
import org.update4j.FileMetadata;
import org.update4j.service.UpdateHandler;

public class AppUpdater implements UpdateHandler {

	private final Bootstrap bootstrap;

	public AppUpdater (Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	@Override
	public void updateCheckUpdatesProgress (float frac) throws Throwable {
		UpdateHandler.super.updateCheckUpdatesProgress(frac);
	}

	@Override
	public void updateDownloadFileProgress (FileMetadata file, float frac) throws Throwable {
		UpdateHandler.super.updateDownloadFileProgress(file, frac);
		Platform.runLater(new Runnable() {
			@Override
			public void run () {
				bootstrap.updateLabel.setText("Downloading: " + file.getUri());
			}
		});
	}

	@Override
	public void updateDownloadProgress (float frac) throws Throwable {
		UpdateHandler.super.updateDownloadProgress(frac);
		Platform.runLater(new Runnable() {
			@Override
			public void run () {
				bootstrap.progressBar.setProgress(frac);

			}
		});
	}

	@Override
	public void failed (Throwable t) {
		UpdateHandler.super.failed(t);
		Platform.runLater(new Runnable() {
			@Override
			public void run () {
				bootstrap.updateLabel.setText(t.getMessage());

			}
		});
	}

	@Override
	public void succeeded () {
		UpdateHandler.super.succeeded();
		Platform.runLater(new Runnable() {
			@Override
			public void run () {
				bootstrap.updateLabel.setText("Completed update");

			}
		});
	}
}
