package ch.meng.symphoniefx.song;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PatternVoice {
    private Map<Float, SongEventPool> eventPools = new HashMap<>();



    // jaxb
    public Map<Float, SongEventPool> getEventPools() {
        return eventPools;
    }

    public void setEventPools(Map<Float, SongEventPool> eventPools) {
        this.eventPools = eventPools;
    }

    public SongEventPool getSongEventPool(final float timePosition, boolean autoallocate) {
        if(eventPools.containsKey(timePosition)) return eventPools.get(timePosition);
        if(!autoallocate) return SongEventPool.emptySongEventPool;
        SongEventPool eventPool = new SongEventPool();
        eventPools.put(timePosition, eventPool);
        eventPool.getSongEvent(true);
        return eventPool;
    }

    public void removeEvents(final float timePosition) {
        eventPools.remove(timePosition);
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        eventPools.forEach((key, value) -> text.append(value.toString()).append(" "));
        return text.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternVoice that = (PatternVoice) o;
        return Objects.equals(eventPools, that.eventPools);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventPools);
    }
}
