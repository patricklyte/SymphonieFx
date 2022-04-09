package ch.meng.symphoniefx.dsp;

public class SymphonieDspIIfloat {
    private boolean active;
    private final int channels = 2;
    private int samplesPerChannel = 0;

    private float feedback;
    private float[] sampleBuffer;
    private int readpos;

    SymphonieDspIIfloat() {
        active = false;
    }

    public void enable(boolean enable) {
        this.active = enable;
    }

    public void setFeedback(float feedback) {
        this.feedback = feedback;
    }

    public void init(float bpm, int frequency, int delayFrames, float feedback) {
        active = false;
        if(bpm < 50 || bpm > 200
                || feedback>0.90f || feedback < 0.001f
                || delayFrames < 1
                || delayFrames > 128
                || frequency < 5
                || frequency > 192000) return;
        this.feedback = feedback;
        samplesPerChannel = delayFrames * (int) (frequency / bpm);
        sampleBuffer = new float[samplesPerChannel];
        active = true;
    }

    public float stream(float sample) {
        if(!active) return sample;
        float wetsample = sample + (sampleBuffer[readpos] * feedback);
        sampleBuffer[readpos] = wetsample;
        readpos = (readpos+1) % samplesPerChannel;
        return wetsample;
    }

}
