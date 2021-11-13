/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.meng.symphoniefx.song;
/*
 *
 * @author (c) Patrick Meng 2008
 */


import ch.meng.symphoniefx.InstrumentSource;
import ch.meng.symphoniefx.VstManager;
import ch.meng.symphoniefx.mixer.SampleInterpolator;
import symreader.*;

import java.io.File;
import java.util.*;

import static symreader.PitchToFrequencyProvider.FREQUENCY_ILLEGAL;

//class InstrMetaInfo {
//    int R = 255, G = 255, B = 255;
//    float x, y, z;
//    boolean MainLead = false;
//    boolean Lead2 = false;
//    boolean Background = false;
//    boolean Percussion = false;
//    boolean IsAFx = false;
//    boolean Beat = false;
//    boolean SubBeat = false;
//    int Speed = 0; // 0 = slow, 10 = fast
//    int Warmth = 0; // 10 = hot, -10 cold
//    int Size = 0;       // 0 = tiny, 10 = huge
//    int Synthethic = 0; // 0 = natural, 10 = very synthetic
//    int purness = 0;
//}



public class SymphonieInstrument {

    private double version = 1.0;
    private InstrumentSource instrumentSource = InstrumentSource.Sample;
    private double bpm = 0;
    private symreader.OldSampleImporter OldSampleImporter = new OldSampleImporter();
    int ID; // Instr-Number in Song Events im Original
    int index;
    private String name = "unnamed";
    private String group = "";
    private String filename = "";
    boolean hasContent = false;

    private boolean loopEnabled = false;                // True -> has a looping set
    private int loopType;
    private int loopStart = 0;
    private int loopLength = 0;
    private int numberOfLoops = 0; // 0 = Endless Loop
    boolean pingpongLoop = false;
    boolean NewLoopSystem = false;       // BIT 4: New LoopSystem

    boolean AutoMaximize;
    int Volume = 100;
    float compressorLevel = 0; //0.0 - 0.99
    float fadeFromVolume = 1;
    float fadeToVolume = 1;


    int Relation = 0;
    int NumbOfChildren = 0;
    int FineTune = 0;
    int Tune = 0;

    boolean PanningActive = false;
    float PanningX = 0;  // -1000 = Left, 1000 = Right
    float PanningY = 0;
    float PanningZ = 0;

    private int PlayFlag; // temp var for import
    private boolean AllowPosDetune = true;   //SPLAYFLAG_DODETUNE
    private boolean isStereoSyncPlayed = false;     //SPLAYFLAG_SUPERFAST
    private boolean dspEnabled = false;           //SPLAYFLAG_NODSP

    // LS FLAGS
    private int LineSampleFlags = 0; // temp var for import
    private boolean isReversed = false;         // BIT 0: reverse sample data
    private boolean isVirtualQueueMix = false;             // BIT 1: interprete as list of samples (no mix)
    private boolean isPhaseMirrored;             // BIT 2;B: mirror x axis sample
    //boolean Resolution16Bit = false;     // BIT 3: for deltapack16 obsolete
    private int SampleResolution = 8;

    //boolean NoDsp2 = true;                // BIT 5: None Dsp    

    int SimpleLPFilter = 0;
    int downsampleSteps = 0;
    // Sampledata
    boolean virtualSample = false;
    private boolean isMuted = false;
    boolean oldMuteStatus = false;
    private SampleChannel sampleChannel; //
    int deltapackType = 0; //0 = Raw, 1= Deltapacked, 2 = Deltapacked16Bit

    Boolean EQActive = false;
    int EQLowGain = 0;
    int EQMidGain = 0;
    int EQHighGain = 0;
    int EQLPFrequency = 880;
    int EQHPFrequency = 5000;

    //SAMPLENAME_RESOFILTERFLAGS EQU	160	;4x LP od HP
    //SAMPLENAME_RESOFILTERNUMB EQU	161	;Anzahl Punkte
    //SAMPLENAME_RESOFILTER	EQU	162	;bis 170
    int resoFilterSweepType = 0;
    int resoFilterSteps = 0;

    // 0-255
    int resoFilterSweepStartFrequency = 0;
    int resoFilterSweepEndFrequency = 0;
    int resoFilterSweepStartResonance = 0;
    int resoFilterSweepEndResonance = 0;

    private List<VirtualMixStep> virtualMixSteps = new ArrayList<>();
    private List<Integer> nullstellen = new Vector<>();
    private LoopTypeEnum loopTypeEnum = LoopTypeEnum.Off;

    private VstManager vstManager;
    private VstSetup vstSetup;

    public VstSetup getVstSetup() {
        if(vstSetup == null) vstSetup = new VstSetup();
        return vstSetup;
    }
    public void setVstSetup(VstSetup vstSetup) {
        this.vstSetup = vstSetup;
    }

    public void loadVstInstrument(final File file, final int bufferLenInSamples, final int sampleRate) {
        if (vstManager == null) vstManager = new VstManager();
        vstManager.loadVstInstrument(file, this, bufferLenInSamples, sampleRate);
    }

    public VstManager getVstManager() {
        return vstManager;
    }

    public InstrumentSource getInstrumentSource() {
        return instrumentSource;
    }
    public void setInstrumentSource(InstrumentSource instrumentSource) {
        this.instrumentSource = instrumentSource;
    }

    public void setVirtualQueueMix(boolean virtualQueueMix) {
        isVirtualQueueMix = virtualQueueMix;
    }

    public symreader.OldSampleImporter getOldSampleImporter() {
        return OldSampleImporter;
    }

    public void setOldSampleImporter(symreader.OldSampleImporter oldSampleImporter) {
        OldSampleImporter = oldSampleImporter;
    }

    public SampleChannel getSampleChannel() {
        return sampleChannel;
    }

    public void setSampleChannel(SampleChannel sampleChannel) {
        this.sampleChannel = sampleChannel;
    }

    MultichannelEnum multiChannel = MultichannelEnum.Mono; // Mono, Stereo L, Stero R, VirtualMix

    public MultichannelEnum getMultiChannel() {
        return multiChannel;
    }

    public double getBpm() {
        return bpm;
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
    }

    public double calcSampleBpm(double frequency, int length) {
        if(frequency <= 1 ) frequency = 44100;
        double seconds = length / frequency;
        double bpm = 60 / seconds;
        while(bpm < 70 || bpm > 140) {
            if(bpm < 70) bpm *= 2;
            if(bpm > 140) bpm /= 2;
        }
        return bpm;
    }

    public boolean isDspEnabled() {
        return this.dspEnabled;
    }

    public void setDspEnabled(boolean dspEnabled) {
        this.dspEnabled = dspEnabled;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public int getResoFilterSweepType() {
        return resoFilterSweepType;
    }

    public void setResoFilterSweepType(int resoFilterSweepType) {
        this.resoFilterSweepType = resoFilterSweepType;
    }

    public int getResoFilterSteps() {
        return resoFilterSteps;
    }

    public boolean hasResonantFilter() {
        return (resoFilterSweepType!=0 && resoFilterSteps>0);
    }

    public void setResoFilterSteps(int resoFilterSteps) {
        this.resoFilterSteps = resoFilterSteps;
    }

    public int getResoFilterSweepStartFrequency() {
        return resoFilterSweepStartFrequency;
    }

    public void setResoFilterSweepStartFrequency(int resoFilterSweepStartFrequency) {
        this.resoFilterSweepStartFrequency = resoFilterSweepStartFrequency;
    }

    public int getResoFilterSweepEndFrequency() {
        return resoFilterSweepEndFrequency;
    }

    public void setResoFilterSweepEndFrequency(int resoFilterSweepEndFrequency) {
        this.resoFilterSweepEndFrequency = resoFilterSweepEndFrequency;
    }

    public int getResoFilterSweepStartResonance() {
        return resoFilterSweepStartResonance;
    }

    public void setResoFilterSweepStartResonance(int resoFilterSweepStartResonance) {
        this.resoFilterSweepStartResonance = resoFilterSweepStartResonance;
    }

    public int getResoFilterSweepEndResonance() {
        return resoFilterSweepEndResonance;
    }

    public void setResoFilterSweepEndResonance(int resoFilterSweepEndResonance) {
        this.resoFilterSweepEndResonance = resoFilterSweepEndResonance;
    }

    public List<Integer> getNullstellen() {
        return nullstellen;
    }

    public void setNullstellen(List<Integer> nullstellen) {
        this.nullstellen = nullstellen;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public float getCompressorLevel() {
        return compressorLevel;
    }

    public void setCompressorLevel(float compressorLevel) {
        this.compressorLevel = compressorLevel;
    }

    public float getFadeFromVolume() {
        return fadeFromVolume;
    }

    public void setFadeFromVolume(float fadeFromVolume) {
        this.fadeFromVolume = fadeFromVolume;
    }

    public float getFadeToVolume() {
        return fadeToVolume;
    }

    public void setFadeToVolume(float fadeToVolume) {
        this.fadeToVolume = fadeToVolume;
    }

    public boolean isPanningActive() {
        return PanningActive;
    }

    public void setPanningActive(boolean panningActive) {
        PanningActive = panningActive;
    }

    public float getPanningX() {
        return PanningX;
    }

    public void setPanningX(float panningX) {
        PanningX = panningX;
    }

    public float getPanningY() {
        return PanningY;
    }

    public void setPanningY(float panningY) {
        PanningY = panningY;
    }

    public float getPanningZ() {
        return PanningZ;
    }

    public void setPanningZ(float panningZ) {
        PanningZ = panningZ;
    }

    public void setAllowPosDetune(boolean allowPosDetune) {
        AllowPosDetune = allowPosDetune;
    }

    public void setReversed(boolean reversed) {
        isReversed = reversed;
    }

    public void setPhaseMirrored(boolean phaseMirrored) {
        isPhaseMirrored = phaseMirrored;
    }

    public void setSimpleLPFilter(int simpleLPFilter) {
        SimpleLPFilter = simpleLPFilter;
    }

    public void setVirtualSample(boolean virtualSample) {
        this.virtualSample = virtualSample;
    }

    public Boolean getEQActive() {
        return EQActive;
    }

    public void setEQActive(Boolean EQActive) {
        this.EQActive = EQActive;
    }

    public int getEQLowGain() {
        return EQLowGain;
    }

    public void setEQLowGain(int EQLowGain) {
        this.EQLowGain = EQLowGain;
    }

    public int getEQMidGain() {
        return EQMidGain;
    }

    public void setEQMidGain(int EQMidGain) {
        this.EQMidGain = EQMidGain;
    }

    public int getEQHighGain() {
        return EQHighGain;
    }

    public void setEQHighGain(int EQHighGain) {
        this.EQHighGain = EQHighGain;
    }

    public int getEQLPFrequency() {
        return EQLPFrequency;
    }

    public void setEQLPFrequency(int EQLPFrequency) {
        this.EQLPFrequency = EQLPFrequency;
    }

    public int getEQHPFrequency() {
        return EQHPFrequency;
    }

    public void setEQHPFrequency(int EQHPFrequency) {
        this.EQHPFrequency = EQHPFrequency;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<VirtualMixStep> getVirtualMixSteps() {
        return virtualMixSteps;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setVirtualMixSteps(List<VirtualMixStep> virtualMixSteps) {
        this.virtualMixSteps = virtualMixSteps;
    }


    public OldSampleImporter getSampleImporter() {
        return OldSampleImporter;
    }

    public void setSampleImporter(OldSampleImporter oldSampleImporter) {
        OldSampleImporter = oldSampleImporter;
    }

    public SampleChannel getSamplePool() {
        return sampleChannel;
    }

    public void setSamplePool(SampleChannel sampleChannel) {
        this.sampleChannel = sampleChannel;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public boolean hasContent() {
        return hasContent;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public int getVolume() {
        return Volume;
    }

    public int getRelation() {
        return Relation;
    }

    public int getNumbOfChildren() {
        return NumbOfChildren;
    }

    public int getFineTune() {
        return FineTune;
    }

    public int getTune() {
        return Tune;
    }

    public int getPlayFlag() {
        return PlayFlag;
    }

    public void setPlayFlag(int playFlag) {
        PlayFlag = playFlag;
    }

    public boolean isAllowPosDetune() {
        return AllowPosDetune;
    }

    public boolean isStereoSyncPlayed() {
        return isStereoSyncPlayed;
    }

    public int getLineSampleFlags() {
        return LineSampleFlags;
    }

    public void setLineSampleFlags(int lineSampleFlags) {
        LineSampleFlags = lineSampleFlags;
    }

    public boolean isReversed() {
        return isReversed;
    }

    public boolean isPhaseMirrored() {
        return isPhaseMirrored;
    }

    public int getSampleResolution() {
        return SampleResolution;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMultiChannel(MultichannelEnum multiChannel) {
        this.multiChannel = multiChannel;
    }

    public void setVolume(int volume) {
        Volume = volume;
    }

    public void setFineTune(int fineTune) {
        FineTune = fineTune;
    }

    public void setTune(int tune) {
        Tune = tune;
    }

    public void setStereoSyncPlayed(boolean stereoSyncPlayed) {
        isStereoSyncPlayed = stereoSyncPlayed;
    }

    public void setSampleResolution(int sampleResolution) {
        SampleResolution = sampleResolution;
    }

    public void setDownsampleSteps(int downsampleSteps) {
        this.downsampleSteps = downsampleSteps;
    }

    public int getSimpleLPFilter() {
        return SimpleLPFilter;
    }

    public int getDownsampleSteps() {
        return downsampleSteps;
    }


    public boolean isVirtualSample() {
        return virtualSample;
    }

    public boolean isVirtualQueue() { // mix one after another insted of layered
        return virtualSample && isVirtualQueueMix;
    }

    public boolean isVirtualMix() { // mix one after another insted of layered
        return virtualSample && !isVirtualQueueMix;
    }

    public boolean isVirtualTranswave() { // Fademix of two loops
        return false;
    }

    public int getDeltapackType() {
        return deltapackType;
    }

    public boolean isLoopEnabled() {
        return loopEnabled;
    }

    public void setLoopEnabled(boolean loopEnabled) {
        this.loopEnabled = loopEnabled;
    }

    public void intiLoopOfSample() {
        getSamplePool().initLoopData(isLoopEnabled(), getLoopStart(), getLoopLength(), getNumberOfLoops());
    }

    public int getLoopType() {
        return loopType;
    }

    @Deprecated
    public void setLoopType(int loopType) {
        this.loopType = loopType;
        if(4==loopType) loopTypeEnum = LoopTypeEnum.Loop;
        if(8==loopType) loopTypeEnum = LoopTypeEnum.Sustained;
    }

    public LoopTypeEnum getLoopTypeEnum() {
        return loopTypeEnum;
    }

    public void setLoopTypeEnum(LoopTypeEnum loopTypeEnum) {
        this.loopTypeEnum = loopTypeEnum;
        if(loopTypeEnum.equals(LoopTypeEnum.Off)) setLoopEnabled(false);
        else setLoopEnabled(true);
    }

    public int getLoopStart() {
        return loopStart;
    }

    public void setLoopStart(int loopStart) {
        this.loopStart = loopStart;
    }

    public void adjustLoopStart(final int newloopstart) {
        if(!hasContent) return;
        int loopEnd = loopStart + loopLength;
        if(newloopstart >= loopEnd) return;
        loopStart = newloopstart;
        loopLength = loopEnd-loopStart;
        getSampleChannel().initLoopData(true, loopStart, loopLength, 0);
    }

    public void loopStartMoveToNextSnap(int direction) {
        if(nullstellen.size() < 2) return;
        if(direction>0) {
            for (int nullstelle : nullstellen) {
                if(nullstelle>loopStart) {
                    adjustLoopStart(nullstelle);
                    return;
                }
            }
            adjustLoopStart(nullstellen.get(nullstellen.size()-1));
            return;
        }
        for(int i = nullstellen.size()-1 ; i > 0; i--) {
            if(nullstellen.get(i)<loopStart) {
                adjustLoopStart(nullstellen.get(i));
                return;
            }
        }
        adjustLoopStart(nullstellen.get(0));
    }

    public void loopLengthMoveToNextSnap(int direction) {
        int actualPosition = loopStart + loopLength;
        actualPosition = movePositionToNextSnap(actualPosition, direction);
        adjustLoopLength(actualPosition-loopStart);
    }

    public void adjustLoopLength(final int newLength) {
        if(!hasContent) return;
        if((loopStart + newLength) >= getSampleChannel().getSamples().length)
            return;
        loopLength = newLength;
        getSampleChannel().initLoopData(true, loopStart, loopLength, 0);
    }

    public int movePositionToNextSnap(int actualPosition, int direction) {
        if(nullstellen.size() < 2) return -1;
        if(direction>0) {
            for (int nullstelle : nullstellen) {
                if(nullstelle>actualPosition) return nullstelle;
            }
            return nullstellen.get(nullstellen.size()-1);
        }
        for(int i = nullstellen.size()-1 ; i > 0; i--) {
            if(nullstellen.get(i)<actualPosition) {
                return nullstellen.get(i);
            }
        }
        return nullstellen.get(0);
    }

    public int getLoopLength() {
        return loopLength;
    }

    public void setLoopLength(int loopLength) {
        this.loopLength = loopLength;
    }

    public int getNumberOfLoops() {
        return numberOfLoops;
    }

    public void setNumberOfLoops(int numberOfLoops) {
        this.numberOfLoops = numberOfLoops;
    }

    public boolean isPingpongLoop() {
        return pingpongLoop;
    }

    public void setPingpongLoop(boolean pingpongLoop) {
        this.pingpongLoop = pingpongLoop;
    }

    public boolean isNewLoopSystem() {
        return NewLoopSystem;
    }

    public void setNewLoopSystem(boolean newLoopSystem) {
        NewLoopSystem = newLoopSystem;
    }

    public void syncLoopTo(SymphonieInstrument otherInstrument) {
        if(otherInstrument == this) return;
        otherInstrument.setLoopTypeEnum(getLoopTypeEnum());
        otherInstrument.setLoopStart(getLoopStart());
        otherInstrument.setLoopLength(getLoopLength());
        otherInstrument.setLoopEnabled(isLoopEnabled());
        otherInstrument.intiLoopOfSample();
    }

    public void optimizeLoop() {
        sampleChannel.snapLoopToNullstellen(loopStart, loopLength, nullstellen);
        setLoopStart(sampleChannel.getLoopStart());
        setLoopLength(sampleChannel.getLoopLen());
    }

    public int getFittingNullstelle(int samplePosition) {
        return sampleChannel.getFittingNullstelle(samplePosition, nullstellen);
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }

    public boolean checkReady() {
        boolean ready = hasContent != false;
        if (sampleChannel == null) {
            ready = false;
        } else {
            if (sampleChannel.getNumbOfSamples() == 0) ready = false;
        }
        return (ready);
    }

    public void deactivateLoop() {
        numberOfLoops = 0; // 0 = Endless loop
        loopEnabled = false;
    }

    public int getDownsamplingTuneCorrection() {
        int tuneCorrection = -(downsampleSteps * 12);
        return tuneCorrection;
    }

    boolean anonymizeSampleNames = false;

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        if(!anonymizeSampleNames) text.append(getName());
        if(instrumentSource.equals(InstrumentSource.Vst)) {
            text.append(" VST Instrument");
            return text.toString();
        }
        if (!isVirtualSample()) {
            text.append(" ")
                    .append(getSampleResolution()).append(" Bit ")
                    .append(getSampleImporter().getFormatShort());
        }
        if (isVirtualSample()) {
            if(1!=virtualMixSteps.size()) {
                if (isVirtualQueue()) text.append(" Queue");
                if (isVirtualMix()) text.append(" Mix");
            } else {
                text.append(" Clone");
            }
            text.append(getVirtualSequenceText());
        }
        if (isReversed()) {
            text.append(" Reversed");
        }
        if(!multiChannel.equals(MultichannelEnum.Virtual)) text.append(" ").append(multiChannel);
        if (isDspEnabled()) text.append(" Dsp");
        if (isMuted) text.append(" Muted");
        return text.toString();
    }

    public String getShortDescription() {
        StringBuilder text = new StringBuilder();
        int clip = getName().lastIndexOf("/");
        if (clip < 0) clip = getName().lastIndexOf("\\");
        if (clip > 0) text.append(getName().substring(clip + 1));
        else text.append(getName());
        if(instrumentSource.equals(InstrumentSource.Vst)) {
            text.append(" VST Instrument");
            return text.toString();
        }
        if (isVirtualSample()) {
            if(1!=virtualMixSteps.size()) {
                if (isVirtualQueue()) text.append(" Queue");
                if (isVirtualMix()) text.append(" Mix");
            } else {
                text.append(" Clone");
            }
            text.append(getVirtualSequenceText());
        }
        if (isDspEnabled()) text.append(" Dsp");
        if (multiChannel.equals(MultichannelEnum.StereoL)) text.append(" L");
        if (multiChannel.equals(MultichannelEnum.StereoR)) text.append(" R");
        if (isReversed) text.append(" RVS");
        return text.toString();
    }

    public String getSongSaveSampleName() {
        StringBuilder text = new StringBuilder();
        int clip = getName().lastIndexOf("/");
        if (clip < 0) clip = getName().lastIndexOf("\\");
        if (clip > 0) text.append(getName().substring(clip + 1));
        else text.append(getName());
        if(instrumentSource.equals(InstrumentSource.Vst)) {
            text.append(" VST Instrument");
            return text.toString();
        }
        if (isVirtualSample()) {
            if(1!=virtualMixSteps.size()) {
                if (isVirtualQueue()) text.append("Virtual Queue");
                if (isVirtualMix()) text.append("Virtual Mix");
            } else {
                text.append("Virtual Clone");
            }
            text.append(getVirtualSequenceText());
        }
        return text.toString();
    }

    public String getRenderDescription() {
        StringBuilder text = new StringBuilder();
        if (isVirtualSample()) {
            if (isVirtualQueue()) {
            }
            if (isVirtualMix()) {
            }
            text.append(getRenderVirtualSequenceText());
        } else {
            int clip = getName().lastIndexOf("/");
            if (clip < 0) clip = getName().lastIndexOf("\\");
            if (clip > 0) text.append(getName().substring(clip + 1));
            else text.append(getName());
            if(instrumentSource.equals(InstrumentSource.Vst)) {
                text.append(" VST Instrument");
                return text.toString();
            }
        }
        //if (isDspEnabled()) text.append(" [DSP] ");
        return text.toString();
    }

    private String getRenderVirtualSequenceText() {
        final StringBuilder text = new StringBuilder();
        for (VirtualMixStep virtualMixStep : virtualMixSteps) {
            text.append(" ").append(virtualMixStep.getRenderDescription());
        }
        if(1==virtualMixSteps.size()) {
            text.append("Clone ");
        }
        return text.toString();
    }

    private String getVirtualSequenceText() {
        final StringBuilder text = new StringBuilder();
        if(1!=virtualMixSteps.size()) {
            text.append("[").append(virtualMixSteps.size()).append("]");
        }
        for (VirtualMixStep virtualMixStep : virtualMixSteps) {
            text.append(" ").append(virtualMixStep.toString());
        }
        return text.toString();
    }

    public void cloneSample(SymphonieInstrument sourceInstrument) {
        if (sourceInstrument == null || sourceInstrument.getSamplePool() == null || sourceInstrument.getSamplePool().getSamples().length < 1)
            return;
        float[] sourceSamples = sourceInstrument.getSamplePool().getSamples();
        SampleChannel sampleChannel = new SampleChannel(sourceSamples.length);
        if (isReversed) {
            for (int i = 0; i < sourceSamples.length; i++) {
                sampleChannel.getSamples()[sourceSamples.length - i - 1] = sourceSamples[i];
            }
        } else {
            sampleChannel.setSamples(sourceSamples.clone());
        }
        setSamplePool(sampleChannel);
    }

    public void buildVirtualSampleMix(Song song) {
        SymphonieInstrument sourceInstrument = song.getInstrument(virtualMixSteps.get(0).getMixInstrumentIndex());
        if (sourceInstrument == null || sourceInstrument.getSamplePool() == null || sourceInstrument.getSamplePool().getSamples().length < 1)
            return;
        SampleChannel sampleChannel = new SampleChannel(sourceInstrument.getSamplePool().getNumbOfSamples());
        int basePitch = virtualMixSteps.get(0).getMixPitch();
        for (VirtualMixStep virtualMixStep : virtualMixSteps) {
            sourceInstrument = song.getInstrument(virtualMixStep.getMixInstrumentIndex());
            addSampleMix(sampleChannel.getSamples(),
                    sourceInstrument.getSamplePool().getSamples(),
                    basePitch - virtualMixStep.getMixPitch(),
                    virtualMixStep.getMixVolume()
            );
        }
        setSamplePool(sampleChannel);
    }

    private void addSampleMix(float[] destinationSamples, float[] sourceSamples, int relativePitch, int mixVolume) {
        SampleInterpolator sampleInterpolator = new SampleInterpolator();
        if (destinationSamples.length <= 0 || sourceSamples.length <= 0) return;
        double SamplePtr = sourceSamples[0];
        double dSample = PitchToFrequencyProvider.getFrequency(relativePitch);
        if(FREQUENCY_ILLEGAL == dSample) return;
        for (int i = 0; i < destinationSamples.length; i++) {
            destinationSamples[i] += sampleInterpolator.getSample(sourceSamples, SamplePtr) * 100 / mixVolume;
            SamplePtr += dSample;
            if (SamplePtr > sourceSamples.length + 1) break;
        }
    }

    public String getLoopLengthInfo() {
        StringBuilder text = new StringBuilder();
        text.append((int) (loopLength / 44.1)).append("ms");
        return text.toString();
    }

    public String getLoopStartInfo() {
        StringBuilder text = new StringBuilder();
        text.append((int) (loopStart / 44.1)).append("ms");
        return text.toString();
    }

    private int getInstrumentAndPositionTune(final SymphonieInstrument instrument, final Song song) {
        if (instrument.isAllowPosDetune()) return song.PosTuneOffset + instrument.getTune();
        return instrument.getTune();
    }

    void playEventVst(final SongEvent songEvent, final Song song) {
        switch (songEvent.songFXType) {
            case SongEventType.FX_KEYON:
                getVstManager().playKeyOn(songEvent.getPitch()+getInstrumentAndPositionTune(this, song), songEvent.getLengthTicks());
                break;
            case SongEventType.FX_KEYOFF:
                getVstManager().allKeysOff();
                break;
        }
    }


}

//LSFLAGS_RVS		EQU	0	;BIT 0: reverse sample
//LSFLAGS_ASQUEUE		EQU	1	;BIT 1: interprete as list of samples (no mix)
//LSFLAGS_MIRROR		EQU	2	;BIT 2;B: mirror x axis sample
//LSFLAGS_16BIT		EQU	3	;BIT 3: for deltapack16
//LSFLAGS_NEWLOOP		EQU	4	;New LoopSystem
//LSFLAGS_NODSP		EQU	5	;None Dsp

    
    
    
    /*

        


INSTRTYPE_SILENT	EQU	-8
INSTRTYPE_KILL	EQU	-4
INSTRTYPE_NONE	EQU	0
INSTRTYPE_LOOP	EQU	4
INSTRTYPE_SUST	EQU	8        

    
SAMPLENAME_INSTRTYPE	EQU	128	;0=No Instr
SAMPLENAME_LOOPSTART	EQU	129	;%
SAMPLENAME_LOOPLEN	EQU	130	;%
SAMPLENAME_LOOPNUMB	EQU	131	;0=ENDLESS, 1-255
SAMPLENAME_MULTI	EQU	132	;0=MONO
SAMPLENAME_AUTOMAXIMIZE	EQU	133	;0=NO, 1=YES
SAMPLENAME_VOLUME	EQU	134	;0=NO, VOLUME in %1...200%
SAMPLENAME_RELATION	EQU	135	;0=INDEPENDET, 1=PARENT 2=CHILD UNUSED
SAMPLENAME_CHILDNUMB	EQU	136	;0=INDEPENDET UNUSED
;SAMPLENAME_SAMPLETYPE	EQU	137	;0=RAW, 1=IFF
SAMPLENAME_FINETUNE	EQU	138	;SIGNED 0=None [-127...+127]
SAMPLENAME_TUNE		EQU	139	;SIGNED 0=None [-24...+24
SAMPLENAME_LSFLAGS	EQU	140	;LINESAMPLE FLAGS
SAMPLENAME_FILTER	EQU	141	;0=NONE
SAMPLENAME_PLAYFLAG	EQU	142	;0=NRM
SAMPLENAME_DOWNSAMPLE	EQU	143	;0=NONE
SAMPLENAME_RESO		EQU	144	;0=NONE
SAMPLENAME_LOADFLAGS	EQU	145	;0=NORMAL
SAMPLENAME_INFO		EQU	146	;BIT0 
SAMPLENAME_RANGEBGN	EQU	147	;%
SAMPLENAME_RANGELEN	EQU	148	;%
SAMPLENAME_LOOPSTARTLO	EQU	150	; lower bits of loop
SAMPLENAME_LOOPLENLO	EQU	152	; lower bits of loop

SAMPLENAME_RESOFILTERFLAGS EQU	160	;4x LP od HP
SAMPLENAME_RESOFILTERNUMB EQU	161	;Anzahl Punkte
SAMPLENAME_RESOFILTER	EQU	162	;bis 170

SAMPLENAME_VFADESTATUS	EQU	170
SAMPLENAME_VFADEBGN	EQU	SAMPLENAME_VFADESTATUS+1
SAMPLENAME_VFADEEND	EQU	SAMPLENAME_VFADEBGN+1    
*/  

/*
 * SAMPLENAME_INSTRTYPE	EQU	128	;0=No Instr
SAMPLENAME_LOOPSTART	EQU	129	;%
SAMPLENAME_LOOPLEN	EQU	130	;%
SAMPLENAME_LOOPNUMB	EQU	131	;0=ENDLESS, 1-255
SAMPLENAME_MULTI	EQU	132	;0=MONO
SAMPLENAME_AUTOMAXIMIZE	EQU	133	;0=NO, 1=YES
SAMPLENAME_VOLUME	EQU	134	;0=NO, VOLUME in %1...200%
SAMPLENAME_RELATION	EQU	135	;0=INDEPENDET, 1=PARENT 2=CHILD UNUSED
SAMPLENAME_CHILDNUMB	EQU	136	;0=INDEPENDET UNUSED
;SAMPLENAME_SAMPLETYPE	EQU	137	;0=RAW, 1=IFF
SAMPLENAME_FINETUNE	EQU	138	;SIGNED 0=None [-127...+127]
SAMPLENAME_TUNE		EQU	139	;SIGNED 0=None [-24...+24
SAMPLENAME_LSFLAGS	EQU	140	;LINESAMPLE FLAGS
SAMPLENAME_FILTER	EQU	141	;0=NONE
SAMPLENAME_PLAYFLAG	EQU	142	;0=NRM
SAMPLENAME_DOWNSAMPLE	EQU	143	;0=NONE
SAMPLENAME_RESO		EQU	144	;0=NONE
SAMPLENAME_LOADFLAGS	EQU	145	;0=NORMAL
SAMPLENAME_INFO		EQU	146	;BIT0 
SAMPLENAME_RANGEBGN	EQU	147	;%
SAMPLENAME_RANGELEN	EQU	148	;%
SAMPLENAME_LOOPSTARTLO	EQU	150	; lower bits of loop
SAMPLENAME_LOOPLENLO	EQU	152	; lower bits of loop

SAMPLENAME_RESOFILTERFLAGS EQU	160	;4x LP od HP
SAMPLENAME_RESOFILTERNUMB EQU	161	;Anzahl Punkte
SAMPLENAME_RESOFILTER	EQU	162	;bis 170

SAMPLENAME_VFADESTATUS	EQU	170
SAMPLENAME_VFADEBGN	EQU	SAMPLENAME_VFADESTATUS+1
SAMPLENAME_VFADEEND	EQU	SAMPLENAME_VFADEBGN+1 
 */
