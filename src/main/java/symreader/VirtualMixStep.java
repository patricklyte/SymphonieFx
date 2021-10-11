package symreader;

import java.util.Objects;

public class VirtualMixStep {
    private int mixInstrumentId;
    private int mixInstrumentIndex;
    private int mixVolume;
    private int mixPitch;
    private int loopStartTranswave;
    private int loopEndTranswave;

    public int getMixInstrumentIndex() {
        return mixInstrumentIndex;
    }

    public void setMixInstrumentIndex(int mixInstrumentIndex) {
        this.mixInstrumentIndex = mixInstrumentIndex;
    }

    public int getMixVolume() {
        return mixVolume;
    }

    public void setMixVolume(int mixVolume) {
        this.mixVolume = mixVolume;
    }

    public int getMixPitch() {
        return mixPitch;
    }

    public void setMixPitch(int mixPitch) {
        this.mixPitch = mixPitch;
    }

    public int getLoopStartTranswave() {
        return loopStartTranswave;
    }

    public void setLoopStartTranswave(int loopStartTranswave) {
        this.loopStartTranswave = loopStartTranswave;
    }

    public int getLoopEndTranswave() {
        return loopEndTranswave;
    }

    public void setLoopEndTranswave(int loopEndTranswave) {
        this.loopEndTranswave = loopEndTranswave;
    }

    public int getMixInstrumentId() {
        return mixInstrumentId;
    }

    public void setMixInstrumentId(int mixInstrumentId) {
        this.mixInstrumentId = mixInstrumentId;
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("Instr:").append(mixInstrumentIndex)
                .append(" Pitch:").append(mixPitch)
                .append(" Vol:").append(mixVolume);
        return text.toString();
    }
    public String getRenderDescription() {
        final StringBuilder text = new StringBuilder();
        text.append("i").append(mixInstrumentIndex)
                .append("p").append(mixPitch)
                .append("v").append(mixVolume);
        return text.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualMixStep that = (VirtualMixStep) o;
        return mixInstrumentIndex == that.mixInstrumentIndex && mixVolume == that.mixVolume && mixPitch == that.mixPitch && loopStartTranswave == that.loopStartTranswave && loopEndTranswave == that.loopEndTranswave;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mixInstrumentIndex, mixVolume, mixPitch, loopStartTranswave, loopEndTranswave);
    }
}


