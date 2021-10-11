package ch.meng.symphoniefx.dsp;

public class DspCrossEchoStereo extends DspStereoBaseEffect implements DspStereoEffect {

    public static String getName(){return "Cross Echo";}

    public DspCrossEchoStereo() {
        active = false;
    }

    @Override
    public void stream(StereoSample sample) {
        if (!active || sampleBuffer.length<2) return;
        try {
            sample.setDspLeft(sample.getDspLeft() + (sampleBuffer[sampleBufferPtr] * feedback));
            sample.setDspRight(sample.getDspRight() + (sampleBuffer[sampleBufferPtr +1] * feedback));
            sampleBuffer[sampleBufferPtr] = sample.getDspRight();
            sampleBuffer[sampleBufferPtr +1] = sample.getDspLeft();
            sampleBufferPtr = (sampleBufferPtr +2) % (effectLengthInSamples * channels);
        } catch(Exception e) {
            System.out.println("Dsp:" + e.getMessage());
        }
    }

}
