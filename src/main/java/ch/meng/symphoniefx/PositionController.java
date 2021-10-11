package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.Song;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;
import ch.meng.symphoniefx.song.Position;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static ch.meng.symphoniefx.SharedConst.*;
import static ch.meng.symphoniefx.SharedStatic.parseDouble;

public class PositionController {
    public static final String ACTION_POSITION_INSERT = "Insert";
    public static final String ACTION_POSITION_DUPLICATE = "Duplicate";
    public static final String ACTION_POSITION_COPY = "Copy";
    public static final String ACTION_POSITION_PASTE = "Paste";
    public static final String ACTION_POSITION_CUT = "Cut";
    public static final String UI_ID_PATTERN = "Pattern";

    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    GridPane gridPane;
    ListView<String> positionList;
    Song song;
    PositionController(GridPane positionDetailGrid, ListView<String> positionList, Song song) {
        gridPane = positionDetailGrid;
        this.song = song;
        this.positionList = positionList;
        buildUI();
    }

    public void setSong(Song song) {
        this.song = song;
    }

    void onObjectChange(int positionIndex, Position position) {}

    int x = 0;
    int y = 0;
    private void buildUI() {
        gridPane.addRow(0);
        addBasicButtons();
        addRowWithFields("Nr",0,511, UI_ID_PATTERN, 0, 128);
        addRowWithFields("Start",0,127, "Length",1,128);
        addRowWithFields("Loop",1,100, "Tune",-24,24);
        addEditField("Speed",1,100);
    }

    private void setPosition(Position position) {
        setPosition(positionIndex, position);
        song.getPositions()[positionIndex] = position;
    }

    Position actualPosition;
    int positionIndex;
    void setPosition(int positionIndex, Position position) {
        proccessEvents = false;
        logger.debug("setPosition:"+position);
        this.positionIndex = positionIndex;
        this.actualPosition = position;
        setUIValue("Nr", positionIndex);
        setUIValue(UI_ID_PATTERN, position.getPatternNumber());
        setUIValue("Start", position.getStartRow());
        setUIValue("Length", position.getRowLength());
        setUIValue("Loop", position.getNumbOfLoops());
        setUIValue("Tune", position.getTune());
        setUIValue("Speed", position.getSpeed_Cycl());
        proccessEvents = true;
    }

    int getActualPositionIndex() {return positionIndex;}
    Position getActualPosition() {return actualPosition;}

    void uiToPosition() {
        actualPosition.setSinglePatternNumber((int) getUIValue(UI_ID_PATTERN));
        actualPosition.setStartRow((int) getUIValue("Start"));
        actualPosition.setRowLength((int) getUIValue("Length"));
        actualPosition.setNumbOfLoops((int) getUIValue("Loop"));
        actualPosition.setTune((int) getUIValue("Tune"));
        actualPosition.setSpeed_Cycl((int) getUIValue("Speed"));
        logger.debug("uiToPosition:"+ actualPosition);
        onObjectChange(positionIndex, actualPosition);
    }

    Button pasteButton;
    private void addBasicButtons() {
        ButtonBar buttonsbar = new ButtonBar();
        gridPane.add(buttonsbar, 0, y, 5,1);
        addButton(ACTION_POSITION_DUPLICATE, buttonsbar, duplicateBackground);
        addButton(ACTION_POSITION_COPY, buttonsbar, copyBackground);
        addButton(ACTION_POSITION_CUT, buttonsbar, cutBackground);
        pasteButton = addButton(ACTION_POSITION_PASTE, buttonsbar, pasteBackground);
        pasteButton.setDisable(true);
        addButton(ACTION_POSITION_INSERT, buttonsbar, null);
        buttonsbar.setButtonMinWidth(50);
        buttonsbar.snappedLeftInset();
        y++;
    }

    private Button addButton(String text, ButtonBar buttonsbar, Background background) {
        Button button = new Button(text);
        if(background != null) button.setBackground(background);
        buttonsbar.getButtons().add(button);
        ButtonBar.setButtonData(button, ButtonBar.ButtonData.LEFT);
        button.setOnAction(event -> buttonAction(button.getText()));
        return button;
    }

    void buttonAction(String command) {
        switch (command) {
            case ACTION_POSITION_INSERT:insertActualPosition();break;
            case ACTION_POSITION_DUPLICATE:duplicateActualPosition();break;
            case ACTION_POSITION_COPY:copyActualPosition();break;
            case ACTION_POSITION_PASTE:pasteToActualPosition();break;
            case ACTION_POSITION_CUT:cutActualPosition();break;
        }
    }

    Position tempPosition = new Position();
    void copyActualPosition() {
        tempPosition = new Position(actualPosition);
        pasteButton.setDisable(false);
    }

    void insertActualPosition() {
        duplicateActualPosition();
        pasteToActualPosition();
    }

    void cutActualPosition() {
        copyActualPosition();
        song.removePositionAt(positionIndex);
        rebuildPositionList();
    }

    void pasteToActualPosition() {
        actualPosition = new Position(tempPosition);
        setPosition(actualPosition);
        rebuildPositionList();
    }

    void duplicateActualPosition() {
        positionIndex = getActualPositionIndex();
        song.insertPositionAt(actualPosition, positionIndex);
        rebuildPositionList();
    }

    public void rebuildPositionList() {
        int index = positionList.getSelectionModel().getSelectedIndex();
        positionList.getItems().clear();
        int positionNr = 0;
        for (Position position : song.getPositions()) {
            positionList.getItems().add(positionNr + " " + position.toString());
            positionNr++;
        }
        positionList.getSelectionModel().select(index);
    }

    private void addRowWithFields(final String text, double min, double max, final String text2, double min2, double max2) {
        x = 0;
        addEditField(text, min, max);
        x = 3;
        addEditField(text2, min2, max2);
        x = 0;
        y++;
    }

    void setUIValue(final String key, double value) {
        //logger.trace("set "+key + " to " + value);
        if(!uiObjects.containsKey(key)) {
            return;
        }
        Spinner<Double> spinner = (Spinner<Double>) uiObjects.get(key);
        spinner.getValueFactory().setValue(value);
    }

    double getUIValue(final String key) {
        if(!uiObjects.containsKey(key)) {
            return 0;
        }
        Spinner<Double> spinner = (Spinner<Double>) uiObjects.get(key);
        //logger.trace("get "+key + " " + spinner.getValueFactory().getValue());
        return spinner.getValueFactory().getValue();
    }

    Map<String, Node> uiObjects = new HashMap();
    private void addEditField(final String text, double min, double max) {
        gridPane.add(new Label(text), x++, y);
        Spinner<Double> spinner = new Spinner<>();
        spinner.setUserData(text);
        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, 0, 1);
        spinner.setValueFactory(valueFactory);
        spinner.setPrefWidth(90);
        addEventHandler(spinner);
        uiObjects.put(text, spinner);
        gridPane.add(spinner, x++, y);
    }

    private void addButton(final String text) {
        gridPane.add(new Button(text), x++, y);
    }

    boolean proccessEvents = false;
    private void addEventHandler(Spinner<Double> spinner) {
        spinner.valueProperty().addListener((observable, oldValue, value) -> {
            if (proccessEvents && value != null) {
                uiToPosition();
            }
        });
        spinner.setEditable(true);
        spinner.focusedProperty().addListener((observable, oldValue, focusGot) -> {
            if (proccessEvents && !focusGot) {
                double value = parseDouble(spinner.getEditor().getText());
                if (ILLEGAL_VALUE_PARSED == value) return;
                spinner.getValueFactory().setValue(value);
                uiToPosition();
            }
        });
    }

}

