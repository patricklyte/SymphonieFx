package ch.meng.symphoniefx;

import java.util.zip.ZipEntry;

public class SongLoadPart {
    ZipEntry zipEntry;
    String extractedPath;
    int instrumentIndex = -1;

    SongLoadPart(ZipEntry zipEntry, String path) {
        this.zipEntry = zipEntry;
        this.extractedPath = path;
        extractInstrumentIndex(zipEntry.getName());
    }

    private void extractInstrumentIndex(String filename) {
        if(filename.startsWith("Instr-")) filename = filename.substring(6);
        int pos = filename.indexOf("-");
        if(pos>0) {
            filename = filename.substring(0, pos);
            instrumentIndex = Integer.parseInt(filename);
        }
    }

    public int getInstrumentIndex() {
        return instrumentIndex;
    }

    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    public void setZipEntry(ZipEntry zipEntry) {
        this.zipEntry = zipEntry;
    }

    public String getExtractedPath() {
        return extractedPath;
    }

    public void setExtractedPath(String extractedPath) {
        this.extractedPath = extractedPath;
    }
}
