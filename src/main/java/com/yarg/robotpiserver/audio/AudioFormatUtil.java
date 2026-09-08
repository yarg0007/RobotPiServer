package com.yarg.robotpiserver.audio;

import javax.sound.sampled.AudioFormat;

public class AudioFormatUtil {

    /**
     * Get the audio format.
     *
     * @return Audio format to use for recording.
     */
    public static AudioFormat getAudioFormat() {

        float sampleRate = 16000.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false; // little-endian to match Android PCM_16BIT

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
