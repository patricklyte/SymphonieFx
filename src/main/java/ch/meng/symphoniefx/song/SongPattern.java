package ch.meng.symphoniefx.song;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class SongPattern {
    private final String Name = "";
    private Song song = null;
    private int numberOfRows = 0;
    private List<PatternVoice> patternVoices = new Vector<>();

    private SongPattern() {
    }
    void addVoice() {
        PatternVoice patternVoice = new PatternVoice();
        patternVoice.getSongEventPool(0,true);
        patternVoices.add(patternVoice);
    }

    public List<PatternVoice> getPatternVoices() {
        return patternVoices;
    }

    public void setPatternVoices(List<PatternVoice> patternVoices) {
        this.patternVoices = patternVoices;
    }

    public PatternVoice getPatternVoice(int index) {
        return patternVoices.get(index);
    }

    SongPattern(final Song song, final int numberOfVoices, final int numberOfRows) {
        this.numberOfRows = numberOfRows;
        this.song = song;
        for (int i = 0; i < numberOfVoices; i++) {
            addVoice();
        }
    }

    public void setNumberOfRows(final int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public int getNumberOfRows() {
        return (numberOfRows);
    }

    SongEvent getSongEvent(final int voiceIndex, final float timePosition) { // autoallocates new if needed
        if(numberOfRows < 1) throw new RuntimeException("numberOfRows is 0");
        if (voiceIndex < patternVoices.size() && timePosition < numberOfRows && voiceIndex>=0 && timePosition>=0) {
            return patternVoices.get(voiceIndex).getSongEventPool(timePosition, false).getSongEvent(false);
        } else {
            return null;
        }
    }

    SongEventPool getSongEventPool(final int voiceIndex, final float timePosition) { // autoallocates new if needed
        if(numberOfRows < 1) throw new RuntimeException("numberOfRows is 0");
        if (voiceIndex < patternVoices.size() && timePosition < numberOfRows) {
            return patternVoices.get(voiceIndex).getSongEventPool(timePosition, false);
        } else {
            return null;
            //throw new RuntimeException("voiceIndex "+voiceIndex + " or rowindex" + timePosition +" illegal");
        }
    }

    public void setSongEvent(final int voiceIndex, final float timePosition, final SongEvent songEvent) {
        if(songEvent.isEmpty()) return;
        patternVoices.get(voiceIndex).getSongEventPool(timePosition, true).set(songEvent);
    }

    public String toStringNoEmpty() {
        StringBuilder text = new StringBuilder();
        for (int rowNr = 0; rowNr < this.getNumberOfRows(); rowNr++) {
            int columnNr = 0;
            for (PatternVoice patternVoice : patternVoices) {
                if (!getSongEvent(columnNr, rowNr).isEmpty()) text.append(getSongEvent(columnNr, rowNr).toString()).append(" ");
            }
        }
        text.append("\n");
        return text.toString();
    }


    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int rowNr = 0; rowNr < this.getNumberOfRows(); rowNr++) {
            int columnNr = 0;
            for (PatternVoice patternVoice : patternVoices) {
                if (getSongEvent(columnNr, rowNr) == null) {
                    text.append("null Songevent");
                } else {
                    text.append(getSongEvent(columnNr, rowNr).toString()).append(" ");
                }
            }
            text.append("\n");
        }
        return text.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongPattern that = (SongPattern) o;
        return numberOfRows == that.numberOfRows && Objects.equals(patternVoices, that.patternVoices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfRows, patternVoices);
    }
}
