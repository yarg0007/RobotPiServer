package com.yarg.robotpiserver.video;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.yarg.robotpiserver.config.ConfigLoader;

public class VideoStream {

	private Runtime runtime;
	private Process videoStreamProcess;
	private static final String MJPEG_COMMAND = "./mjpg-streamer-master/mjpg-streamer-experimental/mjpg_streamer -o \"mjpg-streamer-master/mjpg-streamer-experimental/output_http.so -w ./www\" -i \"mjpg-streamer-master/mjpg-streamer-experimental/input_raspicam.so -x %d -y %d -fps %d\"";

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
			String mjpegCommand = String.format(MJPEG_COMMAND, ConfigLoader.getInstance().getVideoStreamWidth(), ConfigLoader.getInstance().getVideoStreamHeight(), ConfigLoader.getInstance().getVideoStreamFPS());
			//String[] cmd = { "/bin/sh", "-c", "cd ~/mjpg-streamer-master/mjpg-streamer-experimental/", "export LD_LIBRARY_PATH=.", mjpegCommand };

			String[] cmd = { mjpegCommand };

			videoStreamProcess = runtime.exec(cmd);

			InputStream is = videoStreamProcess.getErrorStream();
			if (is != null) {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} else {
				System.out.println("No video console input stream available.");
			}
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
