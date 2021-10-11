package ch.meng.symphoniefx;

import symreader.SampleChannel;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Vector;

public class SampleImporterIff extends NewSampleImporterBase implements NewSampleImporter {

    SampleImporterIff(ByteArrayInputStream samplesData) {
        sampleInfo.setImporterId("Iff Importer v1.0");
        if (samplesData == null || !("FORM".equals(readHunkId4Byte(samplesData)))) return;
        print("Iff FORM");
        int totallength = readLength4Byte(samplesData);
        if (!("8SVX".equals(readHunkId4Byte(samplesData)))) return;
        print("id=FORMnnnn8SVX" + "length=" + totallength);
        while (samplesData.available() > 0) {
            String hunkId = readHunkId4Byte(samplesData);
            int hunklen = readLength4Byte(samplesData);
            print("id=" + hunkId + " length=" + hunklen);
            if (hunkId.equals("BODY")) {
                print("Iff BODY");
                loadSample8Bit(samplesData, hunklen);
                return;
            }
            if (hunkId.equals("CHAN")) {
                print("id=" + hunkId + "length=" + hunklen);
            }
            samplesData.skip(hunklen);
            if (samplesData.available() <= 0) {
                print("Iff End of File reached");
            }
        }
    }

    void loadSample8Bit(ByteArrayInputStream samplesData, int hunklen) {
        sampleInfo.setFormat(SampleFormat.IFF);
        sampleInfo.setSampleResolutionBit(8);
        sampleInfo.setInterleaved(false);
        sampleInfo.setMSBFirst(false);
        sampleInfo.setUnsigned(false);
        sampleInfo.setFormatLong("IFF-8SVX Audio 8 Bit (Amiga OS) v1.0");
        sampleInfo.setFormatShort("IFF-8SVX");
        sampleInfo.setFileSuffix(".8svx.iff");
        addSampleChannel(convertFrom8Bit(samplesData, hunklen));
    }

    void addSampleChannel(List<Float> samples) {
        SampleChannel sampleChannel = new SampleChannel(samples);
        sampleChannels.add(sampleChannel);
    }

    List<Float> convertFrom8Bit(ByteArrayInputStream samplesData, int hunklen) {
        List<Float> samples = new Vector<>(hunklen);
        for (int i = 0; i < hunklen; i++) {
            samples.add(byte0_255ToFloat32767(samplesData.read()));
        }
        exportAsWav(samples); // for verification
        return samples;
    }

    float byte0_255ToFloat32767(int sample) {
        // 128 = max negative, 127 = max positive
        if (sample <= 127) return 32767.0f / 127.f * sample;
        return (32768.0f * (sample - 256)) / 128.0f;
    }

    private void exportAsWav(final List<Float> samples) {
        try {
            byte[] buffer = new byte[samples.size() * 2];
            print("Exporting test file to z:\\test.wav");
            File out = new File("z:\\test.wav");
            int index = 0;
            for (float sample : samples) {
                addSampleToBuffer(sample, buffer, index);
                index += 2;
            }
            final boolean bigEndian = true;
            final boolean signed = true;
            final int bits = 16;
            final int channels = 1;
            AudioFormat format = new AudioFormat((float) 44100, bits, channels, signed, bigEndian);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            AudioInputStream audioOutputStream = new AudioInputStream(byteArrayInputStream, format, buffer.length);
            AudioSystem.write(audioOutputStream, AudioFileFormat.Type.WAVE, out);
            audioOutputStream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("Error:writeWav to z:\\test.wav");
        }
    }

    void addSampleToBuffer(double sample, final byte[] destinationBufferPtr, final int index) {
        if (sample > 32767.0) sample = 32767.0;
        if (sample < -32768.0) sample = -32768.0;
        short shortSample = (short) sample;
        destinationBufferPtr[index + 1] = (byte) shortSample;
        destinationBufferPtr[index] = (byte) java.lang.Short.reverseBytes(shortSample);
    }

    private void convertBinaryToFloatSample(byte[] src, float[] dest) {
        float SampleFactor;
        float Sample_LEN = 2;
        float sample;
        if ((sampleInfo.getSampleLengthByte() > 0)) {
            if (sampleInfo.getSampleResolutionBit() == 8) {
                buildSample8Bit(src, dest, Sample_LEN);
            }
            if (sampleInfo.getSampleResolutionBit() == 16) {
                buildSample16Bit(src, dest, Sample_LEN);
            }
        }
    }

    private void buildSample8Bit(byte[] src, float[] dest, float Sample_LEN) {
        float sample;
        float SampleFactor = 127;
        sampleInfo.setNumbOfSamples(sampleInfo.getSampleLengthByte());
        SampleFactor = Sample_LEN / 256.0f;
        for (int i = 0; i < sampleInfo.getNumbOfSamples(); i++) {
            if (sampleInfo.isUnsigned() == false) {
                sample = src[i + sampleInfo.getSampleStartOffset()];
            } else {
                byte b = src[i + sampleInfo.getSampleStartOffset()];
                if (b >= 0) sample = (float) b - 128;
                else sample = (float) (256 + b) - 128;
            }
            sample = sample * SampleFactor;
            sampleInfo.setMaxSampleAmplitude(Math.max(sampleInfo.getMaxSampleAmplitude(), Math.abs(sample)));
            dest[i] = sample;
        }
    }

    private void buildSample16Bit(byte[] src, float[] dest, float Sample_LEN) {
//        float sample;
//        float SampleFactor;
//        int a, b;
//        SampleFactor = Sample_LEN / (256 * 256);
//        NumbOfSamples = SampleLengthByte / 2;
//        if (isMSBFirst == false) {
//            for (int i = 0; i < NumbOfSamples; i++) {
//                int Offset = SampleStartOffset + (i * 2);
//                b = src[Offset];
//                a = (src[Offset + 1]);
//                sample = SampleFactor * ((float) (IntBytesToInt(a, b)));
//                maxSampleAmplitude = Math.max(maxSampleAmplitude, Math.abs(sample));
//                dest[i] = sample;
//            }
//        } else {
//            for (int i = 0; i < NumbOfSamples; i++) {
//                int Offset = SampleStartOffset + (i * 2);
//                a = src[Offset];
//                b = src[Offset + 1];
//                sample = SampleFactor * ((float) (IntBytesToInt(a, b)));
//                maxSampleAmplitude = Math.max(maxSampleAmplitude, Math.abs(sample));
//                dest[i] = sample;
//            }
//        }
    }

    private int IntBytesToInt(int MSB, int LSB) {
        LSB = LSB & 0x00ff;
        MSB = MSB & 0x00ff;
        MSB = (MSB * 256);
        MSB = MSB + LSB;
        if (MSB >= (128 * 256)) MSB = (-256 * 256) + MSB;
        return (MSB);
    }
//    float byteToFloat(int sample) {
//        if (b >= 0) sample = (float) b - 128;
//        else sample = (float) (256 + b) - 128;
//    }

}


//        if (FindStringAtPos(samplesData, "FORM", 0) && FindStringAtPos(samplesData, "8SVX", 8)) {
//            int posBody = 0;
//            int searchLen = 0;
//
//            Format = SampleFormat.IFF;
//            SampleResolutionBit = 8;
//            isInterleaved = false;
//            isMSBFirst = false;
//            isUnsigned = false;
//            FormatFound = true;
//            numberOfSamplepools = 1;
//            format = "IFF-8SVX Audio 8 Bit (Amiga OS) v1.0";
//            formatShort = "IFF-8SVX";
//            fileSuffix = ".iff.8svx";
//            searchLen = 1024;
//            if (searchLen > len + 8) searchLen = len - 8;
//            posBody = FindString(src, len, "BODY", searchLen);
//            if (posBody > 0) {
//                SampleStartOffset = posBody + 4;
//                SampleLengthByte = getLong(src, posBody+4);
//            }
//            searchLen = 256;
//            if (searchLen > len + 8) searchLen = len - 8;
////            posBody = FindString(src, len, "Conv. by  Symphonie", searchLen);
////            if(posBody > 0 ) {
//            posBody = FindString(src, len, "CHAN", searchLen);
//            if (posBody > 0) {
//                int Chan = ReadBinaryIntMSB(src, posBody + 4);
//                if (Chan == 4) {
//                    numberOfSamplepools = 2;
//                    logger.debug("VERIFY:iff stereo sample with 8 bit");
//                }
//            }
//        }
////        }
//        return (FormatFound);

