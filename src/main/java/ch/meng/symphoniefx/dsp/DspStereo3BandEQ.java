package ch.meng.symphoniefx.dsp;

public class DspStereo3BandEQ extends DspStereoBaseEffect implements DspStereoEffect {

    public static String getName(){return "Cross Echo";}

    public DspStereo3BandEQ() {
        active = false;
    }
    DSP3BandEQ leftEQ = new DSP3BandEQ();
    DSP3BandEQ rightEQ = new DSP3BandEQ();

    public void init(boolean activate,int lpFrequency, int hpFrequency, int mymixfreq) {
        leftEQ.init(activate, lpFrequency, hpFrequency, mymixfreq);
        rightEQ.init(activate, lpFrequency, hpFrequency, mymixfreq);
    }
    public void setFilterTabs(double lowGain, double middleGain, double highGain) {
        leftEQ.setFilterTabs(lowGain, middleGain, highGain);
        rightEQ.setFilterTabs(lowGain, middleGain, highGain);
    }

    @Override
    public void stream(StereoSample sample) {
        if (!active) return;
        try {
        } catch(Exception e) {
            System.out.println("Dsp:" + e.getMessage());
        }
    }

}
