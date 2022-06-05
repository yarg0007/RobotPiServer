package com.yarg.robotpiserver.video;

import java.io.IOException;

public class VideoStream {

	private Runtime runtime;
	private Process videoStreamProcess;
	private static final String MJPEG_COMMAND = "./mjpg_streamer -o \"output_http.so -w ./www\" -i \"input_raspicam.so -x 1280 -y 720 -fps 15\"";

	public VideoStream() {
		this.runtime = Runtime.getRuntime();
	}

	/**
	 * Initialize with custom runtime.
	 *
	 * @param customRuntime
	 *            Custom runtime.
	 */
	public VideoStream(Runtime customRuntime) {
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

		try {
			String[] cmd = { "/bin/sh", "-c", "cd ~/mjpg-streamer-master/mjpg-streamer-experimental/", "export LD_LIBRARY_PATH=.", MJPEG_COMMAND };

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
