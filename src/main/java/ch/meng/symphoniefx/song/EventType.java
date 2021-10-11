package ch.meng.symphoniefx.song;

public enum EventType {
    None,
    KeyOn, KeyOff, Retrig, Emphasis, Stop, Continue,
    VolumeSet, VolumeAdd, VolumeSlideUp, VolumeSlideDown, Vibrato, CV, CVAdd,
    PitchSet, PitchAdd, PitchSlideUp, PitchSlideDown, PitchSlideTo, Tremolo,
    SampleFrom, SampleFromAndPitch, SampleFromPitchVolume, SampleFromAddSet, SampleFromAdd, SampleVibrato,
    DspCrossEcho, DspEcho, DspOff
}

//
//package symreader;
//
//public interface SongEventType { // Event Types as in SymphonieNote.NOTE_FX
//    // General
//    int FX_NONE = 0;
//    int FX_KEYON = 1;
//    int FX_KEYOFF = 2;
//    int FX_RETRIG = 3;
//    int FX_EMPHASIS = 4;
//
//    // Volume
//    //int FX_CLASS_VOL        =1000;
//    int FX_SETVOLUME = 1000;
//    int FX_VOLUMESLIDEUP = 1001;
//    int FX_VOLUMESLIDEDOWN = 1002;
//    int FX_ADDVOLUME = 1003;
//    int FX_VIBRATO = 1004;
//    int FX_CV = 1005;
//    int FX_CVADD = 1006;
//    int FX_STOPSAMPLE = 1007;
//    int FX_CONTSAMPLE = 1008;
//    int FX_STARTSAMPLE = 1009;
//
//    // Pitch
//    //int FX_CLASS_PITCH      =2000;
//    int FX_SETPITCH = 2000;
//    int FX_PSLIDETO = 2001;
//    int FX_PITCHSLIDEUP = 2002;
//    int FX_PITCHSLIDEDOWN = 2003;
//    int FX_ADDPITCH = 2004;
//    int FX_TREMOLO = 2005;
//    int FX_ADDHALVTONE = 2006;
//    int FX_PITCHUP = 2007;
//    int FX_PITCHDOWN = 2008;
//    int FX_PITCHUP2 = 2009;
//    int FX_PITCHDOWN2 = 2010;
//    int FX_PITCHUP3 = 2011;
//    int FX_PITCHDOWN3 = 2012;
//
//    // Sample
//    //int FX_CLASS_SAMPLE     =3000;
//    int FX_REPLAYFROM = 3000;
//    int FX_FROMANDPITCH = 3001;
//    int FX_FROMANDPITCHVOL = 3002;
//    int FX_SETFROMADD = 3003;
//    int FX_FROMADD = 3004;
//    int FX_SAMPLEVIB = 3005;
//
//    // Speed
//    int FX_CLASS_SPEED = 4000;
//    int FX_SETSPEED = 4000;
//    int FX_SPEEDDOWN = 4001;
//    int FX_SPEEDUP = 4002;
//
//    // Dsp
//    int FX_CLASS_DSP = 5000;
//    int FX_CHANNELFILTER = 5000;
//    int FX_DSPECHO = 5001;      // Echo is defined as repeating delays
//    int FX_DSPCROSSECHO = 5002; // Echo is defined as repeating delays
//    int FX_DSPDELAY = 5003;     // Delay
//    int FX_DSPCROSSDELAY = 5004; // Delay
//    int FX_DSPCHOR = 5005;      // Chorus Experimental
//    int FX_FILTERPerfectLP4 = 5006; // DSPFilterPerfectLP4
//    int FX_DSP_DISABLE = 5007; // Disable Echo, Delay and Chorus
//
//    int FX_OTHER = -1;
//}