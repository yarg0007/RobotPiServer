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

		// Raw H264 piped directly to netcat: Android receives via TCP and decodes with
		// MediaCodec hardware decoder. This eliminates the RTSP/VLC jitter buffer that
		// caused multi-second lag. Android connects to this port after the process starts.
		// -ih embeds SPS/PPS at every IDR so the decoder can sync on first keyframe.
		// -b 1500000 keeps bandwidth manageable on the slow ARMv6.
		String videoCommand = String.format(
				"/usr/bin/raspivid -n -t 0 -h 480 -w 640 -fps 15 -b 1500000 -ih -o - | nc -l %d",
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
		// Kill any orphaned raspivid/nc child processes from the previous pipeline.
		// /bin/sh -c "cmd | nc" does not forward SIGTERM to children so they linger,
		// and the next nc -l call fails to bind if nc is still holding the port.
		try {
			runtime.exec(new String[]{"/bin/sh", "-c", "pkill -x raspivid 2>/dev/null; pkill -f 'nc -l' 2>/dev/null"});
			Thread.sleep(300);
		} catch (Exception ignored) {}
	}
}
