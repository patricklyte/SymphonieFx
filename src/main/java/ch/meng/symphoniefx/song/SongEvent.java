package ch.meng.symphoniefx.song;

import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

public class SongEvent {
    private double version = 1.0;
    String key;
    //@Deprecated
    int fxClass;
    //@Deprecated
    int songFXType = SongEventType.FX_NONE;

    private EventClass eventClass = EventClass.None;
    private EventType eventType = EventType.None;
    float A=0, B=0, C=0, D=0;
    double lengthTicks = 0;

    private Map<ValueType, EventValueBase> values = new HashMap<>();
    public Map<ValueType, EventValueBase> getValues() {
        return values;
    }

    public double getLengthTicks() {
        return lengthTicks;
    }

    public void setLengthTicks(double lengthTicks) {
        this.lengthTicks = lengthTicks;
    }

    public EventClass getEventClass() {
        return eventClass;
    }
    public void setEventClass(EventClass eventClass) {
        this.eventClass = eventClass;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public void setValues(Map<ValueType, EventValueBase> values) {
        this.values = values;
    }

    public void setValue(ValueType type, double value) {
        if(values.containsKey(type)) {
            values.get(type).setValue(value);
        } else {
            values.put(type, EventValueFactory.get(type, value));
        }
    }
    public double getValue(ValueType type) {
        if(values.containsKey(type)) {
            return values.get(type).getValue();
        }
        return 0;
    }

    public SongEvent() {
        updateEventClass();
    }

    public int getFxClass() {
        return fxClass;
    }

    public void setFxClass(int fxClass) {
        this.fxClass = fxClass;
    }

    public int getSongFXType() {
        return songFXType;
    }

    public void setSongFXType(int songFXType) {
        this.songFXType = songFXType;
    }

    public boolean isEmpty() {
        return SongEventType.FX_NONE==songFXType;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setA(float a) {
        A = a;
    }
    public void setB(float b) {
        B = b;
    }
    public void setC(float c) {
        C = c;
    }
    public void setD(float d) {
        D = d;
    }
    public float getA() {
        return A;
    }
    public float getB() {
        return B;
    }
    public float getC() {
        return C;
    }
    public float getD() {
        return D;
    }

    @XmlTransient
    public int getInstrument() {
        return (int) A;
    }

    @XmlTransient
    public int getPitch() {
        return (int) B;
    }

    public void setPitch(int pitch) {
        B= pitch;
    }

    @XmlTransient
    public float getVolume() {
        return C;
    }

    @XmlTransient
    public float getSamplePosition() {
        return D;
    }


    void updateEventClass() {
        fxClass = songFXType / 1000;
    }

    public void set(SongEvent sourceEvent) {
        if (sourceEvent != null) {
            A = sourceEvent.A;
            B = sourceEvent.B;
            C = sourceEvent.C;
            D = sourceEvent.D;
            eventType = sourceEvent.eventType;
            eventClass = sourceEvent.eventClass;
            lengthTicks = sourceEvent.lengthTicks;
            songFXType = sourceEvent.songFXType;
            updateEventClass();
        }
    }


    void setType(int i) {
        songFXType = i;
        updateEventClass();
    }

    void clear() {
        songFXType = SongEventType.FX_NONE;
        fxClass = SongEventClass.GENERAL;
        eventType = EventType.None;
        eventClass = EventClass.None;
        A = 0;
        B = 0;
        C = 0;
        D = 0;
    }

    public void setKeyOff(int InstrNr, int Pitch, float Vol) {
        A = InstrNr;
        B = Pitch;
        C = Vol;
        setKeyOff();
    }

    public void setKeyOff() {
        songFXType = SongEventType.FX_KEYOFF;
        eventType = EventType.KeyOff;
        eventClass = EventClass.Key;
        updateEventClass();
    }

    public void setKeyOn(int InstrNr, int Pitch, float Vol) {
        A = InstrNr;
        B = Pitch;
        C = Vol;
        setKeyOn();
    }

    public void setKeyOn() {
        songFXType = SongEventType.FX_KEYON;
        eventType = EventType.KeyOn;
        eventClass = EventClass.Key;
        updateEventClass();
    }

    public void setSampleFromAndPitch(int InstrNr, int Pitch, float SamplePos) {
        A = InstrNr;
        B = Pitch;
        D = SamplePos;
    }

    public void setSampleFromAndPitch() {
        songFXType = SongEventType.FX_FROMANDPITCH;
        eventType = EventType.SampleFromAndPitch;
        eventClass = EventClass.Sample;
        updateEventClass();
    }

    public void setSampleFrom(int InstrNr, float SamplePos) {
        A = InstrNr;
        D = SamplePos;
    }

    public void setSampleFromPosition(float SamplePos) {
        D = SamplePos;
    }

    public void setSampleFrom() {
        songFXType = SongEventType.FX_REPLAYFROM;
        eventType = EventType.SampleFrom;
        eventClass = EventClass.Sample;
        updateEventClass();
    }
    public void setSampleFromAdd() {
        songFXType = SongEventType.FX_FROMADD;
        eventType = EventType.SampleFromAdd;
        eventClass = EventClass.Sample;
        updateEventClass();
    }
    public void setSampleFromAddSet() {
        songFXType = SongEventType.FX_FROMADD_SET;
        eventType = EventType.SampleFromAddSet;
        eventClass = EventClass.Sample;
        updateEventClass();
    }
    public void setRetrig(int InstrNr, float SamplePos) {
    }

    public void setRetrig() {
        songFXType = SongEventType.FX_RETRIG;
        eventType = EventType.Retrig;
        eventClass = EventClass.Key;
        updateEventClass();
    }
    public void setEmphasis(int InstrNr, float SamplePos) {
    }

    public void setEmphasis() {
        songFXType = SongEventType.FX_EMPHASIS;
        eventType = EventType.Emphasis;
        eventClass = EventClass.Key;
        updateEventClass();
    }

    public void setVolume(float Vol) {
        C = Vol;
        setVolume();
    }

    public void setVolume() {
        songFXType = SongEventType.FX_SETVOLUME;
        eventType = EventType.VolumeSet;
        eventClass = EventClass.Volune;
        updateEventClass();
    }

    public void setVolumeAdd(float Vol) {
        C = Vol;
        setVolumeAdd();
    }
    public void setVolumeAdd() {
        songFXType = SongEventType.FX_ADDVOLUME;
        eventType = EventType.VolumeAdd;
        eventClass = EventClass.Volune;
        updateEventClass();
    }

    public void setVolumeSlideUp(float Vol) {
        C = Vol;
        setVolumeSlideUp();
    }
    public void setVolumeSlideUp() {
        songFXType = SongEventType.FX_VOLUMESLIDEUP;
        eventType = EventType.VolumeSlideUp;
        eventClass = EventClass.Volune;
        updateEventClass();
    }

    public void setVolumeSlideDown(float Vol) {
        C = Vol;
        setVolumeSlideDown();
    }
    public void setVolumeSlideDown() {
        songFXType = SongEventType.FX_VOLUMESLIDEDOWN;
        eventType = EventType.VolumeSlideDown;
        eventClass = EventClass.Volune;
        updateEventClass();
    }
    public void setCV() {
        songFXType = SongEventType.FX_CV;
        eventType = EventType.CV;
        eventClass = EventClass.Volune;
        updateEventClass();
    }
    public void setCVAdd() {
        songFXType = SongEventType.FX_CVADD;
        eventType = EventType.CVAdd;
        eventClass = EventClass.Volune;
        updateEventClass();
    }
    public void setVibrato() {
        songFXType = SongEventType.FX_VIBRATO;
        eventType = EventType.Vibrato;
        eventClass = EventClass.Volune;
        updateEventClass();
    }
    public void setTremolo() {
        songFXType = SongEventType.FX_TREMOLO;
        eventType = EventType.Tremolo;
        eventClass = EventClass.Pitch;
        updateEventClass();
    }

    public void setDspCrossEcho() {
        songFXType = SongEventType.FX_DSPCROSSECHO;
        eventType = EventType.DspCrossEcho;
        eventClass = EventClass.Dsp;
        updateEventClass();
    }
    public void setDspEcho() {
        songFXType = SongEventType.FX_DSPECHO;
        eventType = EventType.DspEcho;
        eventClass = EventClass.Dsp;
        updateEventClass();
    }
    public void setDspOff() {
        songFXType = SongEventType.FX_DSP_DISABLE;
        eventType = EventType.DspOff;
        eventClass = EventClass.Dsp;
        updateEventClass();
    }
    public void setPitch(int InstrNr, int Pitch) {
        A = InstrNr;
        B = Pitch;
        setPitch();
    }

    public void setPitchTo(int InstrNr, int Pitch) {
        A = InstrNr;
        B = Pitch;
        setPitchTo();
    }

    public void setPitchTo() {
        songFXType = SongEventType.FX_PSLIDETO;
        eventType = EventType.PitchSlideTo;
        eventClass = EventClass.Pitch;
        updateEventClass();
    }

    public void setPitchAdd() {
        songFXType = SongEventType.FX_ADDPITCH;
        eventType = EventType.PitchAdd;
        eventClass = EventClass.Pitch;
        updateEventClass();
    }

    public void setPitch() {
        songFXType = SongEventType.FX_SETPITCH;
        eventType = EventType.PitchSet;
        eventClass = EventClass.Pitch;
        updateEventClass();
    }
    public void setPitchSlideDown() {
        songFXType = SongEventType.FX_PITCHSLIDEDOWN;
        eventType = EventType.PitchSlideDown;
        eventClass = EventClass.Pitch;
        updateEventClass();
    }
    public void setPitchSlideUp() {
        songFXType = SongEventType.FX_PITCHSLIDEUP;
        eventType = EventType.PitchSlideUp;
        eventClass = EventClass.Pitch;
        updateEventClass();
    }
    public void setStop() {
        songFXType = SongEventType.FX_STOPSAMPLE;
        eventType = EventType.Stop;
        eventClass = EventClass.Key;
        updateEventClass();
    }

    public void setContinue() {
        songFXType = SongEventType.FX_CONTSAMPLE;
        eventType = EventType.Continue;
        eventClass = EventClass.Key;
        updateEventClass();
    }

    int getInstrIndexID() { //-1 = none
        if ((songFXType == SongEventType.FX_SETPITCH) ||
                (songFXType == SongEventType.FX_FROMANDPITCH) ||
                (songFXType == SongEventType.FX_KEYON) ||
                (songFXType == SongEventType.FX_REPLAYFROM)
        ) {
            return ((int) this.B);
        } else {
            return (-1);
        }

    }

    String notes = "C C#D D#E F F#G G#A A#H ";

    String getNotePitch(final int pitch) {
        int noteIndex = (pitch % 12) * 2;
        int octave = pitch / 12;
        StringBuilder text = new StringBuilder();
        text.append(notes.substring(noteIndex, noteIndex + 2).trim());
        text.append(octave);
        return text.toString();
    }

    String getNotePitchAsString() {
        return getNotePitch(getPitch());
    }

    String getSpecialParameterAsString() {
        return Float.toString(getD());
    }

    double getSpecialParameter() {
        return getD();
    }

    String getFromSamplePosition() {
        return String.format("%.2f",getD()) + "%";
    }

    @Override
    public String toString() {
        return getDescription(true);
    }

    private final static String emptyString = "";

    public String getDescription(boolean showEmpty) {
        switch (songFXType) {
            case SongEventType.FX_NONE: {
                if (showEmpty) return "NONE";
                return emptyString;
            }
            case SongEventType.FX_KEYON:
                return "Keyon" + getNotePitchAsString() + " I" + getInstrument() + " V" + (int) getVolume();
            case SongEventType.FX_KEYOFF:
                return "Keyoff";
            case SongEventType.FX_RETRIG:
                return "Retrig";
            case SongEventType.FX_EMPHASIS:
                return "Emphasis";

            case SongEventType.FX_SETPITCH:
                return "SetPitch " + getNotePitchAsString();
            case SongEventType.FX_PSLIDETO:
                return "PSlideTo " + getNotePitchAsString();
            case SongEventType.FX_PITCHSLIDEUP:
                return "PSlideUp " + getSpecialParameterAsString();
            case SongEventType.FX_PITCHSLIDEDOWN:
                return "PSlideDown " + getSpecialParameterAsString();
            case SongEventType.FX_ADDPITCH:
                return "AddPitch " + getSpecialParameterAsString();
            case SongEventType.FX_TREMOLO:
                return "TREMOLO";
            case SongEventType.FX_ADDHALVTONE:
                return "ADDHALVTONE";
            case SongEventType.FX_PITCHUP:
                return "PITCHUP";
            case SongEventType.FX_PITCHDOWN:
                return "PITCHDOWN";
            case SongEventType.FX_PITCHUP2:
                return "PITCHUP2";
            case SongEventType.FX_PITCHDOWN2:
                return "PITCHDOWN2";
            case SongEventType.FX_PITCHUP3:
                return "PITCHUP3";
            case SongEventType.FX_PITCHDOWN3:
                return "PITCHDOWN3";

            case SongEventType.FX_REPLAYFROM:
                return "ReplayFrom " + " i" + getInstrument() + " " + getFromSamplePosition();
            case SongEventType.FX_FROMANDPITCH:
                return "From&Pitch " + " i" + getInstrument() + " " + getNotePitchAsString() + " " + getFromSamplePosition();
            case SongEventType.FX_SAMPLEVIB:
                return "SampleVibrato " + getInstrument() + " " + getSpecialParameterAsString(); // speed, length
            case SongEventType.FX_FROMADD:
                return "FromAdd " + getFromSamplePosition();
            case SongEventType.FX_FROMADD_SET:
                return "FromSet " + getFromSamplePosition();


            case SongEventType.FX_SETVOLUME:
                return "SetVolume " + getVolume();
            case SongEventType.FX_VOLUMESLIDEUP:
                return "VSlideUp " + getSpecialParameterAsString();
            case SongEventType.FX_VOLUMESLIDEDOWN:
                return "VSlideDown " + getSpecialParameterAsString();
            case SongEventType.FX_ADDVOLUME:
                return "Volume Add " + getVolume();
            case SongEventType.FX_VIBRATO:
                return "VIBRATO";
            case SongEventType.FX_CV:
                return "CV "  + getVolume();
            case SongEventType.FX_CVADD:
                return "CV Add " + getVolume();
            case SongEventType.FX_STOPSAMPLE:
                return "Stop";
            case SongEventType.FX_CONTSAMPLE:
                return "Continue";
            case SongEventType.FX_STARTSAMPLE:
                return "STARTSAMPLE";

            case SongEventType.FX_CHANNELFILTER:
                return "CHANNELFILTER";
            case SongEventType.FX_DSPECHO:
                return "Dsp Echo " + getVolume() + " " + getD();
            case SongEventType.FX_DSPCROSSECHO:
                return "Dsp Crossecho" + getVolume() + " " + getD();
            case SongEventType.FX_DSPDELAY:
                return "DSPDELAY";
            case SongEventType.FX_DSPCROSSDELAY:
                return "DSPCROSSDELAY";
            case SongEventType.FX_FILTERPerfectLP4:
                return "FILTERPerfectLP4";

            case SongEventType.FX_SETSPEED:
                return "Speed "+getSpecialParameterAsString();
            case SongEventType.FX_SPEEDUP:
                return "Speedup "+getSpecialParameterAsString();
            case SongEventType.FX_SPEEDDOWN:
                return "SpeedDwn "+getSpecialParameterAsString();


        }
        return "";
    }

    public String getShortDescription() {
        switch (songFXType) {
            case SongEventType.FX_NONE: {
                return emptyString;
            }
            case SongEventType.FX_KEYON:
                return getNotePitchAsString() + " i" + getInstrument() + " " + (int) getVolume();
            case SongEventType.FX_KEYOFF:
                return "Keyoff";
            case SongEventType.FX_RETRIG:
                return "Retrig";
            case SongEventType.FX_EMPHASIS:
                return "Emphasis";

            case SongEventType.FX_SETPITCH:
                return "Pitch " + getNotePitchAsString();
            case SongEventType.FX_PSLIDETO:
                return "Pit to " + getNotePitchAsString();
            case SongEventType.FX_PITCHSLIDEUP:
                return "PSlUp " + getSpecialParameterAsString();
            case SongEventType.FX_PITCHSLIDEDOWN:
                return "PSlDwn "+ getSpecialParameterAsString();
            case SongEventType.FX_ADDPITCH:
                return "Pitch+" + getSpecialParameterAsString();
            case SongEventType.FX_TREMOLO:
                return "Tremolo";
            case SongEventType.FX_ADDHALVTONE:
                return "AddHt " + getSpecialParameterAsString();
            case SongEventType.FX_PITCHUP:
                return "PUp";
            case SongEventType.FX_PITCHDOWN:
                return "PDwn";
            case SongEventType.FX_PITCHUP2:
                return "PUp2";
            case SongEventType.FX_PITCHDOWN2:
                return "PDwn2";
            case SongEventType.FX_PITCHUP3:
                return "PUp3";
            case SongEventType.FX_PITCHDOWN3:
                return "PDwn3";

            case SongEventType.FX_REPLAYFROM:
                return "Fr " + " i" + getInstrument() + " " + getFromSamplePosition();
            case SongEventType.FX_FROMANDPITCH:
                return "Fr" + " i" + getInstrument() + " " + getNotePitchAsString() + " " + getFromSamplePosition();
            case SongEventType.FX_SAMPLEVIB:
                return "SampleVib " + getInstrument() + " " + getSpecialParameterAsString(); // speed, length
            case SongEventType.FX_FROMADD:
                return "FrAdd " + getFromSamplePosition();
            case SongEventType.FX_FROMADD_SET:
                return "FrSet " + getFromSamplePosition();

            case SongEventType.FX_SETVOLUME:
                return "Vol " + getVolume();
            case SongEventType.FX_VOLUMESLIDEUP:
                return "VSlUp" + getSpecialParameterAsString();
            case SongEventType.FX_VOLUMESLIDEDOWN:
                return "VSlDwn" + getSpecialParameterAsString();
            case SongEventType.FX_ADDVOLUME:
                return "Vol+" + getVolume();
            case SongEventType.FX_VIBRATO:
                return "Vibrato";
            case SongEventType.FX_CV:
                return "CV " + getVolume();
            case SongEventType.FX_CVADD:
                return "CV+" + getVolume();
            case SongEventType.FX_STOPSAMPLE:
                return "Stop";
            case SongEventType.FX_CONTSAMPLE:
                return "Cont";
            case SongEventType.FX_STARTSAMPLE:
                return "Start";

            case SongEventType.FX_CHANNELFILTER:
                return "CFilter";
            case SongEventType.FX_DSPECHO:
                return "Echo " + getVolume() + " " + getD();
            case SongEventType.FX_DSPCROSSECHO:
                return "CrEcho " + getVolume() + " " + getD();
            case SongEventType.FX_DSPDELAY:
                return "Delay" + getVolume() + " " + getD();
            case SongEventType.FX_DSPCROSSDELAY:
                return "CrDelay" + getVolume() + " " + getD();
            case SongEventType.FX_DSP_DISABLE:
                return "Dsp Off";
            case SongEventType.FX_FILTERPerfectLP4:
                return "FiltLP4";
            case SongEventType.FX_SETSPEED:
                return "Speed:"+getSpecialParameterAsString();
            case SongEventType.FX_SPEEDUP:
                return "Speedup"+getSpecialParameterAsString();
            case SongEventType.FX_SPEEDDOWN:
                return "SpeedDwn "+getSpecialParameterAsString();
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongEvent songEvent = (SongEvent) o;
        return fxClass == songEvent.fxClass && songFXType == songEvent.songFXType && Float.compare(songEvent.A, A) == 0 && Float.compare(songEvent.B, B) == 0 && Float.compare(songEvent.C, C) == 0 && Float.compare(songEvent.D, D) == 0 && Double.compare(songEvent.lengthTicks, lengthTicks) == 0 && eventClass == songEvent.eventClass && eventType == songEvent.eventType && values.equals(songEvent.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fxClass, songFXType, eventClass, eventType, A, B, C, D, lengthTicks, values);
    }

    public void modifyPitch(int dPitch) {
        if(hasPitch()) setPitch(getPitch()+dPitch);
    }

    public void convertKeyToFrom() {
        if(eventType.equals(SongEventType.FX_KEYON)) {
            setSampleFromAndPitch();
            setSampleFromPosition(0);
        }
    }

    private boolean hasPitch() {
       if(songFXType == SongEventType.FX_KEYON
               || songFXType == SongEventType.FX_FROMANDPITCH
               || songFXType == SongEventType.FX_SETPITCH
       ) {
           return true;
        }
        return false;
    }
}
