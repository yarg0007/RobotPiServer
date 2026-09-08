package com.yarg.robotpiserver.video;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class VideoStreamTest {

	private static final String ADDRESS = "192.168.1.100";
	private static final int PORT = 8554;

	private Runtime runtime = mock(Runtime.class);
	private Process process = mock(Process.class);

	@BeforeMethod
	public void setUp() {
		Mockito.reset(runtime, process);
	}

	@Test
	public void videoStreamNotRunningUntilStarted() {
		VideoStream videoStream = new VideoStream(ADDRESS, PORT, runtime);
		assertFalse(videoStream.isVideoStreamRunning(),
				"Video stream MUST NOT be running until stream is started.");
	}

	@Test
	public void startVideoStreamExecutesCommand() throws Exception {
		when(runtime.exec(Mockito.any(String[].class))).thenReturn(process);
		when(process.isAlive()).thenReturn(true);

		VideoStream videoStream = new VideoStream(ADDRESS, PORT, runtime);
		videoStream.startVideoStream();

		verify(runtime, times(1)).exec(Mockito.any(String[].class));
		assertTrue(videoStream.isVideoStreamRunning());
	}

	@Test
	public void startVideoStreamCommandContainsRaspividAndVlc() throws Exception {
		when(runtime.exec(Mockito.any(String[].class))).thenReturn(process);
		when(process.isAlive()).thenReturn(true);

		VideoStream videoStream = new VideoStream(ADDRESS, PORT, runtime);
		videoStream.startVideoStream();

		ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
		verify(runtime).exec(captor.capture());
		String command = captor.getValue()[2];
		assertTrue(command.contains("raspivid"), "Command must include raspivid");
		assertTrue(command.contains("cvlc"), "Command must include cvlc");
		assertTrue(command.contains(String.valueOf(PORT)), "Command must include the port");
		assertTrue(command.contains("rtsp"), "Command must include rtsp");
	}

	@Test
	public void stopVideoStream() throws Exception {
		when(runtime.exec(Mockito.any(String[].class))).thenReturn(process);
		when(process.isAlive()).thenReturn(true);

		VideoStream videoStream = new VideoStream(ADDRESS, PORT, runtime);
		videoStream.startVideoStream();
		videoStream.stopVideoStream();

		verify(process, times(1)).destroy();
		assertFalse(videoStream.isVideoStreamRunning());
	}

	@Test
	public void stopWhenNotRunningIsNoOp() {
		VideoStream videoStream = new VideoStream(ADDRESS, PORT, runtime);
		videoStream.stopVideoStream();
		assertFalse(videoStream.isVideoStreamRunning());
	}
}
