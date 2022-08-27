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
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.TargetDataLine;

import com.yarg.robotpiserver.config.ConfigLoader;
import com.yarg.robotpiserver.util.Generated;

public class TargetDataLineThread implements Runnable {

	/** The datagram client. Setup to only allow a single client connection. */
	private DatagramSocket clientDatagramSocket = null;

	/** Flag execution state of thread. */
	private boolean running;
	private Thread executionThread;

	/** This is the mic audio input. */
	private TargetDataLine targetDataLine;

	/** Port to send datagrams over. */
	private int serverPort;

	/** Interface for accessing server address to send datagrams to. */
	private DatagramClientReturnAddress serverAddress;

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
	 * @param serverAddress
	 *            Return address interface for retrieving the address of the
	 *            client to send audio to.
	 * @param serverPort
	 *            Server port to send audio data to.
	 */
	@Generated // Ignore Jacoco
	public TargetDataLineThread(DatagramClientReturnAddress serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.mixerWrapper = new MixerWrapper();
	}

	/**
	 * Create a new target data line thread that sends the microphone data over
	 * the network to the designated address and port. TargetDataLine may also
	 * be specified.
	 *
	 * @param clientDatagramSocket
	 * 			  Datagram socket for sending data.
	 * @param serverAddress
	 *            Return address interface for retrieving the address of the
	 *            client to send audio to.
	 * @param serverPort
	 *            Server port to send audio data to.
	 * @param targetDataLine
	 *            Incoming audio line.
	 * @param mixerWrapper
	 * 			  MixerWrapper instance to use.
	 * @param readBuffer
	 * 			  Read audio data.
	 */
	@Generated // Ignore Jacoco
	public TargetDataLineThread(DatagramSocket clientDatagramSocket,  DatagramClientReturnAddress serverAddress, int serverPort,
			TargetDataLine targetDataLine, MixerWrapper mixerWrapper, byte[] readBuffer) {

		this.clientDatagramSocket = clientDatagramSocket;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.targetDataLine = targetDataLine;
		this.mixerWrapper = mixerWrapper;
		this.readBuffer = readBuffer;
	}

	/**
	 * Initialize the instance. Setup Datagram client to connect and then do all
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

				if (ConfigLoader.getInstance().getTargetDataLineMixerName().equals(mixerInfo[i].getName())) {
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
				throw new IllegalStateException("TargetDataLineThread: Unable to find audio mixer matching: " + ConfigLoader.getInstance().getTargetDataLineMixerName());
			}

			targetDataLine.start();

			System.out.println(targetDataLine.getLineInfo().toString());
		}
	}

	@Generated // Skip jacoco
	public void startAudioStreamMicrophone() {

		running = true;
		executionThread = new Thread(this);
		executionThread.start();
	}

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
	 * Initialize the thread in preparation for sending audio data to the client.
	 */
	protected void initializeThread() {

		readBuffer = new byte[getAudioBufferSizeBytes()];

		// Sleep for three seconds and then check if the address to send
		// audio to has been set. Do not proceed until this is set.
		while (serverAddress.getAddress() == null) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		InetAddress address;

		try {
			address = InetAddress.getByName(serverAddress.getAddress());
		} catch (UnknownHostException e) {
			System.out.println(
					"Unreoverable error occurred during startup of audio stream. See stack trace for more information.");
			e.printStackTrace();
			return;
		}

		packet = new DatagramPacket(readBuffer, readBuffer.length, address, serverPort);
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
		int bufferLengthInFrames = targetDataLine.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		return bufferLengthInBytes;
	}
}
