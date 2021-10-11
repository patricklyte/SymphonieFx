package symreader;

public interface SymphonieVolume {
    int VOLUME_STOPSAMPLE = 254;
    int VOLUME_CONTSAMPLE = 253;
    int VOLUME_STARTSAMPLE = 252;
    int VOLUME_KEYOFF = 251;
    int VOLUME_SPEEDDOWN = 250;
    int VOLUME_SPEEDUP = 249;
    int VOLUME_SETPITCH = 248;
    int VOLUME_PITCHUP = 247;
    int VOLUME_PITCHDOWN = 246;
    int VOLUME_PITCHUP2 = 245;
    int VOLUME_PITCHDOWN2 = 244;
    int VOLUME_PITCHUP3 = 243;
    int VOLUME_PITCHDOWN3 = 242;
    int VOLUME_NONE = 0;
    int VOLUME_MIN = 1;
    int VOLUME_MAX = 100;
    int VOLUME_COMMAND = 200;
}
