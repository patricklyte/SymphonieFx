package com.synthbot.audioio.vst;

import com.synthbot.audioplugin.vst.vst2.JVstHost2;
import org.apache.log4j.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class JVstAudioRenderer {
    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private JVstHost2 vst;
    private final float[][] fInputs;
    private final float[][] vstSamples;
    private final byte[] bOutput;
    private int bufferLengthPerChannelInSamples;
    private int numOutputs;
    private int numAudioOutputs;
    private static final float ShortMaxValueAsFloat = Short.MAX_VALUE; //* 0.5f;

    public JVstAudioRenderer(JVstHost2 vst) {
        max = 0;
        this.vst = vst;
        numOutputs = vst.numOutputs();
        numAudioOutputs = Math.min(2, numOutputs); // because most machines do not offer more than 2 output channels
        bufferLengthPerChannelInSamples = vst.getBlockSize();
        fInputs = new float[vst.numInputs()][bufferLengthPerChannelInSamples];
        vstSamples = new float[numOutputs][bufferLengthPerChannelInSamples];
        bOutput = new byte[numAudioOutputs * bufferLengthPerChannelInSamples * numAudioOutputs];
    }

    public void clearMax() {
        max = 0;
        corrections = 0;
        autocorrection = 1.0;
    }

    private double max = 0;
    private double autocorrection = 1.0;
    private int corrections = 0;
    public boolean stream(final double[] destinationBuffer, double volume) {
        if(max > Short.MAX_VALUE) {
            if(max > (Short.MAX_VALUE * 1.25)) corrections = 10;
            autocorrection = autocorrection * 0.9;
            max = 0;
            corrections ++;
        }
        if(corrections > 10) {
            return false;
        }
        volume = Short.MAX_VALUE * 0.01 * volume * autocorrection;
        final int vstBufferSamples = 2 * bufferLengthPerChannelInSamples;
        int loopsTillMixBufferFilled = destinationBuffer.length / vstBufferSamples;

        int destBufferIndex = 0;
        for(int vstloop = loopsTillMixBufferFilled; vstloop>0; vstloop--){
            vst.processReplacing(fInputs, vstSamples, bufferLengthPerChannelInSamples);
            for (int i = 0; i < vstBufferSamples; i += 2) {
                double sample = (volume * vstSamples[0][i / 2]);
                max = Math.max(max, Math.abs(sample));
                destinationBuffer[destBufferIndex + i] += sample;
                destinationBuffer[destBufferIndex + i + 1] += (volume * vstSamples[1][i / 2]);
            }
            destBufferIndex += vstBufferSamples;

            tempAutoKeyOffs = new HashMap<>();
            autoKeyOffs.forEach((delay, message)->processDelayedKeyOff(delay, message));
            autoKeyOffs = tempAutoKeyOffs;
        }
        return true;
    }

    public void addDelayedMessage(final double delayInSamples, final ShortMessage midiMessage) {
        autoKeyOffs.put(delayInSamples, midiMessage);
    }

    private Map<Double, ShortMessage> autoKeyOffs = new HashMap<>();
    private Map<Double, ShortMessage> tempAutoKeyOffs = new HashMap<>();
    public void processDelayedKeyOff(double delay, final ShortMessage midiMessage) {
        delay -= bufferLengthPerChannelInSamples;
        if(delay>0) tempAutoKeyOffs.put(delay, midiMessage);
        else {
            vst.queueMidiMessage(midiMessage);
        }
    }

}