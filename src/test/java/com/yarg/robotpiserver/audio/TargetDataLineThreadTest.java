package com.yarg.robotpiserver.audio;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class TargetDataLineThreadTest {

	private static final Map<String, String> osToMicrophoneMixerMap = new HashMap<String, String>() {{
		put("Mac OS X", "MacBook Pro Microphone");
	}};

	private static final Map<String, String> osToSpeakerMixerMap = new HashMap<String, String>() {{
		put("Mac OS X", "MacBook Pro Speakers");
	}};

	private String microphoneMixerName;
	private String speakerMixerName;
	private static final String LOCALHOST = "127.0.0.1";
	private static final int PORT = 9090;
	private TargetDataLineThread targetDataLineThread;

	@BeforeMethod
	public void setup() {

		// Capture the OS specific speaker and microphone
		String os = System.getProperty("os.name");
		speakerMixerName = osToSpeakerMixerMap.get(os);
		microphoneMixerName = osToMicrophoneMixerMap.get(os);
		if (speakerMixerName == null || microphoneMixerName == null) {
			throw new NullPointerException("Unable to capture speaker and/or microphone");
		}
	}

	@AfterMethod
	public void teardown() {
		if (targetDataLineThread != null) {
			targetDataLineThread.stopAudioStreamMicrophone();
		}
	}

	@Test
	public void checkMatchOfExpectedAudioMixerInfo() {
		MixerWrapper mixerWrapper = new MixerWrapper();
		Mixer.Info[] mixerInfo = mixerWrapper.getMixerInfo();
		System.out.println("Mixers");
		List<String> mixerNames = new ArrayList<>();
		Arrays.stream(mixerInfo).forEach(info -> {
			System.out.println("----------------------");
			System.out.println("Name : " + info.getName());
			System.out.println("Description : " + info.getDescription());
			mixerNames.add(info.getName());
		});
		assertThat(mixerNames, hasItem(microphoneMixerName));
	}

	@Test
	public void getMicrophoneStreamAndThenStop() throws Exception {

		// Create the listener for the microphone stream.
		DatagramSocket listener = new DatagramSocket(PORT);
		byte[] receive = new byte[65534];
		DatagramPacket receivedData = new DatagramPacket(receive, receive.length);

		// Create the target data line thread and start audio streaming.
		targetDataLineThread = new TargetDataLineThread(microphoneMixerName, LOCALHOST, PORT);
		targetDataLineThread.startAudioStreamMicrophone();
		System.out.println("Letting microphone thread run for a moment...");
		Thread.sleep(500);
		System.out.println("Stopping microphone thread.");

		// Make sure microphone data was received.
		listener.receive(receivedData);
		boolean dataReceived = false;
		for (int i = 0; i < receivedData.getLength(); i++) {
			if (receivedData.getData()[i] != 0) {
				dataReceived = true;
				break;
			}
		}

		Assert.assertTrue(dataReceived);
	}

	@Test
	public void recordMicrophoneStreamAndPlaybackAudioToSpeaker() throws Exception {

		// Get the speaker line.
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, AudioFormatUtil.getAudioFormat());
		MixerWrapper mixerWrapper = new MixerWrapper();
		Mixer.Info[] mixerInfo = mixerWrapper.getMixerInfo();
		SourceDataLine sourceDataLine = null;
		for (Mixer.Info info : mixerInfo) {
			if (speakerMixerName.equals(info.getName())) {
				Mixer mixer = mixerWrapper.getMixer(info);
				sourceDataLine = (SourceDataLine) mixer.getLine(dataLineInfo);
				sourceDataLine.open(AudioFormatUtil.getAudioFormat());
				break;
			}
		}

		if (sourceDataLine == null) {
			throw new NullPointerException("Unable to obtain speaker output line");
		}

		sourceDataLine.start();

		// Create the target data line thread and start audio streaming from the microphone.
		targetDataLineThread = new TargetDataLineThread(microphoneMixerName, LOCALHOST, PORT);
		targetDataLineThread.startAudioStreamMicrophone();

		// Capture microphone audio datagrams.
		// Because this is not running on its own dedicated thread, there are large
		// gaps in the audio stream. This is expected. Regardless, we are able to verify
		// that audio streaming is working.
		DatagramSocket audioCollectorSocket = new DatagramSocket(PORT);
		byte[] receive = new byte[65534];
		DatagramPacket receivedData = new DatagramPacket(receive, receive.length);

		int count = 0;
		int countLimit = 10;
		while (count++ < countLimit) {
			audioCollectorSocket.receive(receivedData);
			sourceDataLine.write(receive, 0, receive.length);
		}

		// Close audio connections.
		audioCollectorSocket.close();
		sourceDataLine.drain();
		sourceDataLine.stop();
		sourceDataLine.close();
	}
}
