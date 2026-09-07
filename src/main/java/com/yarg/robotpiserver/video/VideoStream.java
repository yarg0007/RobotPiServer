package com.yarg.robotpiserver.video;

import java.io.IOException;
import java.util.Objects;

public class VideoStream {

	private String address;
	private int port;
	private Runtime runtime;
	private Process videoStreamProcess;

	/**
	 * Initialize with specified client address and port to receive the video stream and
	 * with the specified runtime (Expect: Runtime.getRuntime() to be used as default).
	 *
	 * @param address
	 *            Client address.
	 * @param port The port to connect on.
	 * @param customRuntime
	 *            Provided runtime. Typically, this will be Runtime.getRuntime();.
	 */
	public VideoStream(String address, int port, Runtime customRuntime) {
		Objects.requireNonNull(address, "Address MUST NOT be null.");
		Objects.requireNonNull(customRuntime, "Runtime MUST NOT be null.");
		this.address = address;
		this.port = port;
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
				"/usr/bin/raspivid -n -t 0 -h 480 -w 640 -fps 10 -hf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! udpsink host=%s port=%d",
				address, port);

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
