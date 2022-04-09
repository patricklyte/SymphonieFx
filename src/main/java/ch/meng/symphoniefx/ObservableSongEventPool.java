package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.SongEventPool;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ObservableSongEventPool implements ObservableValue<SongEventPool> {
    SongEventPool eventpool;
    int x;
    ObservableSongEventPool(SongEventPool eventpool) {
        if(!eventpool.getSongEvent(false).isEmpty()) {
            x = 1;
        }
        this.eventpool = eventpool;
    }

    @Override
    public void addListener(ChangeListener<? super SongEventPool> listener) {
    }

    @Override
    public void removeListener(ChangeListener<? super SongEventPool> listener) {
    }

    @Override
    public SongEventPool getValue() {
        return eventpool;
    }

    @Override
    public void addListener(InvalidationListener listener) {
    }

    @Override
    public void removeListener(InvalidationListener listener) {
    }
}
