package ch.meng.symphoniefx.mixer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;

public class JavaAudioDevice {
    private static final Logger logger = LogManager.getLogger();

    private int numberOfHardwareChannels = 2;
    int bytePerSample = 2;
    private SourceDataLine soundLine;
    private boolean deviceReady = false;

    public void close() {
        logger.debug("Closing Audio Device");
        if (soundLine != null) soundLine.close();
    }

    byte[] byteBuffer = new byte[0];
    public void writeToAudioDevice(double[] samples) {
        if (byteBuffer.length != samples.length * numberOfHardwareChannels) {
            byteBuffer = new byte[samples.length * numberOfHardwareChannels];
        }
        if (byteBuffer == null) return;
        int index = 0;
        for (double sample : samples) {
            int intSample = (int) clip(sample, 32767f);
            byteBuffer[index] = (byte) ((intSample >> 8) & 0xff);
            byteBuffer[index + 1] = (byte) (intSample & 0xff);
            index += 2;
        }
        if (deviceReady && soundLine != null && byteBuffer != null) {
            soundLine.write(byteBuffer, 0, samples.length * bytePerSample);
        }
    }

    double clip(double sample, double max) {
        if (sample > max) return max;
        if (sample > -max) return sample;
        return -max;
    }

    private Mixer.Info actualMixerInfo;
    public Mixer.Info getActualMixerInfo() {
        return actualMixerInfo;
    }

    public void init(Mixer.Info info, int numberOfHardwareChannels, int samplesPerChannel, int mixFrequency, double oversample) {
        deviceReady = false;
        this.numberOfHardwareChannels = numberOfHardwareChannels;
        if (info == null) {
            Mixer.Info[] audioDevices = AudioSystem.getMixerInfo();
            info = audioDevices[0];
        }
        actualMixerInfo = info;
        logger.debug("setAudioDevice " + info + ", Buffersize"  + samplesPerChannel * numberOfHardwareChannels + " " + mixFrequency);
        Mixer mixer = AudioSystem.getMixer(info);
        int NumbOfBits = 16;
        logger.debug("mixFrequency:" + mixFrequency + " oversample:" + oversample);
        logger.debug("audio device frequency:" + (float) (mixFrequency / oversample));
        AudioFormat soundFormat = new AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
                , (float) (mixFrequency / oversample), NumbOfBits,
                numberOfHardwareChannels, (NumbOfBits / 8) * numberOfHardwareChannels
                , (float) (mixFrequency / oversample), true); // (Freq / Frames) /2
        DataLine.Info soundlineInfo = new DataLine.Info(SourceDataLine.class, soundFormat); // format is an AudioFormat object

        try {
            soundLine = (SourceDataLine) mixer.getLine(soundlineInfo);
        } catch (LineUnavailableException | IllegalArgumentException e) {
            logger.debug("ERROR: Failed to set AudioDevice " + info + ":" + samplesPerChannel);
            e.printStackTrace();
            return;
        }

        try {
            logger.debug("Opening AudioDevice " + info + ", Buffersize:" + samplesPerChannel * numberOfHardwareChannels + " buffersize in byte:" + (bytePerSample * samplesPerChannel * numberOfHardwareChannels));
            soundLine.open(soundFormat, bytePerSample * samplesPerChannel * numberOfHardwareChannels);
            soundLine.start();
            deviceReady = true;
        } catch (Exception ex) {
            logger.debug("ERROR: Failed to start AudioDevice " + info + ":" + samplesPerChannel);
            logger.debug(ex.getMessage());
        }
    }

}
