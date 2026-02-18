package com.talosvfx.talos.editor.project2.batch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.save.ExportCompleteEvent;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository.AssetRepositoryCatalogueExportOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchExportRunner implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(BatchExportRunner.class);

	private final BatchExportScript script;
	private int currentIndex;

	public BatchExportRunner (BatchExportScript script) {
		this.script = script;
	}

	public void start () {
		currentIndex = 0;
		Notifications.registerObserver(this);
		openNext();
	}

	private void openNext () {
		if (currentIndex >= script.projects.size) {
			logger.info("Batch export complete, all {} projects exported", script.projects.size);
			Notifications.unregisterObserver(this);
			Gdx.app.exit();
			return;
		}

		BatchExportScript.BatchExportEntry entry = script.projects.get(currentIndex);
		logger.info("Batch export [{}/{}]: opening project {}", currentIndex + 1, script.projects.size, entry.projectPath);

		boolean success = SharedResources.talosControl.validateAndOpenProject(new FileHandle(entry.projectPath));
		if (!success) {
			logger.error("Failed to open project: {}, skipping", entry.projectPath);
			currentIndex++;
			openNext();
		}
	}

	@EventHandler
	public void onProjectLoaded (ProjectLoadedEvent event) {
		BatchExportScript.BatchExportEntry entry = script.projects.get(currentIndex);
		logger.info("Project loaded, starting optimized export to {}", entry.exportPath);

		AssetRepositoryCatalogueExportOptions settings = new AssetRepositoryCatalogueExportOptions();
		settings.setExportPathHandle(Gdx.files.absolute(entry.exportPath));

		try {
			AssetRepository.getInstance().exportToFile(settings, true);
		} catch (Exception e) {
			logger.error("Export failed for project: {}", entry.projectPath, e);
			currentIndex++;
			openNext();
		}
	}

	@EventHandler
	public void onExportComplete (ExportCompleteEvent event) {
		BatchExportScript.BatchExportEntry entry = script.projects.get(currentIndex);
		logger.info("Export complete for project: {}", entry.projectPath);

		SharedResources.projectLoader.unloadProject();
		currentIndex++;
		openNext();
	}
}
