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
import java.net.*;

/**
 * Takes microphone input and sends the audio as a stream to the client.
 */
public class TargetDataLineThread implements Runnable {

	private static final String CLIENT_IP_ERROR_MESSAGE = "Unrecoverable error occurred extracting the IP address of the client during startup of audio stream. See stack trace for more information.";

	/** Name of the audio mixer to use. */
	private final String AUDIO_MIXER_NAME;  // For Raspberry PI, we expect this to be = "Set [plughw:1,0]";

	/** The datagram client. Setup to only allow a single client connection. */
	private DatagramSocket clientDatagramSocket = null;

	/** Flag execution state of thread. */
	private boolean running;

	/** Thread executing this runnable. */
	private Thread executionThread;

	/** This is the mic audio input. */
	private TargetDataLine targetDataLine;

	/** Port to send datagrams over. */
	private int port;

	/** IP address to send datagrams to. */
	private InetAddress address;

	/** Wrapper around AudioSystem static methods. */
	private MixerWrapper mixerWrapper;

	/** Read audio data from source. */
	private byte[] readBuffer;

	/** Datagram packet, containing audio data, to send to client. */
	private DatagramPacket packet;

	/**
	 * Create a new target data line thread that sends the microphone data over
	 * the network to the designated address and port.
	 *
	 * @param nameOfMicrophoneMixer Name of the microphone mixer to use.
	 * @param clientIpAddressOrHostName IP address or host name of the client to send microphone audio to.
	 * @param port Port to send audio data to.
	 */
	@Generated // Ignore Jacoco
	public TargetDataLineThread(String nameOfMicrophoneMixer, String clientIpAddressOrHostName, int port) throws TargetDataLineException {
		AUDIO_MIXER_NAME = nameOfMicrophoneMixer;
		extractClientIpAddress(clientIpAddressOrHostName);
		this.port = port;
		this.mixerWrapper = new MixerWrapper();
		initialize();
	}

	/**
	 * Start the microphone stream. This MUST be called to begin capturing and sending the microphone audio.
	 */
	@Generated // Skip jacoco
	public void startAudioStreamMicrophone() {

		running = true;
		executionThread = new Thread(this);
		executionThread.start();
	}

	/**
	 * Stop the microphone stream and release resources.
	 */
	public void stopAudioStreamMicrophone() {

		running = false;
		if (executionThread != null) {
			executionThread.interrupt();
		}

		if (targetDataLine != null) {
			targetDataLine.flush();
			targetDataLine.close();
			targetDataLine = null;
		}

		if (clientDatagramSocket != null) {
			clientDatagramSocket.close();
			clientDatagramSocket = null;
		}

		System.out.println("StopAudioStreamMicrophone complete.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Thread#run()
	 */
	@Generated // Ignore Jacoco
	@Override
	public void run() {

		initializeThread();

		while (running) {

			sendAudioData();
		}
	}

	// -------------------------------------------------------------------------
	// Protected methods
	// -------------------------------------------------------------------------

	/**
	 * Initialize the instance. Setup Datagram client to connect and then do all
	 * the setup magic. Must be called after getting class instance.
	 */
	protected void initialize() {

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

		try {
			clientDatagramSocket = new DatagramSocket();
		} catch (SocketException e2) {
			e2.printStackTrace();
			stopAudioStreamMicrophone();
			return;
		}

		if (clientDatagramSocket != null) {
			clientDatagramSocket.close();
		}

		try {
			clientDatagramSocket = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
			stopAudioStreamMicrophone();
			return;
		}

		if (targetDataLine == null) {

			Info[] mixerInfo = mixerWrapper.getMixerInfo();

			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());

			for (int i = 0; i < mixerInfo.length; i++) {
				System.out.println("TARGET DATA LINE MIXER: " + i);
				System.out.println("\tNAME: " + mixerInfo[i].getName());
				System.out.println("\tDESCRIPTION: " + mixerInfo[i].getDescription());
				System.out.println("\tVENDOR: " + mixerInfo[i].getVendor());
				System.out.println("\tVERSION: " + mixerInfo[i].getVersion());

				if (AUDIO_MIXER_NAME.equals(mixerInfo[i].getName())) {
					Mixer mixer = mixerWrapper.getMixer(mixerInfo[i]);

					try {
						targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
						targetDataLine.open(getAudioFormat());
						break;
					} catch (LineUnavailableException e) {
						e.printStackTrace();
						System.out.println("Target data line unable to open. Bailing");
						stopAudioStreamMicrophone();
						return;
					}
				}
			}

			if (targetDataLine == null) {
				return;
			}

			targetDataLine.start();

			System.out.println(targetDataLine.getLineInfo().toString());
		}
	}

	/**
	 * Initialize the thread in preparation for sending audio data to the client.
	 */
	protected void initializeThread() {

		readBuffer = new byte[getAudioBufferSizeBytes()];
		packet = new DatagramPacket(readBuffer, readBuffer.length, address, port);
	}

	/**
	 * Send audio to the client.
	 */
	protected void sendAudioData() {

		int cnt = targetDataLine.read(readBuffer, 0, readBuffer.length);

		if (cnt > 0) {

			packet.setData(readBuffer);

			try {
				clientDatagramSocket.send(packet);
			} catch (IOException e) {

				System.out.println("Exception on outgoing audio stream. Pausing before continuing.");
				e.printStackTrace();

				// If we ran into an error, pause for a bit to see
				// if it will recover and then try again.
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get the audio format.
	 *
	 * @return Audio format to use for recording.
	 */
	protected AudioFormat getAudioFormat() {
		return AudioFormatUtil.getAudioFormat();
	}

	/**
	 * Size of the playback buffer in bytes.
	 *
	 * @return Size of buffer
	 */
	protected int getAudioBufferSizeBytes() {
		return 4096;
	}

	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------

	/**
	 * Extract the client IP address from the provided string. Both host names and IP addresses will work, if valid.
	 * @param ipAddress IP address, or host name, to use for communicating with the client.
	 * @throws TargetDataLineException If there was an error extracting the IP address from the value provided.
	 */
	private void extractClientIpAddress(String ipAddress) throws TargetDataLineException {
		try {
			this.address = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			System.out.println(CLIENT_IP_ERROR_MESSAGE);
			e.printStackTrace();
			throw new TargetDataLineException(CLIENT_IP_ERROR_MESSAGE, e);
		}
	}
}
