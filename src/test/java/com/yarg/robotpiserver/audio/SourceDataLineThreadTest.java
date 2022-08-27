package com.yarg.robotpiserver.audio;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SourceDataLineThreadTest {

	private SourceDataLineThread sourceDataLineThread;
	private DatagramSocket serverDatagramSocket;
	private SourceDataLine sourceDataLine;
	private DatagramClientReturnAddress clientAddress;
	private int serverPort = 9999;
	private MixerWrapper mixerWrapper;

	@BeforeMethod
	public void resetMocks() {
		serverDatagramSocket = mock(DatagramSocket.class);
		sourceDataLine = mock(SourceDataLine.class);
		clientAddress = mock(DatagramClientReturnAddress.class);
		serverPort = 9999;
		mixerWrapper = mock(MixerWrapper.class);
	}

	@AfterMethod
	public void release() {
		if (sourceDataLineThread != null) {
			sourceDataLineThread.stopAudioStreamSpeakers();
		}
	}

	@Test
	public void getAudioFormat() {

		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, sourceDataLine, clientAddress, mixerWrapper);
		AudioFormat audioFormat = sourceDataLineThread.getAudioFormat();
		assertEquals(audioFormat.getChannels(), 1, "Audio format channel does not match expected.");
		assertEquals(audioFormat.getSampleRate(), 44100.0f, "Sample rate does not match expected.");
		assertEquals(audioFormat.getSampleSizeInBits(), 16, "Sample size, in bits, does not match expected.");
		assertEquals(audioFormat.isBigEndian(), true, "Big endianness does not match expected");
		assertEquals(audioFormat.getFrameSize(), 2, "Frame size does not match expected.");
		assertEquals(audioFormat.getFrameRate(), 44100.0f, "Frame rate does not match expected.");
		assertEquals(audioFormat.getEncoding(), Encoding.PCM_SIGNED, "Encoding does not match expected.");
	}

	@Test
	public void getAudioBufferSizeBytes() {

		when(sourceDataLine.getBufferSize()).thenReturn(1024);
		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, sourceDataLine, clientAddress, mixerWrapper);
		int actualSize = sourceDataLineThread.getAudioBufferSizeBytes();
		assertEquals(actualSize, 256, "Audio buffer size does not match expected.");
	}

	@Test
	public void initializeWithServerDatagramSocket() {

		doNothing().when(serverDatagramSocket).close();

		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, sourceDataLine, clientAddress, mixerWrapper);
		sourceDataLineThread.initialize();
		verify(serverDatagramSocket, times(1)).close();
		verify(sourceDataLine, never()).start();
	}

	@Test
	public void initializeWithNullServerDatagramSocket() {

		sourceDataLineThread = new SourceDataLineThread(serverPort, null, sourceDataLine, clientAddress, mixerWrapper);
		sourceDataLineThread.initialize();
		verify(sourceDataLine, never()).start();
	}

	@Test
	public void initalizeWithSourceDataLineSet() {

		doNothing().when(serverDatagramSocket).close();

		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, sourceDataLine, clientAddress, mixerWrapper);
		sourceDataLineThread.initialize();
		verify(serverDatagramSocket, times(1)).close();
		verify(sourceDataLine, never()).start();

		//		Mockito.spy(SourceDataLineThread.class).
		//		sourceDataLineThread = Mockito.mock(SourceDataLineThread.class, Mockito.withSettings(). .useConstructor(serverPort, serverDatagramSocket, sourceDataLine, clientAddress));
		//		sourceDataLineThread.initialize();
		//		verify(serverDatagramSocket, times(1)).close();
		//		verify(sourceDataLine, never()).start();
	}

	@Test
	public void initializeWithSourceDataLineNotSet() throws Exception {

		Line.Info lineInfo = mock(Line.Info.class);
		when(lineInfo.toString()).thenReturn("BLAH");

		doNothing().when(sourceDataLine).open(Mockito.any(AudioFormat.class));
		when(sourceDataLine.getLineInfo()).thenReturn(lineInfo);

		Mixer mixer = mock(Mixer.class);
		when(mixer.getLine(Mockito.any(Line.Info.class))).thenReturn(sourceDataLine);

		Mixer.Info info = new MyMixerInfo("Set [plughw:1,0]", "Test vendor.", "Test description.", "1.0");

		doNothing().when(serverDatagramSocket).close();
		when(mixerWrapper.getMixerInfo()).thenReturn(new Info[] {info});
		when(mixerWrapper.getMixer(Mockito.any(Info.class))).thenReturn(mixer);

		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, null, clientAddress, mixerWrapper);
		sourceDataLineThread.initialize();
		verify(serverDatagramSocket, times(1)).close();
		verify(sourceDataLine, times(1)).start();
		verify(sourceDataLine, times(1)).open(Mockito.any(AudioFormat.class));
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void initializeWithUnmatchedMixer() throws Exception {

		Line.Info lineInfo = mock(Line.Info.class);
		when(lineInfo.toString()).thenReturn("BLAH");

		doNothing().when(sourceDataLine).open(Mockito.any(AudioFormat.class));
		when(sourceDataLine.getLineInfo()).thenReturn(lineInfo);

		Mixer mixer = mock(Mixer.class);
		when(mixer.getLine(Mockito.any(Line.Info.class))).thenReturn(sourceDataLine);

		Mixer.Info info = new MyMixerInfo("NO MATCH", "Test vendor.", "Test description.", "1.0");

		doNothing().when(serverDatagramSocket).close();
		when(mixerWrapper.getMixerInfo()).thenReturn(new Info[] {info});
		when(mixerWrapper.getMixer(Mockito.any(Info.class))).thenReturn(mixer);

		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, null, clientAddress, mixerWrapper);
		sourceDataLineThread.initialize();
	}

	@Test
	public void initializeWithoutMixerLine() throws Exception {

		Mixer mixer = mock(Mixer.class);
		when(mixer.getLine(Mockito.any(Line.Info.class))).thenThrow(new LineUnavailableException("TEST"));

		Mixer.Info info = new MyMixerInfo("Set [plughw:1,0]", "Test vendor.", "Test description.", "1.0");

		doNothing().when(serverDatagramSocket).close();
		when(mixerWrapper.getMixerInfo()).thenReturn(new Info[] {info});
		when(mixerWrapper.getMixer(Mockito.any(Info.class))).thenReturn(mixer);

		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, null, clientAddress, mixerWrapper);
		sourceDataLineThread.initialize();
		verify(serverDatagramSocket, times(1)).close();
		verify(sourceDataLine, never()).start();
		verify(sourceDataLine, never()).open(Mockito.any(AudioFormat.class));
	}

	@Test
	public void setClientAddress() {
		doNothing().when(clientAddress).setAddress(Mockito.anyString());
		sourceDataLineThread = new SourceDataLineThread(serverPort, null, null, clientAddress, null);
		sourceDataLineThread.setClientAddress("localhost");
		verify(clientAddress, times(1)).setAddress(Mockito.anyString());
	}

	@Test
	public void sendAudioToSpeaker() {
		when(sourceDataLine.write(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(1);
		sourceDataLineThread = new SourceDataLineThread(serverPort, null, sourceDataLine, null, null);

		AudioLevelListener listener = mock(AudioLevelListener.class);
		doNothing().when(listener).audioLevelUpdate(Mockito.anyInt(), Mockito.anyInt());
		sourceDataLineThread.addAudioLevelListener(listener);

		DatagramPacket datagramPacket = new DatagramPacket(new byte[] {0, 1, 0, 1}, 4);
		sourceDataLineThread.sendAudioToSpeaker(datagramPacket);

		verify(listener, times(1)).audioLevelUpdate(Mockito.anyInt(), Mockito.anyInt());
		verify(sourceDataLine, times(1)).write(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
	}

	@Test
	public void sendAudioToSpeakerAfterListenerIsRemoved() {

		when(sourceDataLine.write(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(1);
		sourceDataLineThread = new SourceDataLineThread(serverPort, null, sourceDataLine, null, null);

		AudioLevelListener listener = mock(AudioLevelListener.class);
		sourceDataLineThread.addAudioLevelListener(listener);
		sourceDataLineThread.removeAudioLevelListener(listener);

		DatagramPacket datagramPacket = new DatagramPacket(new byte[] {0, 1, 0, 1}, 4);
		sourceDataLineThread.sendAudioToSpeaker(datagramPacket);

		verify(listener, never()).audioLevelUpdate(Mockito.anyInt(), Mockito.anyInt());
	}

	@Test
	public void stopAudioStreamSpeakers() {

		doNothing().when(sourceDataLine).flush();
		doNothing().when(sourceDataLine).close();
		doNothing().when(serverDatagramSocket).close();
		sourceDataLineThread = new SourceDataLineThread(serverPort, serverDatagramSocket, sourceDataLine, null, null);
		sourceDataLineThread.stopAudioStreamSpeakers();

		verify(sourceDataLine, times(1)).flush();
		verify(sourceDataLine, times(1)).close();
		verify(serverDatagramSocket, times(1)).close();

	}
}
