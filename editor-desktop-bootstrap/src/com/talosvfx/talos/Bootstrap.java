package com.talosvfx.talos;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.update4j.Archive;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.UpdateContext;
import org.update4j.UpdateOptions;
import org.update4j.service.Delegate;
import org.update4j.service.UpdateHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Bootstrap extends Application implements Delegate {

	@FXML
	Label updateLabel;

	@FXML
	ProgressBar progressBar;
	private AppUpdater appUpdater;

	@Override
	public void start (Stage primaryStage) throws Exception {

		appUpdater = new AppUpdater(this);

		URL configUrl = new URL("https://talosvfx.com/update.xml");
		Configuration config = null;
		try (Reader in = new InputStreamReader(configUrl.openStream(), StandardCharsets.UTF_8)) {
			config = Configuration.read(in);
		} catch (IOException e) {
			System.err.println("Could not load remote config, falling back to local.");
			try (Reader in = Files.newBufferedReader(Path.of(getClass().getResource("config.xml").toURI()))) {
				config = Configuration.read(in);
			}
		}



		// set up the scene
		FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
		loader.setController(this);
		Parent root = loader.load();
		Scene scene = new Scene(root);


		URL resource = getClass().getResource("root.css");
		if (resource == null) {
			System.out.println("NO css found");
		} else {
			scene.getStylesheets().add(resource.toExternalForm());
		}
		// set up the stage
		primaryStage.setTitle("Talos VFX");
		primaryStage.setWidth(400);
		primaryStage.setHeight(400);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setScene(scene);
		primaryStage.show();

		if (config.requiresUpdate()) {
			updateLabel.setText("Updating");

			Configuration finalConfig = config;
			Task<Void> doUpdate = new Task<Void>() {
				@Override
				protected Void call () throws Exception {
					Path zip = Paths.get("talos.zip");
					if (finalConfig.update(UpdateOptions.archive(zip).updateHandler(appUpdater)).getException() == null) {
						Archive.read(zip).install();
					}
					return null;
				}
			};

			Thread thread = new Thread(doUpdate);
			thread.setDaemon(true);
			thread.start();

		}

	}


	@Override
	public void main (List<String> args) throws Throwable {
	}


	public static void main (String[] args) {
		launch(args);
	}


}
