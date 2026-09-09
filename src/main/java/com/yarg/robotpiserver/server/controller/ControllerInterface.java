package com.yarg.robotpiserver.server.controller;

import java.net.InetSocketAddress;

public interface ControllerInterface {

    void startController(InetSocketAddress clientSocketAddress);

    void stopController();
}
