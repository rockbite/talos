package com.talosvfx.talos.editor.project2.savestate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.save.ExportRequest;
import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.notifications.CommandEventHandler;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.commands.CommandEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class SaveSystem implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(SaveSystem.class);

    public SaveSystem () {
        Notifications.registerObserver(this);
    }

    @CommandEventHandler(commandType = Commands.CommandType.SAVE)
    public void onSaveAction (CommandEvent actionEvent) {
        Notifications.fireEvent(Notifications.obtainEvent(SaveRequest.class));
    }


    @EventHandler
    public void onSave (SaveRequest event) {
        if (SharedResources.currentProject != null) {
            TalosProjectData currentProject = SharedResources.currentProject;
            try {
                currentProject.save();
                Toasts.getInstance().showInfoToast("Project saved");
            } catch (Exception e) {
                logger.error("Failure to save", e);
                Toasts.getInstance().showErrorToast("Failure to save " + e.getMessage());
            }
        } else {
            Toasts.getInstance().showInfoToast("No project to save");
        }
    }

    @CommandEventHandler(commandType = Commands.CommandType.EXPORT)
    public void onExportAction (CommandEvent actionEvent) {
        Notifications.quickFire(ExportRequest.class);
    }

    @CommandEventHandler(commandType = Commands.CommandType.EXPORT_OPTIMIZED)
    public void onExportOptimized (CommandEvent event) {
        ExportRequest exportRequest = Notifications.obtainEvent(ExportRequest.class);
        exportRequest.setOptimized(true);
        Notifications.fireEvent(exportRequest);
    }

    public String getNodePath () {
        String nodePath = "";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "which node"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            nodePath = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodePath;
    }

    @EventHandler
    public void onExport (ExportRequest event) {
        logger.info("On export");

        TalosProjectData currentProject = SharedResources.currentProject;
        Preferences projectPrefs = TalosLocalPrefs.Instance().getProjectPrefs();
        String exportScript = projectPrefs.getString("project.general.exportScript", "");
        String projectFilePath = projectPrefs.getString("project.general.exportPath", "");

        logger.info("checking project path {}", projectFilePath);

        BaseAssetRepository.AssetRepositoryCatalogueExportOptions settings = new BaseAssetRepository.AssetRepositoryCatalogueExportOptions();
        settings.loadFromPrefs(projectPrefs);

        if (projectFilePath.isEmpty()) {
            Toasts.getInstance().showInfoToast("Provide export path to enable exporting");
            SharedResources.ui.showPreferencesWindow();
        } else {
            try {
                AssetRepository.getInstance().exportToFile(settings, event.isOptimized());
            } catch (Exception e) {
                logger.error("Error when exporting", e);
            }
        }

        if (!exportScript.isEmpty()) {
            Toasts.getInstance().showInfoToast("Export script defined, trying to run");

            FileHandle exportScriptHandle = settings.getExportScriptHandle();
            String projectPath = currentProject.rootProjectDir().path();

            if (exportScriptHandle.exists() && !exportScriptHandle.isDirectory()) {
                Runtime rt = Runtime.getRuntime();
                Toasts.getInstance().showInfoToast("Export script found in file system");

                FileHandle scriptBinaryHandle = settings.getScriptBinaryHandle();
                if (scriptBinaryHandle == null) {
                    Toasts.getInstance().showErrorToast("Script binary not found, make sure to set it in preferences");
                    return;
                }

                try {
                    String scriptCommandBinaryPath = scriptBinaryHandle.file().getAbsolutePath();
                    String buildScriptPath = exportScriptHandle.path();
                    String projectDirectoryPath = "\"" + projectPath + "\"";
                    String projectFilePathComm = "\"" + projectFilePath + "\"";

                    if (TalosMain.Instance().isOsX()) {
                        Toasts.getInstance().showInfoToast("Trying to launch build script runner for " + scriptCommandBinaryPath);

                        ProcessBuilder pb = new ProcessBuilder("bash", "-l", "-c", scriptCommandBinaryPath + " " + buildScriptPath + " " + projectDirectoryPath + " " + projectFilePathComm);
                        startAndPipeToLogger(pb);
                    } else {
                        ProcessBuilder pb = new ProcessBuilder(scriptCommandBinaryPath, buildScriptPath, projectDirectoryPath, projectFilePathComm);
                        startAndPipeToLogger(pb);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Error when running processor", e);
                    Toasts.getInstance().showErrorToast("Error when running processor " + e.getMessage());
                }
            } else {
                Toasts.getInstance().showInfoToast("Export script not found in file system");
            }
        } else {
            Toasts.getInstance().showInfoToast("No export script defined");
        }
    }

    private void startAndPipeToLogger (ProcessBuilder pb) throws IOException {
        Process start = pb.start();
        new Thread(new Runnable() {
            @Override
            public void run () {
                Scanner scanner = new Scanner(start.getInputStream());
                while (scanner.hasNextLine()) {
                    logger.info(scanner.nextLine());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run () {
                Scanner scanner = new Scanner(start.getErrorStream());
                while (scanner.hasNextLine()) {
                    logger.error(scanner.nextLine());
                }
            }
        }).start();
    }
}
