package com.yarg.robotpiserver.server.controller;

import com.yarg.gen.models.ConfigurationModel;
import com.yarg.gen.models.ConfigurationModelAudioStreamServer;
import com.yarg.robotpiserver.audio.AudioStreamServer;
import com.yarg.robotpiserver.audio.TargetDataLineException;
import com.yarg.robotpiserver.config.Configuration;
import com.yarg.robotpiserver.control.InputControlServer;
import com.yarg.robotpiserver.util.Generated;
import com.yarg.robotpiserver.video.VideoStream;

import java.net.InetSocketAddress;

/**
 * Coordinates starting and stopping all robot subsystems (video, audio, input control)
 * in response to client connection lifecycle events.
 *
 * Excluded from JaCoCo coverage because all methods instantiate and coordinate hardware.
 */
@Generated
public class ControllerImpl implements ControllerInterface {

    private VideoStream videoStream;
    private AudioStreamServer audioStreamServer;
    private InputControlServer inputControlServer;

    /** Production constructor. */
    public ControllerImpl() {
    }

    /** Test constructor. Allows injecting pre-built subsystems for unit testing. */
    public ControllerImpl(AudioStreamServer audioStreamServer, InputControlServer inputControlServer) {
        this.audioStreamServer = audioStreamServer;
        this.inputControlServer = inputControlServer;
    }

    @Override
    public void startController(InetSocketAddress clientAddress) {

        String clientIp = clientAddress.getAddress().getHostAddress();
        ConfigurationModel cfg = Configuration.getInstance().getConfigurationModel();

        int videoStreamPort = cfg.getVideoStreamPort();
        videoStream = new VideoStream(clientIp, videoStreamPort, Runtime.getRuntime());
        videoStream.startVideoStream();

        ConfigurationModelAudioStreamServer audioConfig = cfg.getAudioStreamServer();
        if (audioConfig != null) {
            try {
                audioStreamServer = new AudioStreamServer(clientIp, audioConfig);
            } catch (TargetDataLineException e) {
                System.out.println("Failed to initialize audio stream: " + e.getMessage());
                e.printStackTrace();
                audioStreamServer = null;
            }
        }

        inputControlServer = new InputControlServer();
        inputControlServer.startInputControlServer();

        if (audioStreamServer != null) {
            audioStreamServer.addAudioLevelListener(inputControlServer);
            audioStreamServer.startAudioStream();
        }
    }

    @Override
    public void stopController() {

        if (videoStream != null && videoStream.isVideoStreamRunning()) {
            videoStream.stopVideoStream();
            videoStream = null;
        }

        if (audioStreamServer != null) {
            audioStreamServer.stopAudioStream();
            audioStreamServer = null;
        }

        if (inputControlServer != null) {
            inputControlServer.stopInputControlServer();
            inputControlServer = null;
        }
    }
}
