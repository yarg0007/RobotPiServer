package com.yarg.robotpiserver.audio;

import javax.sound.sampled.AudioFormat;

public class AudioFormatUtil {

    /**
     * Get the audio format.
     *
     * @return Audio format to use for recording.
     */
    public static AudioFormat getAudioFormat() {

        // 44100 Hz is one of two rates the C-Media USB adapter supports natively (44100 and 48000).
        // Using 16000 forced ALSA plughw to resample 3x in software, causing CPU spikes
        // and buffer underruns (pops/static) on the slow ARMv6 Pi 1.
        float sampleRate = 44100.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false; // little-endian to match Android PCM_16BIT

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
