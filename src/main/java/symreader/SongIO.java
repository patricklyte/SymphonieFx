/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package symreader;


import ch.meng.symphoniefx.song.*;

import javax.xml.bind.JAXB;
import java.io.*;


import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author File Format designed by Patrick Meng 2008
 * Features: its optimized for robustness and high tolerance
 * loading of data can continue even if large parts of the data is missing
 * or is broken
 */

interface SymTags {
    String Content = "SymphonieSongModule";
    String ObjectBegin = "@OBJECTBEGIN";
    String ObjectEnd = "@OBJECTEND";
    String ArrayNames = "@ARRAYNAME";
    String ArrayPos = "@ARRAYPOS";
    String ArrayEnd = "@ARRAYEND";
    String TRUE = "@TRUE";
    String FALSE = "@FALSE";
    String Objectversion = "ObjectVersion";

    String SymObjSong = "Song";
    String SymObjSequence = "Sequence";

    String Action = "Action";
    String StartPosition = "StartPosition";
    String EndPosition = "EndPosition";
    String Name = "Name";
    String NumbOfLoops = "NumbOfLoops";
    String Tune = "Tune";
    String SymObjPosition = "Position";
    String NumbOfLayers = "NumbOfLayers";
    String StartRow = "StartRow";
    String RowLength = "RowLength";
    String SpeedCycl = "SpeedCycl";
    String NumbOfRows = "NumbOfTimePos";

    String Type = "Type";
    String Volume = "Volume";
    String ID = "ID";
    String FineTune = "FineTune";
    String LoopStart = "LoopStart";
    String Looplen = "Looplen";
    String LineSampleFlags = "LineSampleFlags";
    String PlayFlag = "PlayFlag";
    String MultiChannel = "MultiChannel";
    String Resonance = "Resonance";
    String VirtualSample = "VirtualSample";
    String AllowPosDetune = "AllowPosDetune";
    String NoDsp = "NoDsp";
    String MirrorX = "MirrorX";
    String PlayReverse = "PlayReverse";
    String PlaySynced = "PlaySynced";

    String FXClass = "FXClass";
    String SongFXType = "SongFXType";
    String A = "A";
    String B = "B";
    String C = "C";
    String D = "D";

    String SymObjEvent = "Event";
    String SymObjInstrument = "Instrument";
    String SymObjInstrumentData = "InstrumentData";

    String Mixfrequency = "Mixfrequency";
    String BPM = "BPM";
    String MasterVolume = "MasterVolume";
    String NumbOfSequences = "NumbOfSequences";
    String NumbOfPositions = "NumbOfPositions";
    String NumbOfInstruments = "NumbOfInstruments";
    String NumbOfPatterns = "NumbOfPatterns";
    String NumbOfVoices = "NumbOfVoices";
    String PatternSize = "PatternSize";
    String PatternNr = "PatternNr";
    String FlexibleTiming = "FlexibleTiming";
    String EventsPerEventPool = "EventsPerEventPool";

    String SymObjSamples = "RawSampleData";
    String RawDataBlockLen = "@RAWDATALEN";

    String PanningActive = "PanningActive";
    String PanningX = "PanningX";

    // Meta Info
    String MetaR = "MetaR";
    String MetaG = "MetaG";
    String MetaB = "MetaB";
    String MetaMainLead = "MetaMainLead";
    String MetaLead2 = "MetaLead2";
    String MetaBackground = "MetaBackground";
    String MetaPercussion = "MetaPercussion";
    String MetaBeat = "MetaBeat";
    String MetaSubBeat = "MetaSubBeat";

    // Instr EQ
    String EQActive = "EQActive|v1";
    String EQLow = "EQLow";
    String EQMid = "EQMid";
    String EQHigh = "EQHigh";
    String EQLPFreq = "EQLPFreq";
    String EQHPFreq = "EQHPFreq";

}

class SongToken {
    String Object;
    String ID;
    String Value;
    boolean isValid;

    private int ActArrayPos = 0;
    private float ArrayCoors[] = new float[10]; // upto to 10 Dimensional Arrays

    int Warnings = 0;

    void addArrayCoor(float Coor) {
        if (ActArrayPos < ArrayCoors.length) {
            ArrayCoors[ActArrayPos] = Coor;
            ActArrayPos++;
        } else {
            Warnings++;
        }
    }

    void resetArray() {
        ActArrayPos = 0;
    }

    int getArrayLen() {
        return (ActArrayPos);
    }

    float getArrayCoor(int Index) {
        if (Index <= ActArrayPos) {
            return (ArrayCoors[Index]);
        } else {
            Warnings++;
            return (0);
        }
    }

    String getStringValue() {
        return (Value);
    }

    float getFloatValue() {
        try {
            Number n;
            NumberFormat nf = NumberFormat.getInstance();
            n = nf.parse(Value);
            return n.floatValue();
        } catch (ParseException ex) {
            Logger.getLogger(SongToken.class.getName()).log(Level.SEVERE, null, ex);
            return (0);
        }
    }

    int getIntValue() {
        try {
            Number n;
            NumberFormat nf = NumberFormat.getInstance();
            n = nf.parse(Value);
            return n.intValue();
        } catch (ParseException ex) {
            Logger.getLogger(SongToken.class.getName()).log(Level.SEVERE, null, ex);
            return (0);
        }
    }

    boolean getBoolValue() {
        return (Value.equals(SymTags.TRUE));
    }

    SongToken() {
        resetArray();
        ActArrayPos = 0;
        Warnings = 0;
    }
}


public class SongIO {
    protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodHandles.lookup().lookupClass());
    private boolean ok;
    private File baseFile;
    FileOutputStream basefounbuffered;
    BufferedOutputStream basefo;
    String ObjectID;
    FileInputStream basefi;
    SongToken IOToken = new SongToken();

    byte[] ReadBuffer = new byte[1024];
    int ReadBufferPos = 0;
    boolean EOF = false;
    //String[] TokenArray = new String[20]; // = new String[20];
    //String[] TokenArray2 = new String[20]; // = new String[20];
    char Limiter = 34;
    SymphManager symphManager = new SymphManager();

//    void loadAs(Song MySong) {
//        boolean Sucess = false;
//	File f = new File(SymphManager.getNewModPath());
//	JFC.setCurrentDirectory(f);
//
//	if (JFC.showOpenDialog(JFC) == JFileChooser.APPROVE_OPTION) {
//        try {
//                baseFile = JFC.getSelectedFile();
//                basefi = new FileInputStream(baseFile);
//                if(checkIsSymphonieFormat() && (!this.EOF)) {
//                    Sucess = loadSongInfo(MySong);
//                }
//                basefi.close();
//            } catch (Exception ex) {
//                Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }

//    public Song loadSong(File file) throws IOException {
//        Song song = new Song();
//        boolean Sucess = true;
//        SongPattern MyPattern;
//        Position Pos = null;
//        SongEvent se = new SongEvent();
//        SongEvent myEvent = null;
//        int PatternNr, VoiceIndex;
//        float TimePos;
//        PatternVoice PatVoc;
//        SongEventPool eventPool;
//        int EventCounter;
//        SymphonieInstrument instrument = null;
//        int ActualSampleIndex = 0;
//
//        EventCounter = 0;
//        song.allocDefaultSong();
//
//        baseFile = file;
//        basefi = new FileInputStream(baseFile);
//
//
//        while (EOF == false) {
//            readToken(IOToken);
//            if(!IOToken.isValid) {
//                Sucess = false;
//                break;
//            }
//
//            // Load Song Info
//            if (IOToken.Object.equals(SymTags.SymObjSong)) {
//                if (IOToken.ID.equals(SymTags.BPM)) {
//                    symphManager.PrintToInfoWindow("BPM:" + IOToken.getFloatValue());
//                    symphManager.setBPM(IOToken.getFloatValue());
//                    song.setBpm(IOToken.getFloatValue());
//                }
//                if (IOToken.ID.equals(SymTags.NumbOfSequences)) {
//                    song.allocNumbOfSequences(IOToken.getIntValue());
//                }
//                if (IOToken.ID.equals(SymTags.NumbOfPositions)) {
//                    song.allocNumbOfPositions(IOToken.getIntValue());
//                }
//                if (IOToken.ID.equals(SymTags.NumbOfVoices)) {
//                    song.setNumbOfVoices(IOToken.getIntValue());
//                }
//                if (IOToken.ID.equals(SymTags.NumbOfPatterns)) {
//                    song.allocNumbOfPatterns(IOToken.getIntValue());
//                }
//                if (IOToken.ID.equals(SymTags.NumbOfInstruments)) {
//                    song.allocNumbInstruments(IOToken.getIntValue());
//                }
//                if (IOToken.ID.equals(SymTags.NumbOfRows)) {
//                    song.setNumbOfRows(IOToken.getIntValue());
//                }
//            }
//
//            // Load Sequences
//            if (IOToken.Object.equals(SymTags.SymObjSequence)) {
//            }
//
//            // Load Positions
//            if (IOToken.Object.equals(SymTags.SymObjPosition)) {
//                if (IOToken.ID.equals(SymTags.ArrayPos)) {
//                    Pos = song.getPosition(IOToken.getIntValue());
//                    Pos.setNumbOfLayers(1);
//                    //symphManager.PrintToInfoWindow("Position:"+IOToken.getIntValue());
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.Name)) {
//                    Pos.setName(); = IOToken.getStringValue();
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.StartRow)) {
//                    Pos.setStartRow(IOToken.getIntValue());
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.RowLength)) {
//                    Pos.setRowLength(IOToken.getIntValue());
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.PatternNr)) {
//                    Pos.setSinglePatternNumber(IOToken.getIntValue());
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.NumbOfLayers)) {
//                    Pos.setNumbOfLayers(IOToken.getIntValue());
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.SpeedCycl)) {
//                    Pos.setSpeed_Cycl(IOToken.getIntValue());
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.Tune)) {
//                    Pos.setTune(IOToken.getIntValue());
//                }
//                if (Pos != null && IOToken.ID.equals(SymTags.NumbOfLoops)) {
//                    Pos.setNumbOfLoops(IOToken.getIntValue());
//                }
//
//            }
//
//            // Load Pattern Events
//            if (IOToken.Object.equals(SymTags.SymObjEvent)) {
//                if (IOToken.ID.equals(SymTags.ArrayPos)) {
//                    if (IOToken.getArrayLen() == 3) {
//                        IOToken.resetArray();
//                    }
//
//                    IOToken.addArrayCoor(IOToken.getFloatValue());
//                    if (IOToken.getArrayLen() == 3) {
//                        // allocate Event Position
//                        PatternNr = (int) IOToken.getArrayCoor(0);
//                        VoiceIndex = (int) IOToken.getArrayCoor(1);
//                        TimePos = IOToken.getArrayCoor(2);
//                        MyPattern = song.getPattern((int) IOToken.getArrayCoor(0));
////                        if (MyPattern.checkIsInitialized() == false) {
////                            MyPattern.init(song.getNumbOfVoices());
////                        }
//                        MyPattern.setSongEvent(VoiceIndex, TimePos, se);
//                        PatVoc = MyPattern.getPatternVoice(VoiceIndex);
//                        eventPool = PatVoc.getSongEventPool(TimePos, false);
//                        myEvent = eventPool.getSongEvent(false);
//                        EventCounter++;
//                    }
//                }
//                if (myEvent != null && IOToken.ID.equals(SymTags.FXClass)) {
//                    myEvent.fxClass = IOToken.getIntValue();
//                }
//                if (myEvent != null && IOToken.ID.equals(SymTags.SongFXType)) {
//                    myEvent.songFXType = IOToken.getIntValue();
//                }
//                if (myEvent != null && IOToken.ID.equals(SymTags.A)) {
//                    myEvent.A = IOToken.getIntValue();
//                }
//                if (myEvent != null && IOToken.ID.equals(SymTags.B)) {
//                    myEvent.B = IOToken.getIntValue();
//                }
//                if (myEvent != null && IOToken.ID.equals(SymTags.C)) {
//                    myEvent.C = IOToken.getIntValue();
//                }
//                if (myEvent != null && IOToken.ID.equals(SymTags.D)) {
//                    myEvent.D = IOToken.getIntValue();
//                }
//            }
//
//            // Load Instruments
//            if (IOToken.Object.equals(SymTags.SymObjInstrument)) {
//
//                if (IOToken.ID.equals(SymTags.ArrayPos)) {
//                    int index = IOToken.getIntValue();
//                    instrument = song.getInstrument(ActualSampleIndex++);
//                    instrument.setIndex(index);
//                }
//                if (instrument != null) {
//                    if (IOToken.ID.equals(SymTags.Name)) {
//                        instrument.setName(IOToken.getStringValue());
//                    }
//                    if (IOToken.ID.equals(SymTags.Type)) {
//                        instrument.setLoopType(IOToken.getIntValue());
//                        if (instrument.getLoopType() == 0) {
//                            instrument.deactivateLoop();
//                        }
//                        if (instrument.getLoopType() == 4) {
//                            instrument.setLoopEnabled(true);
//                        }
//                    }
//                    if (IOToken.ID.equals(SymTags.Volume)) {
//                        instrument.Volume = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.ID)) {
//                        instrument.ID = IOToken.getIntValue();
//                        song.setIDOfInstrIndex(instrument.ID, ActualSampleIndex);
//                    }
//                    if (IOToken.ID.equals(SymTags.Tune)) {
//                        instrument.Tune = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.FineTune)) {
//                        instrument.FineTune = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.LoopStart)) {
//                        instrument.setLoopStart(IOToken.getIntValue());
//                    }
//                    if (IOToken.ID.equals(SymTags.Looplen)) {
//                        instrument.setLoopLength(IOToken.getIntValue());
//                    }
//                    if (IOToken.ID.equals(SymTags.NumbOfLoops)) {
//                        instrument.setNumberOfLoops(IOToken.getIntValue());
//                    }
//                    if (IOToken.ID.equals(SymTags.LineSampleFlags)) {
//                        instrument.LineSampleFlags = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.PlayFlag)) {
//                        instrument.PlayFlag = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MultiChannel)) {
//                        instrument.setMultiChannel(MultichannelEnum.Mono);
//                        if(IOToken.getStringValue().equals("StereoL")) instrument.setMultiChannel(MultichannelEnum.StereoL);
//                        if(IOToken.getStringValue().equals("StereoR")) instrument.setMultiChannel(MultichannelEnum.StereoR);
//                        if(IOToken.getStringValue().equals("Virtual")) instrument.setMultiChannel(MultichannelEnum.Virtual);
//                    }
//                    if (IOToken.ID.equals(SymTags.VirtualSample)) {
//                        instrument.virtualSample = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.AllowPosDetune)) {
//                        instrument.AllowPosDetune = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.NoDsp)) {
//                        instrument.dspEnabled = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MirrorX)) {
//                        instrument.isPhaseMirrored = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.PlayReverse)) {
//                        instrument.isReversed = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.PlaySynced)) {
//                        instrument.isStereoSyncPlayed = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.PanningActive)) {
//                        instrument.PanningActive = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.PanningX)) {
//                        instrument.PanningX = IOToken.getIntValue();
//                    }
//
//                    // Meta Info
//                    if (IOToken.ID.equals(SymTags.MetaMainLead)) {
//                        instrument.MetaInfo.MainLead = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MetaLead2)) {
//                        instrument.MetaInfo.Lead2 = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MetaPercussion)) {
//                        instrument.MetaInfo.Percussion = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MetaBeat)) {
//                        instrument.MetaInfo.Beat = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MetaSubBeat)) {
//                        instrument.MetaInfo.SubBeat = IOToken.getBoolValue();
//                    }
//
//                    if (IOToken.ID.equals(SymTags.MetaR)) {
//                        instrument.MetaInfo.R = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MetaG)) {
//                        instrument.MetaInfo.G = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.MetaB)) {
//                        instrument.MetaInfo.B = IOToken.getIntValue();
//                    }
//
//                    // EQ
//                    if (IOToken.ID.equals(SymTags.EQActive)) {
//                        instrument.EQActive = IOToken.getBoolValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.EQLow)) {
//                        instrument.EQLowGain = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.EQMid)) {
//                        instrument.EQMidGain = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.EQHigh)) {
//                        instrument.EQHighGain = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.EQHPFreq)) {
//                        instrument.EQLPFrequency = IOToken.getIntValue();
//                    }
//                    if (IOToken.ID.equals(SymTags.EQLPFreq)) {
//                        instrument.EQHPFrequency = IOToken.getIntValue();
//                    }
//                }
//            }
//
//            // Load Instrument Samples
//            if (IOToken.Object.equals(SymTags.SymObjSamples)) {
//
//                if (IOToken.ID.equals(SymTags.ArrayPos)) {
//                    ActualSampleIndex = IOToken.getIntValue();
//                    instrument = song.getInstrument(ActualSampleIndex);
//                }
//                if (instrument != null) {
//                    if (IOToken.ID.equals(SymTags.RawDataBlockLen)) {
//                        try {
//                            int SampleLenByte = IOToken.getIntValue();
//                            byte[] Samples = new byte[SampleLenByte];
//                            basefi.read(Samples);
//                            //instrument.setName(file.getPath());
//                            loadSample(instrument, Samples, Samples.length, ActualSampleIndex, song);
//                            song.getInstrument(ActualSampleIndex).setHasContent(true);
//                            symphManager.PrintToInfoWindow("Sample imported(" + ActualSampleIndex + "):"
//                                    + Samples.length + " byte");
//                        } catch (IOException ex) {
//                            Sucess = false;
//                            EOF = true;
//                            symphManager.PrintToInfoWindow("Error while reading Sample Data. Aborted.");
//                            Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }
//        }
//        basefi.close();
//        symphManager.PrintToInfoWindow("Number of Events:" + EventCounter);
//        if(Sucess) song.setContentLoaded(true);
//        return song;
//    }

    public void loadSample(final SymphonieInstrument instrument, final byte[] src, final int len, final int ActualSampleIndex, final Song MySong) {
        instrument.getOldSampleImporter().analyseAndImport(instrument, src, len, false);
        instrument.setSampleChannel(instrument.getOldSampleImporter().sampleChannels[0]);
        instrument.getSampleChannel().initLoopData(instrument.isLoopEnabled(), instrument.getLoopStart(), instrument.getLoopLength(), instrument.getNumberOfLoops());
        instrument.setHasContent(true);
        if (instrument.getMultiChannel().equals(MultichannelEnum.StereoL)) {
            if (instrument.getOldSampleImporter().isStereo()) {
                SymphonieInstrument siRight = MySong.getInstrument(ActualSampleIndex + 1);
                siRight.setSampleChannel(instrument.getOldSampleImporter().sampleChannels[1]);
                siRight.getSampleChannel().initLoopData(instrument.isLoopEnabled(), instrument.getLoopStart(), instrument.getLoopLength(), instrument.getNumberOfLoops());
                siRight.setHasContent(true);
                siRight.setMultiChannel(MultichannelEnum.StereoR);
                siRight.setName(instrument.getName());
            } else {
                // error Stereo Sample not correctly recognized
            }
        }
    }

    public void loadNewSample(final SymphonieInstrument instrument, final byte[] src, final int len, final int ActualSampleIndex, final Song MySong, boolean javeImporterEnabled) {
        OldSampleImporter importer = instrument.getSampleImporter();
        importer.analyseAndImport(instrument, src, len, javeImporterEnabled);
        instrument.setSampleChannel(importer.sampleChannels[0]);
        instrument.getSampleChannel().initLoopData(instrument.isLoopEnabled(), instrument.getLoopStart(), instrument.getLoopLength(), instrument.getNumberOfLoops());
        instrument.setHasContent(true);
        if(importer.sampleChannels.length>1) {
            instrument.setMultiChannel(MultichannelEnum.StereoL);
        } else {
            instrument.setMultiChannel(MultichannelEnum.Mono);
        }
        instrument.setSampleResolution(importer.getSampleResolutionBit());

       if (instrument.getMultiChannel().equals(MultichannelEnum.StereoL)) {
            if (instrument.getOldSampleImporter().isStereo()) {
                SymphonieInstrument siRight = MySong.getInstrument(ActualSampleIndex + 1);
                siRight.setSampleChannel(instrument.getOldSampleImporter().sampleChannels[1]);
                siRight.getSampleChannel().initLoopData(instrument.isLoopEnabled(), instrument.getLoopStart(), instrument.getLoopLength(), instrument.getNumberOfLoops());
                siRight.setHasContent(true);
                siRight.setMultiChannel(MultichannelEnum.StereoR);
                siRight.setName(instrument.getName());
                siRight.setSampleResolution(importer.getSampleResolutionBit());
                siRight.setSampleImporter(instrument.getSampleImporter());
                siRight.setIndex(instrument.getIndex()+1);
            } else {
                // error Stereo Sample not correctly recognized
            }
        }
    }

    boolean checkIsSymphonieFormat() {
        EOF = false;
        readToken(IOToken);
        if (IOToken.Object.equals("Content") && IOToken.ID.equals(SymTags.Content)) {
            return (true);
        }
        return (false);
    }

    void readToken(SongToken myToken) {
        String s = readNextLine();
        myToken.isValid = false;
        String[] TokenArray = s.split("" + Limiter, 10);
        if (TokenArray.length >= 5) {
            if (TokenArray[0].equals("{") &&
                    TokenArray[2].equals(":") &&
                    TokenArray[4].equals("}")) {
                s = TokenArray[1];
                myToken.Value = TokenArray[3];
                int pos = TokenArray[1].indexOf(".");
                if (pos > 0) {
                    myToken.Object = s.substring(0, pos);
                    myToken.ID = s.substring(pos + 1);
                    myToken.isValid = true;
                }
            }
        }
    }

    String readNextLine() {
        String s = "";
        boolean done = false;
        int MaxReadLen = 1024;
        int len, Count = 0;
        ReadBufferPos = 0;
        try {
            while (done == false) {
                len = basefi.read();
                if (len == -1) {
                    done = true;
                    EOF = true;
                } else {
                    if (len == 13) {
                        done = true;
                    } else {
                        byte b = (byte) len;
                        s += (char) b;
                        Count++;
                        if (Count >= MaxReadLen) done = true;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
            done = true;
            EOF = true;
        }
        return (s);
    }


//    public void save(Song song, File file) {
//        try {
//            File testxml = new File(file.getPath()+".xml");
//            StringWriter sw = new StringWriter();
//            JAXB.marshal(song, testxml);
//            String xmlString = sw.toString();
//            //ZipDirectory("z:/SymMods/modtocompress");
//            file.delete();
//            if (file.createNewFile() == true) {
//                basefounbuffered = new FileOutputStream(file);
//                basefo = new BufferedOutputStream(basefounbuffered);
//                ok = saveSongInfo(song);
//                ok = saveSongSequences(song);
//                ok = saveSongPositions(song);
//                ok = saveInstruments(song);
//                ok = savePatterns(song);
//                ok = saveInstrumentsSamples(song);
//                basefo.close();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }


    public void exportSample(Song song, File file, int instrumentIndex) {
        logger.debug("exportSample() "+ file.getPath());
        try {
            if (file.createNewFile() == true) {
                basefounbuffered = new FileOutputStream(file);
                basefo = new BufferedOutputStream(basefounbuffered);
                ok = saveSample(song, instrumentIndex);
                basefo.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    boolean saveSongInfo(Song MySong) throws IOException {
//        ObjectID = "Content";
//        writeKey(SymTags.Content, 2.0f);
//        // ----------------------------------------------
//        writeObjectBegin(SymTags.SymObjSong);
//        writeKey(SymTags.Mixfrequency, MySong.MixFrequency);
//        writeKey(SymTags.BPM, MySong.getBpm());
//        writeKey(SymTags.MasterVolume, MySong.Volume);
//        writeKey(SymTags.NumbOfSequences, MySong.getNumbOfSequences());
//        writeKey(SymTags.NumbOfInstruments, MySong.getNumbOfInstruments());
//        writeKey(SymTags.NumbOfPatterns, MySong.getNumberOfPatterns());
//        writeKey(SymTags.NumbOfPositions, MySong.getNumbOfPositions());
//        writeKey(SymTags.NumbOfVoices, MySong.getNumbOfVoices());
//        writeKey(SymTags.PatternSize, MySong.getPatternSize());
//        writeKey(SymTags.FlexibleTiming, false);
//        writeKey(SymTags.EventsPerEventPool, 1);
//        writeKey(SymTags.EventsPerEventPool, 1);
//        writeKey(SymTags.NumbOfRows, MySong.getNumbOfRows());
//        writeObjectEnd();
//        return (true);
//    }
//
//    boolean saveSongSequences(Song MySong) throws IOException {
//        Sequence Obj;
//        writeObjectBegin(SymTags.SymObjSequence);
//        writeKey(SymTags.ArrayNames, "SequenceIndex");
//        for (int i = 0; i < MySong.getNumbOfSequences(); i++) {
//            Obj = MySong.getSequence(i);
//            writeKey(SymTags.ArrayPos, i);
//            writeKey(SymTags.Action, Obj.Action.toString());
//            writeKey(SymTags.StartPosition, Obj.startPositionIndex);
//            writeKey(SymTags.EndPosition, Obj.EndPosition);
//            writeKey(SymTags.Name, Obj.Name);
//            writeKey(SymTags.NumbOfLoops, Obj.NumbOfLoops);
//            writeKey(SymTags.Tune, Obj.Tune);
//            writeKey(SymTags.ArrayEnd, 0);
//        }
//        writeObjectEnd();
//        return (true);
//    }
//
//    boolean saveSongPositions(Song MySong) throws IOException {
//        Position Obj;
//        writeObjectBegin(SymTags.SymObjPosition);
//        writeKey(SymTags.ArrayNames, "PositionIndex");
//        for (int i = 0; i < MySong.getNumbOfPositions(); i++) {
//            Obj = MySong.getPosition(i);
//            writeKey(SymTags.ArrayPos, i);
//            writeKey(SymTags.Name, Obj.Name);
//            writeKey(SymTags.StartRow, Obj.StartRow);
//            writeKey(SymTags.NumbOfLayers, Obj.NumbOfLayers);
//            if (Obj.PatternNumbers != null) {
//                writeKey(SymTags.PatternNr, Obj.PatternNumbers[0]);
//            }
//            writeKey(SymTags.NumbOfLoops, Obj.NumbOfLoops);
//            writeKey(SymTags.RowLength, Obj.RowLength);
//            writeKey(SymTags.SpeedCycl, Obj.getSpeed_Cycl());
//            writeKey(SymTags.Tune, Obj.Tune);
//            writeKey(SymTags.ArrayEnd, 0);
//        }
//        writeObjectEnd();
//        return (true);
//    }
//
//    boolean saveInstruments(Song MySong) {
//        writeObjectBegin(SymTags.SymObjInstrument);
//        writeKey(SymTags.ArrayNames, "InstrumentIndex");
//        for (SymphonieInstrument Obj: MySong.getInstrumentsAsList()) {
//           // if (SymphonieInstrument Obj:MySong.getInstrumentsAsList()) { //geht nicht richtig
//                writeKey(SymTags.ArrayPos, Obj.getIndex());
//                writeKey(SymTags.Name, Obj.getName());
//                writeKey(SymTags.Type, Obj.getLoopType());
//                writeKey(SymTags.Volume, Obj.Volume);
//                writeKey(SymTags.ID, Obj.ID);
//                writeKey(SymTags.Tune, Obj.Tune);
//                writeKey(SymTags.FineTune, Obj.FineTune);
//                writeKey(SymTags.LoopStart, Obj.getLoopStart());
//                writeKey(SymTags.Looplen, Obj.getLoopLength());
//                writeKey(SymTags.NumbOfLoops, Obj.getNumberOfLoops());
//                writeKey(SymTags.LineSampleFlags, Obj.LineSampleFlags);
//                writeKey(SymTags.PlayFlag, Obj.PlayFlag);
//
//                writeKey(SymTags.MultiChannel, Obj.multiChannel.toString());
//
//                // Flags
//                writeKey(SymTags.VirtualSample, Obj.virtualSample);
//                writeKey(SymTags.AllowPosDetune, Obj.AllowPosDetune);
//                writeKey(SymTags.NoDsp, Obj.dspEnabled);
//                writeKey(SymTags.MirrorX, Obj.isPhaseMirrored);
//                writeKey(SymTags.PlayReverse, Obj.isReversed);
//                writeKey(SymTags.PlaySynced, Obj.isStereoSyncPlayed);
//
//                // Panning
//                writeKey(SymTags.PanningActive, Obj.PanningActive);
//                writeKey(SymTags.PanningX, Obj.PanningX);
//
//                // Meta Info
//                writeKey(SymTags.MetaR, Obj.MetaInfo.R);
//                writeKey(SymTags.MetaG, Obj.MetaInfo.G);
//                writeKey(SymTags.MetaB, Obj.MetaInfo.B);
//                writeKey(SymTags.MetaMainLead, Obj.MetaInfo.MainLead);
//                writeKey(SymTags.MetaLead2, Obj.MetaInfo.Lead2);
//                writeKey(SymTags.MetaPercussion, Obj.MetaInfo.Percussion);
//                writeKey(SymTags.MetaBeat, Obj.MetaInfo.Beat);
//                writeKey(SymTags.MetaSubBeat, Obj.MetaInfo.SubBeat);
//
//                // EQ
//                writeKey(SymTags.EQActive, Obj.EQActive);
//                writeKey(SymTags.EQLow, Obj.EQLowGain);
//                writeKey(SymTags.EQMid, Obj.EQMidGain);
//                writeKey(SymTags.EQHigh, Obj.EQHighGain);
//                writeKey(SymTags.EQLPFreq, Obj.EQLPFrequency);
//                writeKey(SymTags.EQHPFreq, Obj.EQHPFrequency);
//
//
//                // END OF ARRAY Instrument
//                writeKey(SymTags.ArrayEnd, 0);
//            //}
//        }
//        writeObjectEnd();
//        return (true);
//    }
//
//
//    boolean saveInstrumentsSamples(Song MySong) throws IOException {
//        SymphonieInstrument Obj;
//        writeObjectBegin(SymTags.SymObjSamples);
//        writeKey(SymTags.ArrayNames, "InstrumentIndex");
//        for (int i = 0; i < MySong.getNumbOfInstruments(); i++) {
//            Obj = MySong.getInstrument(i);
//            if (Obj.OldSampleImporter.RawSample != null) {
//                writeKey(SymTags.ArrayPos, i);
//                writeKey(SymTags.ID, Obj.ID);
//                writeRawByteBlock(Obj.OldSampleImporter.RawSample);
//                writeKey(SymTags.ArrayEnd, 0);
//            }
//        }
//        writeObjectEnd();
//        return (true);
//    }
//
    boolean saveSample(Song song, int instrumentIndex) throws IOException {
        SymphonieInstrument instrument = song.getInstrument(instrumentIndex);
        writeRawbytes(instrument.getOldSampleImporter().RawSample);
        return (true);
    }
//
//
//    boolean savePatterns(Song MySong) throws IOException {
//        SongPattern Obj;
//        writeObjectBegin(SymTags.SymObjEvent);
//        writeKey(SymTags.ArrayNames, "PatternNr,VoiceNr,PosTime");
//        for (int i = 0; i < MySong.getNumberOfPatterns(); i++) {
//            Obj = MySong.getPattern(i);
//
//            for (int PatVocNr = 0; PatVocNr < Obj.getNumbOfVoices(); PatVocNr++) {
//                for (int RowNr = 0; RowNr < Obj.getNumberOfRows(); RowNr++) {
//                    savePatternsEventPool(MySong, Obj, i, PatVocNr, RowNr);
//                }
//            }
//        }
//        writeObjectEnd();
//        return (true);
//    }
//
//    boolean savePatternsEventPool(Song song, SongPattern songPattern, int PatNr, int PatVocNr, float RowNr) {
//        PatternVoice myPatternVoices = songPattern.getPatternVoice(PatVocNr);
//        SongEventPool se;
//        SongEvent mySongEvent;
//        if (myPatternVoices != null) {
//            se = myPatternVoices.getSongEventPool(RowNr, false);
//            if (se != null) {
//                mySongEvent = se.getSongEvent(false);
//                if ((mySongEvent != null) && (mySongEvent.songFXType != SongEventType.FX_NONE)) {
//                    writeKey(SymTags.ArrayPos, PatNr);
//                    writeKey(SymTags.ArrayPos, PatVocNr);
//                    writeKey(SymTags.ArrayPos, RowNr);
//                    writeKey(SymTags.FXClass, mySongEvent.fxClass);
//                    writeKey(SymTags.SongFXType, mySongEvent.songFXType);
//                    writeKey(SymTags.A, mySongEvent.A);
//                    writeKey(SymTags.B, mySongEvent.B);
//                    writeKey(SymTags.C, mySongEvent.C);
//                    writeKey(SymTags.D, mySongEvent.D);
//                    writeKey(SymTags.ArrayEnd, 0);
//                }
//            }
//        }
//        return (true);
//    }

    boolean writeObjectBegin(String myObjectID) {
        writeBeginKey();
        ObjectID = myObjectID;
        writeString2(ObjectID + "." + SymTags.ObjectBegin);
        writeEndKey();
        return (true);
    }

    boolean writeObjectEnd() {
        writeBeginKey();
        writeString2(ObjectID + "." + SymTags.ObjectEnd);
        writeEndKey();
        return (true);
    }

    boolean writeKey(String skey, boolean bValue) {
        if (bValue == true) {
            writeKey(skey, "@TRUE");
        } else {
            writeKey(skey, "@FALSE");
        }
        return (true);
    }

    boolean writeKey(String skey, int sValue) {
        writeKey(skey, "" + ((float) sValue));
        return (true);
    }

    boolean writeKey(String skey, float sValue) {
        writeKey(skey, "" + sValue);
        return (true);
    }

    boolean writeKey(String skey, double sValue) {
        writeKey(skey, "" + sValue);
        return (true);
    }

    boolean writeKey(String skey, String sValue) {
        writeBeginKey();
        writeString2(ObjectID + "." + skey);
        writeString(":");
        writeString2(sValue);
        writeEndKey();
        return (true);
    }

    boolean writeBeginKey() {
        writeString("{");
        return (true);
    }

    boolean writeEndKey() {
        writeString("}");
        writeEOL();
        return (true);
    }

    boolean writeEOL() {
        return (writeByte(13));
    }

    boolean writeByte(int b) {
        try {
            basefo.write(b);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
            return (false);
        }
    }

    boolean writeRawByteBlock(byte[] ByteArray) {
        try {
            writeKey(SymTags.RawDataBlockLen, ByteArray.length);
            basefo.write(ByteArray);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
            return (false);
        }
    }

    boolean writeRawbytes(byte[] bytes) {
        try {
            if(bytes == null) return false;
            basefo.write(bytes);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
            return (false);
        }
    }

    boolean writeString2(String s) {
        writeByte(34);
        writeString(s);
        writeByte(34);
        return (true);
    }

    boolean writeString(String s) {
        if (s != null) {
            for (int i = 0; i < s.length(); i++) {
                try {
                    basefo.write((byte) s.charAt(i));
                } catch (IOException ex) {
                    Logger.getLogger(SongIO.class.getName()).log(Level.SEVERE, null, ex);
                    return (false);
                }
            }
        }
        return (true);
    }
}    
