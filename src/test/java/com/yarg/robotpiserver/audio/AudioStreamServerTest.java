package com.yarg.robotpiserver.audio;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AudioStreamServerTest {

    private AudioStreamServer audioStreamServer;
    private SourceDataLineThread incomingStream = mock(SourceDataLineThread.class);
    private TargetDataLineThread microphoneStream = mock(TargetDataLineThread.class);

    @BeforeMethod(alwaysRun = true)
    public void reset() {
        Mockito.reset(incomingStream, microphoneStream);
    }

    @Test
    public void startAndStopAudioStream() {
        audioStreamServer = new AudioStreamServer(incomingStream, microphoneStream);
        audioStreamServer.startAudioStream();
        audioStreamServer.stopAudioStream();

        verify(incomingStream, times(1)).startAudioStreamSpeakers();
        verify(microphoneStream, times(1)).startAudioStreamMicrophone();
        verify(incomingStream, times(1)).stopAudioStreamSpeakers();
        verify(microphoneStream, times(1)).stopAudioStreamMicrophone();
    }

    @Test
    public void addAndRemoveAudioLevelListener() {
        doNothing().when(incomingStream).addAudioLevelListener(any(AudioLevelListener.class));
        doNothing().when(incomingStream).removeAudioLevelListener(any(AudioLevelListener.class));

        AudioLevelListenerImpl impl = new AudioLevelListenerImpl();

        audioStreamServer = new AudioStreamServer(incomingStream, microphoneStream);
        audioStreamServer.addAudioLevelListener(impl);

        verify(incomingStream, times(1)).addAudioLevelListener(any(AudioLevelListener.class));
        verify(incomingStream, never()).removeAudioLevelListener(any(AudioLevelListener.class));

        audioStreamServer.removeAudioLevelListener(impl);

        verify(incomingStream, times(1)).addAudioLevelListener(any(AudioLevelListener.class));
        verify(incomingStream, times(1)).removeAudioLevelListener(any(AudioLevelListener.class));
    }
}

class AudioLevelListenerImpl implements AudioLevelListener {

    @Override
    public void audioLevelUpdate(int audioLevelBaseline, int currentAudioLevel) {
        ; // Do nothing.
    }
}
