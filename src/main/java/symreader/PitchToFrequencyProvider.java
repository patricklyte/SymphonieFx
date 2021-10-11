package symreader;

import java.util.List;
import java.util.Vector;

public class PitchToFrequencyProvider {
    private static final double[] FreqBase = {1.0000, 1.0595, 1.1225, 1.1892, 1.2599, 1.3348, 1.4142, 1.4983, 1.5874, 1.6818, 1.7818, 1.8878}; // Gleichschwebend
    private static final List<Double> frequencies = new Vector<>();
    private static final int PITCH_MIN = -36;
    private static final int PITCH_MAX = 128;
    public static final int FREQUENCY_ILLEGAL = -1;

    private PitchToFrequencyProvider(){}

    private static void init() {
        int counter = 0;
        double factor = 1.0/8.0;
        for (int pitchIndex = PITCH_MIN; pitchIndex < PITCH_MAX+1; pitchIndex++) {
            frequencies.add(FreqBase[counter] * factor);
            counter++;
            if (counter > FreqBase.length - 1) {
                factor = factor * 2;
                counter = 0;
            }
        }
    }

    public static double getFrequency(int pitchIndex) {
        if(frequencies.isEmpty()) init();
        if(pitchIndex<PITCH_MIN || pitchIndex>PITCH_MAX) return FREQUENCY_ILLEGAL;
        return frequencies.get(pitchIndex - PITCH_MIN);
    }

}
