package ch.meng.symphoniefx.mixer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class VoiceExpanderPitchTests {

    @Test
    void testFrequencyFromPitchAndFinetune() {
        VoiceExpander voiceExpander = new VoiceExpander();
        voiceExpander.initMixSystem(true);
        double frequencyBase = voiceExpander.getPitchToFreq(0, 0);
        double frequencyDouble= voiceExpander.getPitchToFreq(12, 0);
        double frequencyDoubleOneUp = voiceExpander.getPitchToFreq(13, 0);
        double frequencyDoubleOneDown = voiceExpander.getPitchToFreq(11, 0);

        double frequencyDoubleDetuned = voiceExpander.getPitchToFreq(12, 1);
        double frequencyDoubleDetunedNegativ = voiceExpander.getPitchToFreq(12, -1);
        double frequencyDoubleDetunedMax = voiceExpander.getPitchToFreq(12, 127);
        double frequencyDoubleDetunedNegativMax = voiceExpander.getPitchToFreq(12, -128);

        Assertions.assertTrue(frequencyDouble == 2.0f * frequencyBase);
        Assertions.assertTrue(frequencyDoubleDetuned > frequencyDouble);
        Assertions.assertTrue(frequencyDoubleDetunedNegativ < frequencyDouble);
        Assertions.assertEquals(frequencyDoubleOneUp, frequencyDoubleDetunedMax);
        Assertions.assertEquals(frequencyDoubleOneDown, frequencyDoubleDetunedNegativMax);
    }


}