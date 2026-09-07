package com.yarg.robotpiserver.audio;

import com.yarg.gen.models.ConfigurationModelAudioStreamServer;

public class AudioStreamServer {

	private SourceDataLineThread incomingStream;
	private TargetDataLineThread microphoneStream;

	public AudioStreamServer(ConfigurationModelAudioStreamServer audioConfig) {

//		incomingStream = new SourceDataLineThread(audioConfig.getReceivePort());
//		microphoneStream = new TargetDataLineThread(audioConfig.getSendPort());

		incomingStream.initialize();
		microphoneStream.initialize();
	}

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