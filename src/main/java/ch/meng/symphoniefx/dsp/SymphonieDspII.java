package ch.meng.symphoniefx.dsp;

public class SymphonieDspII {
    private boolean active;
    private final int channels = 2;
    private int samplesPerChannel = 0;

    private double feedback;
    private double[] sampleBuffer;
    private int readpos;

    SymphonieDspII() {
        active = false;
    }

    public void enable(boolean enable) {
        this.active = enable;
    }

    public void adjustFeedback(double feedback) {
        this.feedback = feedback;
    }

    public void init(float bpm, int frequency, int delayFrames, double feedback) {
        active = false;
        if(bpm < 50 || bpm > 200
                || feedback>0.90 || feedback < 0.001
                || delayFrames < 1
                || delayFrames > 128
                || frequency < 5
                || frequency > 192000) return;
        this.feedback = feedback;
        samplesPerChannel = delayFrames * (int) (frequency / bpm);
        readpos = 0;
        sampleBuffer = new double[samplesPerChannel];
        active = true;
    }

    public double stream(double sample) {
        if(!active) return sample;
        try {
            double wetsample = sample + (sampleBuffer[readpos] * feedback);
            sampleBuffer[readpos] = wetsample;
            readpos = (readpos+1) % samplesPerChannel;
            return wetsample;
        } catch(Exception e) {
            System.out.println("Dsp:" + e.getMessage());
        }
        return sample;
    }


    // delay
//        double wetsample = sample + sampleBuffer[readpos];
//        sampleBuffer[readpos] = sample * feedback;
}
