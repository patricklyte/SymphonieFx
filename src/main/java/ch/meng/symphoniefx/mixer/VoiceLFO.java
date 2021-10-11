package ch.meng.symphoniefx.mixer;

public class VoiceLFO {
    private boolean Running = false;
    private final boolean BPMrelative = true;
    private boolean FadeToValue = false;
    private final double MinValue;
    private final double MaxValue;
    private double FadeToValueSpeed;
    private double DestValue;
    private double dValue;

    // Can additionally be activated
    private boolean SinusRunning = false;
    private final double SinusIntensity = 100.0f; // 100 = Full Value Range
    private final double SinusSpeed = 1.0f;


    VoiceLFO(double Min, double Max) {
        Running = false;
        SinusRunning = false;
        MinValue = Min;
        MaxValue = Max;
    }

    // Sinus Section
    double applySinusToValue(double Value) {
        if (SinusRunning == true) {
            //Value += sinus()
        }
        return (Value);
    }

    // Slide Section
    double checkLimits(double Value) {
        if ((Value >= MaxValue) && (dValue > 0)) {
            stop();
            Value = MaxValue;
        }

        if ((Value <= MinValue) && (dValue < 0)) {
            stop();
            Value = MinValue;
        }
        return (Value);
    }

    double applyToValue(double Value) {
        if (Running == true) {

            // Fade Up / Down
            if (FadeToValue == false) {
                Value += dValue;
                Value = checkLimits(Value);

                // Fade To Value
            } else {
                dValue = ((DestValue - Value) * FadeToValueSpeed) / 7;
                if (dValue == 0) {
                    Running = false;
                } else {

                    if (dValue > 0) {
                        if ((Value + dValue) >= DestValue) {
                            Value = DestValue;
                            Running = false;
                        }
                    } else {
                        if ((Value + dValue) <= DestValue) {
                            Value = DestValue;
                            Running = false;
                        }
                    }

                    if (Running == true) Value += dValue;
                    Value = checkLimits(Value);
                }
            }
        }
        return (Value);
    }

    void initSlide(double NewdValue) {

        Running = false;
        if (NewdValue != 0) {
            FadeToValue = false;
            dValue = NewdValue;
            Running = true;
        }
    }

    void initSlideToValue(double NewDestValue, double Speed) {
        Running = false;
        if ((NewDestValue >= MinValue) && (NewDestValue <= MaxValue) &&
                (Speed != 0)
        ) {
            FadeToValue = true;
            DestValue = NewDestValue;
            FadeToValueSpeed = Speed;
            Running = true;
        }
    }


    boolean isRunning() {
        return (Running);
    }

    void stop() {
        Running = false;
    }
}
