/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package symreader;

import ch.meng.symphoniefx.SampleImporterWav;
import ch.meng.symphoniefx.song.SymphonieInstrument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;

public class OldSampleImporter {

    protected static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodHandles.lookup().lookupClass());

    private float BaseFrequency = 0; // What Note Pitch has been sampled
    private float SampledFrequency = 0; // At which Frequency the sample has been recorded
    private int numbOfSamples = 0;
    private int sampleStartOffsetByte = 0; // in Samples
    private int sampleLengthByte = 0; // in Byte
    private int SampleResolutionBit = 0;
    private float maxSampleAmplitude = 0;
    private boolean isMSBFirst = false;
    private boolean isInterleaved = false;
    private boolean isUnsigned = true;
    boolean FormatRecognized = false;
    int Format;
    float FormatVersion;
    String format;
    private String formatShort = "n/a";
    private String fileSuffix = ".sample";

    // Sample Pools
    private int numberOfChannels = 0; // 1 = Mono, 2 = Stereo
    private boolean SamplePoolsReady = false; // true = Samples Pools filled and ready to read
    private float[] SamplePoolSrc;
    SampleChannel[] sampleChannels;
    byte[] RawSample;   // Quickfix for Export Raw Sample
    int RawSampleLen;   // Quickfix for Export Raw Sample

    public boolean isMono() {
        return 1 == sampleChannels.length;
    }

    public boolean isStereo() {
        return 2 == sampleChannels.length;
    }

    public int getNumberOfChannels() {
        return sampleChannels.length;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public float getBaseFrequency() {
        return BaseFrequency;
    }

    public float getSampledFrequency() {
        return SampledFrequency;
    }

    public int getSampleResolutionBit() {
        return SampleResolutionBit;
    }

    public boolean isFormatRecognized() {
        return FormatRecognized;
    }

    public int getFormat() {
        return Format;
    }

    public float getFormatVersion() {
        return FormatVersion;
    }

    public String getFormatShort() {
        return formatShort;
    }

    public SampleChannel[] getSamplePools() {
        return sampleChannels;
    }

    public SampleChannel getSamplePool() {
        return sampleChannels[0];
    }

    public SampleChannel getSamplePoolRight() {
        return sampleChannels[1];
    }

    boolean isJavaInternalFormat = false;

    public String getDescription() {
        StringBuilder text = new StringBuilder();
        text.append(SampleResolutionBit + "Bit ");
        text.append(" isMSBFirst:" + isMSBFirst);
        text.append(" isInterleaved:" + isInterleaved);
        text.append(" Channels:" + numberOfChannels);
        text.append(" SampledFrequency:" + SampledFrequency);
        text.append(" Format:" + format);
        text.append(" Bytes:" + sampleLengthByte);
        text.append(" Bit:" + SampleResolutionBit);
        return text.toString();
    }

    SampleImporterJavaInternal javaSampleImporter = new SampleImporterJavaInternal();

    public boolean analyseAndImport(SymphonieInstrument instrument, byte[] src, int len, boolean javeImporterEnabled) {
        logger.debug("analyseAndImport:" + instrument.getShortDescription());
        logger.debug("bytes raw:" + len);
        try {
            FormatRecognized = false;
            SamplePoolsReady = false;
            isJavaInternalFormat = false;
            numberOfChannels = 0;
            if (!FormatRecognized) FormatRecognized = AnalyseIFF(src, len);
            if (!FormatRecognized) FormatRecognized = AnalyseAIF(src, len);
            if (!FormatRecognized) FormatRecognized = AnalyseMAESTRO(src, len);
            if (!FormatRecognized) FormatRecognized = AnalyseWAV(src, len);
            if (!FormatRecognized) FormatRecognized = AnalyseSYMPHEXPORT16BT(src, len);
            if (javaSampleImporter.AnalyseJAVANative(src, len)) {
                if(javaSampleImporter.getSamples() != null) {
                    logger.debug("javaSampleImporter recognized " + javaSampleImporter.getDescription());
                }
                if (javeImporterEnabled && javaSampleImporter.getSamples() != null) {
                    src = javaSampleImporter.getSamples();
                    len = src.length;
                    sampleLengthByte = src.length;
                    sampleStartOffsetByte = 0;
                    logger.debug("SampleImporterJavaInternal JavaImporter:Recognized as");
                    logger.debug(javaSampleImporter.getDescription());
                } else {
                    logger.debug("javaSampleImporter skipped");
                }
            }
            if (FormatRecognized == false) FormatRecognized = AnalyseRAW(src, len);
            if (FormatRecognized) {
                logger.debug("Recognized as " + getDescription());
            }
            makeRawBackup(src, len);
            copyToSamplePoolSrc(src, len);
            buildSamplePools(instrument.isReversed());
            if (sampleChannels != null && sampleChannels[0] != null) {
                instrument.setBpm(instrument.calcSampleBpm(this.SampledFrequency, sampleChannels[0].getNumbOfSamples()));
            }
        } catch(Exception exception) {
            exception.printStackTrace();
            logger.error(exception);
        }
        return (FormatRecognized);
    }

    void makeRawBackup(byte[] src, int len) {
        RawSample = src;//.clone();
        RawSampleLen = len;
    }

    int getSampleLen(int i) {
        if ((SamplePoolsReady == true) && (i < numberOfChannels)) {
            return (sampleChannels[i].getNumbOfSamples());
        } else {
            return (0);
        }
    }

    SampleChannel getSamplePool(int i) {
        if ((SamplePoolsReady == true) && (i < numberOfChannels)) {
            return (sampleChannels[i]);
        } else {
            return (null);
        }
    }

    private int IntBytesToInt(int MSB, int LSB) {
        LSB = LSB & 0x00ff;
        MSB = MSB & 0x00ff;
        MSB = (MSB * 256);
        MSB = MSB + LSB;
        if (MSB >= (128 * 256)) MSB = (-256 * 256) + MSB;
        return (MSB);
    }

    private void copyToSamplePoolSrc(byte[] src, int len) {
        SamplePoolSrc = new float[len];
        assert (SamplePoolSrc == null);
        convertBinaryToFloatSample(src, SamplePoolSrc);
    }

    private void buildSamplePools(boolean isReversed) {
        if (numberOfChannels > 0) {
            sampleChannels = new SampleChannel[numberOfChannels];
            for (int i = 0; i < numberOfChannels; i++) {
                sampleChannels[i] = new SampleChannel();
                copyToSamplePool(isReversed, i);
            }
            SamplePoolsReady = true;
        }
    }

    private void copyToSamplePool(boolean isReversed, int SamplePoolIndex) {
        int StartOffset;
        int LenOneSamplePoolSamples = numbOfSamples / numberOfChannels;
        sampleChannels[SamplePoolIndex].init(LenOneSamplePoolSamples);
        sampleChannels[SamplePoolIndex].setNumbOfSamples(LenOneSamplePoolSamples);
        if (isInterleaved == false) {
            StartOffset = (LenOneSamplePoolSamples * SamplePoolIndex);
            for (int i = 0; i < LenOneSamplePoolSamples; i++) {
                sampleChannels[SamplePoolIndex].getSamples()[i] = SamplePoolSrc[i + StartOffset] / maxSampleAmplitude;
            }
        } else {
            StartOffset = SamplePoolIndex;
            for (int i = 0; i < LenOneSamplePoolSamples; i++) {
                sampleChannels[SamplePoolIndex].getSamples()[i] = SamplePoolSrc[StartOffset + (i * numberOfChannels)] / maxSampleAmplitude;
            }
        }
        if (isReversed) sampleChannels[SamplePoolIndex].reverseSample();
    }

    private void convertBinaryToFloatSample(byte[] src, float[] dest) {
        float SampleFactor;
        int Sample_LEN = 2;
        float sample;
        if (FormatRecognized && (sampleLengthByte > 0)) {
            if (SampleResolutionBit == 8) {
                buildSample8Bit(src, dest, Sample_LEN);
            }
            if (SampleResolutionBit == 16) {
                buildSample16Bit(src, dest, Sample_LEN);
            }
        }
    }

    private void buildSample8Bit(byte[] src, float[] dest, float Sample_LEN) {
        float sample;
        float SampleFactor;
        numbOfSamples = sampleLengthByte;
        SampleFactor = Sample_LEN / 256.0f;
        for (int i = 0; i < numbOfSamples; i++) {
            if (isUnsigned == false) {
                sample = (float) src[i + sampleStartOffsetByte];
            } else {
                byte b = src[i + sampleStartOffsetByte];
                if (b >= 0) sample = (float) b - 128;
                else sample = (float) (256 + b) - 128;
            }
            sample = (sample * SampleFactor);
            maxSampleAmplitude = Math.max(maxSampleAmplitude, Math.abs(sample));
            dest[i] = sample;
        }
    }

    private void buildSample16Bit(byte[] src, float[] dest, float Sample_LEN) {
        float sample;
        float SampleFactor;
        int a, b;
        SampleFactor = Sample_LEN / (256 * 256);
        numbOfSamples = sampleLengthByte / 2;
        logger.debug("number of samples:"+ numbOfSamples);
        if (isMSBFirst == false) {
            for (int i = 0; i < numbOfSamples; i++) {
                b = src[sampleStartOffsetByte + (i * 2)];
                a = (src[sampleStartOffsetByte + (i * 2) + 1]);
                sample = SampleFactor * ((float) (IntBytesToInt(a, b)));
                maxSampleAmplitude = Math.max(maxSampleAmplitude, Math.abs(sample));
                dest[i] = sample;
            }
        } else {
            for (int i = 0; i < numbOfSamples; i++) {
                int Offset = sampleStartOffsetByte + (i * 2);
                a = src[Offset];
                b = src[Offset + 1];
                sample = SampleFactor * ((float) (IntBytesToInt(a, b)));
                maxSampleAmplitude = Math.max(maxSampleAmplitude, Math.abs(sample));
                dest[i] = sample;
            }
        }
    }

    private void InitInfo(int len) {
        BaseFrequency = 440f;
        SampledFrequency = 44100f;
        SampleResolutionBit = 8;
        sampleStartOffsetByte = 0;
        sampleLengthByte = len;
        FormatVersion = 1.0f;
        isInterleaved = false;
        isMSBFirst = false;
        format = "";
        numberOfChannels = 1;
    }

    private boolean FindStringAtPos(byte[] src, int len, String s, int pos) {
        boolean found = true;
        int i = 0;
        for (i = 0; i < s.length(); i++) {
            if (s.charAt(i) != src[i + pos]) found = false;
        }
        return (found);
    }

    private int FindString(byte[] src, int len, String s, int maxoffset) {
        int foundpos = -1;
        for (int i = 0; i < maxoffset; i++) {
            if (FindStringAtPos(src, len, s, i) == true) {
                foundpos = i;
            }
        }
        return (foundpos);
    }

    private int ReadBinaryIntMSB(byte[] src, int pos) { // 4 Bytes
        int BinInt;
        int a, b, c, d;
        d = src[pos] & 0xff;
        c = src[pos + 1] & 0xff;
        b = src[pos + 2] & 0xff;
        a = src[pos + 3] & 0xff;
        BinInt = (d * 256 * 256 * 256) + (c * 256 * 256) + (b * 256) + a;
        return (BinInt);
    }

    private int ReadBinaryInt(byte[] src, int pos) { // 4 Bytes
        int BinInt;
        int a, b, c, d;
        a = src[pos] & 0xff;
        b = src[pos + 1] & 0xff;
        c = src[pos + 2] & 0xff;
        d = src[pos + 3] & 0xff;
        BinInt = (d * 256 * 256 * 256) + (c * 256 * 256) + (b * 256) + a;
        return (BinInt);
    }

    private int read32BitMSBAsInt(byte[] src, int pos) { // 4 Bytes
        int BinInt;
        int a, b, c, d;
        a = src[pos] & 0xff;
        b = src[pos + 1] & 0xff;
        c = src[pos + 2] & 0xff;
        d = src[pos + 3] & 0xff;
        BinInt = (a * 256 * 256 * 256) + (b * 256 * 256) + (c * 256) + d;
        return (BinInt);
    }

    private int ReadBinaryShort(byte[] src, int pos) { // 2 Bytes
        int BinInt;
        int a, b;
        a = src[pos] & 0xff;
        b = src[pos + 1] & 0xff;
        BinInt = (b * 256) + a;
        return (BinInt);
    }

    // +16 Data length in frames
    // +20 $AC44
    // +24 Data
    public static final int MAESTROFORMAT_LENGTH_FRAMES = 16;
    public static final int MAESTROFORMAT_CHANNELS_TYPE = 12;
    public static final int MAESTROFORMAT_OFFSET_DATASTART = 24;

    private boolean AnalyseMAESTRO(byte[] src, int len) {
        boolean FormatFound = false;
        if (FindStringAtPos(src, len, "MAESTRO", 0)) {
            InitInfo(len);

            Format = OldSampleFormat.Maestro;
            SampledFrequency = 44100f;
            SampleResolutionBit = 16;
            isInterleaved = true;
            isMSBFirst = true;
            FormatFound = true;
            // Check Stereo Sample
            sampleStartOffsetByte = MAESTROFORMAT_OFFSET_DATASTART;
            sampleLengthByte -= sampleStartOffsetByte;
            int bytePerSample = 2;
            int numberOfFrames = read32BitMSBAsInt(src, MAESTROFORMAT_LENGTH_FRAMES);

            if (ReadBinaryInt(src, MAESTROFORMAT_CHANNELS_TYPE) == 0) {
                numberOfChannels = 2;
                format = "Maestro v1.0 (Stereo)";
                formatShort = "MAESTRO";
                fileSuffix = ".maestro";
            } else {
                format = "Maestro v1.0 (Mono)";
                formatShort = "MAESTRO MONO";
                fileSuffix = ".maestro";
            }
            logger.debug("Old sampleLengthByte:" + sampleLengthByte);
            int newSampleLengthByte = numberOfFrames * bytePerSample * numberOfChannels;
            if(newSampleLengthByte > 16 && newSampleLengthByte < sampleLengthByte) {
                sampleLengthByte = numberOfFrames * bytePerSample * numberOfChannels;
                logger.debug("Correct sampleLengthByte:" + sampleLengthByte);
            }
        }
        return (FormatFound);
    }


    private boolean AnalyseWAV(byte[] sampleData, int sampleLength) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sampleData, 0, sampleLength);
        SampleImporterWav sampleImporterWav = new SampleImporterWav(byteArrayInputStream);

        boolean FormatFound = false;
        if (FindStringAtPos(sampleData, sampleLength, "RIFF", 0) && FindStringAtPos(sampleData, sampleLength, "WAVE", 8)) {
            int posBody = 0;
            int searchLen = 0;

            InitInfo(sampleLength);
            Format = OldSampleFormat.WAV;
            SampleResolutionBit = 16;
            SampledFrequency = 44100f;
            isInterleaved = true;
            isMSBFirst = false;
            FormatFound = true;
            format = "WAVE-RIFF (Windows Audio) 1.0";
            formatShort = "WAV";
            fileSuffix = ".wav";
            if(sampleImporterWav.getSampleInfo().getNumberOfChannels() == 2) {
                numberOfChannels = 2;
            }

            if (FindStringAtPos(sampleData, sampleLength, "fmt", 12)) {

                int CompressionType = ReadBinaryShort(sampleData, 20);
                int BitsPerSample = ReadBinaryShort(sampleData, 34);
                if (BitsPerSample == 8) {
                    SampleResolutionBit = 8;
                    isUnsigned = true;
                } else {
                    if (BitsPerSample != 16) {
                        Format = OldSampleFormat.Unknown;
                        format = "Warn:WAVE-RIFF (" + BitsPerSample + "Bit unsupported)";
                        return (false);
                    }
                }
                if (CompressionType != 1) {
                    Format = OldSampleFormat.Unknown;
                    format += " Compression unsupported)";
                    return (false);
                }
            }

            searchLen = 256;
            if (searchLen > sampleLength + 8) searchLen = sampleLength - 8;
            posBody = FindString(sampleData, sampleLength, "data", searchLen);
            if (posBody > 0) {
                int dataLengthByte;
                sampleStartOffsetByte = posBody + 8;
                sampleLengthByte -= sampleStartOffsetByte;

                dataLengthByte = ReadBinaryInt(sampleData, posBody + 4);
                if(dataLengthByte> sampleLengthByte) {
                    logger.error("Wrong length in data chunk:"+dataLengthByte);
                    logger.error("Total length of file ist:"+ sampleLengthByte);
                } else {
                    sampleLengthByte = dataLengthByte;
                    logger.debug("Set sample length byte to:"+ sampleLengthByte);
                }
            }
        }

        return (FormatFound);
    }

    private boolean AnalyseIFF(byte[] src, int len) {
        boolean FormatFound = false;
        if (FindStringAtPos(src, len, "FORM", 0) && FindStringAtPos(src, len, "8SVX", 8)) {
            int posBody = 0;
            int searchLen = 0;

            InitInfo(len);
            Format = OldSampleFormat.IFF;
            SampleResolutionBit = 8;
            isInterleaved = false;
            isMSBFirst = false;
            isUnsigned = false;
            FormatFound = true;
            numberOfChannels = 1;
            format = "IFF-8SVX Audio 8 Bit (Amiga OS) v1.0";
            formatShort = "IFF-8SVX";
            fileSuffix = ".8svx.iff";
            searchLen = 1024;
            if (searchLen > len + 8) searchLen = len - 8;
            posBody = FindString(src, len, "BODY", searchLen);
            if (posBody > 0) {
                sampleStartOffsetByte = posBody + 4;
                sampleLengthByte = getLong(src, posBody + 4);
            }
            searchLen = 256;
            if (searchLen > len + 8) searchLen = len - 8;
            posBody = FindString(src, len, "Conv. by  Symphonie", searchLen); // converted from 16 to 8 bit ???
            if(posBody > 0 ) {
                logger.debug("VERIFY Conv. by  Symphonie");
            }
            posBody = FindString(src, len, "CHAN", searchLen);
            if (posBody > 0) {
                int Chan = ReadBinaryIntMSB(src, posBody + 4);
                if (Chan == 4) {
                    numberOfChannels = 2;
                    logger.debug("VERIFY:iff stereo sample with 8 bit");
                }
            }
        }
//        }
        return (FormatFound);
    }

    private boolean AnalyseSYMPHEXPORT16BT(byte[] src, int len) {
        boolean FormatFound = false;
        if (FindStringAtPos(src, len, "16BT", 0)) {
            InitInfo(len);
            Format = OldSampleFormat.SYMPHEXPORT16BT;
            SampleResolutionBit = 16;
            sampleStartOffsetByte = 12 / 2; // buggy
            sampleLengthByte -= 12;
            isInterleaved = false;
            isMSBFirst = true;
            FormatFound = true;
            format = "Symphonie Export 16 Bit v1.0";
            formatShort = "16BT RAW";
            fileSuffix = ".16bit.raw";
        }
        return (FormatFound);
    }

    private boolean AnalyseAIF(byte[] src, int len) {
        boolean FormatFound = false;
        if (FindStringAtPos(src, len, "FORM", 0) && FindStringAtPos(src, len, "AIFF", 8)) {
            int posBody = 0;
            int searchLen = 0;

            InitInfo(len);
            Format = OldSampleFormat.AIF;
            SampleResolutionBit = 16;
            isInterleaved = true;
            isMSBFirst = true;
            FormatFound = true;
            format = "AIF Audio 16 Bit v1.0 (Mono)";
            formatShort = "AIF";
            fileSuffix = ".aif";
            numberOfChannels = 1;

            searchLen = 512;
            if (searchLen > len + 8) searchLen = len - 8;
            posBody = FindString(src, len, "COMM", searchLen);
            if (posBody > 0) {
                if (src[posBody + 9] != 1) {
                    format = "AIF Audio 16 Bit v1.0 (Stereo)";
                    formatShort = "AIF STEREO";
                    numberOfChannels = 2;
                }
            }

            searchLen = 512;
            if (searchLen > len + 8) searchLen = len - 8;
            posBody = FindString(src, len, "SSND", searchLen);
            if (posBody > 0) {
                sampleStartOffsetByte = posBody + 4;
                sampleLengthByte = getLong(src, posBody + 4);
            }
        }
        return (FormatFound);
    }

    int getIntOfShort(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes).getShort(offset);
    }

    int getLong(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes).getInt(offset);
    }


    private boolean AnalyseRAW(byte[] src, int len) {
        InitInfo(len);
        SampleResolutionBit = 8;
        SampledFrequency = 44100f / 2;
        isUnsigned = false;
        Format = OldSampleFormat.Raw;
        format = "RAW (Assuming 8 Bit 0-255)";
        formatShort = "RAW";
        fileSuffix = "8bit.sample";
        return (true);
    }

}
