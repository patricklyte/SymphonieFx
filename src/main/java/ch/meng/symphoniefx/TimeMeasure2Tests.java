package ch.meng.symphoniefx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeMeasure2Tests {
    @Test
    void saveTest() {
        TimeMeasure2 timer = new TimeMeasure2();
        long nanos = 12_000_000_000L;
        String test = timer.getFormatedTime(nanos);
        Assertions.assertEquals("12.000s", test);
    }
}
