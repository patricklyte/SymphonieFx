package ch.meng.symphoniefx.song;

public class Position {
    String Name = "Unamed";
    int NumbOfLayers = 1; // only 1 till now
    int[] PatternNumbers = new int[1]; // List to Patternnumbers ( Layers )
    int StartRow = 0;
    int RowLength = 64;
    int Tune = 0; // 0 = none
    int NumbOfLoops = 1;
    private int Speed_Cycl = 4;

    public Position() {
    }

    public Position(final Position position) {
        Name = position.Name;
        NumbOfLayers = position.NumbOfLayers;
        setSinglePatternNumber(position.getPatternNumber());
        StartRow = position.StartRow;
        RowLength = position.RowLength;
        Tune = position.Tune;
        NumbOfLoops = position.NumbOfLoops;
        Speed_Cycl = position.Speed_Cycl;
    }

    public void setSinglePatternNumber(int index) {
        PatternNumbers = new int[1];
        PatternNumbers[0] = index;
    }

    public int getPatternNumber() {
        return PatternNumbers[0];
    }

    public void setName(String name) {
        Name = name;
    }

    public int getNumbOfLayers() {
        return NumbOfLayers;
    }

    public int[] getPatternNumbers() {
        return PatternNumbers;
    }

    public void setPatternNumbers(int[] patternNumbers) {
        PatternNumbers = patternNumbers;
    }

    public void setStartRow(int startRow) {
        StartRow = startRow;
    }

    public void setRowLength(int rowLength) {
        RowLength = rowLength;
    }

    public void setTune(int tune) {
        Tune = tune;
    }

    public void setNumbOfLoops(int numbOfLoops) {
        NumbOfLoops = numbOfLoops;
    }

    public void setSpeed_Cycl(int speed_Cycl) {
        Speed_Cycl = speed_Cycl;
    }

    public String getName() {
        return Name;
    }


    public int getStartRow() {
        return StartRow;
    }

    public int getRowLength() {
        return RowLength;
    }

    public int getTune() {
        return Tune;
    }

    public int getNumbOfLoops() {
        return NumbOfLoops;
    }

    public int getSpeed_Cycl() {
        return Speed_Cycl;
    }

    public void setNumbOfLayers(int i) {
        PatternNumbers = new int[i];
        NumbOfLayers = i;
    }

    String buildPosListString(int i) {
        return ("" + i + ". Pat:" + PatternNumbers[0]
                + " Spd:" + Speed_Cycl
                + " Loops:" + NumbOfLoops
                + " LineNr:" + StartRow
                + " Len:" + RowLength
                + " Tune:" + Tune
        );
    }

    void copyValues(Position dest) {
        dest.Name = Name;
        dest.NumbOfLayers = NumbOfLayers;
        dest.StartRow = StartRow;
        dest.RowLength = RowLength;
        dest.Tune = Tune;
        dest.NumbOfLoops = NumbOfLoops;
        dest.Speed_Cycl = Speed_Cycl;
        dest.PatternNumbers[0] = PatternNumbers[0];
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("Pattern:").append(PatternNumbers[0])
                .append(" Start:").append(StartRow)
                .append(" Len:").append(RowLength)
                .append(" Tune:").append(Tune)
                .append(" Loops:").append(NumbOfLoops)
                .append(" Cycl:").append(Speed_Cycl);
        return text.toString();
    }
}
