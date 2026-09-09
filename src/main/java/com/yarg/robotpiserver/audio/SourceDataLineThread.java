package com.yarg.robotpiserver.audio;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.yarg.robotpiserver.util.Generated;

import javax.sound.sampled.*;
import javax.sound.sampled.Mixer.Info;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Takes an audio input stream from the client and sends it to the speaker.
 */
public class SourceDataLineThread implements Runnable {

	/** Name of the audio mixer to use. */
	private String AUDIO_MIXER_NAME = "Set [plughw:1,0]";

	/** Port to receive audio stream on. */
	private int port;

	/** The connected client. Setup to only allow a single client connection. */
	private DatagramSocket serverDatagramSocket = null;

	/** Flag execution state of thread. */
	private boolean running;

	/** Thread executing this runnable. */
	private Thread executionThread;

	/** Plays audio to the speakers. */
	private SourceDataLine sourceDataLine;

	/**
	 * Audio level listeners that would like to response to audio level changes.
	 */
	private ArrayList<AudioLevelListener> audioLevelListeners = new ArrayList<AudioLevelListener>();;

	/** Wrapper around AudioSystem static methods. */
	private MixerWrapper mixerWrapper;

	/**
	 * Default constructor.
	 */
	@Generated // Ignore Jacoco
	public SourceDataLineThread(int port) {
		this.port = port;
		audioLevelListeners.clear();
		mixerWrapper = new MixerWrapper();
		initialize();
	}

	/**
	 * Initialize the instance. Setup Datagram server and then do all remaining
	 * the setup magic. Must be called after getting class instance.
	 */
	public void initialize() {

		if (running) {
			running = false;
			if (executionThread != null) {
				executionThread.interrupt();
			}

			// Let the thread terminate and then proceed.
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

		if (serverDatagramSocket != null) {
			serverDatagramSocket.close();
		}

		try {
			serverDatagramSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if (sourceDataLine == null) {

			Info[] mixerInfo = mixerWrapper.getMixerInfo();

			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, AudioFormatUtil.getAudioFormat());

			for (int i = 0; i < mixerInfo.length; i++) {
				System.out.println("SOURCE DATA LINE MIXER: " + i);
				System.out.println("\tNAME: " + mixerInfo[i].getName());
				System.out.println("\tDESCRIPTION: " + mixerInfo[i].getDescription());
				System.out.println("\tVENDOR: " + mixerInfo[i].getVendor());
				System.out.println("\tVERSION: " + mixerInfo[i].getVersion());

				if (AUDIO_MIXER_NAME.equals(mixerInfo[i].getName())) {
					Mixer mixer = mixerWrapper.getMixer(mixerInfo[i]);

					try {
						sourceDataLine = (SourceDataLine) mixer.getLine(dataLineInfo);
						sourceDataLine.open(AudioFormatUtil.getAudioFormat());
						break;
					} catch (LineUnavailableException e) {
						e.printStackTrace();
						System.out.println("Source data line unable to open. Bailing");
						stopAudioStreamSpeakers();
						return;
					}
				}
			}

			// Named mixer not found — fall back to system default.
			if (sourceDataLine == null) {
				try {
					sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
					sourceDataLine.open(AudioFormatUtil.getAudioFormat());
					System.out.println("SourceDataLine using system default mixer.");
				} catch (LineUnavailableException e) {
					e.printStackTrace();
					return;
				}
			}

			System.out.println(sourceDataLine.getLineInfo().toString());
		}
	}

	public void addAudioLevelListener(AudioLevelListener listener) {
		audioLevelListeners.add(listener);
	}

	public void removeAudioLevelListener(AudioLevelListener listener) {
		audioLevelListeners.remove(listener);
	}

	/**
	 * Start the speaker thread after opening connections.
	 */
	@Generated // Skip jacoco
	public void startAudioStreamSpeakers() {

		if (running) {
			return;
		}

		running = true;
		executionThread = new Thread(this);
		executionThread.start();
	}

	/**
	 * Stop the speaker thread, close connections etc.
	 */
	public void stopAudioStreamSpeakers() {

		running = false;
		if (executionThread != null) {
			executionThread.interrupt();
		}

		if (sourceDataLine != null) {
			sourceDataLine.flush();
			sourceDataLine.close();
			sourceDataLine = null;
		}

		if (serverDatagramSocket != null) {
			serverDatagramSocket.close();
			serverDatagramSocket = null;
		}

		System.out.println("StopAudioStreamSpeakers complete.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Thread#run()
	 */
	@Generated // Ignore in Jacoco calculation
	@Override
	public void run() {

		if (sourceDataLine == null) {
			return;
		}

		try {
			serverDatagramSocket.setSoTimeout(50);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		byte[] datagramBuffer = new byte[4096];
		DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);

		// 50 ms of silence at 44.1 kHz / 16-bit / mono = 4410 bytes.
		// Pre-fill the line before start() so the DAC has data immediately,
		// preventing the underrun pop that occurs when start() fires on an empty buffer.
		// The same buffer is written on each socket timeout to keep the line fed.
		byte[] silence = new byte[4410];
		sourceDataLine.write(silence, 0, silence.length);
		sourceDataLine.start();

		System.out.println("SourceDataLineThread running, waiting for audio packets.");

		while (running) {

			try {
				serverDatagramSocket.receive(datagramPacket);
				sendAudioToSpeaker(datagramPacket);
			} catch (SocketTimeoutException ste) {
				// No packet arrived within 50 ms — write silence so the SourceDataLine
				// never underruns. An empty buffer causes ALSA to click/pop on recovery.
				sourceDataLine.write(silence, 0, silence.length);
			} catch (IOException e) {
				System.out.println("Exception on incoming audio stream. Pausing before continuing.");
				e.printStackTrace();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	// -------------------------------------------------------------------------
	// Protected methods
	// -------------------------------------------------------------------------

	/**
	 * Send audio to the speaker using the datagram packet.
	 *
	 * @param datagramPacket
	 *            DatagramPacket containing audio data.
	 */
	protected void sendAudioToSpeaker(DatagramPacket datagramPacket) {

		byte[] rawData = datagramPacket.getData();
		int receivedLen = datagramPacket.getLength();
		int maxSample = 0;
		for (int t = 0; t < receivedLen - 1; t += 2) {
			int low = rawData[t] & 0xFF;
			int high = rawData[t + 1] & 0xFF;
			int sample = (high << 8) | low;
			if (sample > maxSample) {
				maxSample = sample;
			}
		}

		for (AudioLevelListener listener : audioLevelListeners) {
			listener.audioLevelUpdate(300, maxSample);
		}

		int frameSize = sourceDataLine.getFormat().getFrameSize();
		int len = (datagramPacket.getLength() / frameSize) * frameSize;
		if (len > 0) {
			sourceDataLine.write(datagramPacket.getData(), 0, len);
		}
	}


	/**
	 * Size of the playback buffer in bytes.
	 *
	 * @return Size of buffer
	 */
	protected int getAudioBufferSizeBytes() {

		if (sourceDataLine == null) {
			return 1024;
		}
		int frameSizeInBytes = AudioFormatUtil.getAudioFormat().getFrameSize();
		int bufferLengthInFrames = sourceDataLine.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		return bufferLengthInBytes;
	}

}
