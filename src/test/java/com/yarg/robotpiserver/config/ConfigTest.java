package com.yarg.robotpiserver.config;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class ConfigTest {

	private Config config = new Config();

	@Test
	public void defaultTargetDataLineMixerName() {
		assertEquals(config.getTargetDataLineMixerName(), "Set [plughw:1,0]");
	}

	@Test
	public void defaultSourceDataLineMixerName() {
		assertEquals(config.getSourceDataLineMixerName(), "Set [plughw:1,0]");
	}

	@Test
	public void defaultSourceDataLineDatagramPort() {
		assertEquals(config.getSourceDataLineDatagramPort(), 49809);
	}

	@Test
	public void defaultTargetDataLineDatagramPort() {
		assertEquals(config.getTargetDataLineDatagramPort(), 49808);
	}

	@Test
	public void defaultInputControlDatagramPort() {
		assertEquals(config.getInputControlDatagramPort(), 49801);
	}

	@Test
	public void defaultVideoStreamWidth() {
		assertEquals(config.getVideoStreamWidth(), 1280);
	}

	@Test
	public void defaultVideoStreamHeight() {
		assertEquals(config.getVideoStreamHeight(), 720);
	}

	@Test
	public void defaultVideoStreamFPS() {
		assertEquals(config.getVideoStreamFPS(), 15);
	}
}
