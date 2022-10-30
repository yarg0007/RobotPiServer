package com.yarg.robotpiserver.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.ConnectException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.yarg.robotpiserver.server.client.manager.ConnectedClient;
import com.yarg.robotpiserver.server.util.QuickClient;
import com.yarg.robotpiserver.server.util.ResponseModel;

public class ConnectionHandlerTest {

	private HttpServiceManager manager;
	private static final int port = 8082;

	@AfterMethod(alwaysRun = true)
	public void cleanup() {
		if (manager != null) {
			manager.terminate();
		}
	}

	@Test(expectedExceptions = ConnectException.class)
	public void testUnknownEndpoint() throws Exception {
		QuickClient.sendGet(String.format("http://localhost:%d/unknown", port));
	}

	@Test
	public void testConnectSuccess() throws Exception {
		manager = new HttpServiceManager();
		ResponseModel response = QuickClient.sendGet(String.format("http://localhost:%d/connect", port));
		assertNotNull(response);
		assertEquals(response.getResponseCode(), 200);
		assertNotNull(response.getHeaders());
		assertEquals(response.getHeaders().size(), 3);
		assertEquals(response.getPayload(), "Connected");
		assertTrue(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "127.0.0.1");
	}

	@Test
	public void testDisconnectSuccess() throws Exception {
		manager = new HttpServiceManager();
		QuickClient.sendGet(String.format("http://localhost:%d/connect", port));
		ResponseModel response = QuickClient.sendGet(String.format("http://localhost:%d/disconnect", port));
		assertNotNull(response);
		assertEquals(response.getResponseCode(), 200);
		assertNotNull(response.getHeaders());
		assertEquals(response.getHeaders().size(), 3);
		assertEquals(response.getPayload(), "Disconnected");
		assertFalse(ConnectedClient.getInstance().isClientConnected());
		assertEquals(ConnectedClient.getInstance().getClientIpAddress(), "");
	}
}
