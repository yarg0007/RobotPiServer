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
			// Process.isAlive() requires Java 8; use exitValue() for Java 7 compatibility.
			// exitValue() throws IllegalThreadStateException if the process is still running.
			try {
				videoStreamProcess.exitValue();
				return false;
			} catch (IllegalThreadStateException e) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Start the video stream with the client.
	 */
	public void startVideoStream() {

		stopVideoStream();

		// -ih embeds SPS/PPS headers at every IDR frame so reconnecting clients
		// can sync immediately without "damaged access unit" errors.
		// -g 15 sets a 1-second GOP at 15 fps; smaller GOPs allow faster seek/sync.
		// VLC: -q suppresses verbose logging (saves significant CPU on ARMv6).
		// --sout-rtp-caching=0 and --no-sout-rtp-synchronisation minimize transmission delay.
		String videoCommand = String.format(
				"/usr/bin/raspivid -n -t 0 -h 480 -w 640 -fps 15 -hf -b 2000000 -ih -g 15 -o - | " +
				"/usr/bin/cvlc -q stream:///dev/stdin --sout '#rtp{sdp=rtsp://:%d/}' :demux=h264" +
				" --sout-rtp-caching=0 --no-sout-rtp-synchronisation",
				port);

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
			videoStreamProcess = null;
		}
	}
}
