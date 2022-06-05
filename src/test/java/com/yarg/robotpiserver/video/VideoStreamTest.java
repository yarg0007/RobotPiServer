package com.yarg.robotpiserver.video;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.Test;

public class VideoStreamTest {

	private VideoStream videoStream;

	private Runtime runtime = mock(Runtime.class);
	private Process process = mock(Process.class);

	@Test
	public void videoStreamNotRunningUntilStarted() {

		videoStream = new VideoStream(runtime);
		boolean videoStreamRunning = videoStream.isVideoStreamRunning();
		assertFalse(videoStreamRunning, "Video stream MUST NOT be running until stream is started.");
	}

	@Test
	public void startAndStopVideoStream() throws Exception {

		when(runtime.exec(Mockito.any(String[].class))).thenReturn(process);
		doNothing().when(process).destroy();
		when(process.isAlive()).thenReturn(true);

		videoStream = new VideoStream(runtime);
		videoStream.startVideoStream();
		verify(process, never()).destroy();

		boolean videoStreamRunning = videoStream.isVideoStreamRunning();
		assertTrue(videoStreamRunning, "Video stream MUST be running once it is started.");

		videoStream.stopVideoStream();

		verify(process, times(1)).destroy();
	}
}
