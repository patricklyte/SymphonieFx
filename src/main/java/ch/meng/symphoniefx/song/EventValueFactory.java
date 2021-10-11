package ch.meng.symphoniefx.song;

public class EventValueFactory {
    static EventValueBase get(ValueType type, double value) {
        switch (type) {
            case Instrument: return new EventValueInstrument(value);
            case Pitch: return new EventValuePitch(value);
            case Volume: return new EventValueVolume(value);
            case Percent: return new EventValuePercent(value);
        }
        return  new EventValueBase();
    }
}
