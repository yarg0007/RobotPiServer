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
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.TargetDataLine;

public class TargetDataLineThread implements Runnable {

	private String AUDIO_MIXER_NAME = "Set [plughw:1,0]";

	/** The datagram client. Setup to only allow a single client connection.*/
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

	/**
	 * Create a new target data line thread that sends the microphone data
	 * over the network to the designated address and port.
	 * @param serverAddress Return address interface for retrieving the address
	 * of the client to send audio to.
	 * @param serverPort Server port to send audio data to.
	 */
	public TargetDataLineThread(DatagramClientReturnAddress serverAddress, int serverPort) {

		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}

	/**
	 * Initialize the instance. Setup Datagram client to connect and then do all
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

			Info[] mixerInfo = AudioSystem.getMixerInfo();

			DataLine.Info dataLineInfo = new DataLine.Info(
					TargetDataLine.class, getAudioFormat());

			for (int i = 0; i < mixerInfo.length; i++) {
				System.out.println("TARGET DATA LINE MIXER: "+i);
				System.out.println("\tNAME: "+mixerInfo[i].getName());
				System.out.println("\tDESCRIPTION: "+mixerInfo[i].getDescription());
				System.out.println("\tVENDOR: "+mixerInfo[i].getVendor());
				System.out.println("\tVERSION: "+mixerInfo[i].getVersion());

				if (AUDIO_MIXER_NAME.equals(mixerInfo[i].getName())) {
					Mixer mixer = AudioSystem.getMixer(mixerInfo[i]);

					try {
						targetDataLine =
								(TargetDataLine) mixer.getLine(dataLineInfo);
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

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {


		byte[] readBuffer = new byte[getAudioBufferSizeBytes()];

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
			System.out.println("Unreoverable error occurred during startup of audio stream. See stack trace for more information.");
			e.printStackTrace();
			return;
		}

		DatagramPacket packet = new DatagramPacket(readBuffer, readBuffer.length,
				address, serverPort);

		while (running) {

			int cnt = targetDataLine.read(readBuffer, 0,
					readBuffer.length);

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
	}

	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------

	/**
	 * Get the audio format.
	 * @return Audio format to use for recording.
	 */
	protected AudioFormat getAudioFormat() {

		float sampleRate = 44100.0f;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;

		return new AudioFormat(
				sampleRate,
				sampleSizeInBits,
				channels,
				signed,
				bigEndian);
	}

	/**
	 * Size of the playback buffer in bytes.
	 * @return Size of buffer
	 */
	protected int getAudioBufferSizeBytes() {

		int frameSizeInBytes = getAudioFormat().getFrameSize();
		int bufferLengthInFrames = targetDataLine.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		return bufferLengthInBytes;
	}
}
