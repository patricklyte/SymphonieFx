package ch.meng.symphoniefx.song;

public interface SongEventClass { // Event Types as in SymphonieNote.NOTE_FX
    //int NONE        =-1;
    int GENERAL = 0;
    int VOLUME = 1;
    int PITCH = 2;
    int SAMPLE = 3;
    int SPEED = 4;
    int DSP = 5;
    int MAX = 5;
    int ROWNR = 10;
}
