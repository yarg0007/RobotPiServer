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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

import com.yarg.robotpiserver.config.ConfigLoader;
import com.yarg.robotpiserver.util.Generated;

public class SourceDataLineThread implements Runnable {

	private int serverPort;

	/** The connected client. Setup to only allow a single client connection. */
	private DatagramSocket serverDatagramSocket = null;

	/** Flag execution state of thread. */
	private boolean running;
	private Thread executionThread;

	/** Plays audio to the speakers. */
	private SourceDataLine sourceDataLine;

	/** Interface for setting the client address to send audio back to. */
	private DatagramClientReturnAddress clientAddress;

	/**
	 * Audio level listeners that would like to response to audio level changes.
	 */
	private ArrayList<AudioLevelListener> audioLevelListeners = new ArrayList<AudioLevelListener>();;

	private MixerWrapper mixerWrapper;

	/**
	 * Default constructor.
	 */
	@Generated // Ignore Jacoco
	public SourceDataLineThread(int serverPort, DatagramClientReturnAddress clientAddress) {
		this.serverPort = serverPort;
		this.clientAddress = clientAddress;
		audioLevelListeners.clear();
		mixerWrapper = new MixerWrapper();
		initialize();
	}

	/**
	 * Specify the dependencies to use.
	 *
	 * @param serverPort
	 *            Server port to connect to for datagram socket.
	 * @param serverDatagramSocket
	 *            Connected client - setup to only allow a single client
	 *            connection.
	 * @param sourceDataLine
	 *            Plays audio to the speakers.
	 * @param clientAddress
	 *            Interface for setting the client address to send audio back
	 *            to.
	 * @param mixerWrapper
	 *            Mixer wrapper instance.
	 */
	@Generated // Ignore Jacoco
	public SourceDataLineThread(int serverPort, DatagramSocket serverDatagramSocket, SourceDataLine sourceDataLine,
			DatagramClientReturnAddress clientAddress, MixerWrapper mixerWrapper) {
		this.serverPort = serverPort;
		this.serverDatagramSocket = serverDatagramSocket;
		this.sourceDataLine = sourceDataLine;
		this.clientAddress = clientAddress;
		this.mixerWrapper = mixerWrapper;
	}

	/**
	 * Initialize the instance. Setup Datagram server and then do all remaining
	 * the setup magic. Must be called after getting class instance.
	 */
	@Generated // Ignore Jacoco
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
			serverDatagramSocket = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if (sourceDataLine == null) {

			Info[] mixerInfo = mixerWrapper.getMixerInfo();

			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, getAudioFormat());

			for (int i = 0; i < mixerInfo.length; i++) {
				System.out.println("SOURCE DATA LINE MIXER: " + i);
				System.out.println("\tNAME: " + mixerInfo[i].getName());
				System.out.println("\tDESCRIPTION: " + mixerInfo[i].getDescription());
				System.out.println("\tVENDOR: " + mixerInfo[i].getVendor());
				System.out.println("\tVERSION: " + mixerInfo[i].getVersion());

				if (ConfigLoader.getInstance().getSourceDataLineMixerName().equals(mixerInfo[i].getName())) {
					Mixer mixer = mixerWrapper.getMixer(mixerInfo[i]);

					try {
						sourceDataLine = (SourceDataLine) mixer.getLine(dataLineInfo);
						sourceDataLine.open(getAudioFormat());
						break;
					} catch (LineUnavailableException e) {
						e.printStackTrace();
						System.out.println("Source data line unable to open. Bailing");
						stopAudioStreamSpeakers();
						return;
					}
				}
			}

			// If still null, we didn't get a line. Bail.
			if (sourceDataLine == null) {
				throw new IllegalStateException("SourceDataLineThread: Unable to find audio mixer matching: " + ConfigLoader.getInstance().getSourceDataLineMixerName());
			}

			sourceDataLine.start();

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

		int dataLen = getAudioBufferSizeBytes();
		byte[] datagramBuffer = new byte[dataLen];
		DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, dataLen);

		System.out.println("Waiting for initial packet");

		try {
			serverDatagramSocket.receive(datagramPacket);
		} catch (IOException e) {
			System.out.println(
					"Exception occurred with initial incoming audio stream. See stack trace for more infomation.");
			e.printStackTrace();
			stopAudioStreamSpeakers();
			return;
		}

		setClientAddress(datagramPacket.getAddress().getHostAddress());

		while (running) {

			try {
				serverDatagramSocket.receive(datagramPacket);
			} catch (IOException e) {

				System.out.println("Exception on incoming audio stream. Pausing before continuing.");
				e.printStackTrace();

				// Let the system rest and then loop back to try the
				// next incoming data bit.
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}

			sendAudioToSpeaker(datagramPacket);

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
		int maxSample = 0;
		for (int t = 0; t < rawData.length; t += 2) {
			int low = rawData[t];
			t++;
			int high = rawData[t + 1];
			t++;
			int sample = (high << 8) + (low & 0x00ff);
			if (sample > maxSample) {
				maxSample = sample;
			}
		}

		for (AudioLevelListener listener : audioLevelListeners) {
			listener.audioLevelUpdate(300, maxSample);
		}

		sourceDataLine.write(datagramPacket.getData(), 0, datagramPacket.getLength());
	}

	/**
	 * Set the client address to use for communicating with the client.
	 *
	 * @param clientHostAddress
	 *            Client host address.
	 */
	protected void setClientAddress(String clientHostAddress) {

		clientAddress.setAddress(clientHostAddress);
		System.out.println("\nGot packet from: " + clientHostAddress);
	}

	/**
	 * Get the audio format.
	 *
	 * @return Audio format to use for recording.
	 */
	protected AudioFormat getAudioFormat() {

		float sampleRate = 44100.0f;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;

		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	/**
	 * Size of the playback buffer in bytes.
	 *
	 * @return Size of buffer
	 */
	protected int getAudioBufferSizeBytes() {

		int frameSizeInBytes = getAudioFormat().getFrameSize();
		int bufferLengthInFrames = sourceDataLine.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		return bufferLengthInBytes;
	}

}
