package ch.meng.symphoniefx.song;

public class EventValueInstrument extends EventValueBase implements EventValue {
    EventValueInstrument(double value) {
        valueType = ValueType.Instrument;
        value = value;
        max = 127; // todo get from song
    }
}
