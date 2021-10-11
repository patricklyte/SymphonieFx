package ch.meng.symphoniefx.song;

public class EventValuePercent extends EventValueBase implements EventValue {
    EventValuePercent(double value) {
        this.valueType = ValueType.Percent;
        this.value = value;
        max = 100;
    }
}
