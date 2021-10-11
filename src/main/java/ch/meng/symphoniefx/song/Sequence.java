package ch.meng.symphoniefx.song;

public class Sequence {
    String Name = "unnamed";
    SequenceActionEnum Action = SequenceActionEnum.EndOfSong;
    int startPositionIndex = 0;
    int EndPosition = 10;
    int NumbOfLoops = 1; // = 1 Once
    int Tune = 0; // in Halvetones

    public void setName(String name) {
        Name = name;
    }

    public void setAction(SequenceActionEnum action) {
        Action = action;
    }

    public void setStartPositionIndex(int startPositionIndex) {
        this.startPositionIndex = startPositionIndex;
    }

    public void setEndPosition(int endPosition) {
        EndPosition = endPosition;
    }

    public void setNumbOfLoops(int numbOfLoops) {
        NumbOfLoops = numbOfLoops;
    }

    public void setTune(int tune) {
        Tune = tune;
    }

    public String getName() {
        return Name;
    }

    public SequenceActionEnum getAction() {
        return Action;
    }

    public int getStartPositionIndex() {
        return startPositionIndex;
    }

    public int getEndPosition() {
        return EndPosition;
    }

    public int getNumbOfLoops() {
        return NumbOfLoops;
    }

    public int getTune() {
        return Tune;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("Position Start:").append(startPositionIndex).append(" ")
                .append("End:").append(EndPosition).append(" ")
                .append("Tune:").append(Tune).append(" ")
                .append("Loops:").append(NumbOfLoops)
                .append(" ").append(Action.toString());
        return text.toString();
    }
}
