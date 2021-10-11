package ch.meng.symphoniefx.dsp;

public interface DspStereoEffect {
    void enable(boolean enable);
    void adjustFeedback(double feedback);
    void adjustFrequency(double frequency);
    void adjustDelayFrames(int effectLengthInFrames, int effectLengthInSamples);
    void init(int channels, double bpm, int frequency, int effectLengthInFrames, int effectLengthInSamples, double feedback);
    void stream(StereoSample sample);
    Boolean isActive();
    double getFeedback();
    static String getName(){return "Undefined";}

    int getEffectLengthInFrames();
}
