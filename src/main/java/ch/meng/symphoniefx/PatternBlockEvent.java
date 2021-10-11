package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.SongEvent;

public class PatternBlockEvent {
    private double x;
    private double y;
    private SongEvent songEvent;
    PatternBlockEvent(double x, double y, SongEvent songEvent) {
        this.x = x;
        this.y = y;
        this.songEvent = songEvent;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public SongEvent getSongEvent() {
        return songEvent;
    }

    public void setSongEvent(SongEvent songEvent) {
        this.songEvent = songEvent;
    }
}
