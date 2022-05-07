package com.yarg.robotpiserver.audio;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.testng.annotations.Test;

public class AudioStreamServerTest {

	AudioStreamServer audioStreamServer;
	SourceDataLineThread incomingStream = mock(SourceDataLineThread.class);
	TargetDataLineThread microphoneStream = mock(TargetDataLineThread.class);

	@Test
	public void startAndStopServer() throws Exception {

		audioStreamServer = new AudioStreamServer(incomingStream, microphoneStream);
		audioStreamServer.startAudioStream();
		audioStreamServer.stopAudioStream();

		verify(incomingStream, times(1)).startAudioStreamSpeakers();
		verify(microphoneStream, times(1)).startAudioStreamMicrophone();
		verify(incomingStream, times(1)).stopAudioStreamSpeakers();
		verify(microphoneStream, times(1)).stopAudioStreamMicrophone();
	}
}
