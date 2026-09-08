package com.yarg.robotpiserver;

import com.yarg.robotpiserver.server.ConnectionServer;
import com.yarg.robotpiserver.server.controller.ControllerImpl;
import com.yarg.robotpiserver.server.controller.ControllerInterface;
import com.yarg.robotpiserver.server.handler.impl.ConnectHandler;
import com.yarg.robotpiserver.server.handler.impl.DisconnectHandler;
import com.yarg.robotpiserver.util.Generated;

import java.io.IOException;

@Generated // Ignore Jacoco
public class RobotPiServer {

	public static void main(String[] args) throws Exception {

		System.out.println("Setting up server and waiting for connection...");

		ControllerInterface controller = new ControllerImpl();
		final ConnectionServer server = new ConnectionServer(
				new ConnectHandler(controller),
				new DisconnectHandler(controller));

		server.startServer();
		System.out.println("Server started on port " + server.getServerAddress().getPort());

		final Object lock = new Object();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					server.stopServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		});

		synchronized (lock) {
			lock.wait();
		}
	}
}
