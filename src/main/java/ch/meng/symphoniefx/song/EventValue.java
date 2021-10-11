package ch.meng.symphoniefx.song;

import ch.meng.symphoniefx.song.ValueType;

public interface EventValue {


    ValueType getValueType();

    void setValueType(ValueType valueType);

    double getMin();

    void setMin(double min);

    double getMax();

    void setMax(double max);

    double getValue();

    void setValue(double value);

    double getDefaultValue();

    void setDefaultValue(double defaultValue);

    String getName();

    void setName(String name);
}
