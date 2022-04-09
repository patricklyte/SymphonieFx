package ch.meng.symphoniefx;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * utility class for measuring time v2.0
 * <p>
 * usage: a pair of start and stop, getDiff()
 * or multiple pairs of start and sum, getSum()
 */


public class TimeMeasure {
    private final Map<String, Calendar> startTimes = new HashMap<String, Calendar>();
    private final Map<String, Calendar> endTimes = new HashMap<String, Calendar>();
    private final Map<String, Long> sum = new HashMap<String, Long>();
    private final Map<String, String> names = new HashMap<String, String>();
    private final Map<String, Long> operationsCount = new HashMap<String, Long>();

    public void start(final String id) {
        synchronized (this) {
            Calendar calendar = new GregorianCalendar();
            startTimes.put(id, calendar);
        }
    }

    public void start(final String id, final String name) {
        startTimes.put(id, new GregorianCalendar());
        names.put(id, name);
    }

    public void stop(final String id) {
        endTimes.put(id, new GregorianCalendar());
    }

    /**
     * get start timestamp for id
     *
     * @param id
     * @return Timestamp
     */
    Timestamp getStart(final String id) {
        if (startTimes.containsKey(id)) {
            return new Timestamp(startTimes.get(id).getTime().getTime());
        }
        return null;
    }

    /**
     * get stop timestamp for id
     *
     * @param id
     * @return Timestamp
     */
    Timestamp getStop(final String id) {
        if (endTimes.containsKey(id)) {
            return new Timestamp(endTimes.get(id).getTime().getTime());
        }
        return null;
    }

    /**
     * get time between start and stop in ms for id
     *
     * @param id
     * @return Long
     */
    public Long getDiff(final String id) {
        if (getStart(id) != null && getStop(id) != null) {
            Timestamp temp = new Timestamp(endTimes.get(id).getTime().getTime() - startTimes.get(id).getTime().getTime());
            return temp.getTime();
        }
        return 0L;
    }

    public String getDiffString(final String id) {
        if (getStart(id) != null && getStop(id) != null) {
            Timestamp temp = new Timestamp(endTimes.get(id).getTime().getTime() - startTimes.get(id).getTime().getTime());
            return getFormatedTimeMS(temp.getTime());
        }
        return "";
    }

    public String getTimeElapsed(final String id) {
        if (getStart(id) != null) {
            Calendar calendar = new GregorianCalendar();
            Timestamp temp = new Timestamp(calendar.getTime().getTime() - startTimes.get(id).getTime().getTime());
            return getFormatedTimeMS(temp.getTime());
        }
        return "";
    }

    /**
     * get sum for id in ms
     *
     * @param id
     * @return
     */
    public Long getSum(final String id) {
        if (sum.containsKey(id)) {
            return sum.get(id);
        }
        return 0L;
    }

    public void addOperationCount(final String id) {
        long counter = 0;
        if (operationsCount.containsKey(id)) {
            counter = operationsCount.get(id);
        }
        operationsCount.put(id, ++counter);
    }

    /**
     * sum up id
     * can be used instead of stop to sum mutiple time span with one id
     *
     * @param id
     */
    public void sum(final String id) {
        synchronized (this) {
            stop(id);
            Long diff = getDiff(id);
            if (sum.containsKey(id)) {
                diff += sum.get(id);
            }
            sum.put(id, diff);
            addOperationCount(id);
        }
    }



    public String getSumString(final String id) {
        StringBuilder sb = new StringBuilder();
        synchronized (this) {
            if (names.containsKey(id)) {
                sb.append(names.get(id)).append(":");
            }
            if (sum.containsKey(id)) {
                double operationDuration = getSum(id);
                sb.append(getFormatedTimeMS(getSum(id)));
                if (operationsCount.get(id) > 0) {
                    double msPerOperation = operationDuration;
                    msPerOperation /= operationsCount.get(id);
                    sb.append(", ").append(getFormatedTimeMS(msPerOperation)).append(" per op");
                }
                sb.append(", ").append(operationsCount.get(id)).append(" ops");
            } else {
                sb.append(" 0 ms, 0 ops");
            }
        }
        return sb.toString();
    }

    public String getSumStringShort(final String id) {
        StringBuilder sb = new StringBuilder();
        synchronized (this) {
            if (names.containsKey(id)) {
                sb.append(names.get(id)).append(":");
            }
            if (sum.containsKey(id)) {
                double operationDuration = getSum(id);
                sb.append(getFormatedTimeMS(getSum(id)));
            } else {
                sb.append(" 0 ms, 0 ops");
            }
        }
        return sb.toString();
    }

    private String getFormatedTimeMS(double number) {
        if (number < 999.0f) return String.format("%.4f ms", number);
        number = number * 0.001;
        if (number < 120.0f) return String.format("%.3f s", number);
        number = number / 60.0;
        return String.format("%.3f min", number);
    }

}