package com.yarg.robotpiserver.video;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

public class VideoStreamTest {

	private VideoStream videoStream;

	private Runtime runtime = mock(Runtime.class);
	private Process process = mock(Process.class);
	private String ADDRESS = "http://localhost:8080";

	@Test
	public void videoStreamNotRunningUntilStarted() {

//		videoStream = new VideoStream(ADDRESS, runtime);
//		boolean videoStreamRunning = videoStream.isVideoStreamRunning();
//		assertFalse(videoStreamRunning, "Video stream MUST NOT be running until stream is started.");
	}

	@Test
	public void startAndStopVideoStream() throws Exception {

		when(runtime.exec(Mockito.any(String[].class))).thenReturn(process);
		doNothing().when(process).destroy();
		when(process.isAlive()).thenReturn(true);

//		videoStream = new VideoStream(ADDRESS, runtime);
//		videoStream.startVideoStream();
//		verify(process, never()).destroy();

		boolean videoStreamRunning = videoStream.isVideoStreamRunning();
		assertTrue(videoStreamRunning, "Video stream MUST be running once it is started.");

		videoStream.stopVideoStream();

		verify(process, times(1)).destroy();
	}
}
