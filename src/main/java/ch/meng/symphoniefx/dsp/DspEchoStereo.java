package ch.meng.symphoniefx.dsp;

public class DspEchoStereo extends DspStereoBaseEffect implements DspStereoEffect {
    public static String getName(){return "Echo";}

    public DspEchoStereo() {
        active = false;
    }

    @Override
    public void stream(StereoSample sample) {
        if (!active || sampleBuffer.length<2) return;
        try {
            synchronized (active) {
                sample.setDspLeft(sample.getDspLeft() + (sampleBuffer[sampleBufferPtr] * feedback));
                sample.setDspRight(sample.getDspRight() + (sampleBuffer[sampleBufferPtr + 1] * feedback));
                sampleBuffer[sampleBufferPtr] = sample.getDspLeft();
                sampleBuffer[sampleBufferPtr + 1] = sample.getDspRight();
                sampleBufferPtr = (sampleBufferPtr + 2) % (effectLengthInSamples * channels);
            }
        } catch (Exception exception) {
            System.out.println("DspEchoStereo:" + exception.getMessage());
        }
    }

}
