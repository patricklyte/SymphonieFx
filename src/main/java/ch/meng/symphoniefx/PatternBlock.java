package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SongEvent;

import java.util.List;
import java.util.Vector;

public class PatternBlock {
    private int xStart = -1;
    private int yStart;
    private int xEnd;
    private int yEnd;
    private int patternNr;
    private Song song;
    private boolean selected = false;
    private PatternController patternController;

    PatternBlock(final Song song, final PatternController patternController) {
        this.song = song;
        this.patternController = patternController;
    }

    int getBlockHeight() {
        return Math.abs(yEnd - yStart) + 1;
    }

    int getBlockWidth() {
        return Math.abs(xEnd - xStart) + 1;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    boolean isInsideBlock(final double x, final double y) {
        if (!selected) return false;
        if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) return true;
        return false;
    }

    void selectPattern() {
        markBegin(1, 0);
        markEnd(song.getNumbOfVoices(), song.getNumbOfRows() - 1, patternController.getPatternZ());
    }

    void selectTrack() {
        markBegin(patternController.getPatternX(), 0);
        markEnd(patternController.getPatternX(), song.getNumbOfRows() - 1, patternController.getPatternZ());
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    void markBegin(int x, int y) {
        selected = false;
        xStart = x;
        yStart = y;
    }

    void updateEnd(int x, int y) {
        xEnd = x;
        yEnd = y;

    }

    void markEnd(int x, int y, final int patternNr) {
        if (x < xStart) {
            xEnd = xStart;
            xStart = x;
        } else {
            xEnd = x;
        }
        if (y < yStart) {
            yEnd = yStart;
            yStart = y;
        } else {
            yEnd = y;
        }
        this.patternNr = patternNr;
        selected = true;
        copy();
    }

    public int getxStart() {
        return xStart;
    }

    public void setxStart(int xStart) {
        this.xStart = xStart;
    }

    public int getyStart() {
        return yStart;
    }

    public void setyStart(int yStart) {
        this.yStart = yStart;
    }

    public int getxEnd() {
        return xEnd;
    }

    public void setxEnd(int xEnd) {
        this.xEnd = xEnd;
    }

    public int getyEnd() {
        return yEnd;
    }

    public void setyEnd(int yEnd) {
        this.yEnd = yEnd;
    }

    public int getPatternNr() {
        return patternNr;
    }

    public void setPatternNr(int patternNr) {
        this.patternNr = patternNr;
    }

    List<PatternBlockEvent> events = new Vector<>();

    void copy() {
        events.clear();
        for (int x = getxStart(); x < (getxEnd() + 1); x++) {
            for (int y = getyStart(); y < (getyEnd() + 1); y++) {
                SongEvent songEvent = song.getSongEvent(patternNr, x, y);
                PatternBlockEvent event = new PatternBlockEvent(x - getxStart(), y - getyStart(), songEvent);
                events.add(event);
            }
        }
    }

    void paste(final int x, final int y, final boolean copyEmpty) {
        if (events.isEmpty()) return;
        for (PatternBlockEvent event : events) {
            if (!copyEmpty && event.getSongEvent().isEmpty()) continue;
            patternController.setSongEventRelativeTo(x, y, event.getX(), event.getY(), event.getSongEvent());
        }
        patternController.updatePatternViewForce();
    }

    void paste(final boolean copyEmpty) {
        if (events.isEmpty()) return;
        for (PatternBlockEvent event : events) {
            if (!copyEmpty && event.getSongEvent().isEmpty()) continue;
            patternController.setSongEventRelativeToCrsr(event.getX(), event.getY(), event.getSongEvent());
        }
        patternController.updatePatternViewForce();
    }

    void copyBack() {
        if (events.isEmpty()) return;
        for (PatternBlockEvent event : events) {
            patternController.setSongEventAt(event.getX(), event.getY(), event.getSongEvent());
        }
        patternController.updatePatternViewForce();
    }

    void fill(final boolean copyEmpty) {
        if (events.isEmpty()) return;
        int y = 0;
        while (y < song.getNumbOfRows()) {
            for (PatternBlockEvent event : events) {
                if ((y + event.getY()) > song.getNumbOfRows()) break;
                if (!copyEmpty && event.getSongEvent().isEmpty()) continue;
                patternController.setSongEventRelativeToCrsr(event.getX(), event.getY() + y, event.getSongEvent());
            }
            y += getBlockHeight();
        }
        patternController.updatePatternViewForce();
    }

    void modifyPitch(final int dPitch) {
        for (PatternBlockEvent patternBlockEvent : events) {
            patternBlockEvent.getSongEvent().modifyPitch(dPitch);
        }
        patternController.updatePatternViewForce();
    }

    void convertKeyToFrom() {
        for (PatternBlockEvent patternBlockEvent : events) {
            patternBlockEvent.getSongEvent().convertKeyToFrom();
        }
        patternController.updatePatternViewForce();
    }

    int getNumberOfEvents() {
        return (int) events.stream().filter(event -> !(event.getSongEvent().isEmpty())).count();
    }

    @Override
    public String toString() {
        if (xStart < 0) return "";
        final StringBuilder text = new StringBuilder();
        text.append("Block ").append(xStart).append(",").append(yStart);
        if (selected) {
            text.append(" to ").append(xEnd).append(",").append(yEnd)
                    .append(" w").append(getBlockWidth()).append(" h").append(getBlockHeight())
                    .append(" events:").append(getNumberOfEvents());
        } else {
            text.append(" Marking");
        }

        return text.toString();
    }

}
