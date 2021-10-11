package ch.meng.symphoniefx;

import java.util.HashMap;
import java.util.Map;

public class TimeMeasure2 {
    private final Map<String, Boolean> sumOpened = new HashMap<>();
    private final Map<String, Long> startTimes = new HashMap<>();
    private final Map<String, Long> sum = new HashMap<>();
    private final Map<String, Long> operationsCount = new HashMap<>();

    public final void clear() {
        startTimes.clear();
        sum.clear();
        operationsCount.clear();
        sumOpened.clear();
    }

    public final void start(final String id) {
        startTimes.put(id, System.nanoTime());
        sumOpened.put(id, true);
    }

    public final void restart(final String id) {
        start(id);
        sum.remove(id);
    }
    public void stop(final String id) {
        if (sumOpened.getOrDefault(id, false)) {
            sum.put(id, sum.getOrDefault(id, 0L) + System.nanoTime() - startTimes.getOrDefault(id, 0L));
            operationsCount.put(id, operationsCount.getOrDefault(id, 0L) + 1);
        }
        sumOpened.put(id, false);
    }

    public double getTimeElapsedInMilliseconds(final String id) {
        if (sumOpened.getOrDefault(id, false)) {
            return (System.nanoTime() - startTimes.getOrDefault(id, 0L)) / 1_000_000;
        }
        return 0;
    }

    private long getOps(final String id) {
        if (operationsCount.containsKey(id)) return operationsCount.get(id);
        return 0;
    }

    public String getSumAsString(final String id) {
        if (sum.containsKey(id)) {
            StringBuilder text = new StringBuilder();
            text.append(getFormatedTime(sum.get(id)))
                    .append(" ")
                    .append(getOps(id)).append("ops");
            return text.toString();
        }
        return "0ms";
    }

    public double getSumInMS(final String id) {
        return sum.getOrDefault(id, 0L) / 1_000_000.0;
    }

    String getFormatedTime(long nanoSeconds) {
        double valueInMilliseconds = nanoSeconds / 1_000_000.0;
        if (valueInMilliseconds < 1000) return String.format("%.4fms", valueInMilliseconds);
        valueInMilliseconds = valueInMilliseconds / 1000.0;
        if (valueInMilliseconds < 120) return String.format("%.3fs", valueInMilliseconds);
        valueInMilliseconds = valueInMilliseconds / 60.0;
        return String.format("%.3fmin", valueInMilliseconds);
    }

}