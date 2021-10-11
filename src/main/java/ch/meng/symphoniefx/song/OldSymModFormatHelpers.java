package ch.meng.symphoniefx.song;


import org.apache.log4j.Logger;
import symreader.SymphonieEventType;
import symreader.SymphonieVolume;

import java.lang.invoke.MethodHandles;

public class OldSymModFormatHelpers {
    protected static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    static float factor255to100 = 100.0f / 255.0f;

    public static SongEvent ConvertEvent(int FXType, int ParaA, int ParaB, int ParaC) {
        SongEvent se = new SongEvent();
        FXType = FXType & 0x000000ff;
        ParaA = ParaA & 0x000000ff;
        ParaB = ParaB & 0x000000ff;
        ParaC = ParaC & 0x000000ff;
        se.setD(0);
        se.setSongFXType(SongEventType.FX_NONE);
        if ((FXType == 0) && (ParaB == 255) && (ParaA == 0) && (ParaC == 0)) {
            se.setSongFXType(SongEventType.FX_NONE);
            se.setA(0);
            se.setB(0);
            se.C = 0;
        } else {
            se.A = ParaA;
            se.B = ParaB;
            se.C = ParaC;
            switch (FXType) {
                case (SymphonieEventType.FX_KEYON): {
                    se.setKeyOn();
                    if (ParaC > SymphonieVolume.VOLUME_COMMAND) {
                        switch (ParaC) {

                            case 251:
                                se.songFXType = SongEventType.FX_KEYOFF;
                                se.setKeyOff();
                                break;
                            case 252:
                                se.songFXType = SongEventType.FX_STARTSAMPLE;
                                se.setContinue();
                                break;
                            case 253:
                                se.songFXType = SongEventType.FX_CONTSAMPLE;
                                se.setContinue();
                                break;
                            case 254:
                                se.songFXType = SongEventType.FX_STOPSAMPLE;
                                se.setStop();
                                break;
                            case 250:
                                se.songFXType = SongEventType.FX_SPEEDDOWN;
                                break;
                            case 249:
                                se.songFXType = SongEventType.FX_SPEEDUP;
                                break;
                            case 246:
                                se.songFXType = SongEventType.FX_PITCHDOWN;
                                break;
                            case 244:
                                se.songFXType = SongEventType.FX_PITCHDOWN2;
                                break;
                            case 242:
                                se.songFXType = SongEventType.FX_PITCHDOWN3;
                                break;
                            case 247:
                                se.songFXType = SongEventType.FX_PITCHUP;
                                break;
                            case 245:
                                se.songFXType = SongEventType.FX_PITCHUP2;
                                break;
                            case 243:
                                se.songFXType = SongEventType.FX_PITCHUP3;
                                break;
                            case 248:
                                se.songFXType = SongEventType.FX_SETPITCH;
                                se.setPitch();
                                break;
                        } // end case volume special fx
                    } else {
                        if (se.B == 255) {
                            se.B = 0;
                            se.setVolume();
                        } else {
                            se.setKeyOn();//todo:
                        }
                    }
                } //end of Keyon case
                break;

                // Volume FX
                case (SymphonieEventType.FX_ADDVOLUME):
                    se.setVolumeAdd();
                    break;
                case (SymphonieEventType.FX_CV):
                    se.setCV(); // todo:
                    break;
                case (SymphonieEventType.FX_CVADD):
                    se.setCVAdd();  // todo:
                    break;
                case (SymphonieEventType.FX_VIBRATO):
                    se.setVibrato();
                    break;
                case (SymphonieEventType.FX_VOLUMESLIDEDOWN):
                    se.setVolumeSlideDown();
                    se.A = 0;
                    se.B = 0;
                    se.D = se.C;
                    se.C = 0;
                    break;
                case (SymphonieEventType.FX_VOLUMESLIDEUP):
                    se.setVolumeSlideUp();
                    se.A = 0;
                    se.B = 0;
                    se.D = se.C;
                    se.C = 0;
                    break;

                // Pitch FX
                case (SymphonieEventType.FX_ADDPITCH):
                    se.setPitchAdd();
                    se.D = se.C;
                    se.C = 0;
                    break;
                case (SymphonieEventType.FX_ADDHALVTONE):
                    se.D = se.C;
                    se.C = 0;
                    se.songFXType = SongEventType.FX_ADDHALVTONE;
                    break;
                case (SymphonieEventType.FX_PITCHSLIDEDOWN):
                    se.setPitchSlideDown();
                    se.D = se.C;
                    se.C = 0;
                    break;
                case (SymphonieEventType.FX_PITCHSLIDEUP):
                    se.setPitchSlideUp();
                    se.D = se.C;
                    se.C = 0;
                    break;
                case (SymphonieEventType.FX_PSLIDETO):
                    se.setPitchTo();
                    se.D = se.C;
                    se.C = 0;
                    break;
                case (SymphonieEventType.FX_TREMOLO):
                    se.setTremolo();
                    break;

                // DSP Fx
                case (SymphonieEventType.FX_DSPCHOR):
                    se.songFXType = SongEventType.FX_DSPCHOR;
                    se.setDspOff();
                    break;
                case (SymphonieEventType.FX_DSPDELAY):
                    se.songFXType = SongEventType.FX_DSPDELAY;
                    se.setDspOff();
                    break;
                case (SymphonieEventType.FX_DSPECHO):
                    se.D = se.C;
                    se.C = 0;
                    if (se.B == 0) { // off -> 0%
                        se.songFXType = SongEventType.FX_DSP_DISABLE;
                        se.A = 0;
                        se.B = 0;
                        se.C = 0;
                        se.D = 0;
                        se.setDspOff();
                    }
                    if (se.B == 1) {
                        se.songFXType = SongEventType.FX_DSPECHO;
                        int temp = 1 << (int) se.A;
                        float tempf = temp;
                        se.C = 100 / tempf;
                        se.setDspEcho();
                    }
                    if (se.B == 2) {
                        se.songFXType = SongEventType.FX_DSPCROSSECHO;
                        int temp = 1 << (int) se.A;
                        se.C = (100 / temp);
                        se.setDspCrossEcho();
                    }
                    if (se.B == 3) {
                        se.songFXType = SongEventType.FX_DSPCROSSECHO;
                        int temp = 1 << (int) se.A;
                        se.C = 100 - (100 / temp);
                        se.setDspCrossEcho();
                    }
                    se.A = 0;
                    se.B = 0;
                    break;
                // Other FX
                case (SymphonieEventType.FX_EMPHASIS):
                    se.songFXType = SongEventType.FX_EMPHASIS;
                    break;
                case (SymphonieEventType.FX_FILTER):
                    //CFILTER_MAXRESO	EQU	185
                    //CFILTER_MAXFREQ	EQU	240
                    // RTPOSTFILTERWET	blk.l	20,0

                    //RTFILTER_FILTERFREQ	EQU	0
                    //RTFILTER_FILTERRESO     EQU     4
                    //RTFILTER_FILTERBUF      EQU     12
                    //RTFILTER_FILTERTYPE     EQU     8

                    se.songFXType = SongEventType.FX_CHANNELFILTER; // A=Filter Freq
                    break;
                case (SymphonieEventType.FX_SETSPEED):
                    se.D = se.C;
                    se.C = 0;
                    se.songFXType = SongEventType.FX_SETSPEED;
                    break;

                // Sample Pointer FX

                case (SymphonieEventType.FX_FROMANDPITCH):
                    se.D = se.C * factor255to100;
                    se.C = 0;
                    se.songFXType = SongEventType.FX_FROMANDPITCH;
                    se.setSampleFromAndPitch();
                    break;
                case (SymphonieEventType.FX_REPLAYFROM):
                    se.D = se.C * factor255to100;
                    se.C = 0;
                    se.songFXType = SongEventType.FX_REPLAYFROM;
                    se.setSampleFrom();
                    break;
                case (SymphonieEventType.FX_FROMADD):
                    se.D = se.C * factor255to100;
                    se.C = 0;
                    se.setSampleFromAdd();
                    break;
                case (SymphonieEventType.FX_SETFROMADD):
                    se.D = se.C * factor255to100;
                    se.C = 0;
                    se.setSampleFromAddSet();
                    break;
                case (SymphonieEventType.FX_RETRIG):
                    se.D = se.C;
                    se.C = 0;
                    se.songFXType = SongEventType.FX_RETRIG;
                    break;
                case (SymphonieEventType.FX_SAMPLEVIB):
                    se.D = se.C  * factor255to100;
                    se.C = 0;
                    se.songFXType = SongEventType.FX_SAMPLEVIB;
                    break;

            }
        }
        se.updateEventClass();
        return se;
    }

}
