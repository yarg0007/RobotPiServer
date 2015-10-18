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

public class AudioStreamServer extends Thread implements DatagramClientReturnAddress {
	
	private int RECEIVE_PORT = 49809;
	
	private int SEND_PORT = 49808;
	
	private String SERVER_ADDRESS;

	SourceDataLineThread incomingStream;
	TargetDataLineThread microphoneStream;
	
	public AudioStreamServer() {
		SERVER_ADDRESS = null;
		incomingStream = new SourceDataLineThread(RECEIVE_PORT, this);
		microphoneStream = new TargetDataLineThread(this, SEND_PORT);
		
		incomingStream.initialize();
		microphoneStream.initialize();
	}

	public void startAudioStream() {
		incomingStream.startAudioStreamSpeakers();
		microphoneStream.startAudioStreamMicrophone();
	}
	
	public void stopAudioStream() {
		incomingStream.stopAudioStreamSpeakers();
		microphoneStream.stopAudioStreamMicrophone();
	}
	
	public void addAudioLevelListener(AudioLevelListener listener) {
		incomingStream.addAudioLevelListener(listener);
	}
	
	public void removeAudioLevelListener(AudioLevelListener listener) {
		incomingStream.removeAudioLevelListener(listener);
	}

	// -------------------------------------------------------------------------
	// Methods required by DatagramClientReturnAddress
	// -------------------------------------------------------------------------
	
	@Override
	public void setAddress(String address) {
		SERVER_ADDRESS = address;
		
		String videoCommand = String.format("/usr/bin/raspivid -n -t 0 -h 480 -w 640 -fps 10 -hf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! udpsink host=%s port=5000", SERVER_ADDRESS);
		
		try {
			String[] cmd = {
					"/bin/sh",
					"-c",
					videoCommand
					};
			
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getAddress() {
		return SERVER_ADDRESS;
	}

}