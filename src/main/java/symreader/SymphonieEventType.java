package symreader;

public interface SymphonieEventType { // Event Types as in SymphonieNote.NOTE_FX
    int FX_KEYON = 0;
    int FX_VOLUMESLIDEUP = 1;
    int FX_VOLUMESLIDEDOWN = 2;
    int FX_PITCHSLIDEUP = 3;
    int FX_PITCHSLIDEDOWN = 4;
    int FX_REPLAYFROM = 5;
    int FX_FROMANDPITCH = 6;
    int FX_SETFROMADD = 7;
    int FX_FROMADD = 8;
    int FX_SETSPEED = 9;
    int FX_ADDPITCH = 10;
    int FX_ADDVOLUME = 11;
    int FX_VIBRATO = 12;
    int FX_TREMOLO = 13;
    int FX_SAMPLEVIB = 14;
    int FX_PSLIDETO = 15;
    int FX_RETRIG = 16;
    int FX_EMPHASIS = 17;
    int FX_ADDHALVTONE = 18;
    int FX_CV = 19;
    int FX_CVADD = 20;
    int FX_FILTER = 23;
    int FX_DSPECHO = 24; // Echo is defined as repeating delays
    int FX_DSPDELAY = 25; // Delay
    int FX_DSPCHOR = 26; // Chorus Experimental
}