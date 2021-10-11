package ch.meng.symphoniefx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SampleConversionTests {
    SampleImporterIff sampleImporterIff = new SampleImporterIff(null);
    @Test
    void byte0_255ToFloatTest() {
        float sampleMax = sampleImporterIff.byte0_255ToFloat32767(127);
        float sampleMin = sampleImporterIff.byte0_255ToFloat32767(128);
        float sampleZero = sampleImporterIff.byte0_255ToFloat32767(0);
        float sampleMinusOne = sampleImporterIff.byte0_255ToFloat32767(255);
        float sampleOne = sampleImporterIff.byte0_255ToFloat32767(1);
        Assertions.assertEquals(32767.0f, sampleMax);
        Assertions.assertEquals(-32768.0, sampleMin);
        Assertions.assertEquals(0.0f, sampleZero);
        Assertions.assertEquals(-256.0f, sampleMinusOne);
        Assertions.assertEquals(258.00787f, sampleOne);
    }

    @Test
    void addSampleToBufferTest() {
        byte[] destSamples = new byte[32];
        sampleImporterIff.addSampleToBuffer(1.0 * 32767, destSamples, 0); // 7F FF        127  -1
        sampleImporterIff.addSampleToBuffer(-32768, destSamples, 2);// 80 00            -128  0
        sampleImporterIff.addSampleToBuffer(127, destSamples, 4); // 00 7f             0   127
        sampleImporterIff.addSampleToBuffer(-128, destSamples, 6); //FF 80            -1, -128
        sampleImporterIff.addSampleToBuffer(-791, destSamples, 8); //                 -4, -23
        sampleImporterIff.addSampleToBuffer(0.0, destSamples, 10);
//        Assertions.assertEquals(1.0f, sample);
//        Assertions.assertEquals(-1.0f, sample2);
//        Assertions.assertEquals(0.0f, sample4);
    }

}
