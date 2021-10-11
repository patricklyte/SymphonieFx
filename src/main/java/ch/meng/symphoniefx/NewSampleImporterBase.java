package ch.meng.symphoniefx;

import symreader.SampleChannel;
import symreader.SampleInfo;

import java.io.ByteArrayInputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Vector;

public class NewSampleImporterBase implements NewSampleImporter {
    protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodHandles.lookup().lookupClass());

//    protected float sampledPitch = 0; // What Note Pitch has been sampled
//    protected float sampledFrequency = 0; // At which Frequency the sample has been recorded
//
//    protected int NumbOfSamples = 0;
//    protected int SampleStartOffset = 0; // in Samples
//    protected int SampleLengthByte = 0; // in Byte
//    protected int bytesPerSample = 2; // in Byte
//
//    protected int sampleResolutionBit = 0;
//    protected float maxSampleAmplitude = 0;
//    protected boolean isMSBFirst = false;
//    protected boolean isInterleaved = false;
//    protected boolean isUnsigned = true;
//    protected SampleFormat format = SampleFormat.None;
//    protected String formatLong = "";
//    protected String formatShort = "n/a";
//    protected String fileSuffix = ".sample";
//
//    protected String importer = "";
    protected SampleInfo sampleInfo = new SampleInfo();

    protected void print(String text) {
        logger.debug(sampleInfo.getImporterId() + ":" +text);
    }
    protected void error(String text) {
        logger.error(sampleInfo.getImporterId() + " Error:" +text);
    }

    // Sample Pools
    //@Deprecated
    //protected int numberOfSamplepools = 0; // 1 = Mono, 2 = Stereo

    protected List<SampleChannel> sampleChannels = new Vector<>();

//    protected byte[] RawSample;   // Quickfix for Export Raw Sample
//    protected int RawSampleLen;   // Quickfix for Export Raw Sample

    public SampleInfo getSampleInfo() {
        return sampleInfo;
    }

    public String getDescription() {
        StringBuilder text = new StringBuilder();
        text.append(sampleInfo.getSampleResolutionBit() + "Bit ");
        text.append(" isMSBFirst:" + sampleInfo.isMSBFirst());
        text.append(" isInterleaved:" + sampleInfo.isInterleaved());
        text.append(" Channels:" + sampleChannels.size());
        text.append(" SampledFrequency:" + sampleInfo.getSampledFrequency());
        text.append(" Short:" + sampleInfo.getFormatShort());
        text.append(" Format:" + sampleInfo.getFormat());
        return text.toString();
    }

    @Override
    public boolean isMono() {
        return 1== sampleChannels.size();
    }
    @Override
    public boolean isStereo() {
        return 2== sampleChannels.size();
    }
    @Override
    public int getNumberOfChannels() {
        return sampleChannels.size();
    }

//    protected int FindString(byte[] src, int len, String s, int maxoffset) {
//        int foundpos = -1;
//        for (int i = 0; i < maxoffset; i++) {
//            if (FindStringAtPos(src, len, s, i) == true) {
//                foundpos = i;
//            }
//        }
//        return (foundpos);
//    }
//
//    protected boolean FindStringAtPos(ByteArrayInputStream samplesData, String hunkId, int pos) {
//        boolean found = true;
//        int i = 0;
//        for (i = 0; i < hunkId.length(); i++) {
//            if (hunkId.charAt(i) != samplesData.[i + pos]) found = false;
//        }
//        return (found);
//    }

    protected String readHunkId4Byte(ByteArrayInputStream samplesData) {
        String id = "";
        for (int i = 0; i < 4; i++) {
            id+= (char) samplesData.read();
        }
        return  id;
    }

    protected int readLength4Byte(ByteArrayInputStream samplesData) {
        int len=0;
        for (int i = 0; i < 4; i++) {
            len = (len * 256) + samplesData.read();
        }
        return len;
    }
}
