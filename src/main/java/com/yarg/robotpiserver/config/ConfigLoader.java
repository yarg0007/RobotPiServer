package com.yarg.robotpiserver.config;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

import com.google.gson.Gson;

public class ConfigLoader {

	private Config configuration;
	private static ConfigLoader instance;

	private ConfigLoader() {

		String currentWorkingDirectory = System.getProperty("user.dir");
		File configFile = new File(currentWorkingDirectory, "robotPiConfig.json");
		if (configFile.exists()) {
			System.out.println("Found config file - loading...");
			Reader reader = null;
			try {
				reader = Files.newBufferedReader(configFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			Gson gson = new Gson();
			configuration = gson.fromJson(reader, Config.class);
			System.out.println("Config file loaded.");
		} else {
			System.out.println("Using default configuration.");
			configuration = new Config();
		}
	}

	public static ConfigLoader getInstance() {

		if (instance == null) {
			synchronized (ConfigLoader.class) {
				if (instance == null) {
					instance = new ConfigLoader();
				}
			}
		}

		return instance;
	}

	protected static void reset() {
		instance = null;
	}

	public String getTargetDataLineMixerName() {
		return configuration.getTargetDataLineMixerName();
	}

	public String getSourceDataLineMixerName() {
		return configuration.getSourceDataLineMixerName();
	}

	public int getSourceDataLineDatagramPort() {
		return configuration.getSourceDataLineDatagramPort();
	}

	public int getTargetDataLineDatagramPort() {
		return configuration.getTargetDataLineDatagramPort();
	}

	public int getInputControlDatagramPort() {
		return configuration.getInputControlDatagramPort();
	}

	public int getVideoStreamWidth() {
		return configuration.getVideoStreamWidth();
	}

	public int getVideoStreamHeight() {
		return configuration.getVideoStreamHeight();
	}

	public int getVideoStreamFPS() {
		return configuration.getVideoStreamFPS();
	}
}
