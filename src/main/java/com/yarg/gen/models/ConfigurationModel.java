package com.yarg.gen.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class ConfigurationModel {

    @SerializedName("serverIpAddress")
    private String serverIpAddress = null;

    @SerializedName("serverPort")
    private Integer serverPort = 8001;

    @SerializedName("serverBackLogging")
    private Integer serverBackLogging = 0;

    @SerializedName("inputControlServerPort")
    private Integer inputControlServerPort = 49801;

    @SerializedName("audioStreamServer")
    private ConfigurationModelAudioStreamServer audioStreamServer = null;

    @SerializedName("videoStreamPort")
    private Integer videoStreamPort = 5000;

    public String getServerIpAddress() { return serverIpAddress; }
    public void setServerIpAddress(String serverIpAddress) { this.serverIpAddress = serverIpAddress; }

    public Integer getServerPort() { return serverPort; }
    public void setServerPort(Integer serverPort) { this.serverPort = serverPort; }

    public Integer getServerBackLogging() { return serverBackLogging; }
    public void setServerBackLogging(Integer serverBackLogging) { this.serverBackLogging = serverBackLogging; }

    public Integer getInputControlServerPort() { return inputControlServerPort; }
    public void setInputControlServerPort(Integer inputControlServerPort) { this.inputControlServerPort = inputControlServerPort; }

    public ConfigurationModelAudioStreamServer getAudioStreamServer() { return audioStreamServer; }
    public void setAudioStreamServer(ConfigurationModelAudioStreamServer audioStreamServer) { this.audioStreamServer = audioStreamServer; }

    public Integer getVideoStreamPort() { return videoStreamPort; }
    public void setVideoStreamPort(Integer videoStreamPort) { this.videoStreamPort = videoStreamPort; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationModel that = (ConfigurationModel) o;
        return Objects.equals(serverIpAddress, that.serverIpAddress) &&
               Objects.equals(serverPort, that.serverPort) &&
               Objects.equals(serverBackLogging, that.serverBackLogging) &&
               Objects.equals(inputControlServerPort, that.inputControlServerPort) &&
               Objects.equals(audioStreamServer, that.audioStreamServer) &&
               Objects.equals(videoStreamPort, that.videoStreamPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverIpAddress, serverPort, serverBackLogging, inputControlServerPort, audioStreamServer, videoStreamPort);
    }
}
