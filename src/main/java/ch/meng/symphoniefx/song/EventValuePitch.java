package ch.meng.symphoniefx.song;

public class EventValuePitch extends EventValueBase implements EventValue {
    EventValuePitch(double value) {
        valueType = ValueType.Pitch;
        value = value;
        max = 127;
    }
}
