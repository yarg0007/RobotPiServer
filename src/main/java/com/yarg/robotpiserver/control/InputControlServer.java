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

public class InputControlServer extends Thread implements AudioLevelListener{

	private static final int PORT = 49801;

	private boolean running = false;

	// Should be a value between 0 and 100.
	private int talkSoundLevel = 0;

	// This is a copy of the last sound level received from audio input. Can
	// be as large as needed from the audio source.
	private int previousSoundLevel = 0;

	PWMController pwmController;

	/** The connected client. Setup to only allow a single client connection.*/
	private DatagramSocket serverDatagramSocket = null;

	public InputControlServer() {
		initialize();
	}

	public void startInputControlServer() {
		pwmController.startPWMController();
		running = true;
		this.start();
		System.out.println("Input control server ready.");
	}

	public void stopInputControlServer() {

		running = false;
		this.interrupt();

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

		byte[] datagramBuffer = new byte[64];
		DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);

		//			int numTokens = 6;

		int driveInput = 0;
		int turnInput = 0;
		int headLiftInput = 0;
		int headTurnInput = 0;
		int talking = 0;
		int openMouth = 0;

		byte[] rawData;
		String[] tokens;

		try {

			while (running) {

				serverDatagramSocket.receive(datagramPacket);

				rawData = datagramPacket.getData();
				String data = new String(rawData);
				data = data.substring(0, data.indexOf(":"));

				tokens = data.split(",");

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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException nfe) {
			//ignore.
		}

	}

	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------

	private void initialize() {

		if (running) {
			running = false;
			this.interrupt();

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
}
