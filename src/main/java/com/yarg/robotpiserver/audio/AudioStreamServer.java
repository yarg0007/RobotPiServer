package com.yarg.robotpiserver.audio;

import com.yarg.robotpiserver.util.Generated;
import com.yarg.robotpiserver.video.VideoStream;

public class AudioStreamServer implements DatagramClientReturnAddress {

	private int RECEIVE_PORT = 49809;

	private int SEND_PORT = 49808;

	private String SERVER_ADDRESS;

	private SourceDataLineThread incomingStream;
	private TargetDataLineThread microphoneStream;
	private VideoStream videoStream;

	@Generated // Ignore Jacoco
	public AudioStreamServer() {
		SERVER_ADDRESS = null;
		incomingStream = new SourceDataLineThread(RECEIVE_PORT, this);
		microphoneStream = new TargetDataLineThread(this, SEND_PORT);

		incomingStream.initialize();
		microphoneStream.initialize();
	}

	public AudioStreamServer(SourceDataLineThread incomingStream, TargetDataLineThread microphoneStream,
			VideoStream videoStream) {
		this.incomingStream = incomingStream;
		this.microphoneStream = microphoneStream;
		this.videoStream = videoStream;
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
		//
		// NOTE: the client address used to be required for the video stream.
		// With the switch to MJPEG streamer we don't need that now as the client
		// is expected to hook into the server hostname.
		if (videoStream == null) {
			videoStream = new VideoStream();
		}
		videoStream.startVideoStream();
	}

	@Override
	public String getAddress() {
		return SERVER_ADDRESS;
	}

}