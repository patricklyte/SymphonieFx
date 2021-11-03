package ch.meng.symphoniefx.dsp;

import ch.meng.symphoniefx.TimeMeasure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Vector;


class SymphonieDspIITest {
    @BeforeAll
    static void setUp() {
    }

    @Test
    void testStream() {
        SymphonieDspII symphonieDspII = new SymphonieDspII();
        symphonieDspII.init(100f, 800, 1, 0.5);
        List<Double> resultStream = new Vector<>();
        double result = symphonieDspII.stream(60);
        resultStream.add(result);
        result = symphonieDspII.stream(80.0);
        resultStream.add(result);
        result = symphonieDspII.stream(100);
        resultStream.add(result);
        result = symphonieDspII.stream(80);
        resultStream.add(result);
        result = symphonieDspII.stream(60.0);
        resultStream.add(result);
        for(int i= 0;i<30;i++) {
            result = symphonieDspII.stream(0.0);
            resultStream.add(result);
        }
        Assertions.assertEquals(60, resultStream.get(0));
        Assertions.assertEquals(80, resultStream.get(1));
        Assertions.assertEquals(100, resultStream.get(2));
        Assertions.assertEquals(80, resultStream.get(3));
        Assertions.assertEquals(60, resultStream.get(4));

        int i=15;
        Assertions.assertEquals(0, resultStream.get(i++));
        Assertions.assertEquals(15, resultStream.get(i++));
        Assertions.assertEquals(20, resultStream.get(i++));
        Assertions.assertEquals(25, resultStream.get(i++));
        Assertions.assertEquals(20, resultStream.get(i++));
        Assertions.assertEquals(15, resultStream.get(i++));
        Assertions.assertEquals(0, resultStream.get(i++));
    }

    @Test
    void testStreamPerformance() {
        SymphonieDspII symphonieDspII = new SymphonieDspII();
        TimeMeasure timeMeasure = new TimeMeasure();
        symphonieDspII.init(125, 44100, 64, 0.5);
        List<Double> resultStream = new Vector<>();
        double result = symphonieDspII.stream(60);
        resultStream.add(result);
        result = symphonieDspII.stream(80.0);
        resultStream.add(result);
        result = symphonieDspII.stream(100);
        resultStream.add(result);
        result = symphonieDspII.stream(80);
        resultStream.add(result);
        result = symphonieDspII.stream(60.0);
        resultStream.add(result);

        String timerId = "testStreamPerformance";
        timeMeasure.start(timerId);
        for(long i= 0;i<4_410_000L;i++) {
            result = symphonieDspII.stream(result);
            //resultStream.add(result);
        }
        timeMeasure.stop(timerId);
        System.out.println(("double version:" + timeMeasure.getDiffString(timerId)));
    }

    @Test
    void testStreamPerformancefloat() {
        SymphonieDspIIfloat symphonieDspIIfloat = new SymphonieDspIIfloat();
        TimeMeasure timeMeasure = new TimeMeasure();
        symphonieDspIIfloat.init(125, 44100, 64, 0.5f);
        List<Float> resultStream = new Vector<>();

        float result = symphonieDspIIfloat.stream(60);
        resultStream.add(result);
        result = symphonieDspIIfloat.stream(80.0f);
        resultStream.add(result);
        result = symphonieDspIIfloat.stream(100);
        resultStream.add(result);
        result = symphonieDspIIfloat.stream(80);
        resultStream.add(result);
        result = symphonieDspIIfloat.stream(60.0f);
        resultStream.add(result);

        String timerId = "testStreamPerformancefloat";
        timeMeasure.start(timerId);
        for(long i= 0;i<40_410_000L;i++) {
            result = symphonieDspIIfloat.stream(result);
            //resultStream.add(result);
        }
        timeMeasure.stop(timerId);
        System.out.println(("float version:" + timeMeasure.getDiffString(timerId)));
    }
}