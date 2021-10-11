package symreader;

import ch.meng.symphoniefx.dsp.DspMonoFilterSymphonie;
import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Vector;

public class VirtualSampleBuilder {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    private Song song;

    public void buildVirtualSamples(Song song) {
        if (song == null) return;
        this.song = song;
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            buildVirtualSample(instrument);
            postProcessSample(instrument); // all samples
        }
    }

    public void rebuildVirtualSamples(Song song) {
        if (song == null) return;
        this.song = song;
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            if(instrument.isVirtualSample()) {
                buildVirtualSample(instrument);
                postProcessSample(instrument);
            }

        }
    }

    public void postProcessSample(SymphonieInstrument instrument) {
        if(instrument.getSamplePool() == null) return;
        postProcessSampleFade(instrument);
        postProcessCompressor(instrument);
        postProcessResonantFilter(instrument);
        buildNullstellen(instrument);
    }

    public void buildNullstellen(SymphonieInstrument instrument) {
        float[] samples = instrument.getSamplePool().getSamples();
        List<Integer> nullstellen = new Vector<>();
        if(samples==null) {
            logger.debug("Instrument has no Samples:" + instrument.getShortDescription());
            return;
        }
        for (int i = 1; i < samples.length; i++) {
            if(Math.signum(samples[i]) != Math.signum(samples[i-1])) nullstellen.add(i);
        }
        instrument.setNullstellen(nullstellen);
    }

    private void postProcessSampleFade(SymphonieInstrument instrument) {
        if ((1 == instrument.getFadeFromVolume() && 1 == instrument.getFadeToVolume())
                || (0 == instrument.getFadeFromVolume() && 0 == instrument.getFadeToVolume())
                ) return;
        float[] destinationSamples = instrument.getSamplePool().getSamples();
        for (int i = 0; i < destinationSamples.length; i++) {
            double volume = (((instrument.getFadeToVolume() - instrument.getFadeFromVolume()) * i) / destinationSamples.length) + instrument.getFadeFromVolume();
            destinationSamples[i] *= volume;
        }
    }

    private void postProcessCompressor(SymphonieInstrument instrument) {

        if ((0 == instrument.getCompressorLevel() || instrument.getCompressorLevel() > 0.99999)) return;
        float[] destinationSamples = instrument.getSamplePool().getSamples();
        float limit = 0.5f + (instrument.getCompressorLevel() * 0.5f);
        float rest = 1.0f - limit;
        for (int i = 0; i < destinationSamples.length; i++) {
            if (Math.abs(destinationSamples[i]) <= rest) {
                destinationSamples[i] = limit * destinationSamples[i] / rest;
            } else {
                if (destinationSamples[i] > 0) destinationSamples[i] = (rest * (destinationSamples[i]-rest)) + limit;
                else destinationSamples[i] = (rest * (destinationSamples[i]+rest)) - limit;;
            }
        }
    }

    void normalize(SymphonieInstrument instrument) {
        float max = 0.0f;
        float[] destinationSamples = instrument.getSamplePool().getSamples();
        for (int i = 0; i < destinationSamples.length; i++) {
            max = Math.max(max, Math.abs(destinationSamples[i]));
        }
        logger.debug("Max is" + max + " normalizing to 1.0");
        for (int i = 0; i < destinationSamples.length; i++) {
            destinationSamples[i] = destinationSamples[i] / max * 1.0f;
        }
    }

    double clippingMax = 9.0;
    void postProcessResonantFilter(SymphonieInstrument instrument) {
        if(!instrument.hasResonantFilter()) return;
        DspMonoFilterSymphonie filterFX= new DspMonoFilterSymphonie();
        float[] destinationSamples = instrument.getSamplePool().getSamples();
        if(instrument.getResoFilterSteps() == 1) {
            filterFX.setClipping(clippingMax);
            filterFX.init(FilterType.Lowpass,
                    instrument.getResoFilterSweepStartResonance(),
                    instrument.getResoFilterSweepStartFrequency(),
                    44100
            );
            for (int i = 0; i < destinationSamples.length; i++) {
                destinationSamples[i] = (float) filterFX.stream(destinationSamples[i]);
            }
            normalize(instrument);
        }
        if(instrument.getResoFilterSteps() == 2) {
            filterFX.setClipping(clippingMax);
            filterFX.init(FilterType.Lowpass,
                    instrument.getResoFilterSweepStartResonance(),
                    instrument.getResoFilterSweepStartFrequency(),
                    44100);
            for (int samplePosition = 0; samplePosition < destinationSamples.length; samplePosition++) {
                double frequency =  getValueRelativToSamplePosition(instrument.getResoFilterSweepStartFrequency(),
                        instrument.getResoFilterSweepEndFrequency(),
                        samplePosition, destinationSamples.length);
                double resonance  =  getValueRelativToSamplePosition(instrument.getResoFilterSweepStartResonance(),
                        instrument.getResoFilterSweepEndResonance(),
                        samplePosition, destinationSamples.length);
                filterFX.adjust(resonance, frequency, 44100);
                destinationSamples[samplePosition] = (float) filterFX.stream(destinationSamples[samplePosition]);
            }
            normalize(instrument);
        }

    }

    double getValueRelativToSamplePosition(double fromValue, double toValue, double position, double total) {
        return ((position/total) * (toValue-fromValue)) + fromValue;
    }

    void buildVirtualSample(SymphonieInstrument instrument) {
        if (instrument.isVirtualSample()) {
            instrument.setHasContent(true);
            for (VirtualMixStep virtualMixStep : instrument.getVirtualMixSteps()) {
                virtualMixStep.setMixInstrumentIndex(song.getIndexOfInstrumentID(virtualMixStep.getMixInstrumentId()));
                if (virtualMixStep.getMixInstrumentIndex() < 0) return;
            }
            if (1 == instrument.getVirtualMixSteps().size())
                cloneVirtualSample(instrument, instrument.getVirtualMixSteps().get(0));
            else if (instrument.isVirtualMix()) buildVirtualSampleMix(instrument);
        }
    }

    void buildVirtualSampleMix(SymphonieInstrument instrument) {
        SymphonieInstrument sourceInstrument = song.getInstrument(instrument.getVirtualMixSteps().get(0).getMixInstrumentIndex());
        if (sourceInstrument == null) {
            return;
        }
        instrument.buildVirtualSampleMix(song);
        instrument.setTune(instrument.getTune() + sourceInstrument.getDownsamplingTuneCorrection());
    }

    void cloneVirtualSample(SymphonieInstrument instrument, VirtualMixStep virtualMixStep) {
        SymphonieInstrument sourceInstrument = song.getInstrumentWithId(virtualMixStep.getMixInstrumentId());
        if (sourceInstrument == null) {
            return;
        }
        instrument.cloneSample(sourceInstrument);
        instrument.setTune(instrument.getTune() + sourceInstrument.getDownsamplingTuneCorrection());
    }

//    void buildTestSample(SymphonieInstrument instrument) {
//        SamplePool samplePool = new SamplePool(2 * 5000);
//        if (instrument == null) {
//            return;
//        }
//        float[] destinationSamples = samplePool.getSamples();
//        for (int i = -5000; i < 5000; i++) {
//            destinationSamples[5000 + i] = i / 5000.0f;
//        }
//        instrument.setSamplePool(samplePool);
//    }
}
