package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.SongEventPool;

import java.util.List;
import java.util.Vector;

public class PatternRow {
    List<SongEventPool> cells = new Vector();
    int rowNr = 0;

    void add(SongEventPool event, int rowNr) {
        this.rowNr = rowNr;
        cells.add(event);
    }
    SongEventPool get(int index) {

        if(index > (cells.size()-1)) {
            return new SongEventPool();
        }
        return cells.get(index);
    }

    public int getRowNr() {
        return rowNr;
    }
}
