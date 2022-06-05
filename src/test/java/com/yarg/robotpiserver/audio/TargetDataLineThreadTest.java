package com.yarg.robotpiserver.audio;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.TargetDataLine;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TargetDataLineThreadTest {

	private TargetDataLineThread targetDataLineThread;
	private DatagramClientReturnAddress serverAddress;
	private int serverPort;
	private TargetDataLine targetDataLine;
	private MixerWrapper mixerWrapper;
	private byte[] readBuffer;
	private DatagramSocket clientDatagramSocket;

	@BeforeMethod
	public void resetMocks() {
		serverAddress = mock(DatagramClientReturnAddress.class);
		serverPort = 9999;
		targetDataLine = mock(TargetDataLine.class);
		mixerWrapper = mock(MixerWrapper.class);
		clientDatagramSocket = mock(DatagramSocket.class);
	}

	@Test
	public void getAudioFormat() {
		targetDataLineThread = new TargetDataLineThread(null, null, serverPort, null, null, null);
		AudioFormat audioFormat = targetDataLineThread.getAudioFormat();
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

		when(targetDataLine.getBufferSize()).thenReturn(1024);
		targetDataLineThread = new TargetDataLineThread(null, null, serverPort, targetDataLine, null, null);
		int actualSize = targetDataLineThread.getAudioBufferSizeBytes();
		assertEquals(actualSize, 256, "Audio buffer size does not match expected.");
	}

	@Test
	public void sendAudioDataEmptyAudioStream() throws IOException {

		readBuffer = new byte[] {};
		when(targetDataLine.read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(4);
		targetDataLineThread = new TargetDataLineThread(clientDatagramSocket, null, serverPort, targetDataLine, null, readBuffer);
		verify(clientDatagramSocket, never()).send(Mockito.any(DatagramPacket.class));
	}

	@Test
	public void sendAudioData() throws IOException {

		when(serverAddress.getAddress()).thenReturn("localhost");
		when(targetDataLine.read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(4);
		doNothing().when(clientDatagramSocket).send(Mockito.any(DatagramPacket.class));
		targetDataLineThread = new TargetDataLineThread(clientDatagramSocket, serverAddress, serverPort, targetDataLine, mixerWrapper, null);
		targetDataLineThread.initializeThread();
		targetDataLineThread.sendAudioData();
		verify(clientDatagramSocket, times(1)).send(Mockito.any(DatagramPacket.class));
	}

	@Test
	public void sendAudioDataWithoutPackets() throws IOException {

		when(serverAddress.getAddress()).thenReturn("localhost");
		when(targetDataLine.read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(0);
		doNothing().when(clientDatagramSocket).send(Mockito.any(DatagramPacket.class));
		targetDataLineThread = new TargetDataLineThread(clientDatagramSocket, serverAddress, serverPort, targetDataLine, mixerWrapper, null);
		targetDataLineThread.initializeThread();
		targetDataLineThread.sendAudioData();
		verify(clientDatagramSocket, never()).send(Mockito.any(DatagramPacket.class));
	}

	@Test
	public void stopAudioStreamMicrophone() {

		doNothing().when(clientDatagramSocket).close();
		doNothing().when(targetDataLine).flush();
		doNothing().when(targetDataLine).close();
		targetDataLineThread = new TargetDataLineThread(clientDatagramSocket, null, serverPort, targetDataLine, null, null);
		targetDataLineThread.stopAudioStreamMicrophone();
		verify(clientDatagramSocket, times(1)).close();
		verify(targetDataLine, times(1)).flush();
		verify(targetDataLine, times(1)).close();
	}

	@Test
	public void initializeMixer() throws LineUnavailableException {

		javax.sound.sampled.Line.Info infoLine = mock(javax.sound.sampled.Line.Info.class);
		when(infoLine.toString()).thenReturn("TEST");

		doNothing().when(targetDataLine).open(Mockito.any(AudioFormat.class));
		doNothing().when(targetDataLine).start();
		when(targetDataLine.getLineInfo()).thenReturn(infoLine);

		Mixer mixer = Mockito.mock(Mixer.class);
		when(mixer.getLine(Mockito.any(javax.sound.sampled.Line.Info.class))).thenReturn(targetDataLine);

		MyMixerInfo mixerInfo = new MyMixerInfo("Set [plughw:1,0]", "test", "test description", "0.1");
		when(mixerWrapper.getMixerInfo()).thenReturn(new Info[] {mixerInfo});
		when(mixerWrapper.getMixer(Mockito.any(Info.class))).thenReturn(mixer);

		targetDataLineThread = new TargetDataLineThread(null, null, serverPort, null, mixerWrapper, null);
		targetDataLineThread.initialize();

		verify(targetDataLine, times(1)).open(Mockito.any(AudioFormat.class));
		verify(targetDataLine, times(1)).start();
		verify(targetDataLine, times(1)).getLineInfo();
	}

	@Test
	public void initializeMixerErrorOpeningTargetDataLine() throws LineUnavailableException {

		javax.sound.sampled.Line.Info infoLine = mock(javax.sound.sampled.Line.Info.class);
		when(infoLine.toString()).thenReturn("TEST");

		doThrow(LineUnavailableException.class).when(targetDataLine).open(Mockito.any(AudioFormat.class));

		Mixer mixer = Mockito.mock(Mixer.class);
		when(mixer.getLine(Mockito.any(javax.sound.sampled.Line.Info.class))).thenReturn(targetDataLine);

		MyMixerInfo mixerInfo = new MyMixerInfo("Set [plughw:1,0]", "test", "test description", "0.1");
		when(mixerWrapper.getMixerInfo()).thenReturn(new Info[] {mixerInfo});
		when(mixerWrapper.getMixer(Mockito.any(Info.class))).thenReturn(mixer);

		targetDataLineThread = new TargetDataLineThread(null, null, serverPort, null, mixerWrapper, null);
		targetDataLineThread.initialize();

		verify(targetDataLine, times(1)).open(Mockito.any(AudioFormat.class));
		verify(targetDataLine, never()).start();
		verify(targetDataLine, never()).getLineInfo();
	}
}
