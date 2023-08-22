package com.yarg.robotpiserver.server;

import com.yarg.gen.models.DisconnectRequest;
import com.yarg.robotpiserver.client.ClientConnection;
import com.yarg.robotpiserver.config.Configuration;
import com.yarg.robotpiserver.server.handler.impl.ConnectHandler;
import com.yarg.robotpiserver.server.handler.impl.DisconnectHandler;
import com.yarg.robotpiserver.util.SendRequest;
import com.yarg.robotpiserver.util.response.model.TestResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ConnectionServerTest {

    private ConnectionServer server;

    @BeforeMethod(alwaysRun = true)
    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        if (server != null) {
            server.stopServer();
        }

        Configuration.getInstance().reinitializeToDefault();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullConnectHandler() throws Exception {
        server = new ConnectionServer(null, new DisconnectHandler());
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(false)));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullDisconnectHandler() throws Exception {
        server = new ConnectionServer(new ConnectHandler(), null);
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(false)));
    }

    @Test
    public void sendConnectRequest() throws Exception {

        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");

        server = new ConnectionServer(new ConnectHandler(), new DisconnectHandler());
        server.startServer();

        TestResponse response = SendRequest.get("http://localhost:1234/connect");
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getPayload(), is(equalTo("{\"message\":\"Connection established.\"}")));
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(true)));
    }

    @Test
    public void sendConnectRequestAgainAfterInitialConnection() throws Exception {

        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");

        server = new ConnectionServer(new ConnectHandler(), new DisconnectHandler());
        server.startServer();

        SendRequest.get("http://localhost:1234/connect");
        TestResponse response = SendRequest.get("http://localhost:1234/connect");
        assertThat(response.getStatusCode(), is(equalTo(500)));
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(true)));
    }

    @Test
    public void sendDisconnectRequest() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");

        server = new ConnectionServer(new ConnectHandler(), new DisconnectHandler());
        server.startServer();

        SendRequest.get("http://localhost:1234/connect");
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(true)));

        DisconnectRequest disconnectRequest = new DisconnectRequest();
        TestResponse response = SendRequest.post("http://localhost:1234/disconnect", disconnectRequest);
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getPayload(), is(equalTo("{\"message\":\"Connection successfully terminated.\"}")));
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(false)));
    }

    @Test
    public void sendDisconnectRequestTwice() throws Exception {
        Configuration.getInstance().reinitializeWithResourceConfig("/com/yarg/robotpiserver/server/connectionServerTestConfig.json");

        server = new ConnectionServer(new ConnectHandler(), new DisconnectHandler());
        server.startServer();

        SendRequest.get("http://localhost:1234/connect");
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(true)));

        DisconnectRequest disconnectRequest = new DisconnectRequest();
        SendRequest.post("http://localhost:1234/disconnect", disconnectRequest);
        TestResponse response = SendRequest.post("http://localhost:1234/disconnect", disconnectRequest);
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getPayload(), is(equalTo("{\"message\":\"No connection exists. Connection terminated.\"}")));
        assertThat(ClientConnection.getInstance().hasConnection(), is(equalTo(false)));
    }
}