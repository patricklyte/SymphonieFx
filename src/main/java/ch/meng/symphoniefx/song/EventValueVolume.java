package ch.meng.symphoniefx.song;

public class EventValueVolume extends EventValueBase implements EventValue {
    EventValueVolume(double value) {
        valueType = ValueType.Volume;
        this.value = value;
        max = 100;
    }
}
