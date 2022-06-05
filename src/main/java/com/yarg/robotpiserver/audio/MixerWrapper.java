package com.yarg.robotpiserver.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

import com.yarg.robotpiserver.util.Generated;

/**
 * Wraps the static methods defined on AudioSystem. Allows for mocking of AudioSystem static methods.
 */
@Generated // Ignore Jacoco
public class MixerWrapper {

	/**
	 * Get mixer info.
	 *
	 * @return Mixer info.
	 */
	@Generated // Ignore Jacoco
	public Info[] getMixerInfo() {
		return AudioSystem.getMixerInfo();
	}

	/**
	 * Get mixer from mixerInfo.
	 *
	 * @param mixerInfo
	 *            MixerInfo to extract mixer from.
	 * @return Mixer.
	 */
	@Generated // Ignore Jacoco
	public Mixer getMixer(Info mixerInfo) {
		return AudioSystem.getMixer(mixerInfo);
	}
}
