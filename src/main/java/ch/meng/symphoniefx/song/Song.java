/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.meng.symphoniefx.song;

import ch.meng.symphoniefx.InstrumentSource;
import ch.meng.symphoniefx.PlaySongMode;
import ch.meng.symphoniefx.mixer.VoiceExpander;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class Song {
    public static final int DEFAULT_NUMB_POSITIONS = 200;
    public static final int DEFAULT_NUMB_PATTERNS = 100;
    public static final int DEFAULT_NUMB_TRACKS = 24;

    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    private double version = 1.0;
    private PlaySongMode playSongMode = PlaySongMode.Song;
    String Name = "Unnamed";
    float Volume = 100;
    private double bpm = 1.0f;
    float MixFrequency = 44100.0f;

    private int NumbOfSequences = 64;
    private int NumbOfPositions = 200;
    private int NumbOfInstruments = 256;
    private int numbOfVoices = 24;
    private int numbOfRows = 64;
    private int numberOfPatterns = 100;
    private int PatternSize;
    private boolean InstrumentsAllocated = false;

    private Sequence[] sequences;
    private Position[] positions;
    private List<SongPattern> patterns = new Vector<>();
    private int[] IDs;  // original Indexes
    private SymphonieInstrument[] instruments;
    private List<SymphonieInstrument> vstInstruments = new Vector<>();

    // Play Management
    private VoiceExpander LinkedVoiceExpander;
    private boolean isPlaying = false;
    int PlayingPatternNr = 0;
    boolean isPositionPlaying = false;
    private boolean isContentLoaded = false;

    boolean ForceJumpToPos = false;
    boolean ForceSyncedBreak = false;
    int ForcedPosNr = 0;

    int actualPlayingPositionIndex = 0;
    int PosNumbOfLoops = 0;
    int PosStart = 0;
    int PosLen = 0;
    double actualPositionSpeed = 0;
    public int PosTuneOffset = 0;
    private float PlayLinePos = 0;
    private boolean UpdatePlayingPos = false;

    private final boolean RealtimeModifyActivated = false;
    private final int ModifyVoiceNr = 0;
    private final int ModifyParameter = 0;
    private final float ModifyValueTo = 0.0f;
    private boolean replayOnEndOfSong = true;

    public List<SymphonieInstrument> getVstInstruments() {
        return vstInstruments;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public PlaySongMode getPlaySongMode() {
        return playSongMode;
    }

    public SymphonieInstrument getOtherStereoInstrument(SymphonieInstrument instrument) {
        if (instrument.getMultiChannel().equals(MultichannelEnum.StereoL)) {
            return getInstrument(instrument.getIndex()+1);
        }
        if (instrument.getIndex() > 0 && instrument.getMultiChannel().equals(MultichannelEnum.StereoR)) {
            return getInstrument(instrument.getIndex()-1);
        }
        return instrument;
    }

    public void initInstrumentsForNewSong() {
        IDs = new int[256];
        for(int i= 0; i < 256;i++) {
            SymphonieInstrument instrument =  new SymphonieInstrument();
            instrument.setID(i);
            instrument.setIndex(i);
            IDs[i] = i;
            instruments[i] = instrument;
        }
    }

    public void initPositionsForNewSong() {
        int patternNr = 0;
        for(Position position : positions) {
            position.setSinglePatternNumber(patternNr++);
        }
    }


    public List<SongPattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<SongPattern> patterns) {
        this.patterns = patterns;
    }

    public void setInstruments(SymphonieInstrument[] instruments) {
        this.instruments = instruments;
    }

    public void setVolume(float volume) {
        Volume = volume;
    }

    public void setMixFrequency(float mixFrequency) {
        MixFrequency = mixFrequency;
    }

    public void setNumberOfPatterns(int numberOfPatterns) {
        this.numberOfPatterns = numberOfPatterns;
    }

    public void setPatternSize(int patternSize) {
        PatternSize = patternSize;
    }

    public void setSequences(Sequence[] sequences) {
        this.sequences = sequences;
    }

    public void setPositions(Position[] positions) {
        this.positions = positions;
    }

    public int[] getIDs() {
        return IDs;
    }

    public void setIDs(int[] IDs) {
        this.IDs = IDs;
    }

    public Song() {
        allocDefaultSong();
    }

    public VoiceExpander getLinkedVoiceExpander() {
        return LinkedVoiceExpander;
    }

    public void setLinkedVoiceExpander(VoiceExpander linkedVoiceExpander) {
        LinkedVoiceExpander = linkedVoiceExpander;
    }

    public boolean isContentLoaded() {
        return isContentLoaded;
    }

    public void setContentLoaded(boolean contentLoaded) {
        isContentLoaded = contentLoaded;
    }

    public boolean isReplayOnEndOfSong() {
        return replayOnEndOfSong;
    }

    public void setReplayOnEndOfSong(boolean replayOnEndOfSong) {
        this.replayOnEndOfSong = replayOnEndOfSong;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public float getVolume() {
        return Volume;
    }

    public float getMixFrequency() {
        return MixFrequency;
    }

    public Sequence[] getSequences() {
        return sequences;
    }

    public Position[] getPositions() {
        return positions;
    }

    public SymphonieInstrument[] getInstruments() {
        return instruments;
    }

    public Set<String> getInstrumentGroups() {
        Set<String> groups = new HashSet<>();
        for(SymphonieInstrument instrument : getInstrumentsAsList()) {
            groups.add(instrument.getGroup());
        }
        return groups;
    }

    public void muteInstrumentsOfGroup(String group, boolean mute) {
        for(SymphonieInstrument instrument : getInstrumentsAsList()) {
            if(instrument.getGroup().equals(group)) {
                instrument.setMuted(mute);
            } else {
                instrument.setMuted(!mute);
            }
        }
    }

    public List<SymphonieInstrument> getInstrumentsAsList() {
        final List<SymphonieInstrument> instrumentList = new Vector<>();
        if (null == instruments) return instrumentList;
        Arrays.stream(instruments).filter(Objects::nonNull).forEach(instrumentList::add);
        return instrumentList;
    }

    public List<SymphonieInstrument> getVstInstrumentsAsList() {
        final List<SymphonieInstrument> instrumentList = new Vector<>();
        if (null == instruments) return instrumentList;
        Arrays.stream(instruments).filter(instrument -> instrument != null && instrument.getInstrumentSource().equals(InstrumentSource.Vst)).forEach(instrumentList::add);
        return instrumentList;
    }

    boolean checkRealtimeModifyVoice(int VoiceNr) {
        return ((RealtimeModifyActivated == true) && (ModifyVoiceNr == VoiceNr));
    }

    void modifyRealtimeEvent(SongEvent se) {
        if (se.songFXType != SongEventType.FX_NONE) {
            if (ModifyParameter == 4) {
                se.D = ModifyValueTo;
            }
            if (ModifyParameter == 3) {
                se.C = ModifyValueTo;
            }
        }
    }

    public SongEvent getSongEvent(final int patternNr, final int columnIndex, final int rowIndex) {
        SongPattern Pat;
        if (isContentLoaded() && patterns != null) {
            Pat = getPattern(patternNr);
            if (Pat != null) return (Pat.getSongEvent(columnIndex, rowIndex));
        }
        return (null);
    }

    public SongEventPool getSongEventPool(final int patternNr, final int columnIndex, final int rowIndex) {
        SongPattern Pat;
        if (isContentLoaded() && patterns != null) {
            Pat = getPattern(patternNr);
            if (Pat != null) return Pat.getSongEventPool(columnIndex, rowIndex);
        }
        return (null);
    }

    public float getPlayingLineNr() {
        return (PlayLinePos);
    }

    public int getPlayingPatternNr() {
        if (!isContentLoaded || positions == null || getPlayingPositionNr() < 0) return 0;
        return getPosition(getPlayingPositionNr()).getPatternNumber();
    }

    public void initPlayingPosition() {
        actualPlayingPositionIndex = 0;
    }

    public double getPositionSpeed() {
        return actualPositionSpeed;
    }

    public int getPlayingPositionNr() {
        if (isPositionPlaying && isPlaying) {
            return (actualPlayingPositionIndex);
        } else {
            return (0);
        }
    }

    public double getPlayedPercentageOfAllSong() {
        if (isPositionPlaying && isPlaying) {
            double len = getSequence(0).getEndPosition() - getSequence(0).getStartPositionIndex();
            return (1.0 * (actualPlayingPositionIndex - getSequence(0).getStartPositionIndex())) / len;
        } else {
            return (0);
        }
    }

    boolean checkEarlyBreakImmediate() {
        return (ForceJumpToPos && !ForceSyncedBreak);
    }

    boolean checkEarlyBreak() {
        return (ForceJumpToPos);
    }

    void setNextPositionToPlaySynced(int NewNextPos, boolean SyncedBreak) {
        if (NewNextPos < this.getNumbOfPositions()) {
            ForceJumpToPos = true;
            ForcedPosNr = NewNextPos;
            ForceSyncedBreak = SyncedBreak;
        }
    }

    private int getNextPositionToPlay() {
        if(playSongMode.equals(PlaySongMode.PositionRepeat)) return actualPlayingPositionIndex;
        if(playSongMode.equals(PlaySongMode.PatternRepeat)) return actualPlayingPositionIndex;
        if (ForceJumpToPos == true) {
            ForceJumpToPos = false;
            return ForcedPosNr;
        } else return actualPlayingPositionIndex + 1;
    }

    //todo: play all sequences
    private void MoveToNextPosition() {
        actualPlayingPositionIndex = getNextPositionToPlay();
        logger.debug("Playing Position " + actualPlayingPositionIndex);
        if (actualPlayingPositionIndex >= getSequence(0).getEndPosition()) {
            actualPlayingPositionIndex = getSequence(0).getStartPositionIndex();
            if (LinkedVoiceExpander != null) LinkedVoiceExpander.stopAllVoices();
            if(!replayOnEndOfSong) {
                stopSong();
                LinkedVoiceExpander.notifySongsHasFinishedPlaying();
            } else {
                PlayPositionInit();
            }
        } else {
            PlayPositionInit();
        }
        setUpdatePlayingPos(true);
    }

    private void PlayPositionInit() {
        if (((positions != null) && (actualPlayingPositionIndex < positions.length) && (positions[actualPlayingPositionIndex] != null))) {
            PosTuneOffset = positions[actualPlayingPositionIndex].Tune;
            PosNumbOfLoops = positions[actualPlayingPositionIndex].NumbOfLoops;
            PosStart = positions[actualPlayingPositionIndex].StartRow;
            PosLen = positions[actualPlayingPositionIndex].RowLength; // in Lines
            actualPositionSpeed = positions[actualPlayingPositionIndex].getSpeed_Cycl(); // in Lines
            if(!playSongMode.equals(PlaySongMode.PatternRepeat)) PlayingPatternNr = positions[actualPlayingPositionIndex].PatternNumbers[0]; // in Lines
            PlayLinePos = PosStart;
            isPositionPlaying = true;
        }
        setUpdatePlayingPos(true);
    }

    private void playPosition(int positionIndex) {
        logger.debug("Play Position:" + positionIndex);
        isPlaying = false;
        actualPlayingPositionIndex = positionIndex;
        PlayPositionInit();
        setBpm(bpm);
        if(getLinkedVoiceExpander()!=null) getLinkedVoiceExpander().setSongSpeed(bpm, getPosition(actualPlayingPositionIndex).getSpeed_Cycl());
        isPlaying = true;
        setUpdatePlayingPos(true);
    }

    public void playFromPosition(int positionNr) {
        playSongMode = PlaySongMode.Song;
        playPosition(positionNr);
    }

    public void playPositionEndless(int positionNr) {
        if(isPlaying) stopSong();
        else {
            playSongMode = PlaySongMode.PositionRepeat;
            playPosition(positionNr);
        }
    }

     public void playPatternEndless(int positionNr, int patternIndex) {
         if(isPlaying) stopSong();
         else {
             playPositionEndless(positionNr);
             playSongMode = PlaySongMode.PatternRepeat;
             PlayingPatternNr = patternIndex;
             isPlaying = true;
         }
     }

    public void playfromActualPosition() {
        playSongMode = PlaySongMode.Song;
        playPosition(actualPlayingPositionIndex);
    }

    public void PlayFromFirstSequence(boolean replayOnEndOfSong) {
        playSongMode = PlaySongMode.Song;
        this.replayOnEndOfSong = replayOnEndOfSong;
        if (sequences != null && sequences[0] != null) {
            if (this.sequences[0].Action.equals(SequenceActionEnum.Play)) { // 0=PLAY 1=SKIP -1=LAST OF SONG
                playPosition(this.sequences[0].startPositionIndex);
            } else {
                playPosition(0);
            }
        } else {
            playPosition(0);
        }
    }

    public void stopSong() {
        isPositionPlaying = false;
        isPlaying = false;
        ForceJumpToPos = false;
        ForceSyncedBreak = false;
    }

    public void allocDefaultSong() { // alles löschen
        setNumbOfVoices(DEFAULT_NUMB_TRACKS);
        setBpm(120);
        setNumbOfRows(64);
        allocNumbOfSequences(2);
        allocNumbOfPositions(DEFAULT_NUMB_POSITIONS);
        allocNumbOfPatterns(DEFAULT_NUMB_PATTERNS);
        allocDefaultNumbInstruments();
    }

    public void allocDefaultNumbInstruments() {
        if (InstrumentsAllocated == false) {
            allocNumbInstruments(256);
        }
    }

    public void allocNumbOfPatterns(int numberOfPatterns) {
        patterns = new Vector<>();
        for (int i = 0; i < numberOfPatterns; i++) {
            patterns.add(new SongPattern(this, numbOfVoices, numbOfRows));
        }
        this.numberOfPatterns = numberOfPatterns;
    }

    public void allocNumbOfPositions(int quantity) {
        positions = new Position[quantity];
        for (int i = 0; i < quantity; i++) {
            positions[i] = new Position();
        }
        setNumbOfPositions(quantity);
    }

    public void fillPositionsToDefault() {
        for (int i = 0; i < getNumbOfPositions(); i++) {
            positions[i] = getPosition(i);
            positions[i].setNumbOfLayers(1);
        }
    }

    public void allocNumbOfSequences(int numbOfSequences) {
        sequences = new Sequence[numbOfSequences];
        for (int i = 0; i < numbOfSequences; i++) {
            sequences[i] = new Sequence();
        }
        sequences[0].setAction(SequenceActionEnum.Play);
        sequences[0].NumbOfLoops = 1;
        sequences[0].EndPosition = 60;
        setNumbOfSequences(numbOfSequences);
    }

    public void backupInstrumentMuteState() {
        getInstrumentsAsList().forEach(instrument -> instrument.oldMuteStatus = instrument.isMuted());
    }

    public void restoreInstrumentMuteState() {
        getInstrumentsAsList().forEach(instrument -> instrument.setMuted(instrument.oldMuteStatus));
    }

    public void allocNumbInstruments(int i) {
        instruments = new SymphonieInstrument[i];
        instruments[0] = new SymphonieInstrument();
        IDs = new int[i];
        java.util.Arrays.fill(IDs, -1);
        InstrumentsAllocated = true;
        setNumbOfInstruments(i);
    }

    public int getNumbInstrumentsAllocated() {
        if (InstrumentsAllocated == true) {
            return (getNumbOfInstruments());
        } else {
            return (0);
        }
    }

    void updatePatternSize() {
        PatternSize = numbOfVoices * numbOfRows;
    }

    public int getPatternSize() {
        return (PatternSize);
    }

    public int getNumberOfPatterns() {
        return (numberOfPatterns);
    }

    void setNumbOfSequences(int i) {
        NumbOfSequences = i;
    }

    public int getNumbOfSequences() {
        return (NumbOfSequences);
    }

    void setNumbOfPositions(int i) {
        NumbOfPositions = i;
    }

    public int getNumbOfPositions() {
        return (NumbOfPositions);
    }

    public int getNumbOfVoices() {
        return numbOfVoices;
    }

    public void setNumbOfVoices(int numbOfVoices) {
        while(this.numbOfVoices < numbOfVoices) {
            addVoice();
            this.numbOfVoices++;
        }
        this.numbOfVoices = numbOfVoices;
        updatePatternSize();
    }

    private void addVoice() {
        for (SongPattern pattern : patterns) {
            pattern.addVoice();
        }
    }

    public void setNumbOfRows(int i) {
        numbOfRows = i;
        updatePatternSize();
    }

    public int getNumbOfRows() {
        return (numbOfRows);
    }

    void setNumbOfInstruments(int i) {
        NumbOfInstruments = i;
    }

    public int getNumbOfInstruments() {
        if (instruments != null) {
            return (NumbOfInstruments);
        } else {
            return (0);
        }
    }

    public double getBpm() {
        return bpm;
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
        if(getLinkedVoiceExpander()!=null) {
            getLinkedVoiceExpander().setBpm(bpm);
        }
    }

    public Sequence getSequence(int i) {
        if (i < getNumbOfSequences()) {
            if (sequences[i] == null) sequences[i] = new Sequence();
            return (sequences[i]);
        } else {
            return (null);
        }
    }

    public Position getPosition(int i) {
        if (positions != null && i < getNumbOfPositions()) {
            if (positions[i] == null) positions[i] = new Position();
//            positions[i].ParentSong = this;
            return (positions[i]);
        } else {
            return (null);
        }
    }

    public SongPattern getPattern(int patternIndex) {
        if (patterns.size() > patternIndex) {
            return patterns.get(patternIndex);
        }
        throw new RuntimeException("To few Patterns allocated");
    }


    public int getIndexOfInstrumentID(int i) {
        return (IDs[i]);
    }

    SymphonieInstrument getValidInstrumentOfId(int instrumentId) {
        SymphonieInstrument instrument = getInstrumentWithId(instrumentId);
        if(instrument!=null && instrument.hasContent()) return instrument;
        return null;
    }


    public SymphonieInstrument getInstrumentWithId(int i) {
        if (getIndexOfInstrumentID(i) < 0) {
            //logger.error("illegal instrument " + i);
        }
        SymphonieInstrument instrument = getInstrument(getIndexOfInstrumentID(i));
        if(instrument==null) return null;
        if (!instrument.hasContent()) {
            logger.error("illegal instrument content " + i);
        }
        return instrument;
    }

    public boolean hasContentWithActiveInstruments() {
        for(SymphonieInstrument instrument : getInstrumentsAsList()) {
            if(instrument.hasContent()) return true;
        }
        return false;
    }

    void setIDofInstrumentIndex(int i, int newid) {
        SymphonieInstrument si = getInstrument(i);
        if (si != null && i != -1) {
            si.ID = newid;
        }
    }

    int getIDofInstrumentIndex(int i) {
        SymphonieInstrument si = getInstrument(i);
        if (si != null) {
            return (si.ID);
        } else {
            return (-1);
        }
    }

    public SymphonieInstrument getInstrument(int index, boolean autoAllocate) { // auto allocates
        if (autoAllocate) return getInstrument(index);
        if ((index < getNumbOfInstruments()) && (index >= 0)) {
            return instruments[index];
        } else {
            return null;
        }
    }

    public SymphonieInstrument getInstrument(int index) { // auto allocates
        if ((index < getNumbOfInstruments()) && (index >= 0)) {
            if (instruments[index] == null) {
                instruments[index] = new SymphonieInstrument();
            }
            return instruments[index];
        } else {
            return null;
        }
    }

    public void insertPositionAt(Position position, int positionIndex) {
        for(int i = positions.length-2; i >= positionIndex; i--) {
            positions[i+1] = positions[i];
        }
        positions[positionIndex+1] = new Position(position);
    }

    public void removePositionAt(int positionIndex) {
        for(int i = positionIndex; i < positions.length-2; i++) {
            positions[i] = positions[i+1];
        }
        clearLastPosition();
    }

    public void clearLastPosition() {
        positions[positions.length-1] = new Position();
    }

    public void removeInstrumentIndex(int index) {
        if (instruments[index] != null && index < getNumbOfInstruments()) {
            getVstInstruments().remove(instruments[index]);
            if (instruments[index].getMultiChannel().equals(MultichannelEnum.StereoL)) {
                instruments[index+1] = new SymphonieInstrument();
                instruments[index+1].setIndex(index+1);
            }
            if (instruments[index].getMultiChannel().equals(MultichannelEnum.StereoR)) {
                instruments[index-1] = new SymphonieInstrument();
                instruments[index-1].setIndex(index-1);
            }
            instruments[index] = new SymphonieInstrument();
            instruments[index].setIndex(index);
        }
    }

    public boolean checkInstrumentIndexInUse(int i) {
        if ((i < getNumbOfInstruments()) && (i >= 0)) {
            if (instruments[i] == null) return (false);
            return (instruments[i].hasContent);
        } else {
            return (false);
        }
    }

    public void muteAllInstruments(boolean muted) {
        for (SymphonieInstrument instrument : instruments) {
            if (instrument != null) instrument.setMuted(muted);
        }
    }

//    public SymphonieInstrument getFreeInstrument() {
//        for (int i = 0; i < getNumbOfInstruments(); i++) {
//            if (getInstrument(i).hasContent == false) {
//                instruments[i].ID = getFreeInstrID(i);
//                return instruments[i];
//            }
//        }
//        return (null);
//    }
//
//    int getFreeInstrID(int InstrIndex) {
//        for (int i = 0; i < IDs.length; i++) {
//            if (IDs[i] == -1) {
//                IDs[i] = InstrIndex;
//                return (i);
//            }
//        }
//        return (-1);
//    }

    public void setIDOfInstrIndex(int ID, int InstrIndex) {
        IDs[ID] = InstrIndex;
    }

    void setPositionSpeedinCycl(double speed) {
        if(speed <= 0) return;
        actualPositionSpeed = speed;
        if(LinkedVoiceExpander!=null) LinkedVoiceExpander.setSongSpeed(this.bpm, this.actualPositionSpeed);
    }

    // Play Song Section
    public int PlaySongEvent(VoiceExpander voiceExpander) {
        this.LinkedVoiceExpander = voiceExpander;
        if (this.isPositionPlaying == true) {
            PlayEventsAtPos(voiceExpander, PlayLinePos);
            PlayLinePos += 1;
            if ((PlayLinePos >= (this.PosStart + this.PosLen)) || checkEarlyBreakImmediate()) {
                // Last Line reached
                this.PosNumbOfLoops--;
                if ((this.PosNumbOfLoops <= 0) || (checkEarlyBreak())) { // reloop and no early break
                    // Move to next Position or to position forced to play
                    MoveToNextPosition();
                    voiceExpander.setSongSpeed(this.bpm, this.actualPositionSpeed);
                } else {
                    // reloop position
                    PlayLinePos = PosStart;
                }
            }
        } else {
            // Play Pattern
            PlayEventsAtPos(voiceExpander, PlayLinePos);
            PlayLinePos += 1;
            if (PlayLinePos >= this.numbOfRows) {
                PlayLinePos = 0;
                //this.SongPlaying = false; Stop Song playing
            }
        }
        return (0); // No change SamplesTillNext Event
    }

    void setUpdatePlayingPos(boolean b) {
        UpdatePlayingPos = b;
    }

    boolean checkUpdatePlayingPos() {
        boolean b = UpdatePlayingPos;
        UpdatePlayingPos = false;
        return (b);
    }

    void PlayEventsAtPos(VoiceExpander vx, float PlayLinePos) {
        for (int VoiceNr = 0; VoiceNr < this.getNumbOfVoices(); VoiceNr++) {
            PlayEventsOfVoiceAtPos(vx, PlayLinePos, VoiceNr);
        }
    }

    void PlayEventsOfVoiceAtPos(VoiceExpander voiceExpander, float PlayLinePos, int voiceIndex) {
        SongPattern pattern = getPattern(PlayingPatternNr);
        PatternVoice patternVoice = pattern.getPatternVoice(voiceIndex);
        SongEventPool songEventPool = patternVoice.getSongEventPool(PlayLinePos, false);
        if (songEventPool != null) {
            // only 1 event per pool at the moment
            if (songEventPool.getNumberOfSongEvents() > 0)
                PlaySongEvent(voiceExpander, voiceIndex, songEventPool.getSongEvent(false));
//            for (int i = 0; i < songEventPool.getNumberOfSongEvents(); i++) {
//                SongEvent mySongEvent = songEventPool.getSongEvent(i);
//                if (mySongEvent != null) {
//                    PlaySongEvent(vx, VoiceNr, mySongEvent);
//                }
//            }
        }
    }

//    SongEventPool getSongEventPool(int PatNr, int VoiceNr, float TimePosition) {
//        SongPattern Pat = getPattern(PatNr);
//        PatternVoice pv = Pat.PatternVoices[VoiceNr];
//        return (pv.getSongEventPool(TimePosition));
//    }
    
    
/*    int ConvertSymphInstrToNewInstrIndex(int SymphInstr) {
        for(int i=0;i<this.getNumbOfInstruments();i++) {
            SymphonieInstrument si = this.getInstrumentExisting(i);
            if(si != null) {
                if(si.ID==SymphInstr) return(i);
            }
        }
        return(-1);
    }
*/

    boolean checkIsLeftStereoChannel(int ChannelIndex) {
        return ((ChannelIndex & 0x0001) == 0);
    }


//    void addKeyOnEvent(int PatNr, int x, int y, int InstrNr, int Pitch, int Vol) {
//        SongEventPool se = getSongEventPool(PatNr, x, y);
//        SongEvent MySongEvent = se.getSongEventForced(0);
//        if (MySongEvent != null) {
//            MySongEvent.setKeyOn(InstrNr, Pitch, Vol);
//        }
//    }
//
//    void clrEvent(int PatNr, int x, int y) {
//        SongEventPool se = getSongEventPool(PatNr, x, y);
//        SongEvent MySongEvent = se.getSongEvent(0);
//        if (MySongEvent != null) {
//            MySongEvent.clear();
//        }
//    }
//
//    void setEventType(int PatNr, int x, int y, int newEventType) {
//        SongEventPool se = getSongEventPool(PatNr, x, y);
//        SongEvent MySongEvent = se.getSongEventForced(0);
//        if (MySongEvent != null) {
//            MySongEvent.setType(newEventType);
//        }
//    }

    void PlaySongEvent(final VoiceExpander voiceExpander, int VoiceNr, SongEvent songEvent) {
        if(songEvent.isEmpty()) return;

        if (this.checkRealtimeModifyVoice(VoiceNr)) {
            modifyRealtimeEvent(songEvent);
        }

        int tempPitch = (int) songEvent.B;
        int instrumentId = (int) songEvent.A;
        SymphonieInstrument instrument = getValidInstrumentOfId((int) songEvent.A);
        if(instrument != null && instrument.getInstrumentSource().equals(InstrumentSource.Vst)) {
            instrument.playEventVst(songEvent, this);
            return;
        }

        switch (songEvent.songFXType) {
            case SongEventType.FX_KEYON:
                float tempVolume = songEvent.C;
                if (tempVolume <= 100.0f) {
                    if ((getInstrumentWithId(instrumentId) != null) && getInstrumentWithId(instrumentId).hasContent()) {
                        voiceExpander.SongEventKeyOn(getInstrumentWithId(instrumentId), VoiceNr, tempPitch, tempVolume);
                        // Play Stereo Too
                        if ((getInstrumentWithId(instrumentId).getMultiChannel().equals(MultichannelEnum.StereoL)) && (checkIsLeftStereoChannel(VoiceNr))) {
                            int leftInstrumentIndex = getIndexOfInstrumentID(instrumentId);
                            instrument = this.getInstrument(leftInstrumentIndex + 1);
                            if (instrument.hasContent()) {

                                voiceExpander.SongEventKeyOn(instrument, VoiceNr + 1, tempPitch, tempVolume);
                            }
                        }
                    } else {
                        logger.error("PlaySongEvent FX_KEYON Wrong instrument:" + instrumentId);
                    }
                }
                break;

            case SongEventType.FX_SETPITCH:
                if (getInstrumentWithId(instrumentId) != null) {
                    voiceExpander.SongEventSetPitch(getInstrumentWithId(instrumentId), VoiceNr, tempPitch);
                } else {
                    logger.error("PlaySongEvent FX_SETPITCH Wrong instrument:" + instrumentId);
                }
                break;

            case SongEventType.FX_PITCHUP:
                voiceExpander.adjustVoiceFreq(VoiceNr, 1.0078125f);
                break;
            case SongEventType.FX_PITCHUP2:
                voiceExpander.adjustVoiceFreq(VoiceNr, 1.015625f);
                break;
            case SongEventType.FX_PITCHUP3:
                voiceExpander.adjustVoiceFreq(VoiceNr, 1.03125f);
                break;
            case SongEventType.FX_PITCHDOWN:
                voiceExpander.adjustVoiceFreq(VoiceNr, 0.9921875f);
                break;
            case SongEventType.FX_PITCHDOWN2:
                voiceExpander.adjustVoiceFreq(VoiceNr, 0.984375f);
                break;
            case SongEventType.FX_PITCHDOWN3:
                voiceExpander.adjustVoiceFreq(VoiceNr, 0.9788f);
                break;

            case SongEventType.FX_PITCHSLIDEUP:
                voiceExpander.SongEventPSlide(VoiceNr, songEvent.D);
                break;
            case SongEventType.FX_PITCHSLIDEDOWN:
                voiceExpander.SongEventPSlide(VoiceNr, -songEvent.D);
                break;
            case SongEventType.FX_PSLIDETO:
                if (getInstrumentWithId(instrumentId) != null) {
                    voiceExpander.SetVoicePSlideTo(getInstrumentWithId(instrumentId), VoiceNr, tempPitch, songEvent.D);
                } else {
                    logger.error("PlaySongEvent FX_PSLIDETO Wrong instrument:" + instrumentId);
                }
                break;
            case SongEventType.FX_VOLUMESLIDEUP:
                voiceExpander.SongEventVSlide(VoiceNr, songEvent.D);
                break;
            case SongEventType.FX_VOLUMESLIDEDOWN:
                voiceExpander.SongEventVSlide(VoiceNr, -songEvent.D);
                break;
            case SongEventType.FX_ADDVOLUME:
                voiceExpander.SongEventAddVolume(VoiceNr, (songEvent.C / 8));
                break;
            case SongEventType.FX_SETVOLUME:
                voiceExpander.SongEventSetVolume(VoiceNr, songEvent.C);
                break;
            case SongEventType.FX_STOPSAMPLE:
                voiceExpander.SongEventPausePlaying(VoiceNr);
                break;
            case SongEventType.FX_CONTSAMPLE:
                voiceExpander.SongEventContinue(VoiceNr, true);
                break;
            case SongEventType.FX_FROMANDPITCH:
                if (getInstrumentWithId(instrumentId) != null) {
                    voiceExpander.SongEventKeyOnSamplePos(getInstrumentWithId(instrumentId), VoiceNr, tempPitch, songEvent.D);
                } else {
                    logger.error("PlaySongEvent FX_FROMANDPITCH Wrong instrument:" + instrumentId);
                }
                break;
            case SongEventType.FX_REPLAYFROM:
                if (getInstrumentWithId(instrumentId) != null) {
                    voiceExpander.SongEventKeyOnSamplePos(getInstrumentWithId(instrumentId), VoiceNr, songEvent.D);
                } else {
                    logger.error("PlaySongEvent FX_REPLAYFROM Wrong instrument:" + instrumentId);
                }
                break;
            case SongEventType.FX_CHANNELFILTER:
                logger.error("VERIFY:FX_CHANNELFILTER playing disabled");
                //voiceExpander.setFilter(VoiceNr, (int) songEvent.A, songEvent.C, songEvent.D); // Type, q, Freq
                break;
            case SongEventType.FX_FILTERPerfectLP4:
                logger.error("VERIFY:FX_FILTERPerfectLP4 playing disabled");
                //voiceExpander.setFilterPerfectLP4(VoiceNr, (int) songEvent.A, songEvent.C, songEvent.D); // Type, q, Freq
                break;
            case SongEventType.FX_DSP_DISABLE:
                voiceExpander.disableDsp();
                voiceExpander.setDspChanged(true);
                break;
            case SongEventType.FX_DSPCROSSECHO:
                voiceExpander.activateDspCrossEcho(VoiceNr, (int) songEvent.A, songEvent.B, songEvent.C, songEvent.D);
                voiceExpander.setDspChanged(true);
                print("FX_DSPCROSSECHO:" + songEvent.C + " " + songEvent.D);
                break;
            case SongEventType.FX_DSPECHO:
                print("FX_DSPECHO");
                voiceExpander.activateDspEcho(VoiceNr, (int) songEvent.A, songEvent.B, songEvent.C, songEvent.D);
                voiceExpander.setDspChanged(true);
                break;
            case SongEventType.FX_DSPDELAY:
                print("FX_DSPDELAY");
                voiceExpander.setDspChanged(true);
                break;
            case SongEventType.FX_DSPCROSSDELAY:
                print("FX_DSPCROSSDELAY");
                voiceExpander.setDspChanged(true);
                break;
            case SongEventType.FX_SETSPEED:
                setPositionSpeedinCycl(songEvent.getSpecialParameter());
                break;
            case SongEventType.FX_SPEEDDOWN:
                actualPositionSpeed++;
                setPositionSpeedinCycl(actualPositionSpeed);
                break;
            case SongEventType.FX_SPEEDUP:
                actualPositionSpeed--;
                setPositionSpeedinCycl(actualPositionSpeed);
                break;
            case SongEventType.FX_SAMPLEVIB:
                print("FX_SAMPLEVIB");
                break;
            default:
                //print("UNKNOWN EVENT");
                break;
        }
    }

    public String getFirstNonMutedInstrument() {
        for(SymphonieInstrument instrument : getInstrumentsAsList()) {
            if(!instrument.isMuted()) return instrument.getRenderDescription();
        }
        return "";
    }

    public String getFirstNonMutedInstrumentForRendering() {
        for(SymphonieInstrument instrument : getInstrumentsAsList()) {
            if(!instrument.oldMuteStatus) return instrument.getRenderDescription();
        }
        return "";
    }

    private void print(String text) {
        logger.debug(text);
    }

    public List<String> compare(Song otherSong) {
        List<String> errors = new Vector<>();
        int patternIndex = 0;
        for(SongPattern pattern : patterns) {
            if(!pattern.toStringNoEmpty().equals(otherSong.getPattern(patternIndex).toStringNoEmpty())) {
                errors.add("Pattern differs " + patternIndex);
                logger.debug("ORIGINAL:" + pattern.toStringNoEmpty());
                logger.debug("SAVED   :" + otherSong.getPattern(patternIndex).toStringNoEmpty());
            }
            patternIndex++;
        }
        return errors;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("Voices:").append(numbOfVoices)
                .append(" Content:").append(this.isContentLoaded)
                .append(" isPlaying:").append(this.isPlaying)
                .append(" Bpm:").append(this.bpm)
                .append(" Song Name:").append(Name);
        return text.toString();
    }
}

//class PEDBlock {
//
//    PEDBlock(Song newmySong) {
//        mySong = newmySong;
//        numberOfVoices = mySong.getNumbOfVoices();
//    }
//
//    Song mySong;
//    int numberOfVoices = 0;
//    boolean isMarking = false;
//    int x, y, w, h;
//    int x2, y2;
//    int PatNr;
//
//

//    String getInfo() {
//        if (isMarking) {
//            return ("Marking Block...:" + " (x:" + x + ",y:" + y + ")");
////		    + " (w:" + w + ",h:" + h + ")");
//        } else {
//            return ("Block Marked:" + " (x:" + x + ",y:" + y + ")"
//                    + " (w:" + w + ",h:" + h + ")");
//        }
//    }
//
//    void markReset() { // Begin new marking
//        isMarking = false;
//    }
//
//    void markBlockXY(int myPatNr, int tx, int ty) {
//        if (isMarking == false) {
//            x = tx;
//            y = ty;
//        } else {
//            int temp = 0;
//            x2 = tx;
//            y2 = ty;
//            if (x2 < x) {
//                temp = x2;
//                x2 = x;
//                x = temp;
//            }
//            if (y2 < y) {
//                temp = y2;
//                y2 = y;
//                y = temp;
//            }
//            w = x2 - x + 1;
//            h = y2 - y + 1;
//        }
//        PatNr = myPatNr;
//        isMarking = !isMarking;
//
//    }
//
//    void finishMarking(int myPatNr, int tx, int ty) {
//        if (isMarking == true) {
//            markBlockXY(myPatNr, tx, ty);
//        }
//    }
//
//    boolean checkMarkingFinished() {
//        return (!isMarking);
//    }
//
//    void copyBlock(int myPatNr, int tx, int ty) {
//        SongEvent SrcEvent;
//        SongEvent DestEvent;
//        //getTempPattern().ParentSong = mySong;
//        finishMarking(myPatNr, tx, ty);
//        for (int RowNr = 0; RowNr < h; RowNr++) {
//            for (int VoiceNr = 0; VoiceNr < w; VoiceNr++) {
//                SrcEvent = mySong.getSongEvent(myPatNr, x + VoiceNr, y + RowNr);
//                DestEvent = getTempPattern().getSongEvent(VoiceNr, RowNr);
//                if (SrcEvent != null) {
//                    SrcEvent.copyValues(DestEvent);
//                } else {
//                    DestEvent.clear();
//                }
//            }
//        }
//    }
//
//    void rotateBlockDown(int myPatNr, int tx, int ty) {
//        SongEvent SrcEvent;
//        SongEvent DestEvent;
//        SongEvent LowestEvent = new SongEvent();
//        LowestEvent.clear();
//
//        finishMarking(myPatNr, tx, ty);
//        if (h > 1) {
//
//            for (int VoiceNr = 0; VoiceNr < w; VoiceNr++) {
//                SrcEvent = mySong.getSongEvent(myPatNr, x + VoiceNr, y + h - 1);
//                if (SrcEvent != null) SrcEvent.copyValues(LowestEvent);
//
//                for (int RowNr = h - 2; RowNr >= 0; RowNr--) {
//                    SrcEvent = mySong.getSongEvent(myPatNr, x + VoiceNr, y + RowNr);
//                    DestEvent = mySong.getSongEventForced(myPatNr, x + VoiceNr, y + RowNr + 1);
//                    if (SrcEvent != null) {
//                        SrcEvent.copyValues(DestEvent);
//                    } else {
//                        DestEvent.clear();
//                    }
//                }
//                DestEvent = mySong.getSongEventForced(myPatNr, x + VoiceNr, y);
//                LowestEvent.copyValues(DestEvent);
//
//            }
//        }
//    }
//
//    void rotateBlockUp(int myPatNr, int tx, int ty) {
//        SongEvent SrcEvent;
//        SongEvent DestEvent;
//        SongEvent LowestEvent = new SongEvent();
//        LowestEvent.clear();
//
//        finishMarking(myPatNr, tx, ty);
//        if (h > 1) {
//
//            for (int VoiceNr = 0; VoiceNr < w; VoiceNr++) {
//                SrcEvent = mySong.getSongEvent(myPatNr, x + VoiceNr, y);
//                if (SrcEvent != null) SrcEvent.copyValues(LowestEvent);
//
//                for (int RowNr = 1; RowNr < h; RowNr++) {
//                    SrcEvent = mySong.getSongEvent(myPatNr, x + VoiceNr, y + RowNr);
//                    DestEvent = mySong.getSongEventForced(myPatNr, x + VoiceNr, y + RowNr - 1);
//                    if (SrcEvent != null) {
//                        SrcEvent.copyValues(DestEvent);
//                    } else {
//                        DestEvent.clear();
//                    }
//                }
//                DestEvent = mySong.getSongEventForced(myPatNr, x + VoiceNr, y + h - 1);
//                LowestEvent.copyValues(DestEvent);
//
//            }
//        }
//    }
//
//
//    void pasteBlock(int myPatNr, int tx, int ty) {
//        SongEvent SrcEvent;
//        SongEvent DestEvent;
//        if (h == mySong.getNumbOfRows()) ty = 0;
//        if (w == mySong.getNumbOfVoices()) tx = 0;
//        //getTempPattern().ParentSong = mySong;
//        //finishMarking(myPatNr,tx,ty);
//        for (int RowNr = 0; RowNr < h; RowNr++) {
//            for (int VoiceNr = 0; VoiceNr < w; VoiceNr++) {
//                DestEvent = mySong.getSongEventForced(myPatNr, tx + VoiceNr, ty + RowNr);
//                SrcEvent = getTempPattern().getSongEvent(VoiceNr, RowNr);
//                if (SrcEvent != null && DestEvent != null) {
//                    SrcEvent.copyValues(DestEvent);
//                }
//            }
//        }
//    }
//
//    void addBlock(int myPatNr, int tx, int ty) {
//        SongEvent SrcEvent;
//        SongEvent DestEvent;
//        if (h == mySong.getNumbOfRows()) ty = 0;
//        if (w == mySong.getNumbOfVoices()) tx = 0;
//        //getTempPattern().ParentSong = mySong;
//        //finishMarking(myPatNr,tx,ty);
//        for (int RowNr = 0; RowNr < h; RowNr++) {
//            for (int VoiceNr = 0; VoiceNr < w; VoiceNr++) {
//                DestEvent = mySong.getSongEventForced(myPatNr, tx + VoiceNr, ty + RowNr);
//                SrcEvent = getTempPattern().getSongEvent(VoiceNr, RowNr);
//                if (SrcEvent != null && DestEvent != null && DestEvent.songFXType == SongEventType.FX_NONE) {
//                    SrcEvent.copyValues(DestEvent);
//                }
//            }
//        }
//    }
//
//    void clearBlock(int myPatNr, int tx, int ty) {
//        SongEvent DestEvent;
//        if (checkMarkingFinished() == true) {
//            for (int RowNr = 0; RowNr < h; RowNr++) {
//                for (int VoiceNr = 0; VoiceNr < w; VoiceNr++) {
//                    DestEvent = mySong.getSongEvent(myPatNr, x + VoiceNr, y + RowNr);
//                    if (DestEvent != null) {
//                        DestEvent.clear();
//                    }
//                }
//            }
//        }
//    }
//
//    private SongPattern tempPattern;
//    SongPattern getTempPattern() {
//        if (tempPattern == null) tempPattern = new SongPattern(numberOfVoices, numbOfRows);
//        return tempPattern;
//    }
//
//
//}




/*
NOTE_FULLEMPTY	EQU	$00ff0000
NOTE_VI		EQU	2	;WORD VVII

NOTE_SIZEOF	EQU	4

FXADDPITCH_LN		EQU	7+3
FXADDVOL_LN		EQU	5

FX_OLDNONE		EQU	-1
FX_NONE			EQU	0

FX_MIN	EQU	1
FX_MAX	EQU	FX_DSPDELAY

VOLUME_STOPSAMPLE	EQU	254
VOLUME_CONTSAMPLE	EQU	253
VOLUME_STARTSAMPLE	EQU	252
VOLUME_KEYOFF		EQU	251
VOLUME_SPEEDDOWN	EQU	250
VOLUME_SPEEDUP		EQU	249
VOLUME_SETPITCH		EQU	248
VOLUME_PITCHUP		EQU	247
VOLUME_PITCHDOWN	EQU	246
VOLUME_PITCHUP2		EQU	245
VOLUME_PITCHDOWN2	EQU	244
VOLUME_PITCHUP3		EQU	243
VOLUME_PITCHDOWN3	EQU	242
VOLUME_NONE		EQU	0
VOLUME_MIN		EQU	1
VOLUME_MAX		EQU	100
VOLUME_COMMAND		EQU	200


NOTEPITCH_NONOTE	EQU	-1
NOTEPITCH_MIN		EQU	0
NOTEPITCH_OCTAVE	EQU	12
NOTEPITCH_MAX		EQU	(NOTEPITCH_OCTAVE*7)

INSTR_MAX		EQU	127	;FUER EDITOR

DSPNOTE_TYPE	EQU	NOTE_PITCH
DSPNOTE_LEVEL	EQU	NOTE_INSTR
DSPNOTE_BUF	EQU	NOTE_VOLUME


PEDTITLE_H	EQU	1	;CHARS
PEDLINETITLE_W	EQU	5	;CHARS 

 EKeyOn		dc.l	DrawNotePitch,EVENTDrawDecByte,DrawDecByte6
		dc.b	"Key On      ",0,"Pitch ",0,"Instr ",0,"Volume",0
		even

EFX1		dc.l	EDrawNone,EDrawNone,DrawDecByte6
		dc.b	"VSlide Up   ",0,"------",0,"------",0,"Speed ",0
		even
EFX2		dc.l	EDrawNone,EDrawNone,DrawDecByte6
		dc.b	"VSlide Down ",0,"------",0,"------",0,"Speed ",0
		even
EFX3		dc.l	EDrawNone,EDrawNone,DrawDecByte6
		dc.b	"PSlide Up   ",0,"------",0,"------",0,"Speed ",0
		even
EFX4		dc.l	EDrawNone,EDrawNone,DrawDecByte6
		dc.b	"PSlide Down ",0,"------",0,"------",0,"Speed ",0
		even
EFX5		dc.l	DrawFromPi,DrawFromInstr,DrawPerHexXY
		dc.b	"ReplaySaFrom",0,"(Ptch)",0,"(Inst)",0,"Sa.Pos",0
		even
EFX6		dc.l	DrawNotePitch,DrawDecByte6,DrawPerHexXY
		dc.b	"SaFrom&Pitch",0,"Pitch ",0,"Instr ",0,"Sa.Pos",0
		even
EFX7		dc.l	EDrawNone,DrawDecByte6,DrawPerHexXY
		dc.b	"SaFromOffset",0,"------",0,"------",0,"Sa.Pos",0
		even
EFX8		dc.l	EDrawNone,DrawDecByte6,DrawFx8V
		dc.b	"ModifyFOffst",0,"------",0,"------",0,"FinePo",0
		even
EFX9		dc.l	EDrawNone,EDrawNone,DrawDecByte6
		dc.b	"Set Speed   ",0,"------",0,"------",0,"Cycl  ",0
		even
EFX10		dc.l	EDrawNone,EDrawNone,DrawFx12V
		dc.b	"Add Pitch   ",0,"------",0,"------",0,"Intens",0
		even
EFX11		dc.l	EDrawNone,EDrawNone,DrawFx11V
		dc.b	"Add Volume  ",0,"------",0,"------",0,"Intens",0
		even
EFX12		dc.l	EDrawNone,DrawDecByte6,DrawPerHexXY
		dc.b	"Tremolo     ",0,"------",0,"Speed ",0,"Rate  ",0
		even
EFX13		dc.l	EDrawNone,DrawDecByte6,DrawPerHexXY
		dc.b	"Vibrato     ",0,"------",0,"Speed ",0,"Rate  ",0
		even
EFX14		dc.l	EDrawNone,DrawDecByte6,DrawPerHexXY
		dc.b	"SamplePtrVib",0,"------",0,"Speed ",0,"Rate  ",0
		even
EFX15		dc.l	DrawNotePitch,DrawDecByte6,DrawDecByte6
		dc.b	"PitchSlideTo",0,"Pitch ",0,"Instr ",0,"Speed ",0
		even
EFX16		dc.l	EDrawNone,DrawDecPlus1,DrawDecByte6
		dc.b	"Retrig      ",0,"------",0,"Cycl  ",0,"Number",0
		even
EFX17		dc.l	DrawDecByte6,DrawDecByte6,DrawDecByte6
		dc.b	"ShOfEmphasis",0,"Start%",0,"End%  ",0,"Type  ",0
		even
EFX18		dc.l	EDrawNone,EDrawNone,DrawEVCV
		dc.b	"Add HalvTone",0,"------",0,"------",0,"Offset",0
		even
EFX19		dc.l	DrawCVIdXY,DrawDecByte6,DrawEVCV	;DrawSignDecByte6
		dc.b	"Channel Vol ",0,"Type  ",0,"Pos   ",0,"Ampl  ",0
		even
EFX20		dc.l	DrawSignDecByte6,DrawSignDecByte6,DrawSignDecByte6
		dc.b	"Illegal     ",0,"Red   ",0,"Green ",0,"Blue  ",0
		even
EFX23		dc.l	EDrawFilterID,DrawDecByte6,DrawDecByte6
		dc.b	"ResoFilter  ",0,"Type  ",0,"Reso  ",0,"Freq  ",0
		even
EFX24		dc.l	DrawDspFxXY,DrawEVEchoLvl,DrawDecByte6
		dc.b	"Dsp Echo    ",0,"§",0,"Level ",0,"Len   ",0
		even
EFX25		dc.l	DrawDspFxXY,DrawEVDelayLvl,DrawDecByte6
		dc.b	"Dsp Delay   ",0,"§",0,"Level ",0,"Len   ",0
		even
EFX26		dc.l	DrawDspFxXY,DrawDecByte6,DrawDecByte6
		dc.b	"Dsp Chorus  ",0,"§",0,"Level ",0,"Len   ",0
		even

EVibrato	dc.l	EDrawNone,DrawDecByte6,DrawPerHexXY
		dc.b	"Vibrato     ",0,"------",0,"Speed ",0,"Rate  ",0
		even

ETremolo	dc.l	EDrawNone,DrawDecByte6,DrawPerHexXY
		dc.b	"Vibrato     ",0,"------",0,"Speed ",0,"Rate  ",0
		even

ESimpleFX	dc.l	EDrawNone,EDrawNone,DrawNoteVolume
		dc.b	"Simple FX   ",0,"------",0,"------",0,"FX ID ",0  
  
  
 */
