package ch.meng.symphoniefx.mixer;

import ch.meng.symphoniefx.dsp.DSP3BandEQ;
import ch.meng.symphoniefx.dsp.DspMonoFilterSymphonie;
import ch.meng.symphoniefx.dsp.DspMonoFx;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import symreader.FilterType;

public class Voice {
    boolean inUse = false; // not end of sample reached
    private boolean muted = false;

    double mixFrequency;
    private double volume = 100.0;
//    double maxVolume = 0;
//    double maxInstrumentVolume = 0;
    private double PlayFrequency = 440.0f;
    float SourceFrequency = 44100.0f;
    double SamplePtr = 0.0f;

    // Looping
    private int NumbOfLoopsRemaining = 0;
    double LastSample = 0.0f; // Last Sample heard from this voice
    boolean isPausing = false;
    private SymphonieInstrument instrument;
    int LastInstrumentIDPlayed = -1;

    // LFO
    VoiceLFO volumeLFO;
    VoiceLFO LFOPitch;
    VoiceLFO LFOSample;

    //DSPFilterPerfectLP4 filterPerfectLP4;
    private DSP3BandEQ voiceEQ = new DSP3BandEQ();
    // Anticlicksystem
    VoiceSmoother Smoother;

    // Panning
    private boolean PanningActive = false;
    private float PanningX = 0;  // -1000 = Left, 1000 = Right
    private final float PanningY = 0;
    private final float PanningZ = 0;
    double SampleR = 0.0f;

//    void clearMaxima() {
//        maxVolume = 0;
//        maxInstrumentVolume = 0;
//    }

    Voice(int mixFrequency) {
        this.mixFrequency = mixFrequency;
        volumeLFO = new VoiceLFO(0, 100);
        LFOPitch = new VoiceLFO(0, 10000);
        LFOSample = new VoiceLFO(0, 255);
        Smoother = new VoiceSmoother();
        voiceEQ.init(false, 880, 5000, mixFrequency);
        setMuted(false);
        LastInstrumentIDPlayed = -1;
    }

    public void setInstrument(SymphonieInstrument instrument) {
        this.instrument = instrument;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getVolume() {
        return volume;
    }

    public double getSamplePtr() {
        return SamplePtr;
    }

    public boolean isInUse() {
        return inUse;
    }

    public SymphonieInstrument getInstrument() {
        return instrument;
    }

    void setPanningX(float PositionLR) {
        PanningX = PositionLR;
    }

    void setPanningX(SymphonieInstrument si) {
        if (si != null) {
            PanningActive = si.isPanningActive();
            PanningX = si.getPanningX();
        } else {
            PanningActive = false;
        }
    }

    void activatePanning(boolean b) {
        PanningActive = b;
    }

    boolean checkPanningActive() {
        return (PanningActive);
    }

    float getPanningX() {
        if (PanningActive) {
            return (PanningX);
        } else {
            return (0);
        }
    }

    void setMixFrequency(double myMixFrequency) {
        mixFrequency = myMixFrequency;
    }


    int getLastInstrIdPlayed() {
        return (LastInstrumentIDPlayed);
    }

    void processAllLFOs() {
        if (volumeLFO.isRunning() == true) volume = volumeLFO.applyToValue(volume);
        if (LFOPitch.isRunning() == true) PlayFrequency = LFOPitch.applyToValue(PlayFrequency);
    }

    void setMuted(boolean b) {
        this.muted = b;
    }

    boolean isMuted() {
        return (this.muted);
    }

    boolean isInstrumentMuted() {
        return instrument != null && instrument.isMuted();
    }

//    void toogleMute() {
//        this.muted = !this.muted;
//    }

    void pausePlaying() {
        isPausing = true;
    }

    void continuePlaying() {
        isPausing = false;
    }

    void setNumbOfLoopsRemaining(int NumbOfLoops) {
        NumbOfLoopsRemaining = NumbOfLoops;
    }

    int getNumbOfLoopsRemaining() {
        return (NumbOfLoopsRemaining);
    }

    void decNumbOfLoopsRemaining() {
        if (NumbOfLoopsRemaining > 0) NumbOfLoopsRemaining--;
    }

    int getSampleEndPtr() {
        return (this.instrument.getSampleChannel().getNumbOfSamples() - 1);
    }

    void setFreq(float f, boolean StopLFO) {
        PlayFrequency = f;
        if (StopLFO || (f == 0.0f)) {
            LFOPitch.stop();
        }
    }

    void setFreq(double frequency, boolean StopLFO) {
        PlayFrequency = frequency;
        if (StopLFO || 0.0 == frequency) {
            LFOPitch.stop();
        }
    }

    double getFreq() {
        return (PlayFrequency);
    }

    public void stop() {
        volumeLFO.stop();
        LFOPitch.stop();
        LFOSample.stop();
        inUse = false;
        Smoother.stop();
        isPausing = false;
        LastSample = 0.0f;
//        Filter.off();
//        Filter.clearSampleBuffers();
        voiceEQ.init(false);
    }

    public DSP3BandEQ getVoiceEQ() {
        return voiceEQ;
    }

    public double stream(double sample) {
        if (voiceEQ.isActive()) sample = voiceEQ.stream(sample);
        if (resoFilter!=null && resoFilter.isActive()) sample = resoFilter.stream(sample);
        return sample;
    }

    private DspMonoFx resoFilter = new DspMonoFilterSymphonie();
    public void enableResonantFilter(FilterType type, double resonance, double frequency, double mixFrequency) {
        if (!resoFilter.isActive()) {
            resoFilter.init(type, resonance, frequency, mixFrequency);
        } else {
            resoFilter.adjust(resonance, frequency, mixFrequency);
        }
    }
}