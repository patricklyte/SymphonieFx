package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SongEvent;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import symreader.*;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;

class SongSaverTests {
    SongSaver songSaver = new SongSaver();

    @Test
    void testCleanedWindowsFilename() {
        String filename = "c:\\dir\\*filename.txt*";
        filename = songSaver.cleanFileName(filename);
        Assertions.assertEquals("c dir filename.txt", filename);
    }

    @Test
    void testCleanedUnixFilename() {
        String filename = "c:/dir/*filename.txt*";
        filename = songSaver.cleanFileName(filename);
        Assertions.assertEquals("c dir filename.txt", filename);
    }

    @Test
    void buildSaveInstrumentNameTest() {
        SymphonieInstrument instrument = new SymphonieInstrument();
        instrument.setName("c:/dir/sample 01.wav");
        OldSampleImporter oldSampleImporter = new OldSampleImporter();
        instrument.setSampleImporter(oldSampleImporter);
        oldSampleImporter.setFileSuffix(".sampletest");
        String sampleName = songSaver.buildSaveInstrumentName(instrument);
        Assertions.assertEquals("Instr-0-sample 01.wav.sampletest", sampleName);
    }

    @Test
    void saveTest() {
        Song song = new Song();
    }

}
