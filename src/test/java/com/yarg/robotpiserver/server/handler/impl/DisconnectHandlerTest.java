package com.yarg.robotpiserver.server.handler.impl;

import com.yarg.gen.models.DisconnectRequest;
import com.yarg.robotpiserver.client.ClientConnection;
import com.yarg.robotpiserver.config.Configuration;
import com.yarg.robotpiserver.server.ConnectionServer;
import com.yarg.robotpiserver.server.controller.ControllerInterface;
import com.yarg.robotpiserver.util.SendRequest;
import com.yarg.robotpiserver.util.response.model.TestResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class DisconnectHandlerTest {

    private static final String CONNECT_URL = "http://localhost:1234/connect";
    private static final String DISCONNECT_URL = "http://localhost:1234/disconnect";

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
        new DisconnectHandler(null);
    }

    @Test
    public void disconnectWhenConnectedReturns200AndStopsController() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");
        server = new ConnectionServer(new ConnectHandler(controller), new DisconnectHandler(controller));
        server.startServer();

        SendRequest.get(CONNECT_URL);
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(true)));

        DisconnectRequest disconnectRequest = new DisconnectRequest();
        TestResponse response = SendRequest.post(DISCONNECT_URL, disconnectRequest);

        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(false)));
        verify(controller, times(1)).stopController();
    }

    @Test
    public void disconnectWhenNotConnectedIsIdempotent() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");
        server = new ConnectionServer(new ConnectHandler(controller), new DisconnectHandler(controller));
        server.startServer();

        DisconnectRequest disconnectRequest = new DisconnectRequest();
        TestResponse response = SendRequest.post(DISCONNECT_URL, disconnectRequest);

        assertThat(response.getStatusCode(), is(equalTo(200)));
        verify(controller, never()).stopController();
    }

    @Test
    public void shutdownTrueCallsInitiateShutdown() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");

        final boolean[] shutdownCalled = {false};
        DisconnectHandler handler = new DisconnectHandler(controller) {
            @Override
            protected void initiateShutdown() {
                shutdownCalled[0] = true;
            }
        };

        server = new ConnectionServer(new ConnectHandler(controller), handler);
        server.startServer();

        SendRequest.get(CONNECT_URL);

        DisconnectRequest disconnectRequest = new DisconnectRequest();
        disconnectRequest.setShutdown(true);
        SendRequest.post(DISCONNECT_URL, disconnectRequest);

        assertThat(shutdownCalled[0], is(equalTo(true)));
    }

    @Test
    public void shutdownFalseSkipsInitiateShutdown() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");

        final boolean[] shutdownCalled = {false};
        DisconnectHandler handler = new DisconnectHandler(controller) {
            @Override
            protected void initiateShutdown() {
                shutdownCalled[0] = true;
            }
        };

        server = new ConnectionServer(new ConnectHandler(controller), handler);
        server.startServer();

        SendRequest.get(CONNECT_URL);

        DisconnectRequest disconnectRequest = new DisconnectRequest();
        disconnectRequest.setShutdown(false);
        SendRequest.post(DISCONNECT_URL, disconnectRequest);

        assertThat(shutdownCalled[0], is(equalTo(false)));
    }
}
