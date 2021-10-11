package ch.meng.symphoniefx.dsp;

import symreader.FilterType;

public class DspStereoTestFXFilterSymphonie extends DspStereoBaseEffect implements DspStereoEffect {

    public static String getName() {
        return "Cross Echo";
    }

    public DspStereoTestFXFilterSymphonie() {
        active = false;
    }

    DspMonoFx leftFx = new DspMonoFilterSymphonie();
    DspMonoFx rightFx = new DspMonoFilterSymphonie();
    DspMonoFx leftFxDsp = new DspMonoFilterSymphonie();
    DspMonoFx rightFxDsp = new DspMonoFilterSymphonie();

    public void init(FilterType type, double reso, double frequency, double mixFrequency) {
        this.enable(false);
        leftFx.init(type, reso, frequency, mixFrequency);
        rightFx.init(type, reso, frequency, mixFrequency);
        leftFxDsp.init(type, reso, frequency, mixFrequency);
        rightFxDsp.init(type, reso, frequency, mixFrequency);
        this.enable(true);
    }

    public void setClipping(double max) {
        ((DspMonoFilterSymphonie) leftFx).setClipping(max);
        ((DspMonoFilterSymphonie) rightFx).setClipping(max);
        ((DspMonoFilterSymphonie) leftFxDsp).setClipping(max);
        ((DspMonoFilterSymphonie) rightFxDsp).setClipping(max);
    }

    public void adjust(double reso, double frequency, double mixFrequency) {
        leftFx.adjust(reso, frequency, mixFrequency);
        rightFx.adjust(reso, frequency, mixFrequency);
        leftFxDsp.adjust(reso, frequency, mixFrequency);
        rightFxDsp.adjust(reso, frequency, mixFrequency);
    }

    @Override
    public void stream(StereoSample sample) {
        if (!active) return;
        try {
            sample.setDryLeft(leftFx.stream(sample.getDryLeft()));
            sample.setDryRight(rightFx.stream(sample.getDryRight()));
            sample.setDspLeft(leftFxDsp.stream(sample.getDspLeft()));
            sample.setDspRight(rightFxDsp.stream(sample.getDspRight()));
        } catch (Exception e) {
            System.out.println("DspStereoTestFX:" + e.getMessage());
        }
    }

}
