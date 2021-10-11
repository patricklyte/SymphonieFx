package ch.meng.symphoniefx.song;

public class SongEventKeyon extends SongEvent {
    SongEventKeyon(double instrument, double pitch, double volume) {
        this.getValues().put(ValueType.Instrument, new EventValueInstrument(instrument));
        this.getValues().put(ValueType.Pitch, new EventValuePitch(pitch));
        this.getValues().put(ValueType.Volume, new EventValueVolume(volume));
    }


}
