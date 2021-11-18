package ch.meng.symphoniefx.dsp;

class DspEchoStereoTest {

//    @Test
//    void testStream() {
//        DspEchoStereo dsp = new DspEchoStereo();
//        dsp.init(2, 100f, 400, 2, 4, 0.5);
//        List<StereoSample> resultStream = new Vector<>();
//        StereoSample result = dsp.stream(new StereoSample(1, 2, 100, 80));
//        resultStream.add(result);
//        result = dsp.stream(new StereoSample(1, 2, 99, 79));
//        resultStream.add(result);
//        result = dsp.stream(new StereoSample(1, 2, 98, 78));
//        resultStream.add(result);
////        result = dsp.stream(80.0);
////        resultStream.add(result);
////        result = dsp.stream(100);
////        resultStream.add(result);
////        result = dsp.stream(80);
////        resultStream.add(result);
////        result = dsp.stream(60.0);
////        resultStream.add(result);
//        for (int i = 0; i < 60; i++) {
//            result = dsp.stream(new StereoSample());
//            resultStream.add(result);
//        }
////        Assertions.assertEquals(60, resultStream.get(0));
////        Assertions.assertEquals(80, resultStream.get(1));
////        Assertions.assertEquals(100, resultStream.get(2));
////        Assertions.assertEquals(80, resultStream.get(3));
////        Assertions.assertEquals(60, resultStream.get(4));
////
//        int i=15;
////        Assertions.assertEquals(0, resultStream.get(i++));
////        Assertions.assertEquals(15, resultStream.get(i++));
////        Assertions.assertEquals(20, resultStream.get(i++));
////        Assertions.assertEquals(25, resultStream.get(i++));
////        Assertions.assertEquals(20, resultStream.get(i++));
////        Assertions.assertEquals(15, resultStream.get(i++));
////        Assertions.assertEquals(0, resultStream.get(i++));
//    }
//
//    @Test
//    void testStreamPerformance() {
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
//    }

}