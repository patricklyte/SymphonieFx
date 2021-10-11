package ch.meng.symphoniefx;

import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import ch.meng.symphoniefx.song.EventType;
import ch.meng.symphoniefx.song.SongEvent;

public class EventToogleButton  extends ToggleButton {

    private final SongEvent songEvent;
    private final EventType eventType;
    EventToogleButton(String text, SongEvent songEvent, EventType eventType) {
        super(text);
        this.songEvent = songEvent;
        this.eventType = eventType;
    }

   public void handle(ActionEvent event) {
        action();
    }

    public void action() {
        switch (eventType) {
            case KeyOn: songEvent.setKeyOn();
            case VolumeSet: songEvent.setVolume();
            case PitchSet: songEvent.setPitch();
            case SampleFromAndPitch: songEvent.setSampleFromAndPitch();
            case SampleFrom: songEvent.setSampleFrom();
            case Stop: songEvent.setStop();
            case Continue: songEvent.setContinue();
        }
    }
}
