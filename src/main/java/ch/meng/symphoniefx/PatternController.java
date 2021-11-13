package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SongEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Set;

public class PatternController {
    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    private Song song;
    private final Spinner<Integer> patternNrSpinner;
    private final Spinner<Integer> patternStep;
    private final Spinner<Integer> eventLength;
    private final Spinner<Integer> patternTune;
    private final MainController mainController;
    final NewPatternView newPatternView;

    PatternController(final Song song,
                      final AnchorPane patternPane,
                      final Spinner<Integer> patternNrSpinner,
                      final Spinner<Integer> patternStep,
                      final Spinner<Integer> eventLength,
                      final Spinner<Integer> patternTune,
                      final MainController mainController,
                      final ScrollPane patternScrollPane
    ) {
        this.mainController = mainController;
        this.eventLength = eventLength;
        this.patternTune = patternTune;
        this.song = song;
        this.patternNrSpinner = patternNrSpinner;
        this.patternStep = patternStep;
        initPatternViewUI();
        newPatternView = new NewPatternView(patternPane, patternScrollPane, this);
        newPatternView.setSong(song);
    }


    public MainController getMainController() {
        return mainController;
    }

    public void visualizeKeyOn(final Set<Integer> notesPlayingfinal, double beatLength) {
        newPatternView.visualizeKeyOn(notesPlayingfinal, beatLength);
    }

    void notifyCrsrMoved(final int x, final int y) {
        patternX = x;
        patternY = y;
        mainController.notifyPatternCrsrMoved();
    }

    void shutdown() {
        this.song = null;
    }

    public void setSong(final Song song) {
        this.song = song;
        patternNrDisplayed = -1;
        updatePatternViewToPlayingPattern();
        patternBlock.setSong(song);
        newPatternView.setSong(song);
    }

    private void initPatternViewUI() {
        initPatternNr();
        initSpinner(eventLength, 0, 64);
        initSpinner(patternTune, -36, 36);

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 32, 4, 1);
        patternStep.setValueFactory(valueFactory);
        patternStep.setEditable(true);
        patternStep.valueProperty().addListener((observable, oldValue, newValue) -> newPatternView.buildContent());
    }

    private void initPatternNr() {
        final SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, song.getNumberOfPatterns(), 0, 1);
        patternNrSpinner.setValueFactory(valueFactory);
        patternNrSpinner.setEditable(true);
        patternNrSpinner.valueProperty().addListener((observable, oldValue, newValue) -> moveToPattern(newValue, false));
        patternNrSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.isEmpty()) return;
                int value = Integer.parseInt(newValue);
                moveToPattern(value, true);
            } catch (Exception ignore) {
                logger.error("Error patternNrSpinner");
            }
        });
    }

    private void initSpinner(final Spinner<Integer> spinner, final int min, final int max) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, 0, 1));
    }

    void addKeyonEvent(final int pitch, final int instrument) {
        int z = getPatternZ();
        final SongEvent event = song.getPattern(z).getPatternVoice((int) newPatternView.getCrsrX())
                .getSongEventPool((float) newPatternView.getCrsrY(), true).getSongEvent(true);
        event.setKeyOn(instrument, pitch, 100);
        patternZ = z;
        newPatternView.updateAndAdvanceStep(getStepLength());
    }

    int getStepLength() {
        return patternStep.getValue();
    }

    int getEventLength() {
        return eventLength.getValue();
    }

    SongEvent getEventAtCrsrPosition() {
        final int x = (int) newPatternView.getCrsrX();
        final int y = (int) newPatternView.getCrsrY();
        final int z = getPatternZ();
        return song.getPattern(z).getPatternVoice(x)
                .getSongEventPool(y, false).getSongEvent(false);
    }

    void addKeyonEventVst(final int pitch, final int instrument, final int offset, final int punch, double length) {
        if (length < 1) length = 1;
        final int x = (int) newPatternView.getCrsrX() + offset;
        final int y = (int) newPatternView.getCrsrY();
        final int z = getPatternZ();
        final SongEvent event = song.getPattern(z).getPatternVoice(x)
                .getSongEventPool(y, true).getSongEvent(true);
        event.setKeyOn(instrument, pitch, (float) (100.0 * punch / 127.0));
        if (getEventLength() > 0) length = getEventLength();
        event.setLengthTicks(length);
        patternZ = z;
    }

    void setSongEventRelativeToCrsr(final double x, final double y, final SongEvent newevent) {
        final int z = getPatternZ();
        final int eventX = (int) (newPatternView.getCrsrX() + x);
        if (eventX >= song.getNumbOfVoices()) return;
        final float eventY = (float) (newPatternView.getCrsrY() + y);
        if (eventY >= song.getNumbOfRows()) return;
        SongEvent event = song.getPattern(z).getPatternVoice(eventX)
                .getSongEventPool(eventY, true).getSongEvent(true);
        event.set(newevent);
    }

    void setSongEventAt(final double x, final double y, final SongEvent newevent) {
        final int z = getPatternZ();
        if ((getPatternX() + (int) x) >= song.getNumbOfVoices()) return;
        if ((getPatternY() + (int) y) >= song.getNumbOfRows()) return;
        final SongEvent event = song.getPattern(z).getPatternVoice((int) x)
                .getSongEventPool((float) y, true).getSongEvent(true);
        event.set(newevent);
    }

    void setSongEventRelativeTo(final double offsetX, final double offsetY,
                                final double x, final double y, final SongEvent newevent) {
        final int z = getPatternZ();
        if ((offsetX + (int) x) >= song.getNumbOfVoices()) return;
        if ((offsetY + (int) y) >= song.getNumbOfRows()) return;
        SongEvent event = song.getPattern(z).getPatternVoice((int) (offsetX + x))
                .getSongEventPool((float) offsetY + (int) y, true).getSongEvent(true);
        event.set(newevent);
    }

    void setEventAtCrsr(final SongEvent songEvent) {
        final SongEvent event = song.getPattern(getPatternZ()).getPatternVoice(getPatternX())
                .getSongEventPool(getPatternY(), true).getSongEvent(true);
        event.set(songEvent);
        newPatternView.rebuildVoiceVisual(getPatternX());
    }

    void clearEventAtCrsr() {
        song.getPattern(getPatternZ()).getPatternVoice((int) newPatternView.getCrsrX()).removeEvents((float) newPatternView.getCrsrY());
        newPatternView.rebuildVoiceVisual((int) newPatternView.getCrsrX());
    }

    void clearEvent(final int x, final int y) {
        song.getPattern(patternNrSpinner.getValue()).getPatternVoice(x).removeEvents(y);
    }

    private int patternX = 0;
    private int patternY = 0;
    private int patternZ = 0;
    public int getPatternX() {
        return patternX;
    }
    public int getPatternY() {
        return patternY;
    }
    public int getPatternZ() {
        if (patternZ != patternNrSpinner.getValue()) {
            logger.debug("patternZ != patternNrSpinner.getValue()" + patternZ + " " + patternNrSpinner.getValue());
        }
        return patternZ;
    }

    void muteVoice(int voiceIndex, boolean shift) {
        if (voiceIndex >= 0) {
            if (shift) mainController.getVoiceExpander().toggleMuteAllVoice(voiceIndex);
            else mainController.getVoiceExpander().toggleMuteVoice(voiceIndex);
        }
    }

    void muteAllVoices(boolean mute) {
        newPatternView.muteAllVoices(mute);
    }

    private void addGlobalFunctionKeys(KeyEvent event, Song song) {
        if (event.getCode().equals(KeyCode.SPACE)) mainController.tooglePlaySong();
        if (event.getCode().equals(KeyCode.F1)) mainController.instrumentController.loadSample();
        if (event.getCode().equals(KeyCode.F2)) mainController.playWholeSong();
        if (event.getCode().equals(KeyCode.F3)) mainController.stopSong();
        if (event.getCode().equals(KeyCode.ESCAPE)) {
            if (song.isPlaying()) {
                mainController.stopSong();
            }
            mainController.stopAllChannels();
        }
    }

    private boolean markingBlock;
    private final PatternBlock patternBlock = new PatternBlock(song, this);

    PatternBlock getPatternBlock() {
        return patternBlock;
    }

    void toogleBlockMark(int x, int y) {
        markingBlock = !markingBlock;
        if (markingBlock) {
            patternBlock.markBegin(x, y);
        } else {
            patternBlock.markEnd(x, y, getPatternZ());
            newPatternView.drawBlockMarks(patternBlock, false);
        }
    }

    void markBegin(int x, int y) {
        markingBlock = true;
        patternBlock.markBegin(x, y);
    }

    void markEnd(int x, int y) {
        patternBlock.markEnd(x, y, getPatternZ());
        markingBlock = false;
        newPatternView.drawBlockMarks(patternBlock, false);
    }

    void markOngoing(int x, int y) {
        patternBlock.updateEnd(x, y);
        newPatternView.drawBlockMarks(patternBlock, true);
    }

    void updatePatternViewToPlayingPattern() {
        patternNrSpinner.getEditor().setText(Integer.toString(song.getPlayingPatternNr()));
    }

    void moveToPattern(final int patternNr, final boolean force) {
        if ((force || patternNrDisplayed != patternNr) && patternNr >= 0) {
            logger.debug("updatePatternView()" + patternNr);
            patternZ = patternNr;
            newPatternView.moveToPattern(patternNr);
        }
    }

    public void updatePlayingRowMarker() {
        newPatternView.updatePlayingRowMarker();
    }

    public void updatePatternViewForce() {
        newPatternView.moveToPattern(patternZ);
    }

    public void advanceCrsrOneStep() {
        newPatternView.updateAndAdvanceStep(getStepLength());
    }

    void resetVisualsForLoading() {
        patternNrDisplayed = -1;
    }

    private int patternNrDisplayed = -1;
    private int zoom = 18;
    private final int oldZoom = -1;
    private Font tableFont;
    private Font getTableFont() {
        if (zoom != oldZoom) {
            int fontsize = 6 + (zoom / 3);
            tableFont = Font.font(null, FontWeight.BOLD, fontsize);
        }
        return tableFont;
    }

    void blockClear(final BlockDestination blockDestination) {
        if (blockDestination.equals(BlockDestination.Selection)) blockClear(patternBlock);
        if (blockDestination.equals(BlockDestination.Pattern)) blockClear(createPatternBlock());
        if (blockDestination.equals(BlockDestination.Track)) blockClear(createTrackBlock());
        updatePatternViewForce();
    }

    void blockPaste(final boolean copyEmpty) {
        patternBlock.paste(copyEmpty);
    }

    void blockPaste(int x, int y, boolean copyEmpty) {
        patternBlock.paste(x, y, copyEmpty);
    }

    void blockPaste(PatternBlock block, int x, int y, boolean copyEmpty) {
        block.paste(x, y, copyEmpty);
    }

    void blockFill(boolean copyEmpty) {
        patternBlock.fill(copyEmpty);
    }

    void blockModifyPitch(int dPitch) {
        patternBlock.modifyPitch(dPitch);
        patternBlock.copyBack();
    }

    void convertKeyToFrom() {
        patternBlock.convertKeyToFrom();
        patternBlock.copyBack();
    }

    void blockCopy() {
        patternBlock.copy();
    }

    PatternBlock backupPattern() {
        PatternBlock backup = createPatternBlock();
        backup.copy();
        return backup;
    }

    private PatternBlock createPatternBlock() {
        final PatternBlock block = new PatternBlock(song, this);
        block.setxStart(0);
        block.setyStart(0);
        block.setxEnd(song.getNumbOfVoices() - 1);
        block.setyEnd(song.getNumbOfRows() - 1);
        block.setPatternNr(getPatternZ());
        return block;
    }

    void duplicatePattern() {
        try {
            final PatternBlock block = createPatternBlock();
            block.copy();
            moveToNextPattern();
            block.copyBack();
        } catch (Exception exception) {
            logger.error("Error:duplicatePattern()");
            exception.printStackTrace();
        }
    }

    void moveToNextPattern() {
        moveToPattern(getPatternZ() + 1);
    }

    void moveToPattern(int patternIndex) {
        patternNrSpinner.getValueFactory().setValue(patternIndex);
    }


    private PatternBlock createTrackBlock() {
        final PatternBlock block = new PatternBlock(song, this);
        block.setxStart(getPatternX());
        block.setyStart(0);
        block.setxEnd(block.getxStart());
        block.setyEnd(song.getNumbOfRows());
        block.setPatternNr(getPatternZ());
        return block;
    }

    private void blockClear(final PatternBlock block) {
        for (int x = block.getxStart(); x < (block.getxEnd() + 1); x++) {
            for (int y = block.getyStart(); y < (block.getyEnd() + 1); y++) {
                clearEvent(x, y);
            }
        }
    }

}
