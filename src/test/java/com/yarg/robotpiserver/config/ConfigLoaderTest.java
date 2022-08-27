package com.yarg.robotpiserver.config;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigLoaderTest {

	private static final String fileName = "robotPiConfig.json";

	@BeforeMethod
	public void resetSingleton() {
		ConfigLoader.reset();
	}

	@AfterMethod
	public void removeLocalConfigFile() {
		String rootPath = System.getProperty("user.dir");
		File file = new File(rootPath, fileName);
		if (file.exists()) {
			file.delete();
		}
	}

	// -------------------------
	// Evaluate default config

	@Test
	public void defaultTargetDataLineMixerName() {
		assertEquals(ConfigLoader.getInstance().getTargetDataLineMixerName(), "Set [plughw:1,0]");
	}

	@Test
	public void defaultSourceDataLineMixerName() {
		assertEquals(ConfigLoader.getInstance().getSourceDataLineMixerName(), "Set [plughw:1,0]");
	}

	@Test
	public void defaultSourceDataLineDatagramPort() {
		assertEquals(ConfigLoader.getInstance().getSourceDataLineDatagramPort(), 49809);
	}

	@Test
	public void defaultTargetDataLineDatagramPort() {
		assertEquals(ConfigLoader.getInstance().getTargetDataLineDatagramPort(), 49808);
	}

	@Test
	public void defaultInputControlDatagramPort() {
		assertEquals(ConfigLoader.getInstance().getInputControlDatagramPort(), 49801);
	}

	@Test
	public void defaultVideoStreamWidth() {
		assertEquals(ConfigLoader.getInstance().getVideoStreamWidth(), 1280);
	}

	@Test
	public void defaultVideoStreamHeight() {
		assertEquals(ConfigLoader.getInstance().getVideoStreamHeight(), 720);
	}

	@Test
	public void defaultVideoStreamFPS() {
		assertEquals(ConfigLoader.getInstance().getVideoStreamFPS(), 15);
	}

	// -------------------------
	// Evaluate config loading

	@Test
	public void loadTargetDataLineMixerName() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getTargetDataLineMixerName(), "targetMixerName");
	}

	@Test
	public void loadSourceDataLineMixerName() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getSourceDataLineMixerName(), "sourceMixerName");
	}

	@Test
	public void loadSourceDataLineDatagramPort() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getSourceDataLineDatagramPort(), 1);
	}

	@Test
	public void loadTargetDataLineDatagramPort() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getTargetDataLineDatagramPort(), 2);
	}

	@Test
	public void loadInputControlDatagramPort() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getInputControlDatagramPort(), 3);
	}

	@Test
	public void loadVideoStreamWidth() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getVideoStreamWidth(), 4);
	}

	@Test
	public void loadVideoStreamHeight() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getVideoStreamHeight(), 5);
	}

	@Test
	public void loadVideoStreamFPS() throws Exception {
		addFile();
		assertEquals(ConfigLoader.getInstance().getVideoStreamFPS(), 6);
	}

	@Test
	public void loadConfigWithMissingValues() throws Exception {

		StringBuilder jsonBody = new StringBuilder();
		jsonBody.append("{\n");
		jsonBody.append("\t\"targetDataLineMixerName\": \"targetMixerName\",\n");
		jsonBody.append("\t\"sourceDataLineDatagramPort\": 1,\n");
		jsonBody.append("\t\"targetDataLineDatagramPort\": 2,\n");
		jsonBody.append("\t\"inputControlDatagramPort\": 3,\n");
		jsonBody.append("\t\"videoStreamHeight\": 5,\n");
		jsonBody.append("\t\"videoStreamFPS\": 6\n");
		jsonBody.append("}");

		String rootPath = System.getProperty("user.dir");
		File file = new File(rootPath, fileName);
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(jsonBody.toString());
		fileWriter.close();

		assertEquals(ConfigLoader.getInstance().getTargetDataLineMixerName(), "targetMixerName");
		assertEquals(ConfigLoader.getInstance().getSourceDataLineMixerName(), "Set [plughw:1,0]");
		assertEquals(ConfigLoader.getInstance().getSourceDataLineDatagramPort(), 1);
		assertEquals(ConfigLoader.getInstance().getTargetDataLineDatagramPort(), 2);
		assertEquals(ConfigLoader.getInstance().getInputControlDatagramPort(), 3);
		assertEquals(ConfigLoader.getInstance().getVideoStreamWidth(), 1280);
		assertEquals(ConfigLoader.getInstance().getVideoStreamHeight(), 5);
		assertEquals(ConfigLoader.getInstance().getVideoStreamFPS(), 6);
	}

	private void addFile() throws IOException {

		StringBuilder jsonBody = new StringBuilder();
		jsonBody.append("{\n");
		jsonBody.append("\t\"targetDataLineMixerName\": \"targetMixerName\",\n");
		jsonBody.append("\t\"sourceDataLineMixerName\": \"sourceMixerName\",\n");
		jsonBody.append("\t\"sourceDataLineDatagramPort\": 1,\n");
		jsonBody.append("\t\"targetDataLineDatagramPort\": 2,\n");
		jsonBody.append("\t\"inputControlDatagramPort\": 3,\n");
		jsonBody.append("\t\"videoStreamWidth\": 4,\n");
		jsonBody.append("\t\"videoStreamHeight\": 5,\n");
		jsonBody.append("\t\"videoStreamFPS\": 6\n");
		jsonBody.append("}");

		String rootPath = System.getProperty("user.dir");
		File file = new File(rootPath, fileName);
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(jsonBody.toString());
		fileWriter.close();
	}
}
