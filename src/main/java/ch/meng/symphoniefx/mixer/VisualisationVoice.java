package ch.meng.symphoniefx.mixer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VisualisationVoice {
    private final int numberOfSamplesPerChannel;
    private final int numberOfFrames;
    private final int numberOfChannels;
    private List<Integer> samples = new Vector<>(0);
    private Map<Integer, Integer> randomAccessWriteBuffer = new HashMap<>(0);
    private List<Integer> linearWriteBuffer = new Vector<>(0);
    private boolean isRandomAccessWrite = false;
    private int actualStep = 0;
    private int maxStep = 0; //0==disabled
    private int sampleIndexForRandoAccessBuffer = 0;
    private double volume;

    public VisualisationVoice(int numberOfChannels, int numberOfSamplesPerChannel) {
        this.numberOfChannels = numberOfChannels;
        this.numberOfSamplesPerChannel = numberOfSamplesPerChannel;
        this.numberOfFrames = 1;
        samples = new Vector<>(numberOfChannels * numberOfSamplesPerChannel);
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public void saveSampleEveryNthSamples(int maxStep) {
        this.maxStep = maxStep;
        actualStep = 0;
    }

    public void advanceStep() {
        actualStep++;
        if (actualStep > maxStep) {
            actualStep = 0;
            sampleIndexForRandoAccessBuffer++;
        }
    }

    public void CopyWriteBufferToReadBuffer() {
        if (!isRandomAccessWrite) {
            samples = linearWriteBuffer;
            return;
        }
        List<Integer> tempSamples = new Vector<>(0);
        for (int sampleIndex = 0; sampleIndex < numberOfSamplesPerChannel; sampleIndex++) {
            for (int voiceIndex = 0; voiceIndex < numberOfChannels; voiceIndex++) {
                tempSamples.add(randomAccessWriteBuffer.getOrDefault((sampleIndex * numberOfChannels) + voiceIndex, 0));
            }
        }
        this.samples = tempSamples;
    }

    public List<Integer> getNewSampleBuffer() {
        return new Vector<>(numberOfChannels * numberOfSamplesPerChannel);
    }

    public void initLinearWriteBuffer() {
        linearWriteBuffer = new Vector<>(numberOfChannels * numberOfSamplesPerChannel);
        isRandomAccessWrite = false;
    }

    // A hashmap is used as not all voices have data written to
    public void initRandomAccessWriteBuffer() {
        randomAccessWriteBuffer = new HashMap<>(numberOfChannels * numberOfSamplesPerChannel);
        isRandomAccessWrite = true;
        sampleIndexForRandoAccessBuffer = 0;
    }

    public void addSample(final int sample) {
        if (!isRandomAccessWrite) linearWriteBuffer.add(sample);
    }

    public void addSample(final int voiceIndex, final int sample) {
        if (isRandomAccessWrite && maxStep > 0 && 0 == actualStep) {
            randomAccessWriteBuffer.put((sampleIndexForRandoAccessBuffer * numberOfChannels) + voiceIndex, sample);
        }
    }

    public int getSample(final int voiceIndex, final int sampleIndex) {
        try {
            int index = (sampleIndex * numberOfChannels) + voiceIndex;
            if (index < samples.size()) return samples.get(index);
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public void setBuffer(List<Integer> samples) {
        this.samples = samples;
    }

    public int getNumberOfSamplesPerChannel() {
        return numberOfSamplesPerChannel;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public List<Integer> getSamples() {
        return samples;
    }
}
