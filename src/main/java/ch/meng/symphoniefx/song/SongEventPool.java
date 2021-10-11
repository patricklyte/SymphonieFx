package ch.meng.symphoniefx.song;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

// a pool of events at a specific position in time
public class SongEventPool {
    private List<SongEvent> songEvents = new Vector<>();

    public static final SongEvent emptySongEvent = new SongEvent();
    static final SongEventPool emptySongEventPool = new SongEventPool();

    public boolean isEmpty() {
        return this==emptySongEventPool;
    }

    // only for ui
    private double cellRowIndex;
    private double cellColumnIndex;

    // jaxb export
    public List<SongEvent> getSongEvents() {
        if(songEvents.isEmpty() || songEvents.get(0).isEmpty()) return null;
        return songEvents;
    }

    public void setSongEvents(List<SongEvent> songEvents) {
        this.songEvents = songEvents;
    }

    int getNumberOfSongEvents() {return songEvents.size();}


    public SongEvent getSongEvent(boolean autoallocate) {
        if(!autoallocate && songEvents.isEmpty()) return emptySongEvent;
        if(songEvents.isEmpty()) {
            songEvents.add(new SongEvent());
        }
        return songEvents.get(0);
    }

    void set(final SongEvent event) {
        if(songEvents.isEmpty()) {
            songEvents.add(event);
            return;
        }
        songEvents.set(0, event);
    }

    public void setCellPosition(final double x, final double y) {
        cellColumnIndex = x;
        cellRowIndex = y;
    }
    public double getCellX() {return cellColumnIndex;}
    public double getCellY() {return cellRowIndex;}

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (SongEvent event : songEvents) {text.append(event.toString()).append(" ");}
        return text.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongEventPool eventPool = (SongEventPool) o;
        return Objects.equals(songEvents, eventPool.songEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songEvents);
    }
}
