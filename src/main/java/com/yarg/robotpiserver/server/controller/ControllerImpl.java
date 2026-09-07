package com.yarg.robotpiserver.server.controller;

import com.yarg.robotpiserver.audio.AudioStreamServer;
import com.yarg.robotpiserver.config.Configuration;
import com.yarg.robotpiserver.util.Generated;
import com.yarg.robotpiserver.video.VideoStream;

import java.net.InetSocketAddress;

@Generated
public class ControllerImpl implements ControllerInterface {

    private InetSocketAddress clientSocketAddress;
    private VideoStream videoStream;
    private AudioStreamServer audioStreamServer;

    @Override
    public void startController(InetSocketAddress clientSocketAddress) {
        this.clientSocketAddress = clientSocketAddress;
        String clientIpAddress = this.clientSocketAddress.getAddress().getHostAddress();

        int videoStreamPort = Configuration.getInstance().getConfigurationModel().getVideoStreamPort();
        videoStream = new VideoStream(clientIpAddress, videoStreamPort, Runtime.getRuntime());
        videoStream.startVideoStream();

        int audioReceivePort = Configuration.getInstance().getConfigurationModel().getAudioStreamServer().getReceivePort();
        int audioSendPort = Configuration.getInstance().getConfigurationModel().getAudioStreamServer().getSendPort();
//        audioStreamServer = new AudioStreamServer(clientIpAddress, audioReceivePort, audioSendPort);
//        audioStreamServer.startAudioStream();

        // TODO: start input control
    }

    @Override
    public void stopController() {

        if (videoStream != null && videoStream.isVideoStreamRunning()) {
            videoStream.stopVideoStream();
        }

        if (audioStreamServer != null) {
            audioStreamServer.stopAudioStream();
        }
        // TODO: stop audio
        // TODO: stop input control
    }
}
