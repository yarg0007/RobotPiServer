package com.yarg.gen.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class ConfigurationModelAudioStreamServer {

    @SerializedName("receivePort")
    private Integer receivePort = 49809;

    @SerializedName("sendPort")
    private Integer sendPort = 49808;

    @SerializedName("microphoneMixerName")
    private String microphoneMixerName = "Set [plughw:1,0]";

    public Integer getReceivePort() { return receivePort; }
    public void setReceivePort(Integer receivePort) { this.receivePort = receivePort; }

    public Integer getSendPort() { return sendPort; }
    public void setSendPort(Integer sendPort) { this.sendPort = sendPort; }

    public String getMicrophoneMixerName() { return microphoneMixerName; }
    public void setMicrophoneMixerName(String microphoneMixerName) { this.microphoneMixerName = microphoneMixerName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationModelAudioStreamServer that = (ConfigurationModelAudioStreamServer) o;
        return Objects.equals(receivePort, that.receivePort) &&
               Objects.equals(sendPort, that.sendPort) &&
               Objects.equals(microphoneMixerName, that.microphoneMixerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receivePort, sendPort, microphoneMixerName);
    }
}
