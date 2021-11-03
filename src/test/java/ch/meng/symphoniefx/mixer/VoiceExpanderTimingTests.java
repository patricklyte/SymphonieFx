package ch.meng.symphoniefx.mixer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class VoiceExpanderTimingTests {

    @Test
    void testSamplesAtBpm() {
        VoiceExpander voiceExpander = new VoiceExpander();
        voiceExpander.setMixFrequency(44100);
        voiceExpander.initMixSystem(true);
        voiceExpander.setSongSpeed(100, 1);
        int samplesPerCycl = voiceExpander.getNumberOfSamplesPerCycl();
        Assertions.assertEquals(2663, samplesPerCycl);
    }

    @Test
    void testSamplesAtLowBpm() {
        VoiceExpander voiceExpander = new VoiceExpander();
        voiceExpander.setMixFrequency(44100);
        voiceExpander.initMixSystem(true);
        voiceExpander.setSongSpeed(1, 1);
        int samplesPerCycl = voiceExpander.getNumberOfSamplesPerCycl();
        Assertions.assertEquals(266382, samplesPerCycl);
    }

    @Test
    void testSamplesAtHighBpm() {
        VoiceExpander voiceExpander = new VoiceExpander();
        voiceExpander.setMixFrequency(44100);
        voiceExpander.initMixSystem(true);
        voiceExpander.setSongSpeed(1000, 1);
        int samplesPerCycl = voiceExpander.getNumberOfSamplesPerCycl();
        Assertions.assertEquals(266, samplesPerCycl);
    }

    @Test
    void testSamplesAtBpmHighMixFrequency() {
        VoiceExpander voiceExpander = new VoiceExpander();
        voiceExpander.setMixFrequency(4410000);
        voiceExpander.initMixSystem(true);
        voiceExpander.setSongSpeed(100, 1);
        int samplesPerCycl = voiceExpander.getNumberOfSamplesPerCycl();
        Assertions.assertEquals(266382, samplesPerCycl);
    }


}