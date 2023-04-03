package com.yarg.robotpiserver.config;

import com.google.gson.Gson;
import com.yarg.gen.models.ConfigurationModel;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Configuration {

    private static volatile Configuration instance;

    private ConfigurationModel configurationModel;

    private Configuration() {
        init();
    }

    /**
     * Get the singleton instance.
     * @return Singleton instance.
     */
    public static Configuration getInstance() {

        if (instance == null) {
            synchronized (Configuration.class) {
                if (instance == null) {
                    instance = new Configuration();
                }
            }
        }

        return instance;
    }

    /**
     * Reinitialize with the specified config resource file.
     * @param resource Resource file to load as the current configuration.
     * @throws URISyntaxException If getting the URI for the resource file fails.
     */
    public void reinitializeWithResourceConfig(String resource) throws URISyntaxException {
        URL resourceUrl = this.getClass().getResource(resource);
        File configFile = new File(resourceUrl.toURI().getPath());
        parseConfigFile(configFile);
    }

    /**
     * Reinitialize to the default configuration.
     */
    public void reinitializeToDefault() {
        init();
    }

    /**
     * Get the configuration model.
     * @return Configuration model.
     */
    public ConfigurationModel getConfigurationModel() {
        return configurationModel;
    }

    private void init() {
        String currentWorkingDirectory = System.getProperty("user.dir");
        File configFile = new File(currentWorkingDirectory, "config.json");
        parseConfigFile(configFile);
    }

    private void parseConfigFile(File configFile) {

        String fileContent = "";
        try {
            byte[] bytes = Files.readAllBytes(configFile.toPath());
            fileContent = new String (bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error parsing config file [" + configFile.getPath() + "]. Please make sure the path is correct, the file exists and it conforms to the configuration as defined in the openapi.yaml.");
        }

        Gson gson = new Gson();
        configurationModel = gson.fromJson(fileContent, ConfigurationModel.class);
    }
}
