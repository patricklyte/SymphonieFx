/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.meng.symphoniefx.mixer;

import ch.meng.symphoniefx.TimeMeasure2;
import ch.meng.symphoniefx.VstManager;
import ch.meng.symphoniefx.dsp.*;
import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import symreader.FilterType;

import java.lang.invoke.MethodHandles;
import java.util.*;

import static ch.meng.symphoniefx.SharedStatic.convertBytesToString;

public class VoiceExpander {
    public static final int NUMBER_OF_HARDWARE_CHANNELS = 2;
    private int bufferLenInSamples = 0;

    public static final String TIMEID_VOICES = "Voices";
    public static final String TIMEID_VST = "Vst";
    //public static final String TIMEID_VISUALS = "Visuals";
    public static final String TIMEID_TEST = "Test";
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private int mixFrequency = 44100;           // Output Mix Frequency of all Channels
    private double bpm = 120.0f;                 // Used for Beat Synced FX
    private int oversampling = 1;

    private double masterBpmTune = 100.0;                  // 100% = Original Speed
    private double masterVolume = 100.0;        // Global Mix Volume
    private final double masterVolumeCorrection = 80.0;        // Gets lower if clipping takes place
    private double masterTune = 0.0f;
    int maxNumberOfVoices = 64;                      // Numb of Sound Channels

    int MinPitch = 0;
    int MaxPitch = 257;
    private final int BasePitchOffset = 24;
    private final double FreqTableFactor = 0.1042f;


    private boolean isDeclickingEnabled = true;
    int Dithering = 0;

    private Voice[] voices;
    private final double[] FreqBase = {1.0000f, 1.0595f, 1.1225f, 1.1892f, 1.2599f, 1.3348f, 1.4142f, 1.4983f, 1.5874f, 1.6818f, 1.7818f, 1.8878f}; // Gleichschwebend
    private final double[] FreqTable = new double[MaxPitch + 1];
    private DspStereoEffect dsp = new DspEchoStereo();
    private final DSP3BandEQ EQLeft = new DSP3BandEQ();
    private final DSP3BandEQ EQRight = new DSP3BandEQ();
    private final SampleInterpolator sampleInterpolator = new SampleInterpolator();
    private Song song = null;

    private double freqIndependentSamplesPerEvent = 0; //5000
    private int SamplesTillSongEvent = 0; // 1 tick bei cycl=1

    // Sound Design
    private double pitchDiff = 0;
    private int sampleDiff = 0;


    double PannedSample = 0;
    double PanningR = 0;
    private int numberOfVoicesPlaying = 0;
    private Task task;

    public VoiceExpander() {
        voices = new Voice[maxNumberOfVoices];
        initAllVoices();
        InitFreqTable();
        EQLeft.init(false, 880, 5000, mixFrequency);
        EQRight.init(false, 880, 5000, mixFrequency);
    }

    public String getMuteStatus() {
        final StringBuilder text = new StringBuilder();
        text.append("\nInstruments enabled ");
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            if (!instrument.isMuted()) {
                text.append(" " + instrument);
            }
        }
        text.append("\nInstruments MUTED");
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            if (instrument.isMuted()) {
                text.append(" " + instrument);
            }
        }
        text.append("\nVOICES ENABLED");
        int voiceIndex = 0;
        for (Voice voice : voices) {
            if (!voice.isMuted()) {
                text.append(" " + voiceIndex);
            }
            voiceIndex++;
        }
        text.append("\nVOICES MUTED");
        voiceIndex = 0;
        for (Voice voice : voices) {
            if (voice.isMuted()) {
                text.append(" " + voiceIndex);
            }
            voiceIndex++;
        }
        return text.toString();
    }

    public double getMaxAmplitude() {
        return sampleMax;
    }

    public SampleInterpolator getSampleInterpolator() {
        return sampleInterpolator;
    }

    public void setEQLowIntensity(double gain) {
        EQLeft.setLowGain(gain);
        EQRight.setLowGain(gain);
    }

    public void setEQMidIntensity(double gain) {
        EQLeft.setMidGain(gain);
        EQRight.setMidGain(gain);
    }

    public void setEQHighIntensity(double gain) {
        EQLeft.setHighGain(gain);
        EQRight.setHighGain(gain);
    }

    public void setEQLPFrequency(double frequency) {
        EQLeft.setLPFrequency(frequency);
        EQRight.setLPFrequency(frequency);
    }

    public void setEQHPFrequency(double frequency) {
        EQLeft.setHPFrequency(frequency);
        EQRight.setHPFrequency(frequency);
    }

    public void enableEQ(boolean enable) {
        EQLeft.setActive(enable);
        EQRight.setActive(enable);
    }

    public double getBpm() {
        return bpm;
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
        setSongSpeed(bpm);
    }

    public void clearAllPlayedVoices() {
        voices = new Voice[maxNumberOfVoices];
    }

    public void setNotificationTask(Task task) {
        this.task = task;
    }

    public void setMasterVolume(double masterVolume) {
        logger.debug("setMasterVolume" + masterVolume);
        this.masterVolume = masterVolume;
    }

    public void setMasterTune(double masterTune) {
        this.masterTune = masterTune;
    }

    private double getFinalBpm() {
        return bpm * masterBpmTune / 100.0;
    }

    public void setMasterBpmTune(double bpmTune) {
        this.masterBpmTune = bpmTune;
        dsp.init(2,
                getFinalBpm(),
                this.mixFrequency,
                dsp.getEffectLengthInFrames(),
                dsp.getEffectLengthInFrames() * getNumberOfSamplesPerCycl() / 8,
                dsp.getFeedback());
    }

    public void setSampleDiff(double sampleDiff) {
        this.sampleDiff = (int) sampleDiff;
    }

    public void setPitchDiff(double pitchd) {
        pitchDiff = 1 + (pitchd / 5000.0);
    }

//    public byte[] getRenderBuffer() {
//        if (virtual) return audioMixBuffer.getRenderBuffer();
//        return javaAudioDevice.getAudioMixBuffer().getRenderBuffer();
//    }

    public void closeAll() {
        shutdown();
        if (song != null) song.stopSong();
        stopAllVoices();
        //closeAudioDevice();
        song = null;
    }

    private long samplesRendered;
    private final TimeMeasure2 timer = new TimeMeasure2();

    public void resetRenderInfo() {
        sampleMax = 0;
    }

    public double getRenderInfoTimeInMS() {
        return 1000.0 * samplesRendered / mixFrequency;
    }

    public String getRenderInfo() {
        final StringBuilder text = new StringBuilder();
        double time = 1.0 * samplesRendered / mixFrequency;
        text.append(convertBytesToString(samplesRendered / 4))
                .append(" ")
                .append(", Max Amplitude:").append((int) (sampleMax / 327.68)).append("%")
                .append(", Song Time Position:").append(getTimeFormatted(time));
        //.append(" Calculation:");
        return text.toString();
    }

    String getTimeFormatted(double time) {
        final StringBuilder text = new StringBuilder();
        int minutes = (int) (time / 60.0);
        double seconds = (time - (minutes * 60.0));
        if(seconds<10) {
            text.append(minutes).append(":0").append(String.format("%.2f s", seconds));
        } else {
            text.append(minutes).append(":").append(String.format("%.2f s", seconds));
        }
        return text.toString();
    }

    public void printAndResetStatistics() {
        logger.debug("Render Statistics:");
        logger.debug("MasterVolume " + masterVolume
                + ", masterVolumeCorrection " + masterVolumeCorrection
                + ", Mixfrequeny " + mixFrequency
                + ", bufferLenInSamples " + bufferLenInSamples
                + ", Sample Max " + sampleMax);
        logger.debug("Playing Time:" + getRenderInfo());
        logger.debug(getTimerInfo(TIMEID_VOICES)
                + ", " + getTimerInfo(TIMEID_VST));
                //+ ", " + getTimerInfo(TIMEID_VISUALS));
        resetStatistics();
    }

    public void resetStatistics() {
        samplesRendered = 0;
        timer.clear();
    }

    private String getTimerInfo(final String timeidVoices) {
        return timer.getSumAsString(timeidVoices) + ", "
                + getTimerRelativePercentage(timeidVoices);
    }

    private String getTimerRelativePercentage(final String timeidVoices) {
        if (getRenderInfoTimeInMS() <= 0.0) return "";
        return String.format("%.1f", 100.0 * timer.getSumInMS(timeidVoices) / getRenderInfoTimeInMS()) + "%";
    }

    public String getPerformance() {
        return " Samples:" + getTimerRelativePercentage(TIMEID_VOICES)
                + ", Vst:" + getTimerRelativePercentage(TIMEID_VST)
                //+ ", Visuals:" + getTimerRelativePercentage(TIMEID_VISUALS)
                + ", All:" + getTimerRelativePercentage(TIMEID_TEST);
    }

    double getPitchDiffFactor(int VoiceIndex) {
        if ((pitchDiff != 0) && ((VoiceIndex & 0x0001) == 1)) {
            return pitchDiff;
        } else {
            return 1.0f;
        }
    }

    int getSampleDiffOffset(int VoiceIndex) {
        if ((sampleDiff != 0) && ((VoiceIndex & 0x0001) == 1)) {
            return (sampleDiff);
        } else {
            return (0);
        }
    }

    // Set System Parameters
    void setHasDeclickingEnabled(boolean b) {
        isDeclickingEnabled = b;
    }

    // Song Interface
    public void setSong(final Song song) {
        this.song = song;
        this.SamplesTillSongEvent = getNumberOfSamplesPerCycl();
        this.bpm = song.getBpm();
    }

    private static final double CYCL_BASE_VALUE = 266382.0;

    private double actualcycl = 4;

    public void setSongSpeed(double bpm, double cycle) {
        this.bpm = bpm;
        actualcycl = cycle;
        this.freqIndependentSamplesPerEvent = CYCL_BASE_VALUE * cycle / bpm;
    }

    public void setSongSpeed(double bpm) {
        this.freqIndependentSamplesPerEvent = CYCL_BASE_VALUE * actualcycl / bpm;
    }

    public int getNumberOfSamplesPerCycl() {
        this.freqIndependentSamplesPerEvent = CYCL_BASE_VALUE * actualcycl / bpm;
        double temp = (freqIndependentSamplesPerEvent * 100.0) / masterBpmTune;
        temp = temp * mixFrequency / 44100.0;
        VstManager.setNumberOfSamplesPerCycle((int) temp);
        return (int) temp;
    }

    public double getNumberOfMillisecondsPerCycl() {
        this.freqIndependentSamplesPerEvent = CYCL_BASE_VALUE * actualcycl / bpm;
        return 1000.0 * freqIndependentSamplesPerEvent / 44100.0;
    }

    public void setDspEcho(boolean enable, int frames, double feedback) {
        print("Dsp Echo " + enable);
        if (enable) {
            dsp = new DspEchoStereo();
            dsp.init(2, getFinalBpm(), this.mixFrequency, frames, frames * getNumberOfSamplesPerCycl() / 8, feedback);
        } else dsp.enable(false);
    }

    public void setDspCrossEcho(boolean enable, int frames, double feedback) {
        print("Dsp Cross Echo " + enable);
        if (enable) {
            dsp = new DspCrossEchoStereo();
            dsp.init(2, getFinalBpm(), this.mixFrequency, frames, frames * getNumberOfSamplesPerCycl() / 8, feedback);
        } else dsp.enable(false);
    }

    public void disableDsp() {
        print("Dsp disabled");
        dsp.enable(false);
    }

    public DspStereoEffect getDsp() {
        return dsp;
    }

    int getNumbOfPitches() {
        return (MaxPitch - MinPitch);
    }

    private void InitFreqTable() {
        int counter = 0;
        double factor = FreqTableFactor;
        for (int i = 0; i < getNumbOfPitches(); i++) {
            FreqTable[i] = FreqBase[counter] * factor;
            counter++;
            if (counter > FreqBase.length - 1) {
                factor = factor * 2;
                counter = 0;
            }
        }
    }

    double getPitchToFreq(int VoiceNr, int Pitch, int Finetune) { // With Pitch Diff
        double f = getPitchToFreq(Pitch, Finetune);
        f = f * getPitchDiffFactor(VoiceNr);
        return f;
    }

    private double pitchToFrequencyRaw(int Pitch) {
        return FreqTable[Pitch] * 110.0f;
    }

    double getPitchToFreq(int Pitch, int Finetune) {
        double frequency;
        Pitch += BasePitchOffset;
        if (Pitch < MinPitch) Pitch = MinPitch;
        if (Pitch > MaxPitch - 1) Pitch = MaxPitch - 1;

        frequency = pitchToFrequencyRaw(Pitch);
        if (Finetune > 0) {
            double f1 = pitchToFrequencyRaw(Pitch + 1);
            frequency = frequency + ((f1 - frequency) * (Finetune / 127.0f));
        } else if (Finetune < 0) {
            double f1 = pitchToFrequencyRaw(Pitch - 1);
            frequency = frequency + ((frequency - f1) * (Finetune / 128.0f));
        }
        return frequency * 44100.0f / mixFrequency; // Mixrate correction
    }

    Voice getFreeVoice() {
        for (int i = 0; i < maxNumberOfVoices; i++) {
            if (voices[i] == null) {
                voices[i] = new Voice(mixFrequency);
                return (voices[i]);
            } else {
                if (voices[i].inUse == false) {
                    return (voices[i]);
                }
            }
        }
        return (null);
    }

    public Voice getVoiceNr(int i) {
        if (i < maxNumberOfVoices) {
            if (voices[i] == null) voices[i] = new Voice(mixFrequency);
            return (voices[i]);
        }
        return null;
    }

    public SymphonieInstrument getInstrumentOfVoiceNr(int i) {
        final Voice voice = getVoiceNr(i);
        if (voice == null) return null;
        return voice.getInstrument();
    }

    public boolean isVoiceWithDspEnabled(int i) {
        final SymphonieInstrument instrument = getInstrumentOfVoiceNr(i);
        if (instrument == null) return false;
        return instrument.isDspEnabled();
    }

    public Voice playKeyboardInstrumentNote(SymphonieInstrument instrument, int NoteIndex, double vol) {
        if (!instrument.hasContent()) return null;
        return playKeyboardInstrumentNote(instrument, getPitchToFreq(NoteIndex + instrument.getTune(), instrument.getFineTune()), vol);
    }

    private Voice playKeyboardInstrumentNote(SymphonieInstrument instrument, double freq, double vol) {
        final Voice voice = getFreeVoice();
        if (voice == null
                || instrument == null
                || instrument.getSampleChannel() == null
                || instrument.getSampleChannel().getNumbOfSamples() < 2) return null;
        voice.setInstrument(instrument);
        voice.LastInstrumentIDPlayed = voice.getInstrument().getID();
        voice.setVolume(vol);
        voice.setFreq(freq, true);
        voice.SourceFrequency = voice.getInstrument().getOldSampleImporter().getSampledFrequency();
        voice.inUse = true;
        voice.SamplePtr = 0.0f;
        voice.Smoother.activateFadeIn();
        voice.setPanningX(instrument);
        InitVoiceEQ(voice, instrument);
        return voice;
    }

    void InitVoiceEQ(Voice voice, SymphonieInstrument instrument) {
        voice.getVoiceEQ().init(instrument.getEQActive(), instrument.getEQLPFrequency(), instrument.getEQHPFrequency(), mixFrequency);
        voice.getVoiceEQ().setFilterTabs(instrument.getEQLowGain(), instrument.getEQMidGain(), instrument.getEQHighGain());
        voice.getVoiceEQ().clear();
    }

    public void SongEventPSlide(int VoiceNr, double PitchChangeSpeed) {
        SetVoicePSlide(VoiceNr, PitchChangeSpeed);
    }

    public void SongEventVSlide(int VoiceNr, double VolChangeSpeed) {
        SetVoiceVSlide(VoiceNr, VolChangeSpeed);
    }

    public void SongEventSetVolume(int VoiceNr, double vol) {
        double oldvol = GetVoiceVolume(VoiceNr);
        stopVolumeLFO(VoiceNr);
        if (java.lang.Math.abs(vol - oldvol) > 40.0f) {
            getVoiceNr(VoiceNr).Smoother.activateSampleSmoothing();
        }
        SetVoiceVolume(VoiceNr, vol);
    }

    public void SongEventAddVolume(int VoiceNr, double vol) {
        stopVolumeLFO(VoiceNr);
        SetVoiceVolume(VoiceNr, GetVoiceVolume(VoiceNr) + vol);
    }

    public void SongEventKeyOn(final SymphonieInstrument instrument, final int VoiceNr, final int NoteIndex, final double vol) {
        stopVolumeLFO(VoiceNr);
        SongEventKeyOnFreq(instrument, VoiceNr, getPitchToFreq(VoiceNr, NoteIndex + getInstrumentAndPositionTune(instrument), instrument.getFineTune()), vol);
    }

    private int getInstrumentAndPositionTune(final SymphonieInstrument instrument) {
        if (instrument.isAllowPosDetune()) return song.PosTuneOffset + instrument.getTune();
        return instrument.getTune();
    }

    public void SongEventSetPitch(final SymphonieInstrument si, final int VoiceNr, final int NoteIndex) {
        setVoiceFreq(si, VoiceNr, getPitchToFreq(VoiceNr, NoteIndex + getInstrumentAndPositionTune(si), si.getFineTune()));
    }

    public void SongEventKeyOnSamplePos(final SymphonieInstrument instrument, final int VoiceNr, final int PitchIndex, final double SamplePos) {
        int PosTuneOffset = 0;
        SongEventContinue(VoiceNr, false);
        if (instrument.isAllowPosDetune() == true) {
            PosTuneOffset = this.song.PosTuneOffset;
        }
        SetVoiceSamplePos(instrument, VoiceNr, getPitchToFreq(VoiceNr,
                PitchIndex + instrument.getTune() + PosTuneOffset, instrument.getFineTune()),
                SamplePos,
                true);
    }

    public void SongEventKeyOnSamplePos(SymphonieInstrument instrument, int VoiceNr, double SamplePos) {
        SongEventContinue(VoiceNr, false);
        SetVoiceSamplePos(instrument, VoiceNr, 0.0f,
                SamplePos,
                true);
    }

    public void SongEventPausePlaying(int VoiceNr) {
        final Voice voice = getVoiceNr(VoiceNr);
        if (voice != null) {
            voice.pausePlaying();
            voice.Smoother.activateFadeOut();
        }
    }

    public void SongEventContinue(int VoiceNr, boolean ActivateFadeIn) {
        final Voice voice = getVoiceNr(VoiceNr);
        if (voice != null) {
            if (ActivateFadeIn) voice.Smoother.activateFadeIn();
            voice.continuePlaying();
        }
    }

    public void SongEventKeyOnFreq(SymphonieInstrument si, int VoiceNr, double Freq, double volume) {
        playKeyboardInstrumentNote(si, VoiceNr, Freq, volume);
    }

    double GetVoiceVolume(int VoiceNr) {
        final Voice v = getVoiceNr(VoiceNr);
        if (v != null) {
            return (v.getVolume());
        }
        return 0.0;
    }

    void SetVoiceVolume(int VoiceNr, double Vol) {
        final Voice voice = getVoiceNr(VoiceNr);
        if (voice != null) {
            if ((Vol >= 0) && (Vol <= 100)) voice.setVolume(Vol);
        }
    }

    double getSqrFactorRelativTo44100Khz() {
        return (mixFrequency / 44100.0) * (mixFrequency / 44100.0);
    }

    double getSlideFactorRelativTo44100Khz() {
        return ((1 / getSqrFactorRelativTo44100Khz()) * getFinalBpm() / 107.46268656716418) / 18700.0;
    }

    double getSlideToFactorRelativTo44100Khz() {
        return (1 / getSqrFactorRelativTo44100Khz()) * getFinalBpm() * 0.0005;
    }

    public void SetVoicePSlide(int VoiceNr, double PitchChangeSpeed) {
        final Voice voice = getVoiceNr(VoiceNr);
        if (voice != null) voice.LFOPitch.initSlide(getSlideFactorRelativTo44100Khz() * PitchChangeSpeed);
    }

    public void SetVoicePSlideTo(SymphonieInstrument si, int VoiceNr, int Pitch, double PitchChangeSpeed) {
        final Voice v = getVoiceNr(VoiceNr);
        if (v != null) {
            double frequency = getPitchToFreq(VoiceNr, Pitch + si.getTune(), si.getFineTune());
            if (PitchChangeSpeed != 0.0) {
                PitchChangeSpeed = 0.10 / PitchChangeSpeed;
                v.LFOPitch.initSlideToValue(frequency, PitchChangeSpeed * getSlideToFactorRelativTo44100Khz());
            } else {
                v.LFOPitch.stop();
            }
        }
    }

    public void stopPitchLFO(int VoiceNr) {
        final Voice voice = getVoiceNr(VoiceNr);
        if (voice != null) {
            voice.LFOPitch.stop();
        }
    }

    public void SetVoiceVSlide(int VoiceNr, double VolChangeSpeed) {
        final Voice voice = getVoiceNr(VoiceNr);
        if (voice != null) voice.volumeLFO.initSlide(getSlideFactorRelativTo44100Khz() * VolChangeSpeed);
    }

    public void stopVolumeLFO(int VoiceNr) {
        final Voice v = getVoiceNr(VoiceNr);
        if (v != null) {
            v.volumeLFO.stop();
        }
    }

    void setVoiceFreq(SymphonieInstrument si, int VoiceNr, double freq) {
        final Voice v = getVoiceNr(VoiceNr);
        if ((v != null) && (si.checkReady() == true)) {
            v.setInstrument(si);
            v.setFreq(freq, true);
            assert (v.SourceFrequency == 0.0f);
        }
    }

    public void adjustVoiceFreq(final int VoiceNr, final double freqfactor) {
        final Voice v = getVoiceNr(VoiceNr);
        if ((v != null)) {
            v.setFreq(freqfactor * v.getFreq(), false);
        }
    }

    void SetVoiceSamplePos(SymphonieInstrument instrument, int VoiceNr, double freq, double SamplePos, boolean doPitch) {
        //logger.debug("instrument=" + instrument.getName() + "SamplePos=" + SamplePos);
        final Voice voice = getVoiceNr(VoiceNr);
        if ((voice != null) && (instrument.checkReady() == true)) {
            voice.setInstrument(instrument);
            InitVoiceEQ(voice, instrument);
            voice.LastInstrumentIDPlayed = voice.getInstrument().getID();
            if (voice.inUse == true) {
                voice.Smoother.activateSampleSmoothing();
            } else {
                voice.Smoother.activateFadeIn();
            }

            if (doPitch) {
                voice.setFreq(freq, true);
            }
            voice.SourceFrequency = voice.getInstrument().getOldSampleImporter().getSampledFrequency();
            voice.inUse = true;
            if ((SamplePos >= 0) && (SamplePos <= 100.0f)) {
                voice.SamplePtr = SamplePos * (voice.getSampleEndPtr() - 1) * 0.01;
            } else {
                logger.error("ERROR:Illegal Sample Pos " + SamplePos);
                voice.SamplePtr = 0.0f;
            }
            // Apply Sample Diff
            voice.SamplePtr += getSampleDiffOffset(VoiceNr);
            if (voice.SamplePtr > voice.getSampleEndPtr()) voice.SamplePtr = voice.getSampleEndPtr();
            assert (voice.SourceFrequency == 0.0f);
        }
    }

    // Plays into forced channel Number
    void playKeyboardInstrumentNote(SymphonieInstrument instrument, int VoiceNr, double freq, double volume) {
        final Voice voice = getVoiceNr(VoiceNr);
        if ((voice != null) && (instrument.checkReady() == true)) {
            if (voice.inUse == true) {
                voice.Smoother.activateSampleSmoothing();
            } else {
                voice.Smoother.activateFadeIn();
            }
            voice.continuePlaying();
            voice.setInstrument(instrument);
            voice.LastInstrumentIDPlayed = voice.getInstrument().getID();
            voice.setNumbOfLoopsRemaining(voice.getInstrument().getSampleChannel().getNumberOfLoops());
            voice.setVolume(volume);
            voice.setFreq(freq, true);
            voice.SourceFrequency = voice.getInstrument().getOldSampleImporter().getSampledFrequency();
            voice.SamplePtr = 0.0f;

            // Apply Sample Diff
            voice.SamplePtr += getSampleDiffOffset(VoiceNr);
            if (voice.SamplePtr > voice.getSampleEndPtr()) voice.SamplePtr = voice.getSampleEndPtr();

            voice.isPausing = false;
            voice.inUse = true;
            voice.setPanningX(instrument);
            // EQ
            InitVoiceEQ(voice, instrument);
            assert (voice.SourceFrequency == 0.0f);
        }
    }

    // Set LP Filter
//    void setFilter(int VoiceNr, int Type, double q, double freq) {
//        Voice v = getVoiceNr(VoiceNr);
//        if (v != null) {
//            v.Filter.init(Type, q, freq, mixFrequency);
//        }
//    }

    // Set LP Filter
//    void setFilterPerfectLP4(int VoiceNr, FilterType Type, double q, double freq) {
//        Voice voice = getVoiceNr(VoiceNr);
//        if (voice != null) {
//            voice.filterFX.init(Type, q, freq, mixFrequency);
//        }
//    }

    private double dspDelayFrames;

    public void activateDspCrossEcho(int VoiceNr, int Type, double b, double feedbackPercent, double delayFrames) {
        dspDelayFrames = delayFrames;
        setDspCrossEcho(true, (int) delayFrames, feedbackPercent * 0.01);
    }

    public void activateDspEcho(int VoiceNr, int Type, double b, double feedbackPercent, double delayFrames) {
        dspDelayFrames = delayFrames;
        setDspEcho(true, (int) delayFrames, feedbackPercent * 0.01);
    }

    public double getDspDelayFrames() {
        return dspDelayFrames;
    }

    private boolean dspChanged = false;

    public boolean hasDspChanged() {
        boolean changed = dspChanged;
        dspChanged = false;
        return changed;
    }

    public void setDspChanged(boolean dspChanged) {
        this.dspChanged = dspChanged;
    }


    private void stopVoice(int index) {
        if(voices[index]==null) return;
        voices[index].stop();
    }

    public void stopAllVoices() {
        for (int i = 0; i < maxNumberOfVoices; i++) {
            stopVoice(i);
        }
    }

    public void muteAllVoices(final boolean mute) {
        initAllVoices();
        for (int i = 0; i < maxNumberOfVoices; i++) {
            getVoiceNr(i).setMuted(mute);
        }
    }

    public void muteVoice(final int voiceIndex, final boolean mute) {
        final Voice voice = getVoiceNr(voiceIndex);
        if (voice != null) voice.setMuted(mute);
    }

    public void toggleMuteVoice(int voiceIndex) {
        final Voice voice = getVoiceNr(voiceIndex);
        if (voice != null) voice.setMuted(!voice.isMuted());
    }

    public Set<Integer> getMutedVoices() {
        final Set<Integer> mutedVoices = new HashSet<>();
        int voiceIndex = 0;
        for(final Voice voice : voices) {
            if(voice!=null && voice.isMuted()) mutedVoices.add(voiceIndex);
            voiceIndex++;
        }
        return mutedVoices;
    }

    public void toggleMuteAllVoice(int voiceIndex) {
        final Voice voice = getVoiceNr(voiceIndex);
        muteAllVoices(!voice.isMuted());
    }

    public boolean isVoiceMuted(int voiceIndex) {
        final Voice voice = getVoiceNr(voiceIndex);
        if (voice != null) return voice.isMuted();
        return false;
    }

    void setVoiceSmoothingLen(int len) {
        for (int i = 0; i < maxNumberOfVoices; i++) {
            this.voices[i].Smoother.setSmoothingLen(len);
        }
    }

    void endVoice(Voice v, double LastSamplePlayed) {
        v.inUse = false;
        if (LastSamplePlayed != 0.0f) {
            v.LastSample = LastSamplePlayed;
            v.Smoother.activateFadeOut();
        }
    }

    public List<Integer> getPlayingSamplePositionsOfInstrument(SymphonieInstrument instrument) {
        final List<Integer> samplePositions = new Vector<>();
        for (final Voice voice : voices) {
            if (voice != null
                    && voice.getInstrument() == instrument
                    && !instrument.isMuted()
                    && voice.isInUse()
                    && !voice.isMuted()
                    && !voice.isPausing) samplePositions.add((int) voice.getSamplePtr());
        }
        return samplePositions;
    }

    boolean isVoicePlaying(final int i) { // check if voice does play currently
        return (voices[i] != null) && (voices[i].getInstrument() != null) && ((voices[i].inUse == true) ||
                ((voices[i].Smoother.isFadeingOut() == true)));
    }

    boolean isVoicePausing(int i) { // check if voice is active but is pausing sample output
        if (isDeclickingEnabled == true) { // Declicking activated
            return (voices[i] != null) && ((voices[i].isPausing)) && (!voices[i].Smoother.isFadeingOut());
        } else { // without Declicking activated
            return (voices[i] != null) && ((voices[i].isPausing));
        }
    }

    public int getNumbOfVoicesPlaying() {
        int counter = 0;
        for (int i = 0; i < maxNumberOfVoices; i++) {
            if (isVoicePlaying(i) == true) counter++;
        }
        return (counter);
    }

    public int getHighestVoicePlaying() {
        int index = -1;
        for (int i = 0; i < maxNumberOfVoices; i++) {
            if (isVoicePlaying(i) == true) index = i;
        }
        return index;
    }

    void getNextStereoMixSample(final int stereoChannelIndex, final VisualisationVoice perVoiceVisual, final StereoSample stereoSample) { // 0 = Left, 1 = Right
        numberOfVoicesPlaying = getHighestVoicePlaying();
        for (int voiceIndex = 0; voiceIndex <= numberOfVoicesPlaying; voiceIndex += NUMBER_OF_HARDWARE_CHANNELS) {
            double sample = 0.0;
            if ((isVoicePlaying(voiceIndex + stereoChannelIndex)) && !isVoicePausing(voiceIndex + stereoChannelIndex)) {
                sample = getNextVoiceSample(voices[voiceIndex + stereoChannelIndex]);
                stereoSample.addSample(stereoChannelIndex, isVoiceWithDspEnabled(voiceIndex + stereoChannelIndex), sample);
            }
            if (calcVisualisation && perVoiceVisual != null) {
                //timer.start(TIMEID_VISUALS);
                perVoiceVisual.addSample(voiceIndex + stereoChannelIndex, (int) (32000.0f * sample));
                //timer.stop(TIMEID_VISUALS);
            }
        }
        calcAndPlayNextSongEvent(); // check if song is playing, if so do play events
    }

//    double getNextSampleWithPan(int ChannelNr, Voice voice) {
//        if (ChannelNr == 0) {
//            PannedSample = getNextVoiceSample(voice);
//            PanningR = (voice.getPanningX() + 1000) / 2000;
//            voice.SampleR = PannedSample * PanningR;
//            return (PannedSample * (1.0 - PanningR));
//        } else {
//            return (voice.SampleR);
//        }
//    }

    double getNextVoiceSample(final Voice voice) {
        double sample = 0;
        if(voice == null) {
            logger.error("Error:voice null bug");
            return 0;
        }
        voice.processAllLFOs();
        if (voice.inUse == true) {
            sample = 0.0001 * voice.getVolume() * voice.getInstrument().getVolume() * sampleInterpolator.getSample(voice.getInstrument().getSampleChannel().getSamples(), voice.SamplePtr);
            // LP Filter
            //if (voice.Filter.isActive() == true) sample = voice.Filter.getNextFilteredSample(sample);
//            if(voice.getInstrument().hasResonantFilter()) {
//                setResoFilterFromInstrument(voice, voice.getInstrument());
//                sample = voice.getFilterFX().stream(sample);
//            }

            sample = voice.stream(sample);
            if (this.isDeclickingEnabled) sample = voice.Smoother.getSmoothedSample(sample);
            //double dSample = (1.06875d + (masterTune / 50)) * voice.getFreq() / 440.0d;  // Apply Master Tune
            // (1.06875d + (masterTune / 50)) * voice.getFreq() / 440.0d
            double dSample = (1.1d + (masterTune / 50)) * voice.getFreq() / 440.0d;  // Apply Master Tune
            //todo: Be able to play a 44.1 sample at original speed at 44.1 output device
            voice.SamplePtr += dSample; // Advance Samplepointer
            if (voice.getInstrument().getSampleChannel().hasLoop() == true) ProcessLoopSystem(voice, dSample);
            if (voice.SamplePtr > voice.getSampleEndPtr() || voice.SamplePtr < 0.0f) endVoice(voice, sample);
        } else {
            if (this.isDeclickingEnabled)
                sample = voice.Smoother.getSmoothedSample(sample); // Process Fadeout if running
        }
        if (voice.isMuted() || voice.isInstrumentMuted()) return 0.0;   // muted -> dont send to dsp, and return null sample
        return sample;
    }

    void ProcessLoopSystem(Voice v, double dSample) {
        if ((v.getNumbOfLoopsRemaining() > 0) || (v.getInstrument().getSampleChannel().getEndlessLoop() == true)) {
            if (v.getInstrument().isPingpongLoop() == true) {
                if (dSample < 0.0d) {
                    if (v.SamplePtr < v.getInstrument().getSampleChannel().getLoopStart()) {
                        v.SamplePtr = v.getInstrument().getSampleChannel().getLoopStart();
                        if (v.getInstrument().getSampleChannel().getEndlessLoop() == false) v.decNumbOfLoopsRemaining();
                        v.setFreq(-v.getFreq(), false);
                    }
                } else {
                    if (v.SamplePtr > v.getInstrument().getSampleChannel().getLoopEnd()) {
                        v.SamplePtr = v.getInstrument().getSampleChannel().getLoopEnd();
                        if (v.getInstrument().getSampleChannel().getEndlessLoop() == false) v.decNumbOfLoopsRemaining();
                        v.setFreq(-v.getFreq(), false);
                    }
                }
            } else {
                if (v.SamplePtr > v.getInstrument().getSampleChannel().getLoopEnd()) {
                    v.SamplePtr = v.getInstrument().getSampleChannel().getLoopStart();
                    v.Smoother.activateSampleSmoothing();
                    if (v.getInstrument().getSampleChannel().getEndlessLoop() == false) v.decNumbOfLoopsRemaining();
                }
            }
        }
    }

    public void setMixFrequency(int frequency) {
        setMixFrequency(frequency, 1);
    }

    public void setMixFrequency(int frequency, int oversample) {
        if (frequency == mixFrequency && this.oversampling == oversample) return;
        mixFrequency = (frequency * oversample);
        this.oversampling = oversample;
        reinitMixSystem();
        EQLeft.init(false, 880, 5000, mixFrequency);
        EQRight.init(false, 880, 5000, mixFrequency);
        dsp.adjustFrequency(mixFrequency);
        printAndResetStatistics();
    }

//    public void setMixBufferLen(int bufferLenInSamples) {
//        setMixFrequency(frequency, 1);
//    }

//    public void setOversample(int oversample) {
//        if (this.oversampling != oversample) {
//            this.oversampling = oversample;
//            reinitMixSystem();
//            EQLeft.init(false, 880, 5000, mixFrequency);
//            EQRight.init(false, 880, 5000, mixFrequency);
//            dsp.adjustFrequency(mixFrequency);
//        }
//    }

    void reinitMixSystem() {
        print("reinitMixSystem " + mixFrequency);
        printAndResetStatistics();
        initMixSystem(virtual);
        Arrays.stream(voices).filter(Objects::nonNull).forEach(voice -> voice.setMixFrequency(mixFrequency));
    }

    private boolean virtual = false;

    public void initMixSystem(boolean virtual) {
        printAndResetStatistics();
        print("initMixSystem " + mixFrequency + " Bpm " + bpm);
        this.virtual = virtual;
        if (virtual) return;
    }

    boolean shutdown = false;

    public void shutdown() {
        shutdown = true;
        song = null;
    }

    private VisualisationVoice stereoMixVisual = null;
    private VisualisationVoice perVoiceVisual = null;
    private final int numbOfSavedSamplesPerFrame = 32;
    private boolean calcVisualisation = false;

    public VisualisationVoice getStereoMixVisual() {
        return stereoMixVisual;
    }

    public VisualisationVoice getRenderedBufferPerVoice() {
        return perVoiceVisual;
    }

    public void enableVisualisation(final boolean calcVisualisation) {
        this.calcVisualisation = calcVisualisation;
    }

    private void print(final String text) {
        if (task != null && task instanceof BackgroundMusicTask) ((BackgroundMusicTask) task).notifyUI(text);
    }

    public void renderAudioFrame(final double[] destinationBufferPtr, final int numberOfStereoFrames) {
        timer.start(TIMEID_TEST);
        this.bufferLenInSamples = numberOfStereoFrames * NUMBER_OF_HARDWARE_CHANNELS;
        if (!virtual && calcVisualisation) {
            if (stereoMixVisual == null) {
                stereoMixVisual = new VisualisationVoice(2, 1024);
                perVoiceVisual = new VisualisationVoice(maxNumberOfVoices, numbOfSavedSamplesPerFrame);
                perVoiceVisual.saveSampleEveryNthSamples(numbOfSavedSamplesPerFrame);
            }
            stereoMixVisual.initLinearWriteBuffer();
            perVoiceVisual.initRandomAccessWriteBuffer();
        }
        timer.start(TIMEID_VOICES);
        for (int i = 0; i < numberOfStereoFrames; i++) {
            if (shutdown) break;
            renderStereoSample(destinationBufferPtr, i);
        }
        timer.stop(TIMEID_VOICES);
        samplesRendered += numberOfStereoFrames;

        timer.start(TIMEID_VST);
        onAddStream(destinationBufferPtr, numberOfStereoFrames);
        timer.stop(TIMEID_VST);

        postprocess(destinationBufferPtr, numberOfStereoFrames);
        if (!virtual && calcVisualisation && stereoMixVisual!=null)  {
            //timer.start(TIMEID_VISUALS);
            for (int i = 0; i < stereoMixVisual.getNumberOfSamplesPerChannel(); i++) {
                int index = (i * destinationBufferPtr.length / 2) / stereoMixVisual.getNumberOfSamplesPerChannel();
                stereoMixVisual.addSample((int) destinationBufferPtr[2*i]);
                stereoMixVisual.addSample((int) destinationBufferPtr[2*i+1]);
            }
            stereoMixVisual.CopyWriteBufferToReadBuffer();
            perVoiceVisual.CopyWriteBufferToReadBuffer();
            //timer.stop(TIMEID_VISUALS);
        }
        timer.stop(TIMEID_TEST);
    }

    private void postprocess(final double[] destinationBufferPtr, final int numberOfStereoFrames) {
        if (!EQLeft.isActive()) return;
        for (int i = 0; i < destinationBufferPtr.length && !shutdown; i += 2) {
            destinationBufferPtr[i] = EQLeft.stream(destinationBufferPtr[i]);
            destinationBufferPtr[i + 1] = EQRight.stream(destinationBufferPtr[i + 1]);
        }
    }

    public void onAddStream(final double[] destinationBufferPtr, final int numberOfStereoFrames) {
    }

    //DspStereoEffect testFX = new DspStereoTestFXFilterSymphonie();
    final DspStereoEffect testFX = new  DspStereoTest4PolLPFilter();
    public DspStereoEffect getTestFX() {
        return testFX;
    }

    final StereoSample stereoSample = new StereoSample();

    private void renderStereoSample(final double[] destinationBufferPtr, int index) {
        stereoSample.clear();
        getNextStereoMixSample(0, perVoiceVisual, stereoSample);
        getNextStereoMixSample(1, perVoiceVisual, stereoSample);
        stereoSample.adjustVolume(masterVolume * masterVolumeCorrection);// Master Volume
        if (dsp.isActive()) dsp.stream(stereoSample);
        if (testFX.isActive()) testFX.stream(stereoSample);
        addStereoSampleToBuffer(stereoSample, destinationBufferPtr, index);
        if (calcVisualisation && perVoiceVisual != null) perVoiceVisual.advanceStep();
    }

    private void addStereoSampleToBuffer(StereoSample stereoSample, final double[] destinationBufferPtr, int index) {
        addSampleToBuffer(stereoSample.getLeftMix(), destinationBufferPtr, index * 2);
        addSampleToBuffer(stereoSample.getRightMix(), destinationBufferPtr, (index * 2) + 1);
    }

    double sampleMax = 0;

    private void addSampleToBuffer(double sample, final double[] destinationBufferPtr, int index) {
        sampleMax = Math.max(sampleMax, Math.abs(sample));
        if (destinationBufferPtr != null) destinationBufferPtr[index] = sample;
    }

    private void calcAndPlayNextSongEvent() {
        if (song == null) return;
        if (!song.isPlaying()) return;
        if (SamplesTillSongEvent > 0) {
            SamplesTillSongEvent--;
            if (SamplesTillSongEvent <= 0) {
                int tempSamplesTillSongEvent = song.PlaySongEvent(this);
                if (tempSamplesTillSongEvent != 0) {
                    this.freqIndependentSamplesPerEvent = tempSamplesTillSongEvent;
                }
                SamplesTillSongEvent = getNumberOfSamplesPerCycl();
                doPerCycleEvents();
            }
        } else {
            SamplesTillSongEvent = getNumberOfSamplesPerCycl();
            doPerCycleEvents();
        }
    }

    public void testResoFilter(double freq, double reso) {
        try {
            //DspStereoTestFXFilterSymphonie testFX = (DspStereoTestFXFilterSymphonie) this.testFX;
            DspStereoTest4PolLPFilter testFX = (DspStereoTest4PolLPFilter) this.testFX;
            testFX.setClipping(32000);
            testFX.init(FilterType.Lowpass, reso, freq, this.mixFrequency);
        } catch (Exception exception) {
            logger.error(exception.getStackTrace().toString());
            logger.error(exception.getMessage());
        }
    }

    void doPerCycleEvents() {
    }

    boolean hasSongFinishedPlaying = false;

    public void notifySongsHasFinishedPlaying() {
        hasSongFinishedPlaying = true;
    }

    public void setHasSongFinishedPlaying(boolean hasSongFinishedPlaying) {
        this.hasSongFinishedPlaying = hasSongFinishedPlaying;
    }


    public void initAllVoices() {
        for (int i = 0; i < maxNumberOfVoices; i++) voices[i] = new Voice(mixFrequency);
    }

    public boolean renderToRenderBuffer(final double[] destinationBufferPtr, int numberOfStereoFrames) {
        if (song == null || shutdown || hasSongFinishedPlaying) return true;
        try {
            renderAudioFrame(destinationBufferPtr, numberOfStereoFrames);
        } catch (Exception exception) {
            exception.printStackTrace();
            print(exception.getMessage());
        }
        return false;
    }

    public void cloneAudioSetup(VoiceExpander voiceExpander) {
        voiceExpander.setSampleDiff(this.sampleDiff);
        voiceExpander.setPitchDiff(this.pitchDiff);
        voiceExpander.setMasterTune(this.masterTune);
        voiceExpander.setMasterBpmTune(masterBpmTune);
        voiceExpander.setEQLowIntensity(EQLeft.getLowGain());
        voiceExpander.setEQMidIntensity(EQLeft.getMiddleGain());
        voiceExpander.setEQHighIntensity(EQLeft.getHighGain());
        voiceExpander.setEQLPFrequency(EQLeft.getLPFrequency());
        voiceExpander.setEQHPFrequency(EQLeft.getHPFrequency());
        voiceExpander.enableEQ(EQLeft.isActive());
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("Virtual:").append(virtual)
                .append(" Playing Voices:").append(numberOfVoicesPlaying)
                .append(" hasSongFinishedPlaying:").append(hasSongFinishedPlaying);
        if (song != null) {
            text.append(" isPlaying:").append(song.isPlaying())
                    .append(" Song Name:").append(song.getName());
        }
        return text.toString();
    }
}




