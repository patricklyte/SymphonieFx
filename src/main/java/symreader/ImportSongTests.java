package symreader;

import ch.meng.symphoniefx.TimeMeasure;
import ch.meng.symphoniefx.dsp.DspEchoStereo;
import ch.meng.symphoniefx.dsp.StereoSample;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Vector;


class ImportSongTests {


    @Test
    void byteToIntTest() {
        OldSymModFormatImporter importer = new OldSymModFormatImporter();
        byte test = 1;
        Assertions.assertEquals(1, importer.byteToInt(test));

        test = 2;
        Assertions.assertEquals(2, importer.byteToInt(test));

        test = -1;
        Assertions.assertEquals(255, importer.byteToInt(test));

        test = -10;
        Assertions.assertEquals(246, importer.byteToInt(test));

        test = -126;
        Assertions.assertEquals(130, importer.byteToInt(test));

        test = -127;
        Assertions.assertEquals(129, importer.byteToInt(test));

        test = -128;
        Assertions.assertEquals(128, importer.byteToInt(test));

        test = 127;
        Assertions.assertEquals(127, importer.byteToInt(test));
    }
    @Test
    void getLongTest() {
        OldSymModFormatImporter importer = new OldSymModFormatImporter();
        byte[] bytes1 = {0x00, 0x00, 0x00, 0x01};
        Assertions.assertEquals(1, importer.getLong(bytes1, 0));
        byte[] bytes2 = {0x12, 0x34, 0x56, 0x78};
        Assertions.assertEquals(0x12345678, importer.getLong(bytes2, 0));
        byte[] bytes3 = {0x22, 0x12, 0x34, 0x56, 0x78, 0x11};
        Assertions.assertEquals(0x12345678, importer.getLong(bytes3, 1));
        Assertions.assertEquals(0x34567811, importer.getLong(bytes3, 2));
    }

    @Test
    void getIntOfShortTest() {
        OldSymModFormatImporter importer = new OldSymModFormatImporter();
        byte[] bytes3 = {0x22, 0x12, 0x34, 0x56, 0x78, 0x11};
        Assertions.assertEquals(0x2212, importer.getIntOfShort(bytes3, 0));
        Assertions.assertEquals(0x1234, importer.getIntOfShort(bytes3, 1));
        Assertions.assertEquals(0x3456, importer.getIntOfShort(bytes3, 2));
        Assertions.assertEquals(0x5678, importer.getIntOfShort(bytes3, 3));
    }

    @Test
    void getReadMemoryShortToInt() {
        OldSymModFormatImporter importer = new OldSymModFormatImporter();
        byte[] bytes3 = {0x22, 0x12, 0x34, 0x56, 0x78, 0x11};
        Assertions.assertEquals(0x2212, importer.ReadMemoryShortToInt(bytes3, 0));
        Assertions.assertEquals(0x1234, importer.ReadMemoryShortToInt(bytes3, 1));
        Assertions.assertEquals(0x3456, importer.ReadMemoryShortToInt(bytes3, 2));
        Assertions.assertEquals(0x5678, importer.ReadMemoryShortToInt(bytes3, 3));
    }

    @Test
    void testStreamPerformance() {
//        DspEchoStereo dsp = new DspEchoStereo();
//        TimeMeasure timeMeasure = new TimeMeasure();
//        dsp.init(2, 125, 44100, 2, 64, 0.5);
//        List<StereoSample> resultStream = new Vector<>();
//        StereoSample result = dsp.stream(new StereoSample(1, 2, 99, 79));
//        resultStream.add(result);
//        result = dsp.stream(new StereoSample(1, 2, 98, 78));
//        resultStream.add(result);
//
//        String timerId = "testStreamPerformance";
//        timeMeasure.start(timerId);
//        for (long i = 0; i < 40_410_000L; i++) {
//            result = dsp.stream(result);
//            //resultStream.add(result);
//        }
//        timeMeasure.stop(timerId);
//        System.out.println(("DspEchoStereo, StereoSample version:" + timeMeasure.getDiffString(timerId)));
    }

}