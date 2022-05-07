package com.yarg.robotpiserver.audio;

import com.yarg.robotpiserver.video.VideoStream;

public class AudioStreamServer extends Thread implements DatagramClientReturnAddress {

	private int RECEIVE_PORT = 49809;

	private int SEND_PORT = 49808;

	private String SERVER_ADDRESS;

	private SourceDataLineThread incomingStream;
	private TargetDataLineThread microphoneStream;

	private VideoStream videoStream;

	public AudioStreamServer() {
		SERVER_ADDRESS = null;
		incomingStream = new SourceDataLineThread(RECEIVE_PORT, this);
		microphoneStream = new TargetDataLineThread(this, SEND_PORT);

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
		videoStream.stopVideoStream();
		incomingStream.stopAudioStreamSpeakers();
		microphoneStream.stopAudioStreamMicrophone();
	}

	public void addAudioLevelListener(AudioLevelListener listener) {
		incomingStream.addAudioLevelListener(listener);
	}

	public void removeAudioLevelListener(AudioLevelListener listener) {
		incomingStream.removeAudioLevelListener(listener);
	}

	// -------------------------------------------------------------------------
	// Methods required by DatagramClientReturnAddress
	// -------------------------------------------------------------------------

	@Override
	public void setAddress(String address) {
		SERVER_ADDRESS = address;

		// The client address used for audio is the same one we want to use for
		// sending the video stream on. Hence, the video is started here.
		// Stopping of the video stream is also handled at the same time as the
		// audio stream is closed.
		videoStream = new VideoStream(address);
		videoStream.startVideoStream();
	}

	@Override
	public String getAddress() {
		return SERVER_ADDRESS;
	}

}