package ch.meng.symphoniefx.dsp;

import symreader.FilterType;

public interface DspMonoFx {
    void init(FilterType newFiltertype, double myq, double filterFreq, double audioDeviceFrequency);
    void adjust(double qFactor, double filterFreq, double audioDeviceFrequency);
    boolean isActive();
    double stream(double sample);
    String getShortDescription();
}
