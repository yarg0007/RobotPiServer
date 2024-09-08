package com.yarg.robotpiserver.audio;

import javax.sound.sampled.AudioFormat;

public class AudioFormatUtil {

    public static AudioFormat getAudioFormat() {

        float sampleRate = 44100.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
