/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.meng.symphoniefx.mixer;

import com.sun.media.sound.SoftPointResampler;

interface InterpolateSampleType {
    int None = 0;
    int Linear = 1;
    int Cubic = 2;
    int Cosinus = 3;
    int Hermite = 4; // Bicubic Hermite Spline
    int JavaNearest = 5; // Bicubic Hermite Spline
    int Max = 5;
}

public class SampleInterpolator {
    private int Type = InterpolateSampleType.Hermite;
    private final double bias = 0.0d;
    private final double tension = 0.0d;
    private double y0, y1, y2, y3;
    private double mu;
    private int ptr;
    private double Sample;

    public SampleInterpolator() {
    }

    public void setInterpolateType(final InterpolationTypeEnum type) {
        switch (type) {
            case None: Type = InterpolateSampleType.None;
                break;
            case Linear: Type = InterpolateSampleType.Linear;
                break;
            case Cubic: Type = InterpolateSampleType.Cubic;
                break;
            case Cosinus: Type = InterpolateSampleType.Cosinus;
                break;
            case JavaNearest: Type = InterpolateSampleType.JavaNearest;
                break;
            default:
                Type = InterpolateSampleType.Hermite;
        }
    }

    public double getSample(final float[] samples, final double SamplePtr) {
        try {
            ptr = (int) SamplePtr;
            mu = SamplePtr - ptr; // mu range 0  to 1.0
            switch (Type) {
                case InterpolateSampleType.None:
                    return (samples[ptr]);
                case InterpolateSampleType.Linear:
                    if ((ptr + 1) <= (samples.length - 1)) {
                        Sample = samples[ptr + 1] * mu;
                        Sample += samples[ptr] * (1 - mu);
                        return (Sample);
                    } else {
                        return (samples[ptr]);
                    }
                case InterpolateSampleType.Cubic:
                    y1 = samples[ptr];
                    if (ptr < (samples.length - 1)) {
                        y2 = samples[ptr + 1];
                    } else {
                        y2 = samples[ptr];
                    }
                    if (ptr < (samples.length - 2)) {
                        y3 = samples[ptr + 2];
                    } else {
                        y3 = samples[ptr];
                    }
                    if (ptr > 0) {
                        y0 = samples[ptr - 1];
                    } else {
                        y0 = samples[0];
                    }
                    return (CubicInterpolate(y0, y1, y2, y3, mu));
                case InterpolateSampleType.Cosinus:
                    if ((ptr + 1) <= (samples.length - 1)) {
                        return (CosineInterpolate(samples[ptr], samples[ptr + 1], mu));
                    } else {
                        return (samples[ptr]);
                    }
                case InterpolateSampleType.Hermite:
                    y1 = samples[ptr];
                    if (ptr > 0) y0 = samples[ptr - 1];
                    else y0 = y1;
                    if (ptr < (samples.length - 1)) y2 = samples[ptr + 1];
                    else y2 = y1;
                    if (ptr < (samples.length - 2)) y3 = samples[ptr + 2];
                    else y3 = y1;
                    return (interpolateHermite(y0, y1, y2, y3, mu));
            }
        } catch (Exception exception) {
            if(ptr>samples.length-1) return samples[samples.length-1];
        }
        return (samples[ptr]);
    }

    private float CubicInterpolate(final double y0, final double y1, final double y2, final double y3, final double mu) {
        // Interpolate between y1 and y2, mu ist the exact position between y1 and y2
        // mu = 0.0 -> at y1
        final double mu2 = mu * mu;
        final double a0 = y3 - y2 - y0 + y1;
        final double a1 = y0 - y1 - a0;
        final double a2 = y2 - y0;
        final double a3 = y1;
        return ((float) (a0 * mu * mu2 + a1 * mu2 + a2 * mu + a3));
    }

    float CosineInterpolate(double y1, double y2, double mu) {
        final double mu2 = (1 - java.lang.Math.cos(mu * java.lang.Math.PI)) / 2;
        return ((float) (y1 * (1 - mu2) + y2 * mu2));
    }

    float interpolateHermite(final double y0, final double y1, final double y2, final double y3, final double mu) {
        final double mu2 = mu * mu;
        final double mu3 = mu2 * mu;
        final double m0 = ((y1 - y0) * (1 + bias) * (1 - tension) / 2) + ((y2 - y1) * (1 - bias) * (1 - tension) / 2);
        final double m1 = ((y2 - y1) * (1 + bias) * (1 - tension) / 2) + ((y3 - y2) * (1 - bias) * (1 - tension) / 2);
        final double a0 = 2 * mu3 - 3 * mu2 + 1;
        final double a1 = mu3 - 2 * mu2 + mu;
        final double a2 = mu3 - mu2;
        final double a3 = (-2 * mu3 + 3 * mu2);
        return ((float) (a0 * y1 + a1 * m0 + a2 * m1 + a3 * y2));
    }



}

