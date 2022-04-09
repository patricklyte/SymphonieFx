package ch.meng.symphoniefx;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

public class SampleImporterWav extends NewSampleImporterBase implements NewSampleImporter {

    public SampleImporterWav(final ByteArrayInputStream in) throws IOException {
        analyseHeader(in);
//        samples = new int[numChannels][numSamples];
//        for (int i = 0; i < numSamples; i++)
//            for (int ch = 0; ch < numChannels; ch++) {
//                int val = readLittleUint(in, bytesPerSample);
//                if (sampleResolutionBit == 8)
//                    val -= 128;
//                else
//                    val = (val << (32 - sampleResolutionBit)) >> (32 - sampleResolutionBit);
//                samples[ch][i] = val;
//            }
//        }
    }

    public void analyseHeader(final ByteArrayInputStream in) throws IOException {
        sampleInfo.setImporterId("Wav Importer v1.0");
        // Parse and check WAV header
        String idRiff = readString(in, 4);
        if (!idRiff.equals("RIFF")) {
            error("Invalid RIFF file header:"+ idRiff);
            return;
        }
        int bytes = readLittleUint(in, 4);  // Remaining data length
        String idWav = readString(in, 4);
        if (!idWav.equals("WAVE")) {
            error("Invalid WAV file header:" + idWav);
            return;
        }
        print("WAV file header, datalength bytes:" + bytes);
        // Handle the format chunk
        String chuckId = readString(in, 4);
        print("chuckId:"+chuckId);
        if (!chuckId.equals("fmt ")) {
            error("Unrecognized WAV file chunk:"+chuckId);
            //return;
        }
        int filetype = readLittleUint(in, 4);
        print("filetype:"+filetype);
        if (filetype != 16) {
            error("Unsupported WAV file type:" + filetype);
            //return;
        }
        int codectype = readLittleUint(in, 2);
        print("codectype:"+filetype);
        if (codectype != 0x0001) {
            error("Unsupported WAV file codec:" +codectype);
            //return;
        }
        int numChannels = readLittleUint(in, 2);
        print("numChannels "+numChannels);
        if (numChannels < 0 ) {
            //error("Too many (or few) audio channels");
            //return;
        }
        sampleInfo.setNumberOfChannels(numChannels);
        sampleInfo.setSampledFrequency(readLittleUint(in, 4));
        print("sampledFrequency "+sampleInfo.getSampledFrequency());
        int byteRate = readLittleUint(in, 4);
        int blockAlign = readLittleUint(in, 2);
        sampleInfo.setSampleResolutionBit(readLittleUint(in, 2));
        print("sampleResolutionBit "+sampleInfo.getSampleResolutionBit());
        if (sampleInfo.getSampleResolutionBit() == 0)
            error("Unsupported sample depth");
        sampleInfo.setBytesPerSample(sampleInfo.getSampleResolutionBit() / 8);

        // Handle the data chunk
        String dataId = readString(in, 4);
        if (!dataId.equals("data")) {
            error("Unrecognized WAV file chunk:"+dataId);
            //return;
        }
        int sampleDataLen = readLittleUint(in, 4);
//        if (sampleDataLen <= 0 || sampleDataLen % (numChannels * bytesPerSample) != 0) {
//            error("Invalid length of audio sample data");
////            return;
//        }
        int numSamples = sampleDataLen / (numChannels * sampleInfo.getBytesPerSample());
        sampleInfo.setNumbOfSamples(numSamples);
        print("WAV supported file recognized:"
                + sampleDataLen + "bytes, "
                + sampleDataLen/sampleInfo.getBytesPerSample() + "samples, "
                + numChannels + " channels, "
                + numSamples + " samples per channel , "
                + sampleInfo.getBytesPerSample() + " byte per sample , "
        );
    }

    // Reads len bytes from the given stream and interprets them as a UTF-8 string.
    private static String readString(final InputStream in, final int len) throws IOException {
        byte[] temp = new byte[len];
        for (int i = 0; i < temp.length; i++) {
            int b = in.read();
            if (b == -1)
                throw new EOFException();
            temp[i] = (byte)b;
        }
        return new String(temp, StandardCharsets.UTF_8);
    }


    // Reads n bytes (0 <= n <= 4) from the given stream, interpreting
    // them as an unsigned integer encoded in little endian.
    private static int readLittleUint(final InputStream in, final int n) throws IOException {
        int result = 0;
        for (int i = 0; i < n; i++) {
            int b = in.read();
            if (b == -1)
                throw new EOFException();
            result |= b << (i * 8);
        }
        return result;
    }

//    void loadSample8Bit(ByteArrayInputStream samplesData, int hunklen) {
//        sampleInfo.setFormat(SampleFormat.IFF);
//        sampleInfo.setSampleResolutionBit(8);
//        sampleInfo.setInterleaved(false);
//        sampleInfo.setMSBFirst(false);
//        sampleInfo.setUnsigned(false);
//        sampleInfo.setFormatLong("IFF-8SVX Audio 8 Bit (Amiga OS) v1.0");
//        sampleInfo.setFormatShort("IFF-8SVX");
//        sampleInfo.setFileSuffix(".8svx.iff");
//        load(samplesData, hunklen);
//    }

    List<Float> samples;
    void load(final ByteArrayInputStream samplesData, final int hunklen) {
        samples = new Vector<>(hunklen);
        for (int i = 0; i < hunklen; i++) {
            samples.add( byte0_255ToFloat(samplesData.read()));
        }
        writeWav(samples);
    }

    float byte0_255ToFloat(final int sample) {
        // 128 = max negative, 127 = max positive
        if(sample < 128) return sample/128.f;
        return (sample-256) / (128.0f);
    }

    private void writeWav(final List<Float> samples) {
        try {
            byte[] buffer = new byte[samples.size() * 2];
            File out = new File("z:\\test.wav");
            int index = 0;
            for (float sample : samples) {
                addSampleToBuffer(sample * 32767, buffer, index);
                index += 2;
            }
            final boolean bigEndian = true;
            final boolean signed = true;
            final int bits = 16;
            final int channels = 1;
            AudioFormat format = new AudioFormat((float) 44100, bits, channels, signed, bigEndian);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, format, buffer.length);
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
            audioInputStream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("Error:writeWav");
        }
    }

    void addSampleToBuffer(double sample, final byte[] destinationBufferPtr, final int index) {
        if (sample > 32767.0) sample = 32767.0;
        if (sample < -32768.0) sample = -32768.0;
        short shortSample = (short) sample;
        destinationBufferPtr[index + 1] = (byte) shortSample;
        destinationBufferPtr[index] = (byte) Short.reverseBytes(shortSample);
    }

    private void convertBinaryToFloatSample(byte[] src, float[] dest) {
        float SampleFactor;
        float Sample_LEN = 2;
        float sample;
        if ((sampleInfo.getSampleLengthByte() > 0)) {
            if (8 == sampleInfo.getSampleResolutionBit()) {
                buildSample8Bit(src, dest, Sample_LEN);
            }
            if (16 == sampleInfo.getSampleResolutionBit()) {
                buildSample16Bit(src, dest, Sample_LEN);
            }
        }
    }

    private void buildSample8Bit(final byte[] src, final float[] dest, final float Sample_LEN) {
        final float SampleFactor = Sample_LEN / 256.0f;
        sampleInfo.setNumbOfSamples(sampleInfo.getSampleLengthByte());
        for (int i = 0; i < sampleInfo.getNumbOfSamples(); i++) {
            float sample;
            if (sampleInfo.isUnsigned()) {
                sample = src[i + sampleInfo.getSampleStartOffset()];
            } else {
                byte b = src[i + sampleInfo.getSampleStartOffset()];
                if (b >= 0) sample = (float) b - 128;
                else sample = (float) (256 + b) - 128;
            }
            sample = (sample * SampleFactor);
            sampleInfo.setMaxSampleAmplitude(Math.max(sampleInfo.getMaxSampleAmplitude(), Math.abs(sample)));
            dest[i] = sample;
        }
    }

    private void buildSample16Bit(final byte[] src, final float[] dest, final float Sample_LEN) {
        int a, b;
        final float SampleFactor = Sample_LEN / (256 * 256);
        sampleInfo.setNumbOfSamples(sampleInfo.getSampleLengthByte() / 2);
        float sample;
        if (sampleInfo.isMSBFirst() == false) {
            for (int i = 0; i < sampleInfo.getNumbOfSamples(); i++) {
                final int Offset = sampleInfo.getSampleStartOffset() + (i * 2);
                b = src[Offset];
                a = (src[Offset + 1]);
                sample = SampleFactor * ((float) (IntBytesToInt(a, b)));
                sampleInfo.setMaxSampleAmplitude(Math.max(sampleInfo.getMaxSampleAmplitude(), Math.abs(sample)));
                dest[i] = sample;
            }
        } else {
            for (int i = 0; i < sampleInfo.getNumbOfSamples(); i++) {
                final int Offset = sampleInfo.getSampleStartOffset() + (i * 2);
                a = src[Offset];
                b = src[Offset + 1];
                sample = SampleFactor * ((float) (IntBytesToInt(a, b)));
                sampleInfo.setMaxSampleAmplitude(Math.max(sampleInfo.getMaxSampleAmplitude(), Math.abs(sample)));
                dest[i] = sample;
            }
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

}

