/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.meng.symphoniefx.dsp;

import symreader.FilterType;

public class Dsp4PolLPFilter implements DspMonoFx {
    double frequency; // filterfreq
    double resonance;
    private FilterType filtertype = FilterType.Off;
    private double audioDeviceFrequency = 44100;

    public Dsp4PolLPFilter() {
        init(FilterType.Off, 0, 0, 44100);
        clearSampleBuffers();
    }

    private void clearSampleBuffers() {
        z[0] = 0;
        z[1] = 0;
        z[2] = 0;
        z[3] = 0;
        z[4] = 0;
    }

    @Override
    public void adjust(double qFactor, double filterFreq, final double audioDeviceFrequency) {
        if(filterFreq>255) filterFreq = 255;
        if(qFactor>192) qFactor = 192;
        if(filterFreq<0) filterFreq = 0;
        if(qFactor<0) qFactor = 0;

        this.audioDeviceFrequency = audioDeviceFrequency;
        this.frequency = filterFreq * 10.0;
        this.resonance = qFactor + 0.000001;
    }

    @Override
    public void init(final FilterType filterType, double qFactor, double filterFreq, final double audioDeviceFrequency) {
        clearSampleBuffers();

        this.filtertype = filterType;
        if (filterFreq == 0.0) {
            filtertype = FilterType.Off;
            return;
        }
        adjust(qFactor, filterFreq, audioDeviceFrequency);

        double w0 = 2 * Math.PI * frequency;
        double alpha = Math.sin(w0) / (2 * resonance);
        double cosw0 = Math.cos(w0);

        b[0] = (1 - cosw0) / 2;
        b[1] = 1 - cosw0;
        b[2] = b[0];
        a[0] = 1 + alpha;
        a[1] = -2 * cosw0;
        a[2] = 1 - alpha;
    }

    @Override
    public boolean isActive() {
        return filtertype != FilterType.Off;
    }

    @Override
    public double stream(double sample) {
        if (filtertype == FilterType.Off) return sample;
        z[0] = clip(sample * b[0] + z[1] * b[1] + z[2] * b[2] - z[3] * a[1] - z[4] * a[2]);
        z[4] = z[3];
        z[3] = z[2];
        z[2] = z[1];
        z[1] = z[0];
        return z[0];
    }

    double max = 1.0;
    public void setClipping(double max) {
        this.max = max;
    }

    private double clip(double Sample) {
        if (Sample > max) return max;
        if (Sample < -max) return -max;
        return Sample;
    }

    public String getShortDescription() {
        if(!this.isActive()) return "";
        StringBuilder text = new StringBuilder();
        text.append("Reso:").append((int) this.resonance)
                .append("Freq:").append((int) this.frequency);
        return text.toString();
    }

    private final double[] a = new double[5];
    private final double[] b = new double[5];
    private final double[] z = new double[5];



}
