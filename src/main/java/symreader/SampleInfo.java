package symreader;

import ch.meng.symphoniefx.SampleFormat;

public class SampleInfo {
    private float BaseFrequency = 0; // What Note Pitch has been sampled
    private float SampledFrequency = 0; // At which Frequency the sample has been recorded
    private int NumbOfSamples = 0;
    private int SampleStartOffset = 0; // in Samples
    private int SampleLengthByte = 0; // in Byte
    private int SampleResolutionBit = 0;
    int bytesPerSample = 2;
    private float maxSampleAmplitude = 0;
    private boolean isMSBFirst = false;
    private boolean isInterleaved = false;
    private boolean isUnsigned = true;
    private boolean formatRecognized = false;
    private SampleFormat format;
    private float formatVersion;
    private String formatLong = "";
    private String formatShort = "n/a";
    private String fileSuffix = ".sample";
    private int numberOfChannels = 1;
    private String importerId = "";

    public String getImporterId() {
        return importerId;
    }

    public void setImporterId(String importerId) {
        this.importerId = importerId;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public float getBaseFrequency() {
        return BaseFrequency;
    }

    public void setBaseFrequency(float baseFrequency) {
        BaseFrequency = baseFrequency;
    }

    public float getSampledFrequency() {
        return SampledFrequency;
    }

    public void setSampledFrequency(float sampledFrequency) {
        SampledFrequency = sampledFrequency;
    }

    public int getNumbOfSamples() {
        return NumbOfSamples;
    }

    public void setNumbOfSamples(int numbOfSamples) {
        NumbOfSamples = numbOfSamples;
    }

    public int getSampleStartOffset() {
        return SampleStartOffset;
    }

    public void setSampleStartOffset(int sampleStartOffset) {
        SampleStartOffset = sampleStartOffset;
    }

    public int getSampleLengthByte() {
        return SampleLengthByte;
    }

    public void setSampleLengthByte(int sampleLengthByte) {
        SampleLengthByte = sampleLengthByte;
    }

    public int getSampleResolutionBit() {
        return SampleResolutionBit;
    }

    public void setSampleResolutionBit(int sampleResolutionBit) {
        SampleResolutionBit = sampleResolutionBit;
    }

    public int getBytesPerSample() {
        return bytesPerSample;
    }

    public void setBytesPerSample(int bytesPerSample) {
        this.bytesPerSample = bytesPerSample;
    }

    public float getMaxSampleAmplitude() {
        return maxSampleAmplitude;
    }

    public void setMaxSampleAmplitude(float maxSampleAmplitude) {
        this.maxSampleAmplitude = maxSampleAmplitude;
    }

    public boolean isMSBFirst() {
        return isMSBFirst;
    }

    public void setMSBFirst(boolean MSBFirst) {
        isMSBFirst = MSBFirst;
    }

    public boolean isInterleaved() {
        return isInterleaved;
    }

    public void setInterleaved(boolean interleaved) {
        isInterleaved = interleaved;
    }

    public boolean isUnsigned() {
        return isUnsigned;
    }

    public void setUnsigned(boolean unsigned) {
        isUnsigned = unsigned;
    }

    public boolean isFormatRecognized() {
        return formatRecognized;
    }

    public void setFormatRecognized(boolean formatRecognized) {
        this.formatRecognized = formatRecognized;
    }

    public SampleFormat getFormat() {
        return format;
    }

    public void setFormat(SampleFormat format) {
        this.format = format;
    }

    public float getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(float formatVersion) {
        this.formatVersion = formatVersion;
    }

    public String getFormatLong() {
        return formatLong;
    }

    public void setFormatLong(String formatLong) {
        this.formatLong = formatLong;
    }

    public String getFormatShort() {
        return formatShort;
    }

    public void setFormatShort(String formatShort) {
        this.formatShort = formatShort;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }
}
