package ch.meng.symphoniefx.dsp;

import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;

public abstract class DspStereoBaseEffect implements DspStereoEffect {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    protected Boolean active;
    protected int channels = 2;
    protected int effectLengthInSamples = 0;
    protected int effectLengthInFrames = 0; // frequency independent
    protected double feedback;
    protected double[] sampleBuffer;
    protected int sampleBufferPtr;
    protected double bpm = 125;
    protected double frequency = 44100;

    DspStereoBaseEffect() {
        active = false;
    }

    @Override
    public void enable(boolean enable) {
        this.active = enable;
    }

    @Override
    public void adjustFeedback(double feedback) {
        this.feedback = feedback;
    }

    @Override
    public void adjustFrequency(double frequency) {
        synchronized (active) {
            this.frequency = frequency;
            reInit();
        }
    }

//    @Override
//    public void adjustBpm(double bpm) {
//        synchronized (active) {
//            this.bpm = bpm;
//            reInit();
//        }
//    }

    @Override
    public void adjustDelayFrames(int effectLengthInFrames, int effectLengthInSamples) {
        synchronized (active) {
            this.effectLengthInFrames = effectLengthInFrames;
            this.effectLengthInSamples = effectLengthInSamples;
            reInit();
        }
    }

    private void reInit() {
        logger.debug("Init:frequency=" + frequency
                + " effectLengthInSamples=" + effectLengthInSamples
                + " effectLengthInFrames=" + effectLengthInFrames);
        active = false;
        sampleBufferPtr = 0;
        sampleBuffer = new double[effectLengthInSamples * channels];
        active = true;
    }

    @Override
    public void init(int channels, double bpm, int frequency, int effectLengthInFrames, int effectLengthInSamples, double feedback) {

        if (bpm < 10 || bpm > 1000
                || feedback > 1.0 || feedback < 0
                || effectLengthInSamples < 8
                || effectLengthInSamples > 2000000
                || frequency < 5
                || frequency > 19200000) return;
        synchronized (active) {
            logger.debug("Init:frequency=" + frequency
                    + " effectLengthInSamples=" + effectLengthInSamples
                    + " effectLengthInFrames=" + effectLengthInFrames);
            this.active = false;
            this.feedback = feedback;
            this.channels = channels;
            this.sampleBufferPtr = 0;
            this.effectLengthInSamples = effectLengthInSamples;
            this.sampleBuffer = new double[this.effectLengthInSamples * channels];
            this.frequency = frequency;
            this.effectLengthInFrames = effectLengthInFrames;
            this.bpm = bpm;
            this.active = true;
        }
    }

    public Boolean isActive() {
        return active;
    }

    public double getFeedback() {
        return feedback;
    }

    public int getEffectLengthInFrames() {
        return effectLengthInFrames;
    }

    @Override
    public abstract void stream(StereoSample sample);

}
