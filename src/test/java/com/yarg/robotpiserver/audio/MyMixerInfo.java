package com.yarg.robotpiserver.audio;

import javax.sound.sampled.Mixer.Info;

public class MyMixerInfo extends Info {

	public MyMixerInfo(String name, String vendor, String description, String version) {
		super(name, vendor, description, version);
	}
}
