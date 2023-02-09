package com.talosvfx.talos;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.talosvfx.talos.data.ChannelData;
import com.talosvfx.talos.data.LocalPreferences;
import com.talosvfx.talos.data.RepoData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.update4j.Archive;
import org.update4j.Configuration;
import org.update4j.UpdateOptions;
import org.update4j.service.DefaultLauncher;
import org.update4j.service.Delegate;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Bootstrap extends Application implements Delegate {


	private static Start start;
	@FXML
	Label updateLabel;

	@FXML
	ProgressBar progressBar;

	@FXML
	ComboBox<ChannelData> versionBox;

	@FXML
	Button updateButton;
	@FXML
	Button launchButton;

	@FXML
	CheckBox autoLaunchCheckbox;

	private AppUpdater appUpdater;

	private LocalPreferences preferences;
	private ChannelData currentTarget;

	private Configuration currentConfig;
	private OkHttpClient okHttpClient;
	private boolean wasAutoLaunchOnLoad;
	private Stage stage;

	@FXML
	public void onAutoLaunch (ActionEvent actionEvent) {
		preferences.setAutoLaunch(autoLaunchCheckbox.isSelected());
		savePrefsToFile();
	}

	@FXML
	public void onUpdateButton (ActionEvent actionEvent) {
		if (currentConfig != null) {
			updateButton.setDisable(true);
			launchButton.setDisable(true);

			updateLabel.setText("Updating to " + currentConfig.getResolvedProperty("version"));

			Configuration finalConfig = currentConfig;

			progressBar.setVisible(true);
			Task<Void> doUpdate = new Task<Void>() {
				@Override
				protected Void call () throws Exception {
					Path zip = Paths.get("talos.zip");
					if (finalConfig.update(UpdateOptions.archive(zip).updateHandler(appUpdater)).getException() == null) {
						try {
							Archive.read(zip).install();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					Platform.runLater(new Runnable() {
						@Override
						public void run () {
							updateLabel.setText("Update complete");
							launchButton.setDisable(false);
						}
					});
					return null;
				}
			};

			Thread thread = new Thread(doUpdate);
			thread.setName("DoUpdateThread");
			thread.start();

		}
	}

	@FXML
	public void onLaunchButton (ActionEvent actionEvent) {
		if (currentConfig != null) {

			currentConfig.launch(new CustomLauncher(start));
			exitBootsrap();
		}
	}

	private void exitBootsrap () {
		stage.close();
	}

	@FXML
	public void onVersionBoxSelect (ActionEvent actionEvent) {
		SingleSelectionModel<ChannelData> channelDataSingleSelectionModel = versionBox.selectionModelProperty().get();
		currentTarget = channelDataSingleSelectionModel.getSelectedItem();

		preferences.setSelectedChannel(currentTarget.getVersionIdentifier());
		savePrefsToFile();

		//Check the config

		checkConfigForCurrentTarget();
	}

	private void checkConfigForCurrentTarget () {
		updateButton.setDisable(true);
		launchButton.setDisable(true);

		updateLabel.setText("Checking for updates on " + currentTarget.getVersionIdentifier() + " channel...");

		try {
			URL configUrl = new URL("https://editor.talosvfx.com/channels/" + currentTarget.getVersionIdentifier() + "/config.xml");
			currentConfig = null;
			try (Reader in = new InputStreamReader(configUrl.openStream(), StandardCharsets.UTF_8)) {
				currentConfig = Configuration.read(in);

				if (currentConfig.requiresUpdate()) {
					Platform.runLater(new Runnable() {
						@Override
						public void run () {
							updateLabel.setText("Update available");
							updateButton.setDisable(false);

							launchButton.setDisable(!canLaunchChannel(currentTarget));

						}
					});
				} else {
					Platform.runLater(new Runnable() {
						@Override
						public void run () {
							updateLabel.setText("No updates found");
							launchButton.setDisable(false);

							if (wasAutoLaunchOnLoad) {
								onLaunchButton(null);
							}

						}
					});
				}

			} catch (IOException e) {
				System.err.println("Could not load remote config, falling back to local. TODO STORE OLD CACHE");
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	private boolean canLaunchChannel (ChannelData currentTarget) {
		String versionIdentifier = currentTarget.getVersionIdentifier();
		String userHome = System.getProperty("user.home");
		File file = new File(userHome + "/Talos/" + versionIdentifier);
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	private LocalPreferences readPrefsFromFile () {
		String userHome = System.getProperty("user.home");
		File file = new File(userHome + "/Talos/boostrapPreferences.json");
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				return new Gson().fromJson(reader, LocalPreferences.class);
			} catch (IOException e) {
				return new LocalPreferences();
			}
		} else {
			return new LocalPreferences();
		}
	}

	private void savePrefsToFile () {
		String userHome = System.getProperty("user.home");
		File file = new File(userHome + "/Talos/boostrapPreferences.json");
		file.getParentFile().mkdirs();
		String preferencesJson = new Gson().toJson(preferences);

		try (FileWriter writer = new FileWriter(file, false)) {
			writer.write(preferencesJson);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	@Override
	public void start (Stage primaryStage) throws Exception {
		this.stage = primaryStage;

		okHttpClient = new OkHttpClient();

		appUpdater = new AppUpdater(this);

//		URL configUrl = new URL("https://talosvfx.com/update.xml");
//		Configuration config = null;
//		try (Reader in = new InputStreamReader(configUrl.openStream(), StandardCharsets.UTF_8)) {
//			config = Configuration.read(in);
//		} catch (IOException e) {
//			System.err.println("Could not load remote config, falling back to local.");
//			try (Reader in = Files.newBufferedReader(Path.of(getClass().getResource("config.xml").toURI()))) {
//				config = Configuration.read(in);
//			}
//		}

		// set up the scene
		FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
		loader.setController(this);
		Parent root = loader.load();
		Scene scene = new Scene(root);

		scene.setFill(Color.TRANSPARENT);

		primaryStage.setTitle("Talos Launcher");
		primaryStage.setResizable(false);
		JMetro jMetro = new JMetro(Style.DARK);
		jMetro.setScene(scene);

		URL resource = getClass().getResource("root.css");
		if (resource == null) {
			System.out.println("NO css found");
		} else {
			scene.getStylesheets().add(resource.toExternalForm());
		}
		primaryStage.setScene(scene);
		primaryStage.show();

		versionBox.setDisable(true);
		updateButton.setDisable(true);
		launchButton.setDisable(true);

		progressBar.setVisible(false);

		preferences = readPrefsFromFile();

		autoLaunchCheckbox.setSelected(preferences.isAutoLaunch());

		wasAutoLaunchOnLoad = preferences.isAutoLaunch();

		fetchRepoData();

//		if (config.requiresUpdate()) {
//			updateLabel.setText("Updating");
//
//			Configuration finalConfig = config;
//			Task<Void> doUpdate = new Task<Void>() {
//				@Override
//				protected Void call () throws Exception {
//					Path zip = Paths.get("talos.zip");
//					if (finalConfig.update(UpdateOptions.archive(zip).updateHandler(appUpdater)).getException() == null) {
//						Archive.read(zip).install();
//					}
//					return null;
//				}
//			};
//
//			Thread thread = new Thread(doUpdate);
//			thread.setDaemon(true);
//			thread.start();
//
//		}

	}

	private void fetchRepoData () {
		updateLabel.setText("Fetching version data...");

		String url = "https://editor.talosvfx.com/channels/repo.json";

		Call call = okHttpClient.newCall(new Request.Builder().url(url).build());

		call.enqueue(new Callback() {
			@Override
			public void onFailure (@NotNull Call call, @NotNull IOException e) {
				onRepoDataFetchComplete(null);
				e.printStackTrace();
			}

			@Override
			public void onResponse (@NotNull Call call, @NotNull Response response) throws IOException {
				if (response.isSuccessful()) {
					ResponseBody body = response.body();
					if (body != null) {
						String responseString = body.string();
						try {
							RepoData repoData = new Gson().fromJson(responseString, RepoData.class);
							onRepoDataFetchComplete(repoData);
						} catch (JsonSyntaxException jsonSyntaxException) {
							jsonSyntaxException.printStackTrace();
							onRepoDataFetchComplete(null);
						}
					}
				} else {
					onRepoDataFetchComplete(null);
				}
			}
		});
	}

	private void onRepoDataFetchComplete (@Nullable RepoData repoData) {
		if (repoData == null) {
			//defaults
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run () {
					updateLabel.setText("Updates fetched");

					repoData.sort();
					for (ChannelData version : repoData.getVersions()) {
						versionBox.getItems().add(version);
					}

					versionBox.setDisable(false);

					if (preferences.getSelectedChannel() != null) {
						ChannelData channel = repoData.getChannel(preferences.getSelectedChannel());
						if (channel != null) {
							versionBox.getSelectionModel().select(channel);
						}
					}
				}
			});

		}
	}

	@Override
	public void main (List<String> args) throws Throwable {
		System.out.println("main");
	}



	public static void main (String[] args, Start start) {
		Bootstrap.start = start;

		Platform.startup(new Runnable() {
			@Override
			public void run () {
				try {
					new Bootstrap().start(new Stage());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

}
