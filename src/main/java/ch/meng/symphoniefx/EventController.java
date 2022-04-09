package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.EventType;
import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SongEvent;
import ch.meng.symphoniefx.song.SongEventClass;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

import static ch.meng.symphoniefx.song.SongEventPool.emptySongEvent;

public class EventController {
    public static final int INSTRUMENT_INDEX = 0;
    public static final int PITCH_INDEX = 1;
    public static final int VOLUME_INDEX = 2;
    public static final int SPECIAL_INDEX = 3;
    public static final int EVENT_LENGTH_INDEX = 4;
    private final PatternController patternController;

    public EventController(PatternController patternController) {
        this.patternController = patternController;
    }

    void onObjectChange(SongEvent songEvent) {}

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;

    private Stage stage;
    private Group group;
    private Scene scene;
    private Application application;

    @FXML
    AnchorPane rootPane;
    @FXML
    Label titleLabel;
    @FXML
    GridPane eventGridPane;

    private Song song;
    private Parent parentPane;

    public void setStage(Parent parentPane, Scene scene, Group group, Application application, Stage stage, Song song) {
        this.group = group;
        this.scene = scene;
        this.song = song;
        this.stage = stage;
        this.parentPane = parentPane;
        this.application = application;
        stage.setAlwaysOnTop(true);
        addEventHandlers(stage, scene);
        stage.setOnShown(event -> {
            initUI();
            buildUI();
            loadStagePosition(stage);
            titleLabel.setBackground(SharedStatic.eventDesignerTitleBackground);
            patternController.getEventAtCrsrPosition();
            setEvent(song, patternController.getEventAtCrsrPosition());
        });
    }

    private final FlowPane eventDetail = new FlowPane();
    private ToggleButton toogleKeyon;
    private ToggleButton toogleVolume;
    private ToggleButton tooglePitch;
    private ToggleButton toogleSample;
    private ToggleButton toogleDsp;
    private void buildUI() {
        int x = 0;
        int y = 0;
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(3);
        flowPane.setVgap(2);
        ToggleGroup group = new ToggleGroup();
        toogleKeyon = addEventClassButton("Key", flowPane, group);
        toogleKeyon.setOnAction((event -> selectKeyOnGroup()));
        toogleVolume = addEventClassButton("Vol", flowPane, group);
        toogleVolume.setOnAction((event -> selectVolumeGroup()));
        tooglePitch = addEventClassButton("Pit", flowPane, group);
        tooglePitch.setOnAction((event -> selectPitchGroup()));
        toogleSample = addEventClassButton("Samp", flowPane, group);
        toogleSample.setOnAction((event -> selectSampleGroup()));
        toogleDsp = addEventClassButton("Dsp", flowPane, group);
        toogleDsp.setOnAction((event -> selectDspGroup()));
        eventGridPane.add(flowPane, x++, y);
        eventGridPane.add(eventDetail, x++, y, 3, 1);
        x=4;
        HBox copyPasteGroup = new HBox();
        copyPasteGroup.getChildren().add(new Button("Cut"));
        copyPasteGroup.getChildren().add(new Button("Copy"));
        copyPasteGroup.getChildren().add(new Button("Paste"));
        eventGridPane.add(copyPasteGroup, x++, y);
        y = 1;
        x = 0;
        addEventGroup("Instrument", x, y++);
        addEventGroup("Pitch", x, y++);
        addEventGroup("Volume", x, y++);
        addEventGroup("Special", x, y++);
        addEventGroup("Event length", x, y++);
        groupSliders.get(PITCH_INDEX).setMax(127);
        groupSliders.get(VOLUME_INDEX).setMax(100);
        groupSliders.get(INSTRUMENT_INDEX).setMajorTickUnit(1);
        groupSliders.get(INSTRUMENT_INDEX).setSnapToTicks(true);
        groupSliders.get(INSTRUMENT_INDEX).setMinorTickCount(0);
        groupSliders.get(SPECIAL_INDEX).setMax(100);

        groupSliders.get(INSTRUMENT_INDEX).setMajorTickUnit(4);
        groupSliders.get(EVENT_LENGTH_INDEX).setMax(16);
        groupSliders.get(EVENT_LENGTH_INDEX).setMajorTickUnit(0.25);
        selectKeyOnGroup();
    }

    private ToggleButton addEventClassButton(String text, FlowPane eventType, ToggleGroup group) {
        ToggleButton button = new ToggleButton(text);
        eventType.getChildren().add(button);
        button.setSkin(new ToggleSkinEnhancedWithBlueFrame(button));
        group.getToggles().add(button);
        return button;
    }

    void selectKeyOnGroup() {
        toogleKeyon.setSelected(true);
        eventDetail.getChildren().clear();
        detailButtons.clear();
        ToggleGroup group = new ToggleGroup();
        addDetailButton("KOn", EventType.KeyOn, group);
        addDetailButton("KOff", EventType.KeyOff, group);
        addDetailButton("ReTrig", EventType.Retrig, group);
        addDetailButton("Emphase", EventType.Emphasis, group);
        addDetailButton("Continue", EventType.Continue, group);
        addDetailButton("Stop", EventType.Stop, group);
    }


    Map<EventType, ToggleButton> detailButtons = new HashMap<>();
    private void addDetailButton(String text, EventType eventType, ToggleGroup group) {
        ToggleButton button = new ToggleButton(text);
        button.setSkin(new ToggleSkinEnhancedWithBlueFrame(button));
        button.setId(eventType.toString());
        detailButtons.put(eventType, button);
        group.getToggles().add(button);
        eventDetail.getChildren().add(button);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!proccessEvents) return;
                action(eventType, actualEditingEvent);
                updateVisualsFromEvent();
                onObjectChange(actualEditingEvent);
            }
        });
    }

    public void action(EventType eventType, SongEvent songEvent) {
        switch (eventType) {
            case KeyOn: songEvent.setKeyOn(); onObjectChange(songEvent); break;
            case KeyOff: songEvent.setKeyOff(); onObjectChange(songEvent); break;
            case Emphasis: songEvent.setEmphasis(); onObjectChange(songEvent); break;
            case Retrig: songEvent.setRetrig(); onObjectChange(songEvent); break;
            case Stop: songEvent.setStop(); onObjectChange(songEvent); break;
            case Continue: songEvent.setContinue(); onObjectChange(songEvent); break;

            case VolumeSet: songEvent.setVolume(); onObjectChange(songEvent); break;
            case VolumeAdd: songEvent.setVolumeAdd(); onObjectChange(songEvent); break;
            case VolumeSlideUp: songEvent.setVolumeSlideUp(); onObjectChange(songEvent); break;
            case VolumeSlideDown: songEvent.setVolumeSlideDown(); onObjectChange(songEvent); break;

            case PitchSet: songEvent.setPitch(); onObjectChange(songEvent); break;
            case SampleFromAndPitch: songEvent.setSampleFromAndPitch(); onObjectChange(songEvent); break;
            case SampleFrom: songEvent.setSampleFrom(); onObjectChange(songEvent); break;
        }
    }

    void selectVolumeGroup() {
        toogleVolume.setSelected(true);
        detailButtons.clear();
        eventDetail.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        addDetailButton("Volume", EventType.VolumeSet, group);
        addDetailButton("Add", EventType.VolumeAdd, group);
        addDetailButton("SlideUp", EventType.VolumeSlideUp, group);
        addDetailButton("SlideDown", EventType.VolumeSlideDown, group);
        addDetailButton("CV", EventType.CV, group);
        addDetailButton("CV+", EventType.CVAdd, group);
        addDetailButton("Vibrato", EventType.Vibrato, group);
    }

    void selectPitchGroup() {
        tooglePitch.setSelected(true);
        detailButtons.clear();
        eventDetail.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        addDetailButton("Pitch", EventType.PitchSet, group);
        addDetailButton("Add", EventType.PitchAdd, group);
        addDetailButton("SlideUp", EventType.PitchSlideUp, group);
        addDetailButton("SlideDown", EventType.PitchSlideDown, group);
        addDetailButton("SlideTo", EventType.PitchSlideTo, group);
    }

    void selectSampleGroup() {
        toogleSample.setSelected(true);
        detailButtons.clear();
        eventDetail.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        addDetailButton("From", EventType.SampleFrom, group);
        addDetailButton("From&Pitch", EventType.SampleFromAndPitch, group);
        addDetailButton("Add", EventType.SampleFromAdd, group);
        addDetailButton("AddSet", EventType.SampleFromAddSet, group);
        addDetailButton("SVibrato", EventType.SampleVibrato, group);
    }

    void selectDspGroup() {
        toogleDsp.setSelected(true);
        detailButtons.clear();
        eventDetail.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        addDetailButton("CrossEcho", EventType.DspCrossEcho, group);
        addDetailButton("Echo", EventType.DspEcho, group);
        //addDetailButton("Delay");
        addDetailButton("Off", EventType.DspOff, group);
        //toogleSample.setSelected(true);
    }

    private final List<TextField> groupTextFields = new Vector<>();
    private final List<Slider> groupSliders = new Vector<>();
    private void addEventGroup(final String text, int x, int y) {
        eventGridPane.add(new Label(text), x++, y);

        TextField textField = new TextField();
        textField.setUserData(Integer.valueOf(y));
        groupTextFields.add(textField);
        eventGridPane.add(textField, x++, y);

        Slider slider = new Slider();
        groupSliders.add(slider);
        eventGridPane.add(slider, x++, y, 2, 1);
        x++;
        slider.setUserData(Integer.valueOf(y - 1));
        slider.setMin(0);
        slider.setMax(255);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.valueProperty().addListener((ov, old_val, value) -> {
            if(!proccessEvents) return;
            int index = (Integer) slider.getUserData();
            groupTextFields.get(index).setText(Float.toString(value.floatValue()));
            setParameter(index, value.floatValue());
            onObjectChange(actualEditingEvent);
        });
        skinSlider(slider);
        HBox copyPasteGroup = new HBox();
        copyPasteGroup.getChildren().add(new Button("-"));
        copyPasteGroup.getChildren().add(new Button("+"));
        copyPasteGroup.getChildren().add(new Button("Copy"));
        copyPasteGroup.getChildren().add(new Button("Paste"));
        eventGridPane.add(copyPasteGroup, x++, y);
    }

    private void skinSlider(Slider slider) {
        ValueSliderSkinTextOnlyAdded skin = new ValueSliderSkinTextOnlyAdded(slider, 2);
        slider.setSkin(skin);
    }

    void setParameter(int index, float value) {
        switch (index) {
            case 0: actualEditingEvent.setA(value); break;
            case 1: actualEditingEvent.setB(value); break;
            case 2: actualEditingEvent.setC(value); break;
            case 3: actualEditingEvent.setD(value); break;
            case EVENT_LENGTH_INDEX: actualEditingEvent.setLengthTicks(value); break;
        }
    }

    boolean proccessEvents = true;
    SongEvent actualEditingEvent = new SongEvent();
    public void setEvent(Song song, SongEvent songEvent) {
        proccessEvents = false;
        if(songEvent==emptySongEvent) {
            actualEditingEvent = new SongEvent();
        } else {
            actualEditingEvent = songEvent;
        }
        groupSliders.get(INSTRUMENT_INDEX).setMax(song.getInstrumentsAsList().size());
        proccessEvents = true;
        updateVisualsFromEvent();
    }

    private void updateVisualsFromEvent() {
        proccessEvents = false;
        titleLabel.setText("Event Designer - " + actualEditingEvent.getDescription(true));
        groupTextFields.get(0).setText(Float.toString(actualEditingEvent.getA()));
        groupTextFields.get(1).setText(Float.toString(actualEditingEvent.getB()));
        groupTextFields.get(2).setText(Float.toString(actualEditingEvent.getC()));
        groupTextFields.get(3).setText(Float.toString(actualEditingEvent.getD()));
        groupTextFields.get(EVENT_LENGTH_INDEX).setText(Double.toString(actualEditingEvent.getLengthTicks()));
        groupSliders.get(0).setValue(actualEditingEvent.getA());
        groupSliders.get(1).setValue(actualEditingEvent.getB());
        groupSliders.get(2).setValue(actualEditingEvent.getC());
        groupSliders.get(3).setValue(actualEditingEvent.getD());
        groupSliders.get(EVENT_LENGTH_INDEX).setValue(actualEditingEvent.getLengthTicks());
        switch(actualEditingEvent.getFxClass()) {
            case SongEventClass.SAMPLE:
        }
        updateButtonsFromEvent(actualEditingEvent);
        proccessEvents = true;
    }

    public void updateButtonsFromEvent(SongEvent songEvent) {
        switch (songEvent.getEventType()) {
            case KeyOn: selectKeyOnGroup();break;
            case KeyOff: selectKeyOnGroup();break;
            case Emphasis: selectKeyOnGroup();break;
            case Retrig: selectKeyOnGroup();break;
            case Stop: selectKeyOnGroup();break;
            case Continue: selectKeyOnGroup();break;

            case VolumeSet: selectVolumeGroup(); break;
            case VolumeAdd: selectVolumeGroup(); break;
            case VolumeSlideUp: selectVolumeGroup(); break;
            case VolumeSlideDown: selectVolumeGroup(); break;

            case PitchSet: selectPitchGroup(); break;
            case PitchAdd: selectPitchGroup(); break;
            case PitchSlideDown: selectPitchGroup(); break;
            case PitchSlideUp: selectPitchGroup(); break;
            case PitchSlideTo: selectPitchGroup(); break;

            case SampleFromAndPitch: selectSampleGroup(); break;
            case SampleFrom: selectSampleGroup(); break;
            case SampleFromAdd: selectSampleGroup(); break;
            case SampleFromAddSet: selectSampleGroup(); break;
            case SampleVibrato: selectSampleGroup(); break;
            case SampleFromPitchVolume: selectSampleGroup(); break;

            case DspCrossEcho: selectDspGroup(); break;
            case DspEcho: selectDspGroup(); break;
            case DspOff: selectDspGroup(); break;
        }
        ToggleButton button = detailButtons.get(songEvent.getEventType());
        if(button != null) button.setSelected(true);
    }

    private void addEventHandlers(Stage stage, Scene scene) {
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            rootPane.setPrefHeight(scene.getHeight());
            rootPane.setPrefWidth(scene.getWidth());
        });
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            rootPane.setPrefHeight(scene.getHeight());
            rootPane.setPrefWidth(scene.getWidth());
        });
    }

    void quit() {
        saveStagePosition(stage);
    }

    void initUI() {
        initSystemUI();
        initGlobalKeyboardShortcuts();
    }

    private void initSystemUI() {
        stage.setTitle("Event Editor v1.0");
    }

    private void initGlobalKeyboardShortcuts() {
        rootPane.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.E) && event.isControlDown()) {
                quit();
                //stage.setOnCloseRequest();
                stage.close();
            }
        });
    }

    private int parse(String text, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt(text);
        } catch (Exception ignore) {
        }
        return value;
    }

    PropertyManager environment = new PropertyManager("EventEditor", this.getClass().getSimpleName());
    private void saveStagePosition(final Stage stage) {
        environment.saveUIState(stage);
        environment.save();
    }
    private void loadStagePosition(final Stage stage) {
        if (!environment.load()) return;
        environment.initFromSavedProperty(stage);
    }
}
