package com.yarg.robotpiserver;

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

import java.io.Console;
import java.io.IOException;

import com.yarg.robotpiserver.audio.AudioStreamServer;
import com.yarg.robotpiserver.control.InputControlServer;

public class RobotPiServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Setting up server and waiting for connection...");
		
		AudioStreamServer streamServer = new AudioStreamServer();
		streamServer.startAudioStream();
		
		InputControlServer inputControls = new InputControlServer();
		inputControls.startInputControlServer();
		
		streamServer.addAudioLevelListener(inputControls);
		
		Console console = System.console();
		String input = console.readLine("Press enter to quit.");
		
		System.out.println("Shutting down robot pi receiver.");
		
		streamServer.stopAudioStream();
		streamServer.removeAudioLevelListener(inputControls);
		System.out.println("Audio stream stopped.");
		
		inputControls.stopInputControlServer();
		System.out.println("Input control server stopped.");
		
		String[] cmd = {
				"/bin/sh",
				"-c",
				"kill $(ps aux | grep '[g]st-launch-1.0' | awk '{print $2}')",
				"kill $(ps aux | grep '[r]aspivid' | awk '{print $2}')"
				};
		
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Video stream stopped.");
		
		System.out.println("Shutdown complete.");
	}

}
