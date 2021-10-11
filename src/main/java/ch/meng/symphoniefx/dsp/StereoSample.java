package ch.meng.symphoniefx.dsp;

import java.util.Arrays;

public class StereoSample {
    private final double[] drySample = new double[2];
    private final double[] wetSymple = new double[2];

    public StereoSample() {
    }

    public void clear() {
        drySample[0] = 0;
        drySample[1] = 0;
        wetSymple[0] = 0;
        wetSymple[1] = 0;
    }

    public StereoSample(final double left, final double right, final double dspLeft, final double dspRight) {
        drySample[0] = left;
        drySample[1] = right;
        wetSymple[0] = dspLeft;
        wetSymple[1] = dspRight;
    }

    public void add(final StereoSample sample) {
        drySample[0] += sample.drySample[0];
        drySample[1] += sample.drySample[1];
        wetSymple[0] += sample.wetSymple[0];
        wetSymple[1] += sample.wetSymple[1];
    }

    public void multiply(final double factor) {
        drySample[0] *= factor;
        drySample[1] *= factor;
        wetSymple[0] *= factor;
        wetSymple[1] *= factor;
    }

    public void addSample(final int stereoChannel, final boolean dsp, final double sample) {
        if(dsp) wetSymple[stereoChannel] += sample;
        else drySample[stereoChannel] += sample;
    }

    public void adjustVolume(final double volume) {
        drySample[0] *= volume;
        drySample[1] *= volume;
        wetSymple[0] *= volume;
        wetSymple[1] *= volume;
    }

    public double getLeftMix() {return drySample[0] + wetSymple[0];}
    public double getRightMix() {
        return drySample[1] + wetSymple[1];
    }

    public double getDspLeft() {
        return wetSymple[0];
    }
    public double getDspRight() {
        return wetSymple[1];
    }
    public void setDspLeft(double sample) {wetSymple[0] = sample;}
    public void setDspRight(double sample) {
        wetSymple[1] = sample;
    }

    public double getDryLeft() {
        return drySample[0];
    }
    public double getDryRight() {
        return drySample[1];
    }
    public void setDryLeft(double sample) {drySample[0] = sample;}
    public void setDryRight(double sample) {
        drySample[1] = sample;
    }



    @Override
    public String toString() {
        return "StereoSample{" +
                "drySample=" + Arrays.toString(drySample) +
                ", wetSymple=" + Arrays.toString(wetSymple) +
                '}';
    }
}
