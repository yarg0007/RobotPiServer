package com.yarg.robotpiserver.config;

public class Config {

	private String targetDataLineMixerName = "Set [plughw:1,0]";
	private String sourceDataLineMixerName = "Set [plughw:1,0]";
	private int sourceDataLineDatagramPort = 49809;
	private int targetDataLineDatagramPort = 49808;
	private int inputControlDatagramPort = 49801;
	private int videoStreamWidth = 1280;
	private int videoStreamHeight = 720;
	private int videoStreamFPS = 15;

	public String getTargetDataLineMixerName() {
		return targetDataLineMixerName;
	}

	public void setTargetDataLineMixerName(String targetDataLineMixerName) {
		this.targetDataLineMixerName = targetDataLineMixerName;
	}

	public String getSourceDataLineMixerName() {
		return sourceDataLineMixerName;
	}

	public void setSourceDataLineMixerName(String sourceDataLineMixerName) {
		this.sourceDataLineMixerName = sourceDataLineMixerName;
	}

	public int getSourceDataLineDatagramPort() {
		return sourceDataLineDatagramPort;
	}

	public void setSourceDataLineDatagramPort(int sourceDataLineDatagramPort) {
		this.sourceDataLineDatagramPort = sourceDataLineDatagramPort;
	}

	public int getTargetDataLineDatagramPort() {
		return targetDataLineDatagramPort;
	}

	public void setTargetDataLineDatagramPort(int targetDataLineDatagramPort) {
		this.targetDataLineDatagramPort = targetDataLineDatagramPort;
	}

	public int getInputControlDatagramPort() {
		return inputControlDatagramPort;
	}

	public void setInputControlDatagramPort(int inputControlDatagramPort) {
		this.inputControlDatagramPort = inputControlDatagramPort;
	}

	public int getVideoStreamWidth() {
		return videoStreamWidth;
	}

	public void setVideoStreamWidth(int videoStreamWidth) {
		this.videoStreamWidth = videoStreamWidth;
	}

	public int getVideoStreamHeight() {
		return videoStreamHeight;
	}

	public void setVideoStreamHeight(int videoStreamHeight) {
		this.videoStreamHeight = videoStreamHeight;
	}

	public int getVideoStreamFPS() {
		return videoStreamFPS;
	}

	public void setVideoStreamFPS(int videoStreamFPS) {
		this.videoStreamFPS = videoStreamFPS;
	}
}
