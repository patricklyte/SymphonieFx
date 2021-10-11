package ch.meng.symphoniefx.song;

public class VstSetup {
    private byte[] programChunk;
    private int programNr;
    private String dllFilename;

    public String getDllFilename() {return dllFilename;}
    public void setDllFilename(String dllFilename) {this.dllFilename = dllFilename;}

    public byte[] getProgramChunk() {return programChunk;}
    public void setProgramChunk(byte[] programChunk) {this.programChunk = programChunk;}

    public int getProgramNr() {return programNr;}
    public void setProgramNr(int programNr) {this.programNr = programNr;}
}
