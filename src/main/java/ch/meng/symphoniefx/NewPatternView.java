package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.IntStream;


public class NewPatternView {
    private static final Logger logger = LogManager.getLogger();

    public static final Background rowBackground = new Background(new BackgroundFill(Color.color(0.8, 0.9, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background voiceBackground = new Background(new BackgroundFill(Color.color(1.0, 1.0, 0.92, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background voiceBackground2 = new Background(new BackgroundFill(Color.color(1.0, 1.0, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background voiceMutedBackground = new Background(new BackgroundFill(Color.color(0.5, 0.5, 0.5, 0.5), null, new Insets(0.0, 0.0, 2.0, 2.0)));

    public final Color blockMarkingColor = Color.color(1.0, 0.4, 0.0, 0.2);
    public final Color blockMarkingColor2 = Color.color(1.0, 0.4, 0.0, 0.7);
    public final Color blockMarkerColor = Color.color(1.0, 0.90, 0.0, 0.25);
    public final Color blockMarkerColor2 = Color.color(0.85, 0.85, 0.0, 1.0);
    public final Color gridColor = Color.color(0.9, 0.85, 0.3, 0.5);
    public final Color mouseMarkerColor = Color.color(0.0, 0.5, 1.0, 0.15);
    public final Color mouseBorderMarkerColor = Color.color(0.0, 0.0, 1.0, 0.25);
    public final Color playingRowColor = Color.color(0.5, 0.6, 1.0, 0.25);
    public final Color crsrColor = Color.color(1.0, 0.0, 0.0, 0.15);
    public final Color crsrColor2 = Color.color(1.0, 0.0, 0.0, 1.0);
    public final Color beatVisualizeColor = Color.color(0.5, 1.0, 0.0, 0.5);

    double cellW = 76;
    double cellH = 18;
    double headerH = 20;
    double rowHeaderW = 40;
    int mouseCellX = 0;
    double mouseCellY = 0;
    int crsrX = 0;
    double crsrY = 0;

    private final Rectangle blockMarker = new Rectangle();
    private final Rectangle mouseMarker = new Rectangle();
    private final Rectangle mouseRowMarker = new Rectangle();
    private final Rectangle mouseColumnMarker = new Rectangle();
    private final Rectangle playingRowMarker = new Rectangle();
    private final Rectangle crsrMarker = new Rectangle();
    private final Rectangle crsrMarker2 = new Rectangle();
    private final Pane rootPane = new Pane();
    private final Pane markerPane = new Pane();
    private final ScrollPane patternScrollPane;
    private final ScrollPane patternHeaders = new ScrollPane();
    private final ScrollPane rowHeaders = new ScrollPane();
    private Song song;
    private final AnchorPane anchorPane;
    private final PatternController patternController;

    NewPatternView(AnchorPane anchorPane, ScrollPane patternScrollPane, PatternController patternController) {
        this.patternScrollPane = patternScrollPane;
        this.anchorPane = anchorPane;
        this.patternController = patternController;
        init();
        buildPitchColors();
    }

    final List<Color> pitchColors = new ArrayList<>();
    void buildPitchColors() {
        final int range = 360;
        final int start = 0;
        for (double hue = start; hue < start+range; hue += (range/24)) {
            pitchColors.add(Color.hsb(hue, 0.5, 1.0, 0.5));
        }
    }

    Pane beatVisualizer = new Pane();
    double beatH = cellH * 1.0;
    double beatW = cellW / 2.0;
    public void visualizeKeyOn(final Set<Integer> notesPlayingfinal, final double beatLength) {
        if(beatVisualizer == null) {
            beatVisualizer = new Pane();
            rootPane.getChildren().add(beatVisualizer);
        }
        beatVisualizer.getChildren().clear();
        if(notesPlayingfinal.isEmpty()) return;

        double y = 0;
        double x = 0;
        beatH = cellH / patternController.getMainController().getActualPositionCycl();
        for(int pitch : notesPlayingfinal) {
            pitch = pitch % 24;
            addKeyVisualPitch(beatLength, y, x, pitchColors.get(pitch));
            x += beatW;
        }
        beatVisualizer.setTranslateX(getCrsrX()*cellW);
        beatVisualizer.setTranslateY(getCrsrY()*cellH);
    }

    private void addKeyVisualPitch(double beatLength, double y, double x, Color color) {
        for(int i = 0; i < (int) beatLength; i++){
            final Rectangle rectangle = new Rectangle(x, y, beatW, beatH * 0.9);
            rectangle.setFill(color);
            beatVisualizer.getChildren().add(rectangle);
            y += beatH;
        }
        final double fraction = beatLength - ((int) beatLength);
        final Rectangle rectangle = new Rectangle(x, y, beatW, beatH * 0.9 * fraction);
        rectangle.setFill(color);
        beatVisualizer.getChildren().add(rectangle);
    }

    public double getCrsrX() {
        return crsrX;
    }

    public double getCrsrY() {
        return crsrY;
    }

    public void updateAndAdvanceStep(final int stepY) {
        crsrY += stepY;
        if(crsrY > song.getNumbOfRows()) crsrY-= song.getNumbOfRows();
        enforceCrsrInPattern();
        updateCrsrVisuals();
        rebuildVoiceVisual(crsrX);
    }

    private void moveCrsrToMousePosition() {
        if(markingBlock) {
            markingBlock = false;
            return;
        }
        crsrX = mouseCellX;
        crsrY = mouseCellY;
        buildContent();
        updateCrsrVisuals();
    }

    private void updateCrsrVisuals() {
        crsrMarker.setX(crsrX * cellW);
        crsrMarker.setY(crsrY * cellH);
        crsrMarker2.setX(crsrMarker.getX()-1);
        crsrMarker2.setY(crsrMarker.getY());
        patternController.notifyCrsrMoved(crsrX, (int) crsrY);
    }


    public void notifyPositionChanged(Position position) {
        buildContent();
    }

    public void setSong(Song song) {
        this.song = song;
        patternIndex = 0;
        buildContent();
    }

    int patternIndex = 0;

    public void moveToPattern(int patternIndex) {
        this.patternIndex = patternIndex;
        buildContent();
    }

    void init() {
        blockMarker.setFill(blockMarkerColor);
        mouseMarker.setFill(mouseMarkerColor);
        playingRowMarker.setFill(playingRowColor);
        mouseRowMarker.setFill(mouseBorderMarkerColor);
        mouseColumnMarker.setFill(mouseBorderMarkerColor);
        crsrMarker.setFill(crsrColor);
        crsrMarker2.setFill(crsrColor2);
        patternScrollPane.setContent(rootPane);
        patternScrollPane.setPrefViewportHeight(100);
        addMouseHandler();
        patternScrollPane.vvalueProperty().addListener((ov, oldValue, value) -> {
            rowHeaders.setVvalue(patternScrollPane.getVvalue());
            rowHeaders.setPrefHeight(patternScrollPane.getHeight());
        });
        patternScrollPane.hvalueProperty().addListener((ov, oldValue, value) -> {
            patternHeaders.setHvalue(patternScrollPane.getHvalue());
            patternHeaders.setPrefWidth(patternScrollPane.getWidth());
        });
        addKeyboardHandler();

        patternHeaders.setOnMouseMoved(mouseEvent -> updateCrsr(mouseEvent, true, false));
        rowHeaders.setOnMouseMoved(mouseEvent -> updateCrsr(mouseEvent, false, true));
        patternHeaders.setOnMouseClicked(mouseEvent -> {
            toogleMute(mouseCellX, mouseEvent.isShiftDown());
            buildContent();
        });

        patternHeaders.setMinHeight(headerH);
        patternHeaders.setPrefHeight(headerH);
        patternHeaders.setMaxHeight(headerH);
        AnchorPane.setTopAnchor(patternHeaders, 32.0);
        AnchorPane.setLeftAnchor(patternHeaders, rowHeaderW);
        AnchorPane.setRightAnchor(patternHeaders, 0.0);
        patternHeaders.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        patternHeaders.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        rowHeaders.setMinWidth(rowHeaderW);
        rowHeaders.setPrefWidth(rowHeaderW);
        rowHeaders.setMaxWidth(rowHeaderW);
        AnchorPane.setTopAnchor(rowHeaders, 32.0+headerH);
        rowHeaders.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rowHeaders.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        anchorPane.getChildren().add(rowHeaders);
        anchorPane.getChildren().add(patternHeaders);
    }

    private void addMouseHandler() {
        rootPane.setOnMouseMoved(mouseEvent -> updateCrsr(mouseEvent, true, true));
        rootPane.setOnMouseClicked(mouseEvent -> {
            moveCrsrToMousePosition();
            rootPane.requestFocus();
        });
        rootPane.setOnMouseDragEntered(mouseEvent -> logger.debug("setOnMouseDragEntered"));
        rootPane.setOnMouseDragged(mouseEvent -> {
            if(!markingBlock) patternController.markBegin(mouseCellX, (int) mouseCellY);
            markingBlock = true;
            updateCrsr(mouseEvent, true, true);
            patternController.markOngoing(mouseCellX, (int) mouseCellY);
        });
        rootPane.setOnMouseReleased(mouseEvent -> {
            if(markingBlock) {
                patternController.markEnd(mouseCellX, (int) mouseCellY);
            }
        });
    }

    boolean markingBlock = false;
    private void addKeyboardHandler() {
        rootPane.setOnKeyPressed(event -> {
            if(event.getCode().isArrowKey()) {
                logger.debug("Arrow Key" + event.getCode());
                if(event.getCode().equals(KeyCode.DOWN)) crsrY++;
                else if(event.getCode().equals(KeyCode.UP)) crsrY--;
                else if(event.getCode().equals(KeyCode.RIGHT)) crsrX++;
                else if(event.getCode().equals(KeyCode.LEFT)) crsrX--;
                enforceCrsrInPattern();
                event.consume();
                updateCrsrVisuals();
            }
            else if(event.getCode().equals(KeyCode.ENTER)) {
                patternController.toogleBlockMark(crsrX, (int) crsrY);
            }  else if(event.isControlDown()) {
                if(event.getCode().equals(KeyCode.C)) patternController.blockCopy();
                if(event.getCode().equals(KeyCode.X)) {
                    patternController.blockCopy();
                    patternController.blockClear(BlockDestination.Selection);
                }
                if(event.getCode().equals(KeyCode.V)) patternController.blockPaste(crsrX, (int) crsrY, true);
            }
        });
    }

    void drawBlockMarks(final PatternBlock patternBlock, final boolean marking) {
        if(marking) {
            blockMarker.setFill(blockMarkingColor);
            blockMarker.setStroke(blockMarkingColor2);
        }
        else {
            blockMarker.setFill(blockMarkerColor);
            blockMarker.setStroke(blockMarkerColor2);
        }
        blockMarker.setX(Math.min(patternBlock.getxStart()*cellW, patternBlock.getxEnd()*cellW));
        blockMarker.setY(Math.min(patternBlock.getyStart()*cellH, patternBlock.getyEnd()*cellH));
        blockMarker.setWidth(patternBlock.getBlockWidth()*cellW);
        blockMarker.setHeight(patternBlock.getBlockHeight()*cellH);
    }

    void toogleMute(final int voiceIndex, final boolean shift) {
        muteVoice(voiceIndex, !isVoiceMuted(voiceIndex), shift);
    }

    boolean isVoiceMuted(int voiceIndex) {
        return mutedVoices.contains(voiceIndex);
    }

    final Set<Integer> mutedVoices = new HashSet<>();
    void muteVoice(final int voiceIndex, final boolean muteVoice, final boolean shift) {
        if(shift) {
            muteAllVoices(!isVoiceMuted(voiceIndex));
        } else {
            if(muteVoice) mutedVoices.add(voiceIndex);
            else mutedVoices.remove(voiceIndex);
        }
    }

    void muteAllVoices(boolean muteVoice) {
        if(!muteVoice) {
            mutedVoices.clear();
            return;
        }
        IntStream.range(0, song.getNumbOfRows()).forEach(mutedVoices::add);
    }

    void enforceCrsrInPattern() {
        if(crsrX < 0) crsrX=song.getNumbOfVoices()-1;
        else if(crsrX > song.getNumbOfVoices()-1) crsrX=0;
        else if(crsrY < 0) crsrY=song.getNumbOfRows()-1;
        else if(crsrY > song.getNumbOfRows()-1) crsrY=0;
        ensureRowNrVisible(crsrY);
    }

    int zoom = 18;
    private int oldZoom = -1;
    private Font tableFont;

    private Font getTableFont() {
        if (zoom != oldZoom) {
            int fontsize = 14;
            tableFont = Font.font(null, FontWeight.NORMAL, fontsize);
            oldZoom = zoom;
        }
        return tableFont;
    }

    final TimeMeasure2 timer = new TimeMeasure2();

    public void buildContent() {
        beatVisualizer = null;
        timer.restart("buildContent");
        cellW = 74;
        cellH = 18;
        rootPane.getChildren().clear();
        rootPane.setPrefWidth(cellW * song.getNumbOfVoices());
        markerPane.setPrefWidth(rowHeaderW + cellW * song.getNumbOfVoices());
        mouseMarker.setWidth(cellW);
        mouseMarker.setHeight(cellH);
        crsrMarker.setWidth(cellW);
        crsrMarker.setHeight(cellH);
        crsrMarker2.setWidth(2);
        crsrMarker2.setHeight(cellH);
        mouseColumnMarker.setWidth(cellW);
        mouseColumnMarker.setHeight(headerH);
        mouseRowMarker.setWidth(rowHeaderW);
        mouseRowMarker.setHeight(cellH);
        playingRowMarker.setHeight(cellH);
        updatePlayingRowMarker();

        final Pane leftRowsNumbers2 = buildRowNumberPane(song.getNumbOfRows());
        leftRowsNumbers2.getChildren().add(mouseRowMarker);
        rowHeaders.setContent(leftRowsNumbers2);

        final Pane headerPane2 = buildHeaderPane(song.getNumbOfVoices());
        patternHeaders.setContent(headerPane2);
        headerPane2.getChildren().add(mouseColumnMarker);

        buildVoicesVisuals();
        rootPane.getChildren().add(mouseMarker);
        rootPane.getChildren().add(playingRowMarker);
        rootPane.getChildren().add(crsrMarker);
        rootPane.getChildren().add(crsrMarker2);
        rootPane.getChildren().add(blockMarker);
        buildGrid(rootPane);
        timer.stop("buildContent");
        logger.debug(timer.getSumAsString("buildContent"));
    }

    void buildGrid(Pane pane) {
        for(int i = 0; i < song.getNumbOfRows(); i+= patternController.getStepLength()) {
            Rectangle line = new Rectangle(0, i*cellH, pane.getWidth(), 1);
            line.setFill(gridColor);
            rootPane.getChildren().add(line);
        }
    }

    private Pane buildHeaderPane(int numberOfColumns) {
        Pane header = new Pane();
        header.setBackground(rowBackground);
        header.setPrefWidth(cellW * numberOfColumns);
        header.setPrefHeight(headerH);
        for (int i = 0; i < numberOfColumns; i++) {
            Label trackHeader = new Label("Track" + Integer.toString(i));
            trackHeader.setTranslateX(i * cellW);
            header.getChildren().add(trackHeader);
        }
        return header;
    }

    final Map<Integer, Pane> voicePanes = new HashMap<>();
    private void buildVoicesVisuals() {
        IntStream.range(0, song.getNumbOfVoices()).forEach(this::rebuildVoiceVisual);
    }

    void rebuildVoiceVisual(final int voiceIndex) {
        if(voicePanes.containsKey(voiceIndex)) {
            rootPane.getChildren().remove(voicePanes.get(voiceIndex));
            voicePanes.remove(voiceIndex);
        }
        final Pane voicePane = buildVoice(voiceIndex);
        voicePane.setTranslateX(cellW * voiceIndex);
        rootPane.getChildren().add(0, voicePane);
        voicePanes.put(voiceIndex, voicePane);
    }

    private Pane buildRowNumberPane(final int rows) {
        final Pane leftRowsNumbers = new Pane();
        leftRowsNumbers.setBackground(rowBackground);
        leftRowsNumbers.setPrefWidth(rowHeaderW);
        leftRowsNumbers.setPrefHeight(rows * cellH);
        for (int i = 0; i < rows; i++) {
            final Label rowLabel = new Label(Integer.toString(i));
            rowLabel.setTranslateY(i * cellH);
            leftRowsNumbers.getChildren().add(rowLabel);
        }
        return leftRowsNumbers;
    }

    void updateCrsr(final MouseEvent mouseEvent, final boolean doX, final boolean doY) {
        if(doX) mouseCellX = (int) (mouseEvent.getX() / cellW);
        if(doY) mouseCellY = (int) (mouseEvent.getY() / cellH);
        mouseMarker.setTranslateX(mouseCellX * cellW);
        mouseMarker.setTranslateY(mouseCellY * cellH);
        mouseRowMarker.setTranslateY(mouseMarker.getTranslateY());
        mouseColumnMarker.setTranslateX(mouseMarker.getTranslateX());
    }

    int getNumberOfRowsVisible() {
        return (int) ((patternScrollPane.getHeight() - headerH) / cellH);
    }

    void updatePlayingRowMarker() {
        if (song.isPlaying()) {
            playingRowMarker.setTranslateY(headerH + song.getPlayingLineNr() * cellH);
            playingRowMarker.setWidth(cellW * song.getNumbOfVoices());
            ensureRowNrVisible(song.getPlayingLineNr());
        } else {
            playingRowMarker.setWidth(0);
        }
    }

    private void ensureRowNrVisible(final double rowNr) {
        if (rowNr < getNumberOfRowsVisible() / 2.0) {
            patternScrollPane.setVvalue(0);
        }
        if (rowNr > (song.getNumbOfRows() - getNumberOfRowsVisible() / 2.0)) {
            patternScrollPane.setVvalue(1);
        } else {
            double visible = song.getNumbOfRows() - getNumberOfRowsVisible();
            patternScrollPane.setVvalue((rowNr - getNumberOfRowsVisible() / 2.0) / visible);
        }
    }

    private Pane buildVoice(final int voiceIndex) {
        lengthPosX = 0;
        final Pane pane = new Pane();
        pane.setPrefHeight(song.getNumbOfRows() * cellH);
        pane.setPrefWidth(cellW);
        if ((voiceIndex % 2) == 0) pane.setBackground(voiceBackground);
        else pane.setBackground(voiceBackground2);
        final PatternVoice patternVoice = song.getPattern(patternIndex).getPatternVoice(voiceIndex);
        patternVoice.getEventPools().forEach((timePosition, songEventPool) ->
                visualizeEvent(pane, voiceIndex, timePosition, songEventPool));
        if(isVoiceMuted(voiceIndex)) {
            final Rectangle rectangle = new Rectangle();
            rectangle.setWidth(cellW);
            rectangle.setHeight(song.getNumbOfRows() * cellH);
            rectangle.setFill(Color.color(0.0, 0.0, 0.0, 0.25));
            pane.getChildren().add(rectangle);
        }
        return pane;
    }

    private void visualizeEvent(final Pane pane, final int voiceIndex, final float timePosition, final SongEventPool songEventPool) {
        if (songEventPool.isEmpty()) return;
        final SongEvent event = songEventPool.getSongEvent(false);
        if (event.isEmpty()) return;
        renderCellGfx(pane, voiceIndex, timePosition, event);
    }

    private static final Color dspBackground = Color.color(0.99, 0.8, 1.0, 1.0);
    private static final Color volumeBackground = Color.color(0.7, 0.8, 1.0, 1.0);
    private static final Color sampleBackground = Color.color(0.8, 0.99, 1.0, 1.0);
    private static final Color keryonBackground = Color.color(0.70, 0.70, 0.70, 0.5);

    private void renderCellGfx(final Pane pane, final int voiceIndex, final float timePosition, final SongEvent event) {
        if (event.getFxClass() == SongEventClass.DSP) {
            addCellContent(pane, voiceIndex, timePosition, event, dspBackground);
        } else if (event.getFxClass() == SongEventClass.VOLUME) {
            addCellContent(pane, voiceIndex, timePosition, event, volumeBackground);
        } else if (event.getFxClass() == SongEventClass.SAMPLE) {
            addCellContent(pane, voiceIndex, timePosition, event, sampleBackground);
        } else {
            addCellContent(pane, voiceIndex, timePosition, event, keryonBackground);
        }

    }

    private void addCellContent(final Pane pane, final int voiceIndex, final float timePosition, final SongEvent event, final Color color) {
        double percentage = 1;
        switch(event.getEventType()) {
            case VolumeSet:
            case KeyOn:
                percentage = event.getVolume() / 100.0;
                addEventLengthVisual(pane, voiceIndex, timePosition, color, event.getLengthTicks());
                break;
            case SampleFrom:
            case SampleFromAndPitch:
                percentage = event.getSamplePosition() / 100.0;
                break;
        }
        addPercentageRectangle(pane, timePosition, color, percentage);
        final Text text = new Text(event.getShortDescription());
        text.setFont(getTableFont());
        text.setTranslateY(cellH-2 + timePosition * cellH);
        pane.getChildren().add(text);
    }

    int lengthPosX = 0;
    private double numberOfParallelLanges = 6.0;
    private void addEventLengthVisual(final Pane pane, final int voiceIndex, final float timePosition, final Color color, final double lengthTicks) {
        final Rectangle rectangle = new Rectangle();
        rectangle.setTranslateY(timePosition * cellH);
        rectangle.setTranslateX((lengthPosX / numberOfParallelLanges) * cellW);
        rectangle.setWidth(cellW * (1/numberOfParallelLanges));
        rectangle.setHeight(cellH * lengthTicks / patternController.getMainController().getActualPositionCycl());
        if(crsrX == voiceIndex && crsrY == timePosition) {
            rectangle.setFill(Color.color(1.0, 0.0,0.0,0.2));
        } else {
            rectangle.setFill(color);
        }
        pane.getChildren().add(rectangle);
        lengthPosX += 1;
        lengthPosX = lengthPosX % (int) numberOfParallelLanges;
    }

    private void addPercentageRectangle(final Pane pane, final float timePosition, final Color color, final double percentFill) {
        final Rectangle rectangle = new Rectangle();
        rectangle.setTranslateY(timePosition * cellH);
        rectangle.setWidth(cellW * percentFill);
        rectangle.setHeight(cellH);
        rectangle.setFill(color);
        pane.getChildren().add(rectangle);
        if(percentFill < 1) {
            final Color color2 = color.deriveColor(0.0, 1.0, 1.0, 0.4);
            final Rectangle rectangleFill = new Rectangle();
            rectangleFill.setTranslateY(timePosition * cellH);
            rectangleFill.setTranslateX(4 + cellW * percentFill);
            rectangleFill.setWidth(-4 + cellW * (1-percentFill));
            rectangleFill.setHeight(cellH);
            rectangleFill.setFill(color2);
            pane.getChildren().add(rectangleFill);
        }
    }
}
