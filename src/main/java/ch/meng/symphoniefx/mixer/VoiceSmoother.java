package ch.meng.symphoniefx.mixer;

class VoiceSmoother {
    private boolean SampleSmoothing;
    private boolean FadeOut = false;
    private int SampleSmoothingRemaining = 0;
    private double PrevSample;
    private int SmoothNumbOfSamples = 25;

    VoiceSmoother() {
        SampleSmoothing = false;
        FadeOut = false;
    }

    void setSmoothingLen(int len) {
        SmoothNumbOfSamples = len;
    }

    void stop() {
        SampleSmoothing = false;
        FadeOut = false;
    }

    void activateSampleSmoothing() {
        SampleSmoothing = false;
        SampleSmoothingRemaining = SmoothNumbOfSamples;
        SampleSmoothing = true;
        FadeOut = false;
    }

    void activateFadeOut() {
        SampleSmoothing = false;
        SampleSmoothingRemaining = SmoothNumbOfSamples;
        SampleSmoothing = true;
        FadeOut = true;
    }

    void activateFadeIn() {
        SampleSmoothing = false;
        SampleSmoothingRemaining = SmoothNumbOfSamples;
        PrevSample = 0.0f;
        SampleSmoothing = true;
        FadeOut = false;
    }

    boolean isFadeingOut() {
        return (SampleSmoothing && FadeOut);
    }

    double getSmoothedSample(double Sample) {
        if ((SampleSmoothing == true) && (SampleSmoothingRemaining > 0)) {
            if (FadeOut == true) Sample = 0.0f;
            double OriginalPart = SampleSmoothingRemaining;
            OriginalPart = OriginalPart / SmoothNumbOfSamples;
            Sample = (OriginalPart * PrevSample) + (Sample * (1 - OriginalPart));
            SampleSmoothingRemaining--;
            if (SampleSmoothingRemaining <= 0) SampleSmoothing = false;
        } else {
            SampleSmoothing = false;
            this.PrevSample = Sample;
        }
        return (Sample);
    }
}