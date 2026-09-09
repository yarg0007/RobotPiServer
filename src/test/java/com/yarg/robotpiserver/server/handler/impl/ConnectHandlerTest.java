package com.yarg.robotpiserver.server.handler.impl;

import com.yarg.robotpiserver.client.ClientConnection;
import com.yarg.robotpiserver.config.Configuration;
import com.yarg.robotpiserver.server.ConnectionServer;
import com.yarg.robotpiserver.server.controller.ControllerInterface;
import com.yarg.robotpiserver.util.SendRequest;
import com.yarg.robotpiserver.util.response.model.TestResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConnectHandlerTest {

    private static final String CONNECT_URL = "http://localhost:1234/connect";

    private ConnectionServer server;
    private ControllerInterface controller = mock(ControllerInterface.class);

    @BeforeMethod(alwaysRun = true)
    @AfterMethod(alwaysRun = true)
    public void cleanup() throws Exception {
        if (server != null) {
            server.stopServer();
        }
        reset(controller);
        ClientConnection.getInstance().disconnectClient();
        Configuration.getInstance().reinitializeToDefault();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullController() {
        new ConnectHandler(null);
    }

    @Test
    public void freshConnectReturns200AndStartsController() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");
        server = new ConnectionServer(new ConnectHandler(controller), new DisconnectHandler(controller));
        server.startServer();

        TestResponse response = SendRequest.get(CONNECT_URL);

        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(true)));
        verify(controller, never()).stopController();
        verify(controller, times(1)).startController(any(InetSocketAddress.class));
    }

    @Test
    public void reconnectStopsExistingSessionThenStartsNew() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");
        server = new ConnectionServer(new ConnectHandler(controller), new DisconnectHandler(controller));
        server.startServer();

        SendRequest.get(CONNECT_URL);
        TestResponse response = SendRequest.get(CONNECT_URL);

        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(true)));
        verify(controller, times(1)).stopController();
        verify(controller, times(2)).startController(any(InetSocketAddress.class));
    }
}
