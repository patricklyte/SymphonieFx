package ch.meng.symphoniefx.dsp;

import symreader.FilterType;

import java.util.Arrays;

public class DspStereoTest4PolLPFilter extends DspStereoBaseEffect implements DspStereoEffect {

    public static String getName() {
        return "DspStereoTest4PolLPFilter";
    }

    public DspStereoTest4PolLPFilter() {
        active = false;
    }

    DspMonoFx leftFx = new Dsp4PolLPFilter();
    DspMonoFx rightFx = new Dsp4PolLPFilter();
    DspMonoFx leftFxDsp = new Dsp4PolLPFilter();
    DspMonoFx rightFxDsp = new Dsp4PolLPFilter();

    public void init(FilterType type, double reso, double frequency, double mixFrequency) {
        this.enable(false);
        for (DspMonoFx dspMonoFx : Arrays.asList(leftFx, rightFx, leftFxDsp, rightFxDsp)) {
            dspMonoFx.init(type, reso, frequency, mixFrequency);
        }
        this.enable(true);
    }

    public void setClipping(double max) {
        for (DspMonoFx dspMonoFx : Arrays.asList(leftFx, rightFx, leftFxDsp, rightFxDsp)) {
            ((Dsp4PolLPFilter) dspMonoFx).setClipping(max);
        }
    }

    public void adjust(double reso, double frequency, double mixFrequency) {
        for (DspMonoFx dspMonoFx : Arrays.asList(leftFx, rightFx, leftFxDsp, rightFxDsp)) {
            dspMonoFx.adjust(reso, frequency, mixFrequency);
        }
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
            System.out.println("Dsp4PolLPFilter:" + e.getMessage());
        }
    }

}
