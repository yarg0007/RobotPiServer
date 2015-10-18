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
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

public class SourceDataLineThread extends Thread {

	private String AUDIO_MIXER_NAME = "Set [plughw:1,0]";
	
	private int serverPort;
	
	/** The connected client. Setup to only allow a single client connection.*/
	private DatagramSocket serverDatagramSocket = null;
	
	/** Flag execution state of thread. */
	private boolean running;

	/** Plays audio to the speakers. */
	private SourceDataLine sourceDataLine;
	
	/** Interface for setting the client address to send audio back to. */
	private DatagramClientReturnAddress clientAddress;
	
	private ArrayList<AudioLevelListener> audioLevelListeners;

	/**
	 * Default constructor.
	 */
	public SourceDataLineThread(int serverPort, DatagramClientReturnAddress clientAddress) {
		this.serverPort = serverPort;
		this.clientAddress = clientAddress;
		audioLevelListeners = new ArrayList<AudioLevelListener>();
		initialize();
	}
	
	/**
	 * Initialize the instance. Setup Datagram server and then do all remaining
	 * the setup magic. Must be called after getting class instance.
	 */
	public void initialize() {
		
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
			serverDatagramSocket = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		if (sourceDataLine == null) {
			
			Info[] mixerInfo = AudioSystem.getMixerInfo();
			
			DataLine.Info dataLineInfo = 
					new DataLine.Info(SourceDataLine.class, getAudioFormat());
			
			for (int i = 0; i < mixerInfo.length; i++) {
				System.out.println("SOURCE DATA LINE MIXER: "+i);
				System.out.println("\tNAME: "+mixerInfo[i].getName());
				System.out.println("\tDESCRIPTION: "+mixerInfo[i].getDescription());
				System.out.println("\tVENDOR: "+mixerInfo[i].getVendor());
				System.out.println("\tVERSION: "+mixerInfo[i].getVersion());
				
				if (AUDIO_MIXER_NAME.equals(mixerInfo[i].getName())) {
					Mixer mixer = AudioSystem.getMixer(mixerInfo[i]);
					
					try {
						sourceDataLine = 
								(SourceDataLine) mixer.getLine(dataLineInfo);
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
				return;
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
	public void startAudioStreamSpeakers() {
		
		running = true;
		this.start();
	}
	
	/**
	 * Stop the speaker thread, close connections etc.
	 */
	public void stopAudioStreamSpeakers() {
		
		running = false;
		this.interrupt();
		
		sourceDataLine.flush();
		sourceDataLine.close();
		sourceDataLine = null;
		
		serverDatagramSocket.close();
		serverDatagramSocket = null;
		
		System.out.println("StopAudioStreamSpeakers complete.");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {			
			
			int dataLen = getAudioBufferSizeBytes();
			byte[] datagramBuffer = new byte[dataLen];
			DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, dataLen);
			byte[] rawData;
			
			
				System.out.println("Waiting for initial packet");
				
				try {
					serverDatagramSocket.receive(datagramPacket);
				} catch (IOException e) {
					System.out.println("Exception occurred with initial incoming audio stream. See stack trace for more infomation.");
					e.printStackTrace();
					stopAudioStreamSpeakers();
					return;
				}
				
				clientAddress.setAddress(datagramPacket.getAddress().getHostAddress());
				System.out.println("\nGot packet from: "+datagramPacket.getAddress().getHostAddress());
				
				while (running) {
					
					try {
						serverDatagramSocket.receive(datagramPacket);
					} catch (IOException e) {
						
						System.out.println("Exception on incoming audio stream. Pausing before continuing.");
						e.printStackTrace();
						
						// Let the system rest and then loop back to try the
						// next incoming data bit.
						try {
							sleep(500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						continue;
					}
					
					rawData = datagramPacket.getData();
					int maxSample = 0;
					for (int t = 0; t < rawData.length; t+=2) {
							int low = (int) rawData[t];
							t++;
							int high = (int) rawData[t+1];
							t++;
							int sample = (high << 8) + (low & 0x00ff);
							if (sample > maxSample)
									maxSample = sample;
					}

					for (AudioLevelListener listener : audioLevelListeners) {
						listener.audioLevelUpdate(300, maxSample);
					}
						
					sourceDataLine.write(
							datagramPacket.getData(), 
							0, 
							datagramPacket.getLength());
					
				}

	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Get the audio format.
	 * @return Audio format to use for recording.
	 */
	private AudioFormat getAudioFormat() {
		
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
	private int getAudioBufferSizeBytes() {

        int frameSizeInBytes = getAudioFormat().getFrameSize();
        int bufferLengthInFrames = sourceDataLine.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        return bufferLengthInBytes;
	}
	
}
