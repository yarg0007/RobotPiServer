package com.yarg.robotpiserver.video;

import java.io.IOException;
import java.util.Objects;

public class VideoStream {

	private String address;
	private Runtime runtime;
	private Process videoStreamProcess;

	/**
	 * Initialize with specified client address to receive the video stream.
	 *
	 * @param address
	 *            Client address.
	 */
	public VideoStream(String address) {
		this(address, Runtime.getRuntime());
	}

	/**
	 * Initialize with specified client address to receive the video stream and
	 * with the custom runtime.
	 *
	 * @param address
	 *            Client address.
	 * @param customRuntime
	 *            Custom runtime.
	 */
	public VideoStream(String address, Runtime customRuntime) {
		Objects.requireNonNull(address, "Address MUST NOT be null.");
		this.address = address;
		this.runtime = customRuntime;
	}

	/**
	 * Check if the video stream is running.
	 *
	 * @return True if running, false otherwise.
	 */
	public boolean isVideoStreamRunning() {
		if (videoStreamProcess != null) {
			return videoStreamProcess.isAlive();
		}

		return false;
	}

	/**
	 * Start the video stream with the client.
	 */
	public void startVideoStream() {

		stopVideoStream();

		String videoCommand = String.format(
				"/usr/bin/raspivid -n -t 0 -h 480 -w 640 -fps 10 -hf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! udpsink host=%s port=5000",
				address);

		try {
			String[] cmd = { "/bin/sh", "-c", videoCommand };

			videoStreamProcess = runtime.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop the video stream.
	 */
	public void stopVideoStream() {
		if (isVideoStreamRunning()) {
			videoStreamProcess.destroy();
		}
	}
}
