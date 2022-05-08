package com.yarg.robotpiserver.audio;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.yarg.robotpiserver.video.VideoStream;

public class AudioStreamServerTest {

	AudioStreamServer audioStreamServer;
	SourceDataLineThread incomingStream = mock(SourceDataLineThread.class);
	TargetDataLineThread microphoneStream = mock(TargetDataLineThread.class);
	VideoStream videoStream = mock(VideoStream.class);

	@Test
	public void startAndStopServer() throws Exception {

		audioStreamServer = new AudioStreamServer(incomingStream, microphoneStream, videoStream);
		audioStreamServer.startAudioStream();
		audioStreamServer.stopAudioStream();

		verify(incomingStream, times(1)).startAudioStreamSpeakers();
		verify(microphoneStream, times(1)).startAudioStreamMicrophone();
		verify(incomingStream, times(1)).stopAudioStreamSpeakers();
		verify(microphoneStream, times(1)).stopAudioStreamMicrophone();
		verify(videoStream, times(1)).stopVideoStream();
	}

	@Test
	public void addAndRemoveAudioListener() throws Exception {

		doNothing().when(incomingStream).addAudioLevelListener(Mockito.any(AudioLevelListener.class));
		doNothing().when(incomingStream).removeAudioLevelListener(Mockito.any(AudioLevelListener.class));
		AudioLevelListenerImpl impl = new AudioLevelListenerImpl();

		audioStreamServer = new AudioStreamServer(incomingStream, microphoneStream, videoStream);
		audioStreamServer.addAudioLevelListener(impl);

		verify(incomingStream, times(1)).addAudioLevelListener(Mockito.any(AudioLevelListener.class));
		verify(incomingStream, never()).removeAudioLevelListener(Mockito.any(AudioLevelListener.class));

		audioStreamServer.removeAudioLevelListener(impl);

		verify(incomingStream, times(1)).addAudioLevelListener(Mockito.any(AudioLevelListener.class));
		verify(incomingStream, times(1)).removeAudioLevelListener(Mockito.any(AudioLevelListener.class));
	}

	@Test
	public void setAddress() throws Exception {
		audioStreamServer = new AudioStreamServer(incomingStream, microphoneStream, videoStream);
		String address = audioStreamServer.getAddress();
		assertNull(address, "Address is expected to be null by default.");

		String addressValue = "http://localhost:8080";
		audioStreamServer.setAddress(addressValue);
		address = audioStreamServer.getAddress();
		assertEquals(address, addressValue);
	}
}

class AudioLevelListenerImpl implements AudioLevelListener {

	@Override
	public void audioLevelUpdate(int audioLevelBaseline, int currentAudioLevel) {
		; // Do nothing.
	}

}
