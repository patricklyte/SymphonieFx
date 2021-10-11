package symreader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SampleImporterJavaInternal {
    protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodHandles.lookup().lookupClass());
    float BaseFrequency = 0; // What Note Pitch has been sampled
    float SampledFrequency = 0; // At which Frequency the sample has been recorded
    private int NumbOfSamples = 0;
    private int SampleStartOffset = 0; // in Samples
    private int SampleLengthByte = 0; // in Byte
    int SampleResolutionBit = 0;
    int numberOfSamplepools = 0;

    private boolean isMSBFirst = false;
    private boolean isInterleaved = false;
    private boolean isUnsigned = true;

    boolean FormatRecognized = false;
    int Format;
    float FormatVersion;
    String format;
    private String formatShort = "n/a";
    private String fileSuffix = ".sample";
    javax.sound.sampled.AudioInputStream myAudioInputStream;
    javax.sound.sampled.spi.FormatConversionProvider myFormatConversionProvider;
    byte[] converted;

    public byte[] getSamples() {
        return converted;
    }

    public String getDescription() {
        StringBuilder text = new StringBuilder();
        text.append("JAVA:"+SampleResolutionBit + "Bit ");
        text.append(" isMSBFirst:" + isMSBFirst);
        text.append(" isInterleaved:" + isInterleaved);
        text.append(" Channels:" + numberOfSamplepools);
        text.append(" SampledFrequency:" + SampledFrequency);
        text.append(" format:" + format);
        text.append(" bytes:" + converted.length);
        return text.toString();
    }

    public boolean AnalyseJAVANative(byte[] src, int len) {
        try {
            ByteArrayInputStream myByteArrayInputStream = new ByteArrayInputStream(src);
            AudioFileFormat myAudioFileFormat = AudioSystem.getAudioFileFormat(myByteArrayInputStream);
            logger.debug(myAudioFileFormat.toString());
            SampleResolutionBit = myAudioFileFormat.getFormat().getSampleSizeInBits();
            isMSBFirst = myAudioFileFormat.getFormat().isBigEndian();
            isInterleaved = true;
            format = "JAVA Converted to: " + myAudioFileFormat.toString();
            numberOfSamplepools = myAudioFileFormat.getFormat().getChannels();
            SampledFrequency = myAudioFileFormat.getFormat().getSampleRate();
            converted = convertToWav(src, myByteArrayInputStream, myAudioFileFormat);
            return true;
        } catch (UnsupportedAudioFileException ex) {
            logger.debug("JavaSampleImporter:Not supported");
            return false;
        }
        catch (Exception ex) {
            logger.error(ex);
            return false;
        }
    }


    // convertiert immer nach 16 bit
    public byte[] convertToWav(byte[] src, ByteArrayInputStream myByteArrayInputStream, AudioFileFormat sourceAudioFormat) {
        try {

            AudioFormat DestAudioFormat = new AudioFormat(SampledFrequency, 16, numberOfSamplepools, true, false);
            //    AudioFormat(float sampleRate, int sampleSizeInBits,int channels, boolean signed, boolean bigEndian)

            logger.debug("JAVA:Source:"+ sourceAudioFormat);
            logger.debug("JAVA:Dest:"+ DestAudioFormat);

            if (AudioSystem.isConversionSupported(DestAudioFormat, sourceAudioFormat.getFormat())) {
                myAudioInputStream = AudioSystem.getAudioInputStream(myByteArrayInputStream);
                //SampleFrames = (int) myAudioInputStream.getFrameLength();
                //convDestArray = new byte[(int) myAudioInputStream.getFrameLength()];
                ByteArrayOutputStream tempDestStreamWAV = new ByteArrayOutputStream();
                AudioSystem.write(myAudioInputStream, AudioFileFormat.Type.AIFF, tempDestStreamWAV);
                logger.debug("Converter to aiff succesful");
                return tempDestStreamWAV.toByteArray();
            } else {
                logger.debug("Converter to aiff failed");
            }
        } catch (UnsupportedAudioFileException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        return src;
    }
}
