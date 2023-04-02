package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.SongEvent;
import jakarta.xml.bind.JAXB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

class SongSaverMarshallingUnmarshallingTests {

    @Test
    void marshallSongEventTest() {
        StringWriter stringWriter = new StringWriter();
        SongEvent songEvent = new SongEvent();
        songEvent.setKeyOn(1,2,3);
        JAXB.marshal(songEvent, stringWriter);
        StringReader reader = new StringReader(stringWriter.toString());
        SongEvent unmarshalledSongEvent = JAXB.unmarshal(reader, SongEvent.class);
        Assertions.assertEquals(songEvent.toString(), unmarshalledSongEvent.toString());
        Assertions.assertTrue(songEvent.equals(unmarshalledSongEvent));
    }
}
