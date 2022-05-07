package com.yarg.robotpiserver.control;

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

import com.yarg.robotpiserver.audio.AudioLevelListener;

public class InputControlServer implements Runnable, AudioLevelListener{

	private static final int PORT = 49801;

	private Thread executionThread;
	private boolean running = false;

	// Should be a value between 0 and 100.
	private int talkSoundLevel = 0;

	// This is a copy of the last sound level received from audio input. Can
	// be as large as needed from the audio source.
	private int previousSoundLevel = 0;

	private PWMController pwmController;

	// The connected client. Setup to only allow a single client connection.
	private DatagramSocket serverDatagramSocket = null;

	// Datagram packet
	private byte[] datagramBuffer = new byte[64];
	private DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);

	// Datagram parsing variables.
	private int driveInput = 0;
	private int turnInput = 0;
	private int headLiftInput = 0;
	private int headTurnInput = 0;
	private int talking = 0;
	private int openMouth = 0;

	private byte[] rawData;
	private String[] tokens;
	private String data;
	private int dataDelimiterIndex;

	/**
	 * Create instance with default initialization.
	 */
	public InputControlServer() {
		initialize();
	}

	/**
	 * Create instance with specified dependencies.
	 * @param pwmController PWMController to use.
	 * @param serverDatagramSocket Datagram socket to receive packets.
	 */
	public InputControlServer(PWMController pwmController, DatagramSocket serverDatagramSocket) {
		this.pwmController = pwmController;
		this.serverDatagramSocket = serverDatagramSocket;
	}

	/**
	 * Start the input control server. MUST be called to start receiving input from client.
	 */
	public void startInputControlServer() {
		pwmController.startPWMController();
		running = true;
		executionThread = new Thread(this);
		executionThread.start();
		System.out.println("Input control server ready.");
	}

	/**
	 * Stop the input control server. Shuts down the thread and closes the datagram socket.
	 */
	public void stopInputControlServer() {

		running = false;
		if (executionThread != null) {
			executionThread.interrupt();
		}

		serverDatagramSocket.close();
		serverDatagramSocket = null;

		pwmController.stopPWMController();

		System.out.println("Input control server stopped.");
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		while (running) {
			processInput();
		}
	}

	// -------------------------------------------------------------------------
	//	Methods required by AudioLevelListener
	// -------------------------------------------------------------------------

	@Override
	public void audioLevelUpdate(int audioLevelBaseline, int currentAudioLevel) {

		if (currentAudioLevel <= audioLevelBaseline) {
			talkSoundLevel = 0;
		} else if (currentAudioLevel > previousSoundLevel) {
			talkSoundLevel = 100;
		} else {
			talkSoundLevel = 0; //(int) (((float)currentAudioLevel - (float)audioLevelBaseline)/((float)previousSoundLevel - (float)audioLevelBaseline) * 100.0f);
		}

		if (talkSoundLevel > 100) {
			talkSoundLevel = 100;
		}

		previousSoundLevel = currentAudioLevel;
	}

	// -------------------------------------------------------------------------
	// Protected methods
	// -------------------------------------------------------------------------

	protected void processInput() {

		try {
			serverDatagramSocket.receive(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return;
		}

		rawData = datagramPacket.getData();
		data = new String(rawData);
		dataDelimiterIndex = data.indexOf(":");
		if (dataDelimiterIndex < 0) {
			return;
		}
		data = data.substring(0, dataDelimiterIndex);

		tokens = data.split(",");

		if (tokens.length != 6) {
			return;
		}

		driveInput = 0;
		turnInput = 0;
		headLiftInput = 0;
		headTurnInput = 0;
		talking = 0;
		openMouth = 0;

		driveInput = Integer.valueOf(tokens[0]);
		turnInput = Integer.valueOf(tokens[1]);
		headLiftInput = Integer.valueOf(tokens[2]);
		headTurnInput = Integer.valueOf(tokens[3]);
		talking = Integer.valueOf(tokens[4]);
		openMouth = Integer.valueOf(tokens[5]);

		pwmController.setDriveInput(driveInput);
		pwmController.setTurnInput(turnInput);
		pwmController.setHeadLiftInput(headLiftInput);
		pwmController.setHeadTurnInput(headTurnInput);
		pwmController.setTalkInput(talking, talkSoundLevel, openMouth);
	}

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

		if (serverDatagramSocket != null) {
			serverDatagramSocket.close();
		}

		try {
			serverDatagramSocket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		pwmController = new PWMController();
	}
}
