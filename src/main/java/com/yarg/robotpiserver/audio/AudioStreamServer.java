package com.yarg.robotpiserver.audio;

import com.yarg.gen.models.ConfigurationModelAudioStreamServer;
import com.yarg.robotpiserver.util.Generated;

import java.util.Objects;

public class AudioStreamServer {

	private SourceDataLineThread incomingStream;
	private TargetDataLineThread microphoneStream;

	/**
	 * Production constructor. Creates audio threads configured to stream to/from the given client IP.
	 * Both SourceDataLineThread and TargetDataLineThread initialize hardware in their constructors.
	 *
	 * @param clientIpAddress IP address of the connected client.
	 * @param audioConfig Audio stream configuration (receive/send ports, mixer name).
	 * @throws TargetDataLineException If the microphone mixer cannot be initialized.
	 */
	@Generated
	public AudioStreamServer(String clientIpAddress, ConfigurationModelAudioStreamServer audioConfig) throws TargetDataLineException {
		Objects.requireNonNull(clientIpAddress, "clientIpAddress MUST NOT be null.");
		Objects.requireNonNull(audioConfig, "audioConfig MUST NOT be null.");
		incomingStream = new SourceDataLineThread(audioConfig.getReceivePort());
		microphoneStream = new TargetDataLineThread(
				audioConfig.getMicrophoneMixerName(), clientIpAddress, audioConfig.getSendPort());
	}

	/**
	 * Test constructor. Injects pre-built thread instances for unit testing.
	 *
	 * @param incomingStream Thread receiving audio from client and playing through speakers.
	 * @param microphoneStream Thread capturing microphone and sending to client.
	 */
	public AudioStreamServer(SourceDataLineThread incomingStream, TargetDataLineThread microphoneStream) {
		this.incomingStream = incomingStream;
		this.microphoneStream = microphoneStream;
	}

	public void startAudioStream() {
		incomingStream.startAudioStreamSpeakers();
		microphoneStream.startAudioStreamMicrophone();
	}

	public void stopAudioStream() {
		incomingStream.stopAudioStreamSpeakers();
		microphoneStream.stopAudioStreamMicrophone();
	}

	public void addAudioLevelListener(AudioLevelListener listener) {
		incomingStream.addAudioLevelListener(listener);
	}

	public void removeAudioLevelListener(AudioLevelListener listener) {
		incomingStream.removeAudioLevelListener(listener);
	}
}
