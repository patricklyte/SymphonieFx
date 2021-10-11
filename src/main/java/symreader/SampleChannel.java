package symreader;

import ch.meng.symphoniefx.song.SymphonieInstrument;

import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

public class SampleChannel {
    private float[] samples;
    private int NumbOfSamples = 0;
    // Looping
    private boolean loopEnabled = false;
    private boolean endlessLoop = false;
    private int numberOfLoops = 0;
    private int loopStart = 0;
    private int loopLength = 0;
    private int loopEnd = 0;


    SampleChannel() {
        samples = null;
        NumbOfSamples = 0;
        loopEnabled = false;
    }

    public SampleChannel(int numbOfSamples) {
        init(numbOfSamples);
        loopEnabled = false;
    }

    // New Sample Loader
    public SampleChannel(List<Float> samplesIn) {
        init(samplesIn.size());
        loopEnabled = false;
        samples = new float[samplesIn.size()];
        int index = 0;
        for(Float sample : samplesIn) {
            samples[index++] = sample;
        }
    }



    @XmlTransient // dont save with jaxb xxxxx
    public float[] getSamples() {
        return samples;
    }

    public void setSamples(float[] samplePtr) {
        samples = samplePtr;
    }

    public void init(int numberOfSamples) {
        samples = new float[numberOfSamples];
        this.NumbOfSamples = numberOfSamples;
    }

    public void setSample(int index, float sample) {
        samples[index] = sample;
    }

    public float getSample(int index) {
        if(index < samples.length) return samples[index];
        return 0;
    }

    public int getNumbOfSamples() {
        if (this.samples == null) this.NumbOfSamples = 0;
        assert (this.NumbOfSamples != samples.length);
        return (this.NumbOfSamples);
    }

    void setNumbOfSamples(int i) {
        this.NumbOfSamples = i;
        if (this.NumbOfSamples == 0) this.samples = null;
    }

    public boolean hasLoop() {
        return (loopEnabled);
    }

    public int getNumberOfLoops() {
        if (loopEnabled == true) {
            return (numberOfLoops);
        } else {
            return (0);
        }
    }

    public int getLoopStart() {
        return (loopStart);
    }

    public int getLoopLen() {
        return (loopLength);
    }

    public int getLoopEnd() {
        return (loopEnd);
    }

    public boolean getEndlessLoop() {
        return (endlessLoop);
    }

    public void initLoopData(boolean loopEnabled, float loopStart, float loopLength, int numberOfLoops) {
        this.loopEnabled = false;
        this.endlessLoop = false;
        if ((this.NumbOfSamples > 0) && (loopLength > 0) && loopEnabled) {
            this.loopStart = (int) loopStart;
            this.loopLength = (int) loopLength;
            this.loopEnd = this.loopStart + this.loopLength;
            if (loopEnd >= this.NumbOfSamples) {
                this.loopEnd = this.NumbOfSamples - 1;
                this.loopLength = this.loopEnd - this.loopStart;
            }
            this.endlessLoop = false;
            if (numberOfLoops == 0) {
                this.endlessLoop = true;
            }
            this.numberOfLoops = numberOfLoops;
            assert (this.loopStart >= 0);
            assert (this.loopLength >= 0);
            assert (this.loopEnd >= 0);
            this.loopEnabled = true;
        }
    }



    void initLoopDateSymphonieFormat(SymphonieInstrument si, boolean InstrHasLoop, float Start, float Len, int newNumbOfLoops) {
        initLoopData(InstrHasLoop,
                ((Start * this.NumbOfSamples) / (100 * 256 * 256)),
                ((Len * this.NumbOfSamples) / (100 * 256 * 256)), newNumbOfLoops);
        // Update Loopparameters in Instrument
        si.setLoopStart(loopStart);
        si.setLoopLength(loopLength);
    }
    public int getFittingNullstelle(int samplePosition, List<Integer> nullstellen) {
        return  snapToNullstelle(nullstellen, samplePosition);
    }

    public void snapLoopToNullstellen(int Start, int Len, List<Integer> nullstellen) {
        loopStart = Start;
        loopLength = Len;
        loopEnd = loopStart + loopLength;
        if (loopEnd > getNumbOfSamples() - 1) {
            loopEnd = getNumbOfSamples() - 1;
            loopLength = loopEnd - loopStart;
            assert (this.loopStart >= 0);
            assert (this.loopLength >= 0);
            assert (this.loopEnd >= 0);
        }
        if(nullstellen.isEmpty()) return;
        loopStart = snapToNullstelle(nullstellen, loopStart);
        loopEnd = snapToNullstelle(nullstellen, loopEnd);
        loopLength = loopEnd - loopStart;
    }

    private int snapToNullstelle(List<Integer> nullstellen, int samplePosition) {
        if(nullstellen.size()<4) return samplePosition;
        for(int nullstelle : nullstellen) {
            if(nullstelle>samplePosition) return nullstelle;
        }
        return nullstellen.get(nullstellen.size()-1);
    }

    public void reverseSample() {
        if (samples == null || samples.length <= 1) return;
        float[] reversedSamples = new float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            reversedSamples[i] = samples[samples.length - i - 1];
        }
        samples = reversedSamples;
    }
}