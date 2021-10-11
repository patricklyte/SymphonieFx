/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package symreader;

import ch.meng.symphoniefx.TimeMeasure;
import ch.meng.symphoniefx.song.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

interface SymphonieSequence {
    int SEQUENCE_STARTPOS = 0;
    int SEQUENCE_LENGTH = 2;
    int SEQUENCE_LOOP = 4;    //;1 = 1xLOOPEN
    int SEQUENCE_INFO = 6;    //;0=PLAY 1=SKIP -1=LAST OF SONG
    int SEQUENCE_TUNE = 8;    //;SEQ TRANSPOSE HALBTONES
    int SEQUENCE_SIZEOF = 16;
    int SEQINFO_PLAY = 0;
    int SEQINFO_SKIP = 1;
    int SEQINFO_ENDSONG = -1;
}

interface SymphonieNote { // Offsets to Event
    int NOTE_FX = 0;
    int NOTE_PITCH = 1;
    int NOTE_VOLUME = 2;
    int NOTE_INSTR = 3;
}





interface SymphonieNotePitch {
    int NOTEPITCH_NONOTE = -1;
    int NOTEPITCH_MIN = 0;
    int NOTEPITCH_OCTAVE = 12;
    int NOTEPITCH_MAX = (NOTEPITCH_OCTAVE * 7);
}

// Patterneditor Handling
class SymphSongEvent {
    int Type;
    int ParaA;
    int ParaB;
    int ParaC;

    SymphSongEvent() {
        Type = 0;
        ParaA = 0;
        ParaB = 0;
        ParaC = 0;
    }
};

public class OldSymModFormatImporter {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    public static final String SYMPHONIE_HEADER_ID = "SymM";
    public static final String LOAD_SAMPLES = "LoadSamples";
    public static final String LOAD_DATA_BLOCK = "LoadDataBlock";
    public static final String SONG_DATA_BLOCK = "BuildSongData";
    private Song song = new Song();
    double BPMScaleFactor = 100.0 / 67;

    // Temp Vars for file reading
    private int actualSampleIndex = 0;

    // Temp Vars, often used
    private SymphSongEvent songEvent = new SymphSongEvent();


    // Info Displaying
    private void addInstrumentList(String s) {
        print(s);
    }

    private void addPositionList(String s) {
    }

    private void PrintToInfoWindow(String s) {
        print(s);
    }

    private void PrintToInfoWindow(String s, int i) {
        s = s + i;
        PrintToInfoWindow(s);
    }

    private void PrintToInfoWindow(String s, int i, String s2) {
        s = s + i + " " + s2;
        PrintToInfoWindow(s);
    }

    // Info Displaying
    private List<String> loadingMessages = new Vector<>();
    private void print(final String text) {
        logger.debug(text);
        loadingMessages.add(text);
    }

    public List<String> getLoadingMessages() {
        return loadingMessages;
    }

    private void PrintByteBlockAsString(byte[] SrcByteBlock, int Len) {
        String s = "";
        char c;
        int i = 0;
        int counter = 0;
        boolean CR;
        for (i = 0; i < Len; i++) {
            CR = false;

            c = (char) SrcByteBlock[i];
            if (SrcByteBlock[i] == 13) CR = true;
            if (SrcByteBlock[i] == 10) CR = true;
            if (SrcByteBlock[i] > 31) s = s + c;
            counter++;
            if ((counter > 256) || CR) {
                counter = 0;
                PrintToInfoWindow("        " + s);
                s = "";
            }
        }
        PrintToInfoWindow(s);
    }

    // File and memory i/o
    private String ReadID(FileInputStream fi) {
        String hunkId = "";
        int databyte = 0;
        try {
            databyte = fi.read();
            hunkId = hunkId + (char) databyte;
            databyte = fi.read();
            hunkId = hunkId + (char) databyte;
            databyte = fi.read();
            hunkId = hunkId + (char) databyte;
            databyte = fi.read();
            hunkId = hunkId + (char) databyte;
        } catch (java.io.IOException e) {
            print("ReadID():Error while reading file. (" + hunkId + ")");
        }
        return (hunkId);
    }

    long ReadLong(FileInputStream fi) {
        int databyte = 0;
        int longValue = 0;

        try {
            databyte = fi.read();
            databyte = databyte & 0x000000ff;
            longValue = databyte;
            databyte = fi.read();
            databyte = databyte & 0x000000ff;
            longValue = (256 * longValue) + databyte;
            databyte = fi.read();
            databyte = databyte & 0x000000ff;
            longValue = (256 * longValue) + databyte;
            databyte = fi.read();
            databyte = databyte & 0x000000ff;
            longValue = (256 * longValue) + databyte;
        } catch (java.io.IOException e) {
            print("ReadLong():Error while reading file.");
        }
        return (longValue);
    }

    int ReadInt(FileInputStream fi) {
        int databyte;
        try {
            fi.read();
            fi.read();
            fi.read();
            databyte = fi.read();
        } catch (java.io.IOException e) {
            print("ReadInt():Error while reading file.");
            return 10000; // illegal Event Id
        }
        if (databyte < 128) return databyte;
        return databyte - 256;
    }

    int ReadMemoryShortToInt(byte[] bytes, int offset) {
        return getIntOfShort(bytes, offset);
    }

    int getIntOfShort(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes).getShort(offset);
    }

    int getLong(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes).getInt(offset);
    }

    // convert -128..+127 to 0..255
    int byteToInt(byte tempbyte) {
        if (tempbyte < 0) return ((int) tempbyte) + 256;
        return tempbyte;
    }

    private void loadPattern(byte[] Src, int Len) {
        int SrcEventLen = 4;
        int PatternSize = song.getNumbOfRows() * song.getNumbOfVoices() * SrcEventLen;
        int NewNumbOfPatterns = Len / PatternSize;
        PrintToInfoWindow("Loading Pattern. Total Length:", Len);
        if (Len > 0) {
            timer.start(SONG_DATA_BLOCK);
            if (NewNumbOfPatterns > 0) song.allocNumbOfPatterns(NewNumbOfPatterns);
            importPatterns(Src, Len);
            timer.sum(SONG_DATA_BLOCK);
            PrintToInfoWindow("Pattern Import Done.");
        }
    }

    SongEvent BuildSongEvent(final byte[] Src, final int PatternIndex, final int VoiceIndex, final int rowIndex) {
        int w = song.getNumbOfVoices();
        int h = song.getNumbOfRows();
        int SrcEventLen = 4;
        int PatternSize = w * h * SrcEventLen;
        int BasePtr = (PatternSize * PatternIndex) +
                (rowIndex * w * SrcEventLen) +
                (VoiceIndex * SrcEventLen);
        SongEvent event = OldSymModFormatHelpers.ConvertEvent(Src[BasePtr + SymphonieNote.NOTE_FX],
                Src[BasePtr + SymphonieNote.NOTE_INSTR],
                Src[BasePtr + SymphonieNote.NOTE_PITCH],
                Src[BasePtr + SymphonieNote.NOTE_VOLUME]);
        if(event.getSongFXType() != SongEventType.FX_NONE) {
//            String rawEvent = " RAWEVENT:" + Src[BasePtr + SymphonieNote.NOTE_FX]
//                    + " i" +  Src[BasePtr + SymphonieNote.NOTE_INSTR]
//                    + " p" +  Src[BasePtr + SymphonieNote.NOTE_PITCH]
//                    + " v" +  Src[BasePtr + SymphonieNote.NOTE_VOLUME];
//            logger.debug("Pattern:" + PatternIndex + " Voice:" + VoiceIndex + " Row:"+ rowIndex + " " + event.getShortDescription() + " " + rawEvent) ;
//
        }
        return event;
 }

    void importPatterns(final byte[] Src, int len) {
        for (int PatternIndex = 0; PatternIndex < song.getNumberOfPatterns(); PatternIndex++) {
            SongPattern myPat = song.getPattern(PatternIndex);
            for (int VoiceIndex = 0; VoiceIndex < song.getNumbOfVoices(); VoiceIndex++) {
                for (int LineNr = 0; LineNr < song.getNumbOfRows(); LineNr++) {
                    SongEvent event = BuildSongEvent(Src, PatternIndex, VoiceIndex, LineNr);
                    String text = "x" + VoiceIndex + ",y" + LineNr + ",z" + PatternIndex;
                    event.setKey(text);
                    myPat.setSongEvent(VoiceIndex, LineNr, event);
                    len--;
                    if(len < 0) {
                        break;
                    }
                }
            }
        }
    }

    // Hunk Handling
    private String GetElementName(final int ElementId) {
        String elementText = "Unknown Element ID";
        switch (ElementId) {
            case -1:
                elementText = "Number of Audiochannels";
                break;
            case -2:
                elementText = "Tracklength";
                break;
            case -3:
                elementText = "Total Patternlength in Events";
                break;
            case -4:
                elementText = "Number of Instruments";
                break;
            case -5:
                elementText = "Length of one Songevent in bytes";
                break;
            case -6:
                elementText = "System BPM";
                break;
            case -7:
                elementText = "Is a pure Song without Samples included (Flag)";
                break;
            case -10:
                elementText = "Songdata";
                break; // Positions ?
            case -11:
                elementText = "Sample (Binary)";
                break;
            case -12:
                elementText = "";
                break; //s = "Emptysample (no data following)";break;
            case -13:
                elementText = "Notedata (Songevents)";
                break;
            case -14:
                elementText = "Samplenames (Instrumentdefinitions)";
                break;
            case -15:
                elementText = "Sequence";
                break;
            case -16:
                elementText = "Infotext";
                break;
            case -17:
                elementText = "Sample (Binary Deltapacked)";
                break;
            case -18:
                elementText = "Sample (Binary Deltapacked as 16 Bit)";
                break;
            case -19:
                elementText = "Infotype";
                break;
            case -20:
                elementText = "Infoobject (Binary)";
                break;
            case -21:
                elementText = "Infostring";
                break;
            case 10:
                elementText = "NG Sampleboost activated (Global Samplevolume)";
                break;
            case 11:
                elementText = "Stereo Enhancer, Pitchdiff";
                break;
            case 12:
                elementText = "Stereo Enhancer, Samplediff";
                break;
        }
        return (elementText);
    }

    private boolean isElementWithSingleParameter(final int ElementId) {
        if ((ElementId <= -1) && (ElementId >= -7)) return true;
        if ((ElementId <= 12) && (ElementId >= 10)) return true;
        return false;
    }

    // Instrument loading
    private void MoveNextNonVirtualSample() {
        boolean done = false;
        do {
            actualSampleIndex++;
            if (actualSampleIndex >= song.getNumbOfInstruments()) {
                done = true;
            } else {
                SymphonieInstrument si = song.getInstrument(actualSampleIndex, false);
                if (si != null) {
                    if (!(si.getMultiChannel().equals(MultichannelEnum.Virtual) || si.getMultiChannel().equals(MultichannelEnum.StereoR)))
                        done = true;
                }
            }
        } while (done == false);
    }

    private void LoadInstrumentNamesBlock(final byte[] SrcByteBlock, final long Len) {
        int NumbOfInstruments;
        int i, j;
        String s;
        char c;
        int SrcOffset = 0;
        int ActualInstrNr = 0;

        NumbOfInstruments = (int) Len / 256;
        assert (NumbOfInstruments != 0);
        song.allocNumbInstruments(NumbOfInstruments);
        for (i = 0; i < NumbOfInstruments; i++) {
            s = "";
            c = (char) SrcByteBlock[SrcOffset];
            j = 0;

            while (c != 0) {
                s = s.concat(String.valueOf(c));
                j++;
                c = (char) SrcByteBlock[SrcOffset + j];
                if (j > 127) c = 0;
            }

            if (s.equals("ViRT")) {
                s = "* ViRTual";
                addInstrumentList(s);
                LoadInstrumentDefBlock(SrcByteBlock, i, SrcOffset, ActualInstrNr, s, true);
                ActualInstrNr++;
            } else {
                if (!s.isEmpty()) {
                    addInstrumentList(s);
                    LoadInstrumentDefBlock(SrcByteBlock, i, SrcOffset, ActualInstrNr, s, false);
                    ActualInstrNr++;
                }
            }
            SrcOffset += 256;
        }
    }

    /*
SAMPLENAME_INSTRTYPE	EQU	128	;0=No Instr
INSTRTYPE_SILENT	EQU	-8
INSTRTYPE_KILL	EQU	-4
INSTRTYPE_NONE	EQU	0
INSTRTYPE_LOOP	EQU	4
INSTRTYPE_SUST	EQU	8
*/
    //MULTISAMPLE_MONO	EQU	0
    //MULTISAMPLE_STEREOL	EQU	1
    //MULTISAMPLE_STEREOR	EQU	2
    //MULTISAMPLE_LINESRC	EQU	3

    // Volume
    //SAMPLENAME_VFADESTATUS	EQU	170
    //SAMPLENAME_VFADEBGN	EQU	SAMPLENAME_VFADESTATUS+1
    //SAMPLENAME_VFADEEND	EQU	SAMPLENAME_VFADEBGN+1
    private void LoadInstrumentDefBlock(final byte[] byteBlock, final int id,
                                        final int byteOffset, final int instrIndex, final String s, final boolean isVirtual) {
        SymphonieInstrument instrument;
        instrument = song.getInstrument(instrIndex);
        instrument.setName(s);
        instrument.setVirtualSample(isVirtual);
        instrument.setID(id);
        instrument.setIndex(instrIndex);
        song.setIDOfInstrIndex(instrument.getID(), instrIndex);
        instrument.setLoopType(byteToInt(byteBlock[byteOffset + 128]));// 0=Normal, 4 = Looped
        int multiChannel = byteToInt(byteBlock[byteOffset + 132]); //0=MONO
        switch (multiChannel) {
            case (MultichannelType.StereoL):
                instrument.setMultiChannel(MultichannelEnum.StereoL);
                break;
            case (MultichannelType.StereoR):
                instrument.setMultiChannel(MultichannelEnum.StereoR);
                break;
            case (MultichannelType.Virtual):
                instrument.setMultiChannel(MultichannelEnum.Virtual);
                break;
            default:
                instrument.setMultiChannel(MultichannelEnum.Mono);
                break;
        }
        instrument.setVolume(byteToInt(byteBlock[byteOffset + 134]));
        if (2 == byteToInt(byteBlock[byteOffset + 170])) { // 0=off, 2= from to fade
            instrument.setFadeFromVolume(0.01f * ((float) byteToInt(byteBlock[byteOffset + 171])));
            instrument.setFadeToVolume(0.01f * ((float) byteToInt(byteBlock[byteOffset + 172])));
        }
        if (instrument.getVolume() == 0) instrument.setVolume(100); // default to 100%
        if (instrument.getVolume() > 100) {
            logger.debug("Original Volume:" + instrument.getVolume());
            instrument.setCompressorLevel(0.01f * (instrument.getVolume() - 100));
            instrument.setVolume(100);
            logger.debug("Compressor:" + instrument.getCompressorLevel());
        }

        // Tune
        instrument.setTune(byteBlock[byteOffset + 139]);
        if (byteBlock[byteOffset + 143] != 0) { // SAMPLENAME_DOWNSAMPLE	EQU	143	;0=NONE
            instrument.setDownsampleSteps(byteBlock[byteOffset + 143]);
            // Correct Pitch because of Downsampling, this was used to have better quality as the realtime mixing
            // not really did interpolate the samples because of lack of cpu power
            instrument.setTune(instrument.getTune() + getDownsamplingTuneCorrection(instrument.getDownsampleSteps()));
        }

        instrument.setFineTune(byteBlock[byteOffset + 138]);

        // Looping
        // Temporary set loop values, which will be converted to Sampleindex later on
        instrument.setLoopStart(byteToInt(byteBlock[byteOffset + 129]) * 256 * 256); // 100% * 256 * 256
        instrument.setLoopLength(byteToInt(byteBlock[byteOffset + 130]) * 256 * 256);
        instrument.setNumberOfLoops(byteToInt(byteBlock[byteOffset + 131]));
        instrument.setNewLoopSystem((instrument.getLineSampleFlags() & 16) != 0);
        if (instrument.isNewLoopSystem() == true) {
            instrument.setLoopStart((byteToInt(byteBlock[byteOffset + 129]) * 256 * 256)
                    + (byteToInt(byteBlock[byteOffset + 150]) * 256)
                    + byteToInt(byteBlock[byteOffset + 151]));
            instrument.setLoopLength((byteToInt(byteBlock[byteOffset + 130]) * 256 * 256) +
                    +(byteToInt(byteBlock[byteOffset + 152]) * 256)
                    + byteToInt(byteBlock[byteOffset + 153]));
        }
        if (instrument.getLoopType() == 0) {
            instrument.deactivateLoop();
        }
        if (instrument.getLoopType() == 4) {
            instrument.setLoopEnabled(true);
        }
        if (instrument.getLoopType() == 8) { // sustained
            instrument.setLoopEnabled(true);
            logger.debug("Sustained loop");
        }

        // Playflags
        instrument.setPlayFlag(byteToInt(byteBlock[byteOffset + 142]) & 0xff);
        instrument.setAllowPosDetune((instrument.getPlayFlag() & 1) == 0);
        instrument.setDspEnabled((instrument.getPlayFlag() & 2) == 0);
        instrument.setStereoSyncPlayed((instrument.getPlayFlag() & 4) != 0);
        //SAMPLENAME_PLAYFLAG	EQU	142	;0=NRM
        //SPLAYFLAG_DODETUNE	EQU	0	;BIT 0 SET:DO POS TUNE OFFSET
        //SPLAYFLAG_NODSP		EQU	1	;BIT 1 SET:DRY ROOM
        //SPLAYFLAG_SUPERFAST	EQU	2	;BIT 2 SET:SYNC PLAY (MONO-> 2xMONO, STEREO->SYNCED)
        // SAMPLENAME_PLAYFLAG

        // Linesample Flags
//        SPLAYFLAG_DODETUNE	EQU	0	;BIT 0 SET:DO POS TUNE OFFSET
//        SPLAYFLAG_NODSP		EQU	1	;BIT 1 SET:DRY ROOM
//        SPLAYFLAG_SUPERFAST	EQU	2	;BIT 2 SET:SYNC PLAY (MONO-> 2xMONO, STEREO->SYNCED)
//
//
//
//        MULTISAMPLE_MONO	EQU	0
//        MULTISAMPLE_STEREOL	EQU	1
//        MULTISAMPLE_STEREOR	EQU	2
//        MULTISAMPLE_LINESRC	EQU	3

//        LSFLAGS_RVS		EQU	0	;BIT 0: reverse sample
//        LSFLAGS_ASQUEUE		EQU	1	;BIT 1: interprete as list of samples (no mix)
//        LSFLAGS_MIRROR		EQU	2	;BIT 2: mirror x axis sample
//        LSFLAGS_16BIT		EQU	3	;BIT 3: for deltapack16
//        LSFLAGS_NEWLOOP		EQU	4	;New LoopSystem
//        LSFLAGS_NODSP		EQU	5	;None Dsp
        instrument.setLineSampleFlags(byteToInt(byteBlock[byteOffset + 140]) & 0xff);
        instrument.setReversed((instrument.getLineSampleFlags() & 1) != 0);
        instrument.setVirtualQueueMix((instrument.getLineSampleFlags() & 2) != 0);
        if (isVirtual) {
            //                LINENAMEID		EQU	"ViRT"	;only visual info, not to be used for real ID !!!
//
//                ;LINENAMEDUR		EQU	"DUR "
//                ;LINENAMEMOLL		EQU	"MOLL "
//
//
//                BUILDINFO_ESIZEOF	EQU	4
//
//                LINENAME_ID		EQU	0   "ViRT"
//                LINENAME_ZERO		EQU	4
//                LINENAME_VERSION	EQU	6	;unused
//                LINENAME_MIXINFO	EQU	8	;unused
//                LINENAME_EOS		EQU	12	;NULL
//                LINENAME_SRCNUMB	EQU	14
//                LINENAME_HEADLEN	EQU	16
//                LINENAME_SRCDEFLEN	EQU	18
//                LINEHEAD_SIZEOF	EQU	20
//
//
//                LINESRCDEF_NPITCH	EQU	0
//                LINESRCDEF_NINSTRUMENT	EQU	2
//                LINESRCDEF_NVOLUME	EQU	3
//                LINESRCDEF_SIZEOF	EQU	4
//
//                LOOPSTEPFAK_INIT	EQU	3

            List<Byte> bytes = getBytesRange(byteBlock, byteOffset, 128);
            int lineSourceDefPtr = getWord(bytes, 16);
            int virtualMixSteps = getWord(bytes, 14);
            for (int step = 0; step < virtualMixSteps; step++) {
                VirtualMixStep virtualMixStep = new VirtualMixStep();
                int instrumentId = getByte(bytes, lineSourceDefPtr + 3);
                virtualMixStep.setMixInstrumentId(instrumentId);
                virtualMixStep.setMixPitch(getWord(bytes, lineSourceDefPtr));
                virtualMixStep.setMixVolume(getByte(bytes, lineSourceDefPtr + 2));
                instrument.getVirtualMixSteps().add(virtualMixStep);
                lineSourceDefPtr += 4;
            }
            if (instrument.isVirtualMix()
                    && 2 == instrument.getVirtualMixSteps().size()
                    && instrument.getVirtualMixSteps().get(0).equals(instrument.getVirtualMixSteps().get(1))) {
                instrument.getVirtualMixSteps().remove(1); // remove one if mix of two identical sources
            }
        }

        instrument.setPhaseMirrored((instrument.getLineSampleFlags() & 4) != 0);
        if ((instrument.getLineSampleFlags() & 8) != 0) {
            instrument.setSampleResolution(16);
        } else {
            instrument.setSampleResolution(8);
        }
        instrument.setNewLoopSystem((instrument.getLineSampleFlags() & 16) != 0);
        //si.NoDsp = (si.LineSampleFlags & 32) != 0;
        //SAMPLENAME_RESOFILTERFLAGS EQU	160	;4x LP od HP : 5=1 activate and LP, 1=activate and HP
        //SAMPLENAME_RESOFILTERNUMB EQU	161	;Anzahl Punkte : 1=static, 2=filtersweep
        //SAMPLENAME_RESOFILTER	EQU	162	;bis 170 : 162 Freq1, 163 Reso1, 164 Freq2, 165 Reso2
        instrument.setResoFilterSweepType(byteToInt(byteBlock[byteOffset + 160]) & 0xff);
        if(instrument.getResoFilterSweepType()!=0) {
            instrument.setResoFilterSteps(byteToInt(byteBlock[byteOffset + 161]) & 0xff);
            if(instrument.getResoFilterSteps()>2) logger.error("Resofilter Steps > 2:" +instrument.getResoFilterSteps());
            instrument.setResoFilterSweepStartFrequency(byteToInt(byteBlock[byteOffset + 162]));
            instrument.setResoFilterSweepStartResonance(byteToInt(byteBlock[byteOffset + 163]));
            instrument.setResoFilterSweepEndFrequency(byteToInt(byteBlock[byteOffset + 164]));
            instrument.setResoFilterSweepEndResonance(byteToInt(byteBlock[byteOffset + 165]));
        }
        instrument.setHasContent(true);
        StringWriter sw = new StringWriter();
        //JAXB.marshal(instrument, sw);
        String xmlString = sw.toString();
        logger.debug(xmlString);
    }

    private int getDownsamplingTuneCorrection(final int downSampleSteps) {
        int tuneCorrection = -(downSampleSteps * 12);
        if (tuneCorrection >= 0) tuneCorrection = 0;
        if (tuneCorrection < -60) tuneCorrection = -60;
        return tuneCorrection;
    }

    private int getWord(final List<Byte> bytes, final int offset) {
        return bytes.get(offset) * 256 + bytes.get(offset + 1);
    }

    private int getByte(final List<Byte> bytes, final int offset) {
        return bytes.get(offset);
    }

    private List<Byte> getBytesRange(final byte[] srcBytes, final int startIndex, int length) {
        if (startIndex + length > srcBytes.length) length = srcBytes.length - startIndex;
        if (length <= 0) return new ArrayList<>();

        final List<Byte> list = new ArrayList<>();
        for (int i = startIndex; i < startIndex + length; i++) {
            list.add(srcBytes[i]);
        }
        return list;
    }

    // Sample Import
    // Attach Sampledata to Instrument
    private void loadInstrumentSample(final byte[] src, final long len) {
        int i;
        i = actualSampleIndex;
        buildInstrumentSamples(song.getInstrument(i), src, (int) len);
        song.getInstrument(i).setHasContent(true);
    }

    private void buildInstrumentSamples(final SymphonieInstrument instrument, final byte[] src, final int len) {
        int LoopStart, LoopLen;
        instrument.getOldSampleImporter().analyseAndImport(instrument, src, len, false);
        instrument.setSamplePool(instrument.getOldSampleImporter().sampleChannels[0]);
        LoopStart = instrument.getLoopStart();
        LoopLen = instrument.getLoopLength();
        instrument.getSampleChannel().initLoopDateSymphonieFormat(instrument, instrument.isLoopEnabled(), instrument.getLoopStart(), instrument.getLoopLength(), instrument.getNumberOfLoops());
        if (instrument.getMultiChannel().equals(MultichannelEnum.StereoL)) {
            if (instrument.getOldSampleImporter().isStereo()) {
                SymphonieInstrument siRight = song.getInstrument(actualSampleIndex + 1);
                siRight.setSamplePool(instrument.getOldSampleImporter().sampleChannels[1]);
                siRight.getSampleChannel().initLoopDateSymphonieFormat(instrument, instrument.isLoopEnabled(), LoopStart, LoopLen, instrument.getNumberOfLoops());
                siRight.setHasContent(true);
                siRight.setMultiChannel(MultichannelEnum.StereoR);
                siRight.setSampleResolution(instrument.getSampleResolution());
                siRight.setName(instrument.getName());
                siRight.setLoopEnabled(instrument.isLoopEnabled());
                siRight.setSampleImporter(instrument.getSampleImporter());
                siRight.setLoopLength(instrument.getLoopLength());
                siRight.setLoopStart(instrument.getLoopStart());
            } else {
                logger.error("Stereo sample not correctly recognized");
                // error Stereo Sample not correctly recognized
            }
        }
    }

    private void DecodeSampleDeltapacked(final byte[] Src, final int Len) {
        int i;
        int Index;
        long ActByte;
        long AddByte;
        byte tempByte;

        Index = 0;
        ActByte = (long) Src[Index];
        ActByte = ActByte & 0x000000ff;
        for (i = 1; i < Len; i++) {
            AddByte = (long) Src[Index + 1];
            ActByte += AddByte;
            ActByte = ActByte & 0x000000ff;

            if (ActByte > 255) ActByte -= 256;

            tempByte = (byte) ActByte;
            Src[Index + 1] = tempByte;
            Index++;
        }
        i = 1;
    }

    private void DecodeSampleDeltapacked(final byte[] Src, final int Len, final int StartOffset) {
        int i;
        int Index;
        long ActByte;
        long AddByte;
        byte tempByte;

        Index = StartOffset;
        ActByte = (long) Src[Index];
        ActByte = ActByte & 0x000000ff;
        for (i = 1; i < Len; i++) {
            AddByte = (long) Src[Index + 1];
            ActByte += AddByte;
            ActByte = ActByte & 0x000000ff;

            if (ActByte > 255) ActByte -= 256;

            tempByte = (byte) ActByte;
            Src[Index + 1] = tempByte;
            Index++;
        }
        i = 1;
    }

    private void DecodeSampleDeltapacked16(final byte[] Src, final int Len) {
        int i, j;
        int TempSrcOffset = 0;
        int SrcOffsetMSB = 0;   // Vorderen 8 Bit
        int SrcOffsetLSB = 0;   // Hinteren 8 Bit
        int DestIndex = 0;
        int NumbOfBlocks;
        int BlockSize = 4096;
        byte[] DestTempBlock = new byte[BlockSize];

        if (Len > BlockSize) {
            NumbOfBlocks = Len / (BlockSize);
            SrcOffsetMSB = (BlockSize / 2);
            SrcOffsetLSB = 0;

            for (j = 0; j < (NumbOfBlocks); j++) {
                DestIndex = 0;
                TempSrcOffset = SrcOffsetLSB;
                DecodeSampleDeltapacked(Src, BlockSize, SrcOffsetLSB);

                // Reshuffle Bytes
                for (i = 0; i < (BlockSize / 2); i++) {
                    byte tempbyte;
                    tempbyte = Src[SrcOffsetMSB++];
                    DestTempBlock[DestIndex++] = tempbyte;
                    DestTempBlock[DestIndex++] = Src[SrcOffsetLSB++];
                }
                DestIndex = 0;
                // Copyback
                for (i = 0; i < (BlockSize); i++) {
                    Src[TempSrcOffset++] = DestTempBlock[DestIndex++];
                }
                SrcOffsetMSB = TempSrcOffset + (BlockSize / 2);
                SrcOffsetLSB = TempSrcOffset;
            }
        }
    }

    private boolean CheckIsRLEPacked(final byte[] SrcByteBlock, final long Len) {
        boolean isRLEPacked = true;
        // Look for "PACK" following a -1 as doublebyte
        if (SrcByteBlock[0] != 80) isRLEPacked = false;
        if (SrcByteBlock[1] != 65) isRLEPacked = false;
        if (SrcByteBlock[2] != 67) isRLEPacked = false;
        if (SrcByteBlock[3] != 75) isRLEPacked = false;
        if (SrcByteBlock[4] != -1) isRLEPacked = false;
        if (SrcByteBlock[5] != -1) isRLEPacked = false;
        if (Len <= 16) isRLEPacked = false;
        return (isRLEPacked);
    }

    private void decodeByteBlockRLE(byte[] SrcByteBlock, final long Len, final int DataBlockID) {
        long unpackedLen;
        boolean done = false;
        byte packtype;
        int blocklen;
        int SrcOffset = 0, DestOffset = 0;
        int i;
        unpackedLen = Len;
        if (CheckIsRLEPacked(SrcByteBlock, Len) == true) {
            unpackedLen = getLong(SrcByteBlock, 6);
            if (unpackedLen != 0) {
                byte[] DestByteBlock = new byte[(int) unpackedLen];
                SrcOffset = 10;
                //decodedToDest = true;
                while (!done) {
                    packtype = SrcByteBlock[SrcOffset];
                    SrcOffset++;
                    switch (packtype) {
                        case 0:  // 1:1 copy of upto 255 bytes
                            blocklen = byteToInt(SrcByteBlock[SrcOffset]);
                            SrcOffset++;
                            for (i = 0; i < blocklen; i++) {
                                DestByteBlock[DestOffset] = SrcByteBlock[SrcOffset];
                                DestOffset++;
                                SrcOffset++;
                            }
                            break;

                        case 3:  // up to 255 bytes 0 bytes (zero bytes)
                            blocklen = byteToInt(SrcByteBlock[SrcOffset]);
                            SrcOffset++;
                            for (i = 0; i < blocklen; i++) {
                                DestByteBlock[DestOffset] = 0;
                                DestOffset++;
                            }
                            break;

                        case 2:  // two identical long s to unpack
                            DestByteBlock[DestOffset] = SrcByteBlock[SrcOffset];
                            DestByteBlock[DestOffset + 1] = SrcByteBlock[SrcOffset + 1];
                            DestByteBlock[DestOffset + 2] = SrcByteBlock[SrcOffset + 2];
                            DestByteBlock[DestOffset + 3] = SrcByteBlock[SrcOffset + 3];
                            DestOffset += 4;
                            DestByteBlock[DestOffset] = SrcByteBlock[SrcOffset];
                            DestByteBlock[DestOffset + 1] = SrcByteBlock[SrcOffset + 1];
                            DestByteBlock[DestOffset + 2] = SrcByteBlock[SrcOffset + 2];
                            DestByteBlock[DestOffset + 3] = SrcByteBlock[SrcOffset + 3];
                            DestOffset += 4;
                            SrcOffset += 4;
                            break;

                        case 1:  // 1:1 copy of upto 255 long s
                            blocklen = byteToInt(SrcByteBlock[SrcOffset]);
                            SrcOffset++;
                            for (i = 0; i < blocklen; i++) {
                                DestByteBlock[DestOffset] = SrcByteBlock[SrcOffset];
                                DestByteBlock[DestOffset + 1] = SrcByteBlock[SrcOffset + 1];
                                DestByteBlock[DestOffset + 2] = SrcByteBlock[SrcOffset + 2];
                                DestByteBlock[DestOffset + 3] = SrcByteBlock[SrcOffset + 3];
                                DestOffset += 4;
                            }
                            SrcOffset += 4;
                            break;

                        case -1:
                            done = true;
                            break;

                    }

                    // end of block
                    if (SrcOffset >= Len) {
                        done = true;
                    }

                    // check for illegal packing type
                    if (packtype > 3) {
                        done = true;
                    }

                    // check for illegal packing type
                    if (packtype < -1) {
                        done = true;

                    }
                }

                // Copy Back to Source Block
                SrcByteBlock = new byte[(int) unpackedLen];
                for (i = 0; i < unpackedLen; i++) {
                    SrcByteBlock[i] = DestByteBlock[i];
                }
                PrintToInfoWindow("RLE packed. Unpacked size in Byte:", (int) unpackedLen);
            }
        }
        LoadDataBlock(SrcByteBlock, unpackedLen, DataBlockID);
    }

    void CopyByteBlock(final byte[] src, final byte[] dest, final int len) {
        for (int i = 0; i < len; i++) {
            dest[i] = src[i];
        }
    }

    private byte[] tempPatternData;
    private TimeMeasure timer = new TimeMeasure();
    private void LoadDataBlock(final byte[] SrcByteBlock, final long Len, final int DataBlockID) {
        timer.start(LOAD_DATA_BLOCK);
        if (Len > 0) {
            switch (DataBlockID) {
                case -14:
                    LoadInstrumentNamesBlock(SrcByteBlock, Len);
                    break;
                case -11:
                    timer.start(LOAD_SAMPLES);
                    loadInstrumentSample(SrcByteBlock, Len);
                    MoveNextNonVirtualSample();
                    timer.sum(LOAD_SAMPLES);
                    break;
                case -13:
                    tempPatternData = new byte[(int) Len];
                    CopyByteBlock(SrcByteBlock, tempPatternData, (int) Len);
                    break;
                case -17: // Deltapacked 8 Bit
                    timer.start(LOAD_SAMPLES);
                    DecodeSampleDeltapacked(SrcByteBlock, (int) Len);
                    loadInstrumentSample(SrcByteBlock, Len);
                    MoveNextNonVirtualSample();
                    timer.sum(LOAD_SAMPLES);
                    break;
                case -18: // Deltapacked 16 Bit
                    timer.start(LOAD_SAMPLES);
                    DecodeSampleDeltapacked16(SrcByteBlock, (int) Len);
                    loadInstrumentSample(SrcByteBlock, Len);
                    MoveNextNonVirtualSample();
                    timer.sum(LOAD_SAMPLES);
                    break;
                case -15: // Sequence
                    importSequence(SrcByteBlock, (int) Len);
                    break;
                case -10: // Songdata
                    importPositions(SrcByteBlock, (int) Len);
                    break;
            }
        }
        timer.sum(LOAD_DATA_BLOCK);
    }

    void importSequence(final byte[] src, final long Len) {
        int of;
        int NumbOfElements = ((int) Len / SymphonieSequence.SEQUENCE_SIZEOF);
        song.allocNumbOfSequences(NumbOfElements);
        if ((Len > 0) && (src != null) && (NumbOfElements > 0)) {
            if (NumbOfElements > song.getNumbOfSequences()) NumbOfElements = song.getNumbOfSequences();
            int highestSequencePlay = -1;
            for (int i = 0; i < NumbOfElements; i++) {
                of = SymphonieSequence.SEQUENCE_SIZEOF * i;
                Sequence Seq = song.getSequence(i);
                Seq.setStartPositionIndex(ReadMemoryShortToInt(src, of + SymphonieSequence.SEQUENCE_STARTPOS));
                Seq.setEndPosition(ReadMemoryShortToInt(src, of + SymphonieSequence.SEQUENCE_LENGTH));
                Seq.setEndPosition(Seq.getEndPosition() - Seq.getStartPositionIndex());
                int sequenceAction = getIntOfShort(src, of + SymphonieSequence.SEQUENCE_INFO);
                switch (sequenceAction) {
                    case SequenceAction.EndOfSong:
                        Seq.setAction(SequenceActionEnum.EndOfSong);
                        break;
                    case SequenceAction.Skip:
                        Seq.setAction(SequenceActionEnum.Skip);
                        break;
                    default: {
                        Seq.setAction(SequenceActionEnum.Play);
                    }
                }
                Seq.setTune(getIntOfShort(src, of + SymphonieSequence.SEQUENCE_TUNE));
                Seq.setNumbOfLoops(ReadMemoryShortToInt(src, of + SymphonieSequence.SEQUENCE_LOOP));
                if (0 == Seq.getNumbOfLoops()) Seq.setAction(SequenceActionEnum.Skip);
                if (Seq.getAction().equals(SequenceActionEnum.Play)) highestSequencePlay = i;
            }
            if (highestSequencePlay >= 0 && highestSequencePlay < 63) {
                song.getSequence(highestSequencePlay + 1).setAction(SequenceActionEnum.EndOfSong);
            }
        } else {
            print("Error:Importing Sequence.");
        }
    }

    void importPositions(final byte[] src, final long Len) {
        int of;
        int NumbOfElements = ((int) Len / SymphoniePosition.POSITION_SIZEOF);
        song.allocNumbOfPositions(NumbOfElements);
        if ((Len > 0) && (src != null) && (NumbOfElements > 0)) {
            print("Number of Positions:" + NumbOfElements);
            if (NumbOfElements > song.getNumbOfPositions()) NumbOfElements = song.getNumbOfPositions();
            for (int i = 0; i < NumbOfElements; i++) {
                of = SymphoniePosition.POSITION_SIZEOF * i;
                Position Pos = song.getPosition(i);
                Pos.setNumbOfLayers(1);
                Pos.setSinglePatternNumber(ReadMemoryShortToInt(src, of + SymphoniePosition.POSITION_PATNUM));
                Pos.setTune(getIntOfShort(src, of + SymphoniePosition.POSITION_TUNE));
                Pos.setStartRow(ReadMemoryShortToInt(src, of + SymphoniePosition.POSITION_STARTPOINT));
                Pos.setRowLength(ReadMemoryShortToInt(src, of + SymphoniePosition.POSITION_LEN));
                Pos.setNumbOfLoops(ReadMemoryShortToInt(src, of + SymphoniePosition.POSITION_LOOPNUMB));
                Pos.setSpeed_Cycl(ReadMemoryShortToInt(src, of + SymphoniePosition.POSITION_SPEED));
                addPositionList("" + i + ". Pat:" + Pos.getPatternNumber()
                        + " Spd:" + Pos.getSpeed_Cycl()
                        + " Loops:" + Pos.getNumbOfLoops()
                        + " LineNr:" + Pos.getStartRow()
                        + " Len:" + Pos.getRowLength()
                        + " Tune:" + Pos.getTune()
                );
            }
        } else {
            print("Error:Importing Positions.");
        }
    }

    private void LoadDataValue(final int dataBlockID, final int value) {
        switch (dataBlockID) {
            case -2:
                song.setNumbOfRows(value); // Trachklength
                break;
            case -1:
                song.setNumbOfVoices(value); // Number of Channels
                break;
            case -6: {
                song.setBpm(value * BPMScaleFactor); // BMP
            }
            break;
        }
    }


    final static long MAX_ELEMENTS = 1000;

    private VirtualSampleBuilder virtualSampleBuilder = new VirtualSampleBuilder();

    public Song load(final File file) {
        timer.start("Total");
        song.setName(file.getPath());
        song.initPlayingPosition();
        String s = "";
        FileInputStream inputStream;
        long length = 0;
        int DataBlockID = 0;
        boolean done = false;

        long ElementCounter = 0;
        long FileLength = 0;
        long ReadLength = 0;
        String sDataBlockID;
        byte[] SrcByteBlock;
        actualSampleIndex = 0;
        print("Loading SymMod " + file.getPath());
        print(file.getPath());
        try {
            inputStream = new FileInputStream(file);
            s = ReadID(inputStream);
            print(s);
            if (s.equals(SYMPHONIE_HEADER_ID)) {
                print(">> Symphonie Modul Header ID detected.");
            } else {
                done = true;
                print(">> Aborted. No Symphonie Modul Header detected.");
                if (s.equals("XPKF")) {
                    print(">> Possibly XPK packed File. Unpack this file and try again.");
                }
            }
            length = ReadLong(inputStream);
            PrintToInfoWindow(">> Format Version:", (int) length); // should always be 1
            FileLength = file.length();
            PrintToInfoWindow(">> Filelength in Byte:", (int) FileLength);

            ReadLength += 8;
            while (!done) {
                // Abort if too many Elements -> no symphonie Module
                ElementCounter++;
                if (ElementCounter >= MAX_ELEMENTS) {
                    done = true;
                    print(">> Aborted Reading: Possibly no Symphonie Module Format.");
                }

                // Read Element ID
                DataBlockID = ReadInt(inputStream);
                ReadLength += 4;
                sDataBlockID = GetElementName(DataBlockID);
                if (sDataBlockID != "") PrintToInfoWindow(">> Element ID:", (int) DataBlockID, sDataBlockID);

                // if id is not -12, it is a element with data following
                if (DataBlockID != -12) {
                    if (isElementWithSingleParameter(DataBlockID)) {
                        length = ReadLong(inputStream);
                        if ((DataBlockID == -4) || (DataBlockID == -5)) {
                            length = length & 0x0000ffff;
                        }
                        LoadDataValue(DataBlockID, (int) length);
                        PrintToInfoWindow("Size:" + length);// ?
                        ReadLength += 4;
                    } else {
                        length = ReadLong(inputStream);
                        ReadLength += 4;
                        PrintToInfoWindow(" Binary Datalength in Byte:", (int) length);
                        try {
                            SrcByteBlock = new byte[(int) length];
                            int numberread = inputStream.read(SrcByteBlock, 0, (int) length);
                            decodeByteBlockRLE(SrcByteBlock, length, DataBlockID);
                            if (DataBlockID == -16) {
                                PrintByteBlockAsString(SrcByteBlock, (int) length);
                            }
                            ReadLength += length;
                        } catch (java.io.IOException ex) {
                            print("IOException:While reading datablock." + ex);
                            print(ex.getStackTrace().toString());
                        } catch (Exception ex) {
                            print("Exception:While reading." + ex);
                            print(ex.getStackTrace().toString());
                        }
                        //done = true;
                    }
                } else {
                    //SymphManager.ActualSampleIndex++;
                }
                if (ReadLength >= (FileLength)) {
                    done = true;
                    print(">> Finished. End of File reached.");
                    song.setContentLoaded(true);
                }
            }
            if (tempPatternData != null) loadPattern(tempPatternData, tempPatternData.length);
            virtualSampleBuilder.buildVirtualSamples(song);

        } catch (java.io.FileNotFoundException ex) {
            print("File not Found..." + file.getAbsolutePath());
            print(ex.getStackTrace().toString());
        }

        timer.stop("Total");
        StringBuilder text = new StringBuilder();
        text.append("Total loading time:").append(timer.getSumString(LOAD_DATA_BLOCK));
        text.append("\nSamples time:").append(timer.getSumString(LOAD_SAMPLES));
        text.append("\nBuild Song Data time:").append(timer.getSumString(SONG_DATA_BLOCK));
        text.append("\nTotal time:").append(timer.getDiffString("Total"));
        print(text.toString());
        return song;
    }


}
