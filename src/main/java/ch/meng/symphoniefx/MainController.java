package ch.meng.symphoniefx;

import ch.meng.symphoniefx.dsp.DspCrossEchoStereo;
import ch.meng.symphoniefx.dsp.DspEchoStereo;
import ch.meng.symphoniefx.dsp.DspStereoEffect;
import ch.meng.symphoniefx.mixer.*;
import ch.meng.symphoniefx.rendering.AudioRenderingController;
import ch.meng.symphoniefx.song.*;
import com.sun.jmx.remote.internal.ArrayQueue;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import symreader.*;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ch.meng.symphoniefx.SharedConst.*;
import static ch.meng.symphoniefx.SharedStatic.*;


public class MainController {
    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    public static final String SYMPHONIE_VERSION = "Symphonie Fx v2.10";
    public static final String LASTSONG_KEY = "LASTSONG";
    public static final String SAVE_SONG_AS_PATH = "saveSongAsPath";
    public static final String EVENT_DESIGNER_VISIBLE_KEY = "EventDesignerVisible";
    public static final String ROOM_DESIGNER_VISIBLE_KEY = "RoomDesignerVisible";
    public static final int LAST_OPENED_FILES_MAX = 16;

    public static final String FILE_HISTORY = "FileHistory";
    public static final String SAMPLE_HISTORY = "SampleHistory";
    public static final String HISTORY_SPLITTER = "!-!";
    public static final String userDir = System.getProperty("user.home");


    public MainController() {
        logger.debug("MainController() booting...");
    }

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    VBox rootPane;

    @FXML
    AnchorPane mainPane;

    @FXML
    ListView<String> positionList;
    @FXML
    ListView<String> sequenceList;
    @FXML
    ListView<Label> instrumentList;

    @FXML
    ListView<String> logList;

    @FXML
    SplitPane songDetailSplitter;
    @FXML
    SplitPane sequencePositionsSplitter;
    @FXML
    SplitPane instrumentPatternSplitter;
    @FXML
    SplitPane instrumentWaveformSplitter;
    @FXML
    SplitPane songInstrPatternSplitter;


    @FXML
    AnchorPane visualsPane;
    @FXML
    AnchorPane patternPane;
    @FXML
    Pane testPane;
    @FXML
    ScrollPane patternScrollPane;

    @FXML
    Label mainLabel;

    @FXML
    ProgressIndicator progressIndicator;

    @FXML
    ChoiceBox<String> sampleRateChoice;
    @FXML
    ChoiceBox<String> oversampleChoice;
    @FXML
    Button volumeButton;
    @FXML
    Slider volumeSlider;
    @FXML
    Button tuneButton;
    @FXML
    Slider tuneSlider;
    @FXML
    Button bpmButton;
    @FXML
    Slider bpmSlider;

    @FXML
    Button sampleDiffButton;
    @FXML
    Slider sampleDiffSlider;
    @FXML
    Button pitchDiffButton;
    @FXML
    Slider pitchDiffSlider;

    @FXML
    ChoiceBox<String> dspChoice;
    @FXML
    Button feedbackResetButton;
    @FXML
    Slider dspFeedbackSlider;
    @FXML
    Button dspResetFramesButton;
    @FXML
    Slider dspFrames;




    @FXML
    ChoiceBox<InterpolationTypeEnum> interpolationChoiceBox;
    @FXML
    CheckBox anticlickActivatedCheckbox;
    @FXML
    Slider anticlickSlider;
    @FXML
    ChoiceBox ditheringChoiceBox;

    @FXML
    CheckBox eqActivatedCheckBox;
    @FXML
    Slider eqLowIntensitySlider;
    @FXML
    Slider eqMidIntensitySlider;
    @FXML
    Slider eqHighIntensitySlider;
    @FXML
    Slider eqLPFrequencySlider;
    @FXML
    Slider eqHPFrequencySlider;


    @FXML
    MenuItem reloadSongMenuItem;
    @FXML
    Menu recentMenu;
    @FXML
    Menu recentSampleMenu;
    @FXML
    ToggleButton recordToogle;

    @FXML
    GridPane testGridPane;

    @FXML
    CheckMenuItem javaSampleImporter;
    @FXML
    CheckMenuItem updatePatternMenuItem;

    @FXML
    GridPane sequenceDetailGrid;
    @FXML
    GridPane positionDetailGrid;

    private Stage stage;
    private Group group;
    private Scene scene;
    private HostServices hostServices;
    private boolean isRendering = false;
    boolean mouseHandlerAdded = false;
    PropertyManager mainProperties = new PropertyManager("Environment", this.getClass().getSimpleName());
    PatternController patternController;
    PositionController positionController;
    SequenceController sequenceController;
    TestController testController;

    public HostServices getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    void loadWithDefaultProgramm() {
        getHostServices().showDocument(instrumentController.getActualInstrument().getName());
    }

    @FXML
    void reloadSample() {
        File sampleFile = new File(instrumentController.getActualInstrument().getName());
        instrumentController.doLoadSample(sampleFile);
    }

    void initPositionsUI() {
        positionController = new PositionController(positionDetailGrid, positionList, song) {
            @Override
            void onObjectChange(int positionIndex, Position position) {
                logger.debug("onObjectChange:" + position);
                refreshPositionListAt(positionIndex, position);
                patternController.newPatternView.notifyPositionChanged(position);
            }
        };
    }

    @FXML
    void loadSample() {
        instrumentController.loadSample();
    }

    @FXML
    Button duplicateButton;

    @FXML
    void DuplicatePattern() {
        patternController.duplicatePattern();
    }

    @FXML
    void Add2Voices() {
        song.setNumbOfVoices(song.getNumbOfVoices()+2);
        patternController.setRebuildColumns();
        patternController.updatePatternViewForce();
        //patternController.moveToPattern(patternNrSpinner.getValue());
    }

    @FXML
    void doubleRowlength() {

    }

    @FXML
    ToggleButton playToggle;

    @FXML
    void clearSample() {
        instrumentController.clearSample();
    }

    private void refreshPositionListAt(int positionIndex, Position position) {
        logger.debug("refreshPositionListAt:" + positionIndex + "to" + position);
        positionList.getItems().set(positionIndex, positionIndex + " " + position.toString());
        moveToPattern(position.getPatternNumber());
    }

    void moveToPattern(int patternIndex) {
        patternNrSpinner.getValueFactory().setValue(patternIndex);
    }

    @FXML
    Spinner<Integer> eventLength;
    @FXML
    Spinner<Integer> patternTune;

    void initUI() {
        logger.debug("VERSION:" + SYMPHONIE_VERSION);
        logger.debug("initUI()");
        playToggle.setSkin(new ToggleSkinEnhancedWithBlueFrame(playToggle));
        duplicateButton.setBackground(duplicateBackground);
        initSystemUI();
        initDspUI();
        initPositionsUI();
        initGlobalKeyboardShortcuts();
        patternController = new PatternController(song, patternPane,
                patternNrSpinner, patternStep, eventLength, patternTune, this, patternScrollPane) {
//            @Override
//            void onPositionMove() {
//                notifyPatternCrsrMoved();
//            }
        };

        recordToogle.selectedProperty().addListener((ov, old_val, value) -> {
            if (value.booleanValue()) {
                recordToogle.setBackground(recordBackground);
            } else {
                recordToogle.setBackground(null);
            }
        });
        initSoundUI();
        initInstrumentUI();
        newSong();

        if (mainProperties.getBooleanProperty(EVENT_DESIGNER_VISIBLE_KEY)) showEventDesigner();
        initAudioDeviceUI();


        testController = new TestController(testGridPane, getVoiceExpander());
    }

    InstrumentController instrumentController;
    void initInstrumentUI() {
        instrumentController = new InstrumentController(this,instrumentList, song);
    }

    @FXML
    ChoiceBox<String> audioDeviceChoiceBox;
    @FXML
    ComboBox<Integer> bufferLenComboBox;

    AudioHAL audioHAL = new AudioHAL();

    @FXML
    void reinitAudioSystem() {
        shutdownAndExit();
        initAudioDeviceUI();
        voiceExpander = null;
        backgroundMusicTask = null;
        executorService = null;
        startAudioSystem();
    }

    void initAudioDeviceUI() {
//        audioDevices = AudioSystem.getMixerInfo();
//        for (Mixer.Info mixerinfo : audioDevices) {
//            audioDeviceChoiceBox.getItems().add(mixerinfo.getName() + ":" + mixerinfo.getDescription());
//        }
        audioDeviceChoiceBox.getItems().addAll(audioHAL.getAudioOutputDevices());
        audioDeviceChoiceBox.getSelectionModel().select(0);

        bufferLenComboBox.getItems().add(2048);
        bufferLenComboBox.getItems().add(4096);
        bufferLenComboBox.getItems().add(8192);
        bufferLenComboBox.getItems().add(8192*2);
        bufferLenComboBox.getItems().add(8192*4);
        bufferLenComboBox.getSelectionModel().select(1);
        audioDeviceChoiceBox.valueProperty().addListener((ov, old_val, type) -> {
            initAudioWithNewFrequency();
        });
        bufferLenComboBox.valueProperty().addListener((ov, old_val, type) -> {
            initAudioWithNewFrequency();
        });
    }

    @FXML
    void rebuildAllVirtualSamples() {
        VirtualSampleBuilder virtualSampleBuilder = new VirtualSampleBuilder();
        virtualSampleBuilder.buildVirtualSamples(song);
    }

    private void initSoundUI() {
        interpolationChoiceBox.getItems().addAll(InterpolationTypeEnum.None,
                InterpolationTypeEnum.Linear,
                InterpolationTypeEnum.Cubic,
                InterpolationTypeEnum.Cosinus,
                InterpolationTypeEnum.Hermite, InterpolationTypeEnum.JavaNearest);
        interpolationChoiceBox.setValue(InterpolationTypeEnum.Hermite);
        interpolationChoiceBox.valueProperty().addListener((ov, old_val, type) -> {
            voiceExpander.getSampleInterpolator().setInterpolateType(type);
        });
    }

    private void initSystemUI() {
        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 1000, 120, 0.1);
        songBpmSpinner.setValueFactory(valueFactory);
        // if user enters value it first enters valueProperty, then textProperty
        // if user clicks up or down it first enters textProperty, then valueProperty
        songBpmSpinner.valueProperty().addListener((observable, oldValue, value) -> {
            if (value != null) {
                logger.debug("Bpm value set to:" + value);
                song.setBpm(value);
                if (getVoiceExpander() != null) getVoiceExpander().setBpm(value);
            }
        });
        songBpmSpinner.setEditable(true);
        songBpmSpinner.focusedProperty().addListener((observable, oldValue, focusGot) -> {
            if (!focusGot) {
                double value = parseDouble(songBpmSpinner.getEditor().getText());
                if (ILLEGAL_VALUE_PARSED == value) return;
                logger.debug("Bpm focusedProperty" + value);
                song.setBpm(value);
                if (getVoiceExpander() != null) getVoiceExpander().setBpm(value);
            }
        });

        sampleRateChoice.getItems().addAll("11.025 kHz", "22.05 kHz", "44.1 kHz", "48 kHz", "88.2 kHz", "96 kHz", "192 kHz");
        if (!mainProperties.initFromSavedProperty(sampleRateChoice, "sampleRateChoice")) {
            sampleRateChoice.setValue(sampleRateChoice.getItems().get(2));
            print("MixFrequency resetted to " + sampleRateChoice.getValue());
        }
        sampleRateChoice.valueProperty().addListener((ov, old_val, text) -> {
            float value = Float.parseFloat(text.replace(" kHz", ""));
            if (getVoiceExpander() != null) {
                mixfrequency = (int) (value * 1000);
                initAudioWithNewFrequency();
            }
        });

        oversampleChoice.getItems().addAll("Off", "2x Oversample", "4x Oversample", "8x Oversample");
        if (!mainProperties.initFromSavedProperty(oversampleChoice, "oversampleChoice")) {
            oversampleChoice.setValue(oversampleChoice.getItems().get(0));
        }
        oversampleChoice.valueProperty().addListener((ov, old_val, text) -> {
            oversample = 1 << oversampleChoice.getSelectionModel().getSelectedIndex();

            initAudioWithNewFrequency();
        });


        volumeSlider.valueProperty().addListener((ov, old_val, newValue) -> {
            updateVolumeSliderBackground();
            if (voiceExpander != null) voiceExpander.setMasterVolume(newValue.floatValue());
        });
        bpmSlider.valueProperty().addListener((ov, old_val, bpm) -> {
            if (voiceExpander != null) voiceExpander.setMasterBpmTune(bpm.floatValue());
        });
        tuneSlider.valueProperty().addListener((ov, old_val, newValue) -> {
            if (voiceExpander != null) voiceExpander.setMasterTune(newValue.floatValue());
        });
        sampleDiffSlider.valueProperty().addListener((ov, old_val, newValue) -> {
            if (voiceExpander != null) voiceExpander.setSampleDiff(newValue.floatValue());
        });
        pitchDiffSlider.valueProperty().addListener((ov, old_val, newValue) -> {
            if (voiceExpander != null) voiceExpander.setPitchDiff(newValue.floatValue());
        });

        instrumentPatternSplitter.getDividers().get(0).positionProperty().addListener((ov, old_val, value) -> {
            instrumentController.visualizeWaveform();
        });

        eqLowIntensitySlider.valueProperty().addListener((ov, old_val, value) -> {
            if (voiceExpander != null) voiceExpander.setEQLowIntensity(value.floatValue() / 50);
        });
        eqMidIntensitySlider.valueProperty().addListener((ov, old_val, value) -> {
            if (voiceExpander != null) voiceExpander.setEQMidIntensity(value.floatValue() / 50);
        });
        eqHighIntensitySlider.valueProperty().addListener((ov, old_val, value) -> {
            if (voiceExpander != null) voiceExpander.setEQHighIntensity(value.floatValue() / 50);
        });
        eqLPFrequencySlider.valueProperty().addListener((ov, old_val, value) -> {
            if (voiceExpander != null) voiceExpander.setEQLPFrequency(value.floatValue());
        });
        eqHPFrequencySlider.valueProperty().addListener((ov, old_val, value) -> {
            if (voiceExpander != null) voiceExpander.setEQHPFrequency(value.floatValue());
        });
        eqActivatedCheckBox.selectedProperty().addListener((ov, old_val, value) -> {
            if (voiceExpander != null) voiceExpander.enableEQ(value);
            updateTitle();
        });
        eqLPFrequencySlider.setValue(880);
        eqHPFrequencySlider.setValue(5000);
        mainProperties.initFromSavedProperty(eqLowIntensitySlider, "eqLowIntensitySlider");
        mainProperties.initFromSavedProperty(eqMidIntensitySlider, "eqMidIntensitySlider");
        mainProperties.initFromSavedProperty(eqHighIntensitySlider, "eqHighIntensitySlider");
        mainProperties.initFromSavedProperty(eqLPFrequencySlider, "eqLPFrequencySlider");
        mainProperties.initFromSavedProperty(eqHPFrequencySlider, "eqHPFrequencySlider");

        getVoiceExpander().setEQLowIntensity(eqLowIntensitySlider.getValue());
        getVoiceExpander().setEQMidIntensity(eqMidIntensitySlider.getValue());
        getVoiceExpander().setEQHighIntensity(eqHighIntensitySlider.getValue());
        getVoiceExpander().setEQLPFrequency(eqLPFrequencySlider.getValue());
        getVoiceExpander().setEQHPFrequency(eqHPFrequencySlider.getValue());
    }

    @FXML
    void resetEQToDefault() {
        eqLPFrequencySlider.setValue(880);
        eqHPFrequencySlider.setValue(5000);
        eqLowIntensitySlider.setValue(0);
        eqMidIntensitySlider.setValue(0);
        eqHighIntensitySlider.setValue(0);
    }

    private void initGlobalKeyboardShortcuts() {
        rootPane.setOnKeyPressed(event -> {
            addStandardKeyEvents(event);
            if (event.isControlDown()) return;
            if (scene.getFocusOwner() instanceof Spinner) return;
            if (scene.getFocusOwner() instanceof ChoiceBox) return;
            if (scene.getFocusOwner() instanceof ComboBox) return;
            if (scene.getFocusOwner() instanceof TextField) return;
            if(event.getCode().equals(KeyCode.CONTROL)) return;
            addKeyboardPlayEvents(event);
        });
        rootPane.setOnKeyReleased(event -> {
            addKeyboardOffEvents(event);
        });
        midiManager = new MidiManager();
    }

    public void addStandardKeyEvents(KeyEvent event) {
        if (event.getCode().equals(KeyCode.SPACE)) tooglePlaySong();
        if (event.getCode().equals(KeyCode.F1)) instrumentController.loadSample();
        if (event.getCode().equals(KeyCode.F2)) playWholeSong();
        if (event.getCode().equals(KeyCode.F3)) {
            stopSong();
        }
        if (event.getCode().equals(KeyCode.ESCAPE)) {
            if (song.isPlaying()) {
                stopSong();
            }
            stopAllChannels();
        }
        if (event.getCode().equals(KeyCode.ADD)) {instrumentController.programNext();}
        if (event.getCode().equals(KeyCode.SUBTRACT)) instrumentController.programPrevious();
    }

    MidiManager midiManager;

    private void addKeyboardPlayEvents(KeyEvent event) {
        int noteBase = this.noteBase;
        int noteBaseLower = this.noteBaseLower;
        if(event.isShiftDown()) noteBase += 12;
        if(event.isShiftDown()) noteBaseLower -= 12;
        switch (event.getCode()) {
            case BACK_SPACE:
            case DELETE:
                patternController.clearEventAtCrsr();
                break;
            case Q:
                keyboardKeyon(noteBase);
                break;
            case DIGIT2:
                keyboardKeyon(noteBase + 1);
                break;
            case W:
                keyboardKeyon(noteBase + 2);
                break;
            case DIGIT3:
                keyboardKeyon(noteBase + 3);
                break;
            case E:
                keyboardKeyon(noteBase + 4);
                break;
            case R:
                keyboardKeyon(noteBase + 5);
                break;
            case DIGIT5:
                keyboardKeyon(noteBase + 6);
                break;
            case T:
                keyboardKeyon(noteBase + 7);
                break;
            case DIGIT6:
                keyboardKeyon(noteBase + 8);
                break;
            case Z:
                keyboardKeyon(noteBase + 9);
                break;
            case DIGIT7:
                keyboardKeyon(noteBase + 10);
                break;
            case U:
                keyboardKeyon(noteBase + 11);
                break;
            case I:
                keyboardKeyon(noteBase + 12);
                break;
            case DIGIT9:
                keyboardKeyon(noteBase + 13);
                break;
            case O:
                keyboardKeyon(noteBase + 14);
                break;
            case DIGIT0:
                keyboardKeyon(noteBase + 15);
                break;
            case P:
                keyboardKeyon(noteBase + 14);
                break;
            case Y:
                keyboardKeyon(noteBaseLower);
                break;
            case S:
                keyboardKeyon(noteBaseLower + 1);
                break;
            case X:
                keyboardKeyon(noteBaseLower + 2);
                break;
            case D:
                keyboardKeyon(noteBaseLower + 3);
                break;
            case C:
                keyboardKeyon(noteBaseLower + 4);
                break;
            case V:
                keyboardKeyon(noteBaseLower + 5);
                break;
            case G:
                keyboardKeyon(noteBaseLower + 6);
                break;
            case B:
                keyboardKeyon(noteBaseLower + 7);
                break;
            case H:
                keyboardKeyon(noteBaseLower + 8);
                break;
            case N:
                keyboardKeyon(noteBaseLower + 9);
                break;
            case J:
                keyboardKeyon(noteBaseLower + 10);
                break;
            case M:
                keyboardKeyon(noteBaseLower + 11);
                break;
            case COMMA:
                keyboardKeyon(noteBaseLower + 12);
                break;
            case L:
                keyboardKeyon(noteBaseLower + 13);
                break;
            case PERIOD:
                keyboardKeyon(noteBaseLower + 14);
                break;
            case UNDEFINED:
                if(event.getText().equals("ö")) keyboardKeyon(noteBaseLower + 15);
                break;
            case MINUS:
                keyboardKeyon(noteBaseLower + 16);
                break;
        }
    }

    private void addKeyboardOffEvents(KeyEvent event) {
        int noteBase = this.noteBase;
        int noteBaseLower = this.noteBaseLower;
        if(event.isShiftDown()) noteBase += 12;
        if(event.isShiftDown()) noteBaseLower -= 12;
        switch (event.getCode()) {
            case Q:
                keyboardKeyoff(noteBase);
                break;
            case DIGIT2:
                keyboardKeyoff(noteBase + 1);
                break;
            case W:
                keyboardKeyoff(noteBase + 2);
                break;
            case DIGIT3:
                keyboardKeyoff(noteBase + 3);
                break;
            case E:
                keyboardKeyoff(noteBase + 4);
                break;
            case R:
                keyboardKeyoff(noteBase + 5);
                break;
            case DIGIT5:
                keyboardKeyoff(noteBase + 6);
                break;
            case T:
                keyboardKeyoff(noteBase + 7);
                break;
            case DIGIT6:
                keyboardKeyoff(noteBase + 8);
                break;
            case Z:
                keyboardKeyoff(noteBase + 9);
                break;
            case DIGIT7:
                keyboardKeyoff(noteBase + 10);
                break;
            case U:
                keyboardKeyoff(noteBase + 11);
                break;
            case I:
                keyboardKeyoff(noteBase + 12);
                break;
            case DIGIT9:
                keyboardKeyoff(noteBase + 13);
                break;
            case O:
                keyboardKeyoff(noteBase + 14);
                break;
            case DIGIT0:
                keyboardKeyoff(noteBase + 15);
                break;
            case P:
                keyboardKeyoff(noteBase + 14);
                break;
            case Y:
                keyboardKeyoff(noteBaseLower);
                break;
            case S:
                keyboardKeyoff(noteBaseLower + 1);
                break;
            case X:
                keyboardKeyoff(noteBaseLower + 2);
                break;
            case D:
                keyboardKeyoff(noteBaseLower + 3);
                break;
            case C:
                keyboardKeyoff(noteBaseLower + 4);
                break;
            case V:
                keyboardKeyoff(noteBaseLower + 5);
                break;
            case G:
                keyboardKeyoff(noteBaseLower + 6);
                break;
            case B:
                keyboardKeyoff(noteBaseLower + 7);
                break;
            case H:
                keyboardKeyoff(noteBaseLower + 8);
                break;
            case N:
                keyboardKeyoff(noteBaseLower + 9);
                break;
            case J:
                keyboardKeyoff(noteBaseLower + 10);
                break;
            case M:
                keyboardKeyoff(noteBaseLower + 11);
                break;
            case COMMA:
                keyboardKeyoff(noteBaseLower + 12);
                break;
            case L:
                keyboardKeyoff(noteBaseLower + 13);
                break;
            case PERIOD:
                keyboardKeyoff(noteBaseLower + 14);
                break;
            case UNDEFINED:
                if(event.getText().equals("ö")) keyboardKeyoff(noteBaseLower + 15);
                break;
            case MINUS:
                keyboardKeyoff(noteBaseLower + 16);
                break;
        }
    }

    private void initDspUI() {
        dspChoice.getItems().addAll("Off", DspCrossEchoStereo.getName(), DspEchoStereo.getName());
        dspChoice.setValue(dspChoice.getItems().get(0));
        dspChoice.valueProperty().addListener((ov, old_val, text) -> {
            if (voiceExpander == null) return;
            double feedback = dspFeedbackSlider.getValue() * 0.01;
            int delay = (int) dspFrames.getValue();
            if (text.equals("Off")) voiceExpander.setDspEcho(false, delay, 0);
            if (text.equals(DspEchoStereo.getName())) voiceExpander.setDspEcho(true, delay, feedback);
            if (text.equals(DspCrossEchoStereo.getName())) voiceExpander.setDspCrossEcho(true, delay, feedback);
        });
        dspFeedbackSlider.setValue(50);
        dspFeedbackSlider.setMax(100);
        dspFeedbackSlider.setMin(0);
        dspFeedbackSlider.setMajorTickUnit(10);
        dspFeedbackSlider.setShowTickLabels(true);
        dspFeedbackSlider.setShowTickMarks(true);
        dspFeedbackSlider.valueProperty().addListener((ov, old_val, newValue) -> {
            if (voiceExpander != null) voiceExpander.getDsp().adjustFeedback(dspFeedbackSlider.getValue() / 100.0);
        });

        dspFrames.setValue(16);
        dspFrames.setMax(64);
        dspFrames.setMin(2);
        dspFrames.setSnapToTicks(true);
        dspFrames.setMajorTickUnit(4);
        dspFrames.setShowTickLabels(true);
        dspFrames.setShowTickMarks(true);
        dspFrames.valueProperty().addListener((ov, old_val, frames) -> {
            updateDspNumberOfFrames();
        });
        ValueSliderSkinTextOnlyAdded skin = new ValueSliderSkinTextOnlyAdded(dspFrames, 0);
        dspFrames.setSkin(skin);
        ValueSliderSkinTextOnlyAdded skin2 = new ValueSliderSkinTextOnlyAdded(dspFeedbackSlider, 1);
        dspFeedbackSlider.setSkin(skin2);
    }

    private void updateDspNumberOfFrames() {
        if (voiceExpander != null) {
            voiceExpander.getDsp().adjustDelayFrames((int) dspFrames.getValue(),
                    (int) dspFrames.getValue() * voiceExpander.getNumberOfSamplesPerCycl() / 8);
        }
    }

    private void updateDspUI() {
        if (voiceExpander.hasDspChanged()) {
            DspStereoEffect dsp = voiceExpander.getDsp();
            if (!dsp.isActive()) {
                dspChoice.getSelectionModel().select(0);
            } else {
                dspFeedbackSlider.setValue(dsp.getFeedback() * 100);
                dspFrames.setValue(voiceExpander.getDspDelayFrames());
                if (dsp instanceof DspCrossEchoStereo) {
                    dspChoice.getSelectionModel().select(1);
                }
                if (dsp instanceof DspEchoStereo) {
                    dspChoice.getSelectionModel().select(2);
                }
            }
            updateTitle();
        }
    }

    int noteBase = 36;
    int noteBaseLower = noteBase - 12;

    void stopAllChannels() {
        initVoiceExpander();
        voiceExpander.stopAllVoices();
        voiceExpander.printAndResetStatistics();
        allVstPluginsStop();
        notesPlaying.clear();
        voicesPlaying.clear();
    }

    Set<Integer> notesPlaying = new HashSet<>();
    Map<Integer, Voice> voicesPlaying = new HashMap<>();
    Map<Double, Integer> keyOffPitches = new HashMap<>();
    TimeMeasure2 keyboardTimer = new TimeMeasure2();

    void keyboardKeyon(int pitch) {
        pitch = addKeyboardPitchOffset(pitch);
        if(notesPlaying.isEmpty()) {
            keyboardTimer.start("Keyon");
        }
        if(notesPlaying.contains(pitch)) return;
        notesPlaying.add(pitch);
        if(instrumentController.getActualInstrument().getInstrumentSource().equals(InstrumentSource.Vst)) {
            instrumentController.getActualInstrument().getVstManager()
                    .playKeyOn(pitch+instrumentController.getActualInstrument().getTune(), 0);
        } else {
            initVoiceExpander();
            Voice voice = voiceExpander.playKeyboardInstrumentNote(instrumentController.getActualInstrument(), pitch, 100);
            voicesPlaying.put(pitch, voice);
            startAudioDevice();
            if (recordToogle.isSelected()) {
                patternController.addKeyonEvent(pitch, instrumentController.getActualInstrument().getIndex());
            }
        }
    }

    private int addKeyboardPitchOffset(int pitch) {
        pitch += patternTune.getValue();
        return pitch;
    }

    void keyboardKeyoff(int pitch) {
        pitch = addKeyboardPitchOffset(pitch);
        if (!notesPlaying.contains(pitch)) return;
        notesPlaying.remove(pitch);
        if(instrumentController.getActualInstrument().getInstrumentSource().equals(InstrumentSource.Vst)) {
            instrumentController.getActualInstrument().getVstManager().playKeyOff(pitch+instrumentController.getActualInstrument().getTune());
            keyOffPitches.put(keyboardTimer.getTimeElapsedInMilliseconds("Keyon"), pitch+instrumentController.getActualInstrument().getTune());
        } else if(instrumentController.getActualInstrument().getInstrumentSource().equals(InstrumentSource.Sample)) {
            Voice voice = voicesPlaying.get(pitch);
            if(voice!=null) {
                voice.stop();
                voicesPlaying.remove(pitch);
            }
        }
        if(notesPlaying.isEmpty()) {
            recordVstEvents();
            keyOffPitches.clear();
            if(notesPlaying.isEmpty()) {
                keyboardTimer.start("Keyon");
            }
        }
    }

    private void recordVstEvents() {
        if (recordToogle.isSelected() && instrumentController.getActualInstrument().getInstrumentSource().equals(InstrumentSource.Vst)) {
            int offset = 0;
            int length = getKeyOnLengthQuantized();
            for(int pitch : keyOffPitches.values()) {
                patternController.addKeyonEventVst(pitch,
                        instrumentController.getActualInstrument().getIndex(),
                        offset,
                        100,
                        length);
                offset++;
            }
            resetVisualsForLoading();
            patternController.updatePatternViewForce();
            patternController.advanceCrsrOneStep();
        }
    }

    int getKeyOnLengthQuantized() {
        int length = 1 + ((int) getKeyOnBeatLength() / patternStep.getValue());
        length *= patternStep.getValue();
        return length;
    }

    double getKeyOnBeatLength() {
        if(keyboardTimer.getTimeElapsedInMilliseconds("Keyon") <= 0) return 0;
        //return 2 * keyboardTimer.getTimeElapsedInMilliseconds("Keyon") / (getActualPositionCycl() * getVoiceExpander().getNumberOfMillisecondsPerCycl());
        return 2 * keyboardTimer.getTimeElapsedInMilliseconds("Keyon") / (getVoiceExpander().getNumberOfMillisecondsPerCycl());

    }

    String getKeyOnTime() {
        if(notesPlaying.isEmpty()) return "";
        double time = keyboardTimer.getTimeElapsedInMilliseconds("Keyon");
        double test = getVoiceExpander().getNumberOfMillisecondsPerCycl();
        double cycl = positionController.getActualPosition().getSpeed_Cycl();
        return " Rows:" + getKeyOnLengthQuantized() + " Beat:" + String.format("%.3f", getKeyOnBeatLength()) + " Time:" + time;
    }

    public int getActualPositionCycl() {
        return positionController.getActualPosition().getSpeed_Cycl();
    }

    @FXML
    void setDefaultVolume() {
        if (100.0 == volumeSlider.getValue()) {
            volumeSlider.setValue(0.0);
        } else {
            volumeSlider.setValue(100.0);
        }
        updateVolumeSliderBackground();
    }

    private void updateVolumeSliderBackground() {
        if (0.0 == volumeSlider.getValue()) {
            volumeSlider.setBackground(errorBackgroundLight);
        } else {
            volumeSlider.setBackground(null);
        }
    }

    @FXML
    void setDefaultTune() {
        tuneSlider.setValue(0.0);
    }

    @FXML
    void setDefaultBpm() {
        bpmSlider.setValue(100.0);
    }

    @FXML
    void setDefaultPitchDiff() {
        pitchDiffSlider.setValue(0.0);
    }

    @FXML
    void setDefaultSampleDiff() {
        sampleDiffSlider.setValue(0.0);
    }


    @FXML
    void stopSong() {
        if (song != null) song.stopSong();
        if (voiceExpander != null) voiceExpander.stopAllVoices();
        voiceExpander.printAndResetStatistics();
        print("Playing Song Stopped.");
        updateVisualToPlayMode();
    }

    @FXML
    void playSong() {
        playWholeSong();
        updateVisualToPlayMode();
    }

    @FXML
    void playPositionEndless() {
        if (voiceExpander != null) voiceExpander.clearAllPlayedVoices();
        song.playPositionEndless(positionController.getActualPositionIndex());
        updateVisualToPlayMode();
    }

    @FXML
    void playPatternEndless() {
        if (voiceExpander != null) voiceExpander.clearAllPlayedVoices();
        logger.debug("playPatternEndless");
        logger.debug("patternNrSpinner.getValue():" + patternNrSpinner.getValue());
        logger.debug("getPatternZ"+ patternController.getPatternZ());
        song.playPatternEndless(positionController.getActualPositionIndex(), patternController.getPatternZ());
        updateVisualToPlayMode();
    }

    @FXML
    void playFromPosition() {
        if (voiceExpander != null) voiceExpander.clearAllPlayedVoices();
        song.playPositionEndless(positionController.getActualPositionIndex());
        updateVisualToPlayMode();
    }

    private int playFromActualPosition() {
        int index = positionList.getSelectionModel().getSelectedIndex();
        if (voiceExpander != null) voiceExpander.clearAllPlayedVoices();
        song.playFromPosition(index);
        updateVisualToPlayMode();
        return index;
    }

    @FXML
    void continuePlayingSong() {
        if (song != null && song.isContentLoaded()) {
            song.playfromActualPosition();
        }
        updateVisualToPlayMode();
    }

    void tooglePlaySong() {
        if (song == null) return;
        if (song.isPlaying()) {
            stopSong();
        } else {
            continuePlayingSong();
        }
    }

    private void updateVisualToPlayMode() {
        if(!song.isPlaying()) {
            mainLabel.setBackground(null);
            playToggle.setSelected(false);
        }
        else if(song.getPlaySongMode().equals(PlaySongMode.PatternRepeat) || song.getPlaySongMode().equals(PlaySongMode.PositionRepeat)) {
            mainLabel.setBackground(playingSongRepeatedBackground);
        } else {
            setSongPlayingVisual();
        }
    }

    private void setSongPlayingVisual() {
        mainLabel.setBackground(playingSongBackground);
    }

    @FXML
    Button renderToFileButton;

    @FXML
    void renderToFile() {
        if (isRendering) return;
        getVoiceExpander().setSong(new Song());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AudioRendering2.fxml"));
        AnchorPane parentPane = null;
        AudioRenderingController audioRenderingController = new AudioRenderingController();
        try {
            loader.setController(audioRenderingController);
            parentPane = loader.load();
        } catch (IOException exception) {
            logger.error(exception);
            exception.printStackTrace();
            return;
        }
        Stage audioRenderingStage = new Stage();
        audioRenderingStage.setTitle("Audio Rendering v1.0");
        Group group = new Group(parentPane);
        Scene scene = new Scene(group, 1000, 600, true);
        audioRenderingStage.setScene(scene);
        audioRenderingController.setStage(parentPane, scene, group, application, audioRenderingStage, song);
        audioRenderingStage.setOnCloseRequest(event -> {
            audioRenderingController.quit();
            onAudioRenderingClosing();
        });
        isRendering = true;
        audioRenderingStage.show();
    }

    void onAudioRenderingClosing() {
        if (getVoiceExpander() != null) getVoiceExpander().setSong(song);
        isRendering = false;
        song.restoreInstrumentMuteState();
        progressIndicator.setProgress(0.0);
    }


    void notifyPatternCrsrMoved() {
        if (eventController == null) return;
        SongEvent event = song.getSongEvent(patternController.getPatternZ(),
                patternController.getPatternX(),
                patternController.getPatternY());
        if (event == null) return;
        eventController.setEvent(song, event);
    }

    EventController eventController;
    Stage eventDesignerStage;
    @FXML
    void showEventDesigner() {
        if(eventDesignerStage!= null && !eventDesignerStage.isShowing()) {
            eventController = null;
        }
        if (eventController != null) {
            eventController.quit();
            eventDesignerStage.close();
            eventController = null;
            return;
        }

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventEditor.fxml"));
        final AnchorPane parentPane;
        eventController = new EventController(patternController) {
            @Override
            void onObjectChange(SongEvent songEvent) {
                patternController.setEventAtCrsr(songEvent);
                logger.debug("Event Changed:" + songEvent.getShortDescription());
            }
        };

        try {
            loader.setController(eventController);
            parentPane = loader.load();
        } catch (IOException exception) {
            logger.error(exception);
            exception.printStackTrace();
            return;
        }
        eventDesignerStage = new Stage();
        final Group group = new Group(parentPane);
        final Scene scene = new Scene(group, 1000, 600, true);
        eventDesignerStage.setScene(scene);
        eventController.setStage(parentPane, scene, group, application, eventDesignerStage, song);
        eventDesignerStage.setOnCloseRequest(event -> {
            eventController.quit();
            eventController = null;
        });
        eventDesignerStage.show();
    }

    Stage roomDesignerStage;
    RoomDesignerController roomDesignerController;

    @FXML
    void showRoomDesigner() {
        if (roomDesignerController != null) {
            roomDesignerController.quit();
            roomDesignerStage.close();
            roomDesignerController = null;
            return;
        }
        AnchorPane parentPane;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RoomDesigner.fxml"));
            roomDesignerController = new RoomDesignerController();
            loader.setController(roomDesignerController);
            parentPane = loader.load();
        } catch (IOException exception) {
            logger.error(exception);
            exception.printStackTrace();
            return;
        }
        roomDesignerStage = new Stage();
        Group group = new Group(parentPane);
        Scene scene = new Scene(group, 1000, 600, true);
        roomDesignerStage.setScene(scene);
        roomDesignerController.setStage(parentPane, scene, group, application, roomDesignerStage, song);
        roomDesignerStage.setOnCloseRequest(event -> {
            roomDesignerController.quit();
            roomDesignerController = null;
            showRoomDesignerMenuItem.setSelected(false);
        });
        roomDesignerStage.show();
    }

    @FXML
    void clearLog() {
        logList.getItems().clear();
    }

    public void print(final String text) {
        logger.debug(text);
        logList.getItems().add(text);
        //logList.getSelectionModel().select(logList.getItems().size() - 1);
    }

    private void print(final Exception exception) {
        print(exception.getMessage());
    }

    int renderedVSTFrames = 0;
    boolean vstOk = true;
    private VoiceExpander voiceExpander;
    private void initVoiceExpander() {
        if (song == null) {
            print("Init Song");
            song = new Song();
            instrumentController.setActualInstrument(song.getInstrument(0));
        }
        if (voiceExpander == null) {
            print("initVoiceExpander");
            voiceExpander = new VoiceExpander() {
                @Override
                public void onAddStream(final double[] destinationBufferPtr, final int numberOfStereoFrames) {
                    try {
                        for(SymphonieInstrument vstInstrument : song.getVstInstruments()) {
                            if(vstInstrument.getVstManager() != null
                                    && vstInstrument.getVstManager().isReady()
                                    && vstInstrument.getVstManager().getRenderer() != null) {
                                vstOk = vstInstrument.getVstManager().getRenderer()
                                        .stream(destinationBufferPtr, vstInstrument.getVolume());
                            }
                        }

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        logger.error(exception);
                    }
                }
            };
            voiceExpander.initMixSystem(false);
            if(song!=null) song.setLinkedVoiceExpander(voiceExpander);
        }
    }

    VoiceExpander getVoiceExpander() {
        initVoiceExpander();
        return voiceExpander;
    }

    private Song song = new Song();

    @FXML
    void newSong() {
        this.song.stopSong();
        getVoiceExpander().stopAllVoices();
        voiceExpander.printAndResetStatistics();
        this.song = new Song();
        songBpmSpinner.getValueFactory().setValue(song.getBpm());
        song.initInstrumentsForNewSong();
        song.initPositionsForNewSong();
        this.song.setContentLoaded(true);
        getVoiceExpander().setSong(song);
        patternController.muteAllVoices(false);
        getVoiceExpander().setMuteAllVoices(false);
        updateSong();
        updateTitle();
        updateStatus();
        setMainLabel("Unnamed");
        resetVisualsForLoading();
        rebuildSequenceAndPositionLists();
        instrumentController.rebuildInstrumentList();
        updateVisualsToPosition(0, true);
        instrumentController.setActualInstrument(song.getInstrument(0));
        instrumentController.updateInstrumentUI(instrumentController.getActualInstrument());
        instrumentController.visualizeWaveform();
        patternController.setRebuildColumns();
        patternController.moveToPattern(0, true);
    }

    @FXML
    void saveSong() {
        if (!song.isContentLoaded()) {
            addProgressMessage("Error:Nothing to save. saveSong Aborted.");
            return;
        }
        print("Saving Song");
        SongSaver songSaver = new SongSaver();
        String message = songSaver.saveSong(song);
        addProgressMessage(message);
    }

    void addProgressMessage(String message) {
        ProgressMessage.add(group, message);
    }

    @FXML
    void saveSongAs() {
        String saveSongAsPath = mainProperties.getProperty(SAVE_SONG_AS_PATH);
        if (saveSongAsPath.isEmpty()) {
            saveSongAsPath = mainProperties.getProperty(LASTSONG_KEY);
        }
        File directory = new File(saveSongAsPath);
        if (!directory.exists()) saveSongAsPath = "";
        File fileToSave = showSaveAsDialog(saveSongAsPath, getModSaveFilters());
        if(fileToSave==null) return;
        song.setName(fileToSave.getPath());
        print("Saving Song:" + fileToSave.getPath());

        mainProperties.setProperty(SAVE_SONG_AS_PATH, fileToSave.getParent());
        mainProperties.save();

        SongSaver songSaver = new SongSaver();
        String message = songSaver.saveSong(song);
        addToFileHistory(songSaver.getSavedSongPath());
        addProgressMessage(message);
    }

    TimeMeasure timeMeasure = new TimeMeasure();

    void playWholeSong() {
        timeMeasure.start("Song");
        if (song == null || !song.isContentLoaded()) return;
        print("Playing Song started...");
        initVoiceExpander();
        voiceExpander.setSong(song);
        song.stopSong();
        voiceExpander.stopAllVoices();
        voiceExpander.printAndResetStatistics();
        voiceExpander.setSong(song);
        voiceExpander.setSongSpeed(song.getBpm(), song.getPositionSpeed());
        song.PlayFromFirstSequence(true);
        voiceExpander.setSongSpeed(song.getBpm(), song.getPositionSpeed());
        startAudioDevice();
        setSongPlayingVisual();
        resetVisualsForLoading();
        patternNrSpinner.getValueFactory().setValue(song.getPlayingPatternNr());
    }

    Application application;

    @FXML
    void quit() {
        shutdownAllVstPlugins();
        if (eventController != null) eventController.quit();
        if (roomDesignerController != null) roomDesignerController.quit();
        shutdownAndExit();
        //freeResources();
        Platform.exit();
    }

    void shutdownAllVstPlugins() {
        for(SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            if(instrument.getVstManager()!=null) instrument.getVstManager().shutdownVst();
        }
    }
    void allVstPluginsStop() {
        for(SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            if(instrument.getVstManager()!=null) instrument.getVstManager().allKeysOff();
        }
    }

    public void setStage(final Application application, final Stage stage, final Group group, final Scene scene) {
        this.stage = stage;
        this.group = group;
        this.scene = scene;
        this.application = application;

        stage.setOnCloseRequest(event -> {
            quit();
        });
        stage.setOnShown(event -> {
            addPositionsEventListeners();
            addEventHandlers(stage, scene);
            styleUI();
            startAudioSystem();
            initUI();
            updateTitle();
            loadStagePosition(stage);
        });
    }



    private void addEventHandlers(final Stage stage, final Scene scene) {
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            rootPane.setPrefHeight(scene.getHeight());
            rootPane.setPrefWidth(scene.getWidth());
        });
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            rootPane.setPrefHeight(scene.getHeight());
            rootPane.setPrefWidth(scene.getWidth());
        });

        if (!mouseHandlerAdded) {
            mouseHandlerAdded = true;
//            stage.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
//                if (event.getCode().equals(KeyCode.ESCAPE)) {
//                }
//                if (event.getCode().equals(KeyCode.F1)) {
//                }
//                if (event.getCode().equals(KeyCode.F2)) {
//                }
//                if (event.getCode().equals(KeyCode.F3)) {
//                }
//                if (event.getCode().equals(KeyCode.F4)) {
//                }
//            });
            visualsPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.isControlDown()) {
                    visibleVoices -= event.getTextDeltaY();
                    if (visibleVoices < 2) visibleVoices = 2;
                    if (visibleVoices > 48) visibleVoices = 48;
                } else {
                    patternMouseScroll -= event.getTextDeltaY();
                    if (patternMouseScroll < 0) patternMouseScroll = 0;
                    if (patternMouseScroll + visibleVoices > 64) patternMouseScroll = 64 - visibleVoices;
                }
            });
        }
    }

    private int visibleVoices = 16;
    int patternMouseScroll = 0;

    final static Color blackTransparentColor = Color.color(0.0, 0.0, 0.0, 0.8);
    final static Background blackBackground = new Background(new BackgroundFill(Color.color(0.0, 0.0, 0.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    final static Color textColor = Color.color(1.0, 1.0, 1.0, 1);
    final static Background errorBackground = new Background(new BackgroundFill(Color.color(1.0, 0.0, 0.0, 0.7), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    final static Background tuneButtonBackground = new Background(new BackgroundFill(Color.color(0.9, 0.95, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    final static Background stereoExpandBackground = new Background(new BackgroundFill(Color.color(0.9, 1.0, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    final static Background renderBackground = new Background(new BackgroundFill(Color.color(1.0, 1.0, 0.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));

    private void styleUI() {
        loadButton.setBackground(blackBackground);
        renderToFileButton.setBackground(renderBackground);
        if (song != null) {
            loadButton.setTextFill(Color.LIGHTBLUE);
        } else {
            loadButton.setTextFill(textColor);
        }
        volumeButton.setBackground(playingSongBackground);
        tuneButton.setBackground(tuneButtonBackground);
        bpmButton.setBackground(tuneButtonBackground);
        sampleDiffButton.setBackground(stereoExpandBackground);
        pitchDiffButton.setBackground(stereoExpandBackground);
    }

    private boolean shutdown = false;
    void shutdownAndExit() {
        shutdown = true;
        saveStagePosition(this.stage);
        if (voiceExpander != null) voiceExpander.closeAll();
        if (backgroundMusicTask != null) {
            backgroundMusicTask.shutdown();
            backgroundMusicTask.cancel(true);
        }
        if (executorService != null) executorService.shutdown();
        voiceExpander.shutdown();
        voiceExpander = null;
    }

    private void freeResources() {
        audioHAL = null;
        backgroundMusicTask = null;
        song = null;
        instrumentController = null;
        positionController = null;
        patternController.shutdown();
        patternController = null;
        eventController = null;
        positionList = null;
        sequenceController = null;
        midiManager = null;
        System.gc();
    }

    @FXML
    void loadMod() {
        if (isRendering) return;
        print("loadMod");
        mainLabel.setBackground(null);
        final String oldText = mainLabel.getText();
        setMainLabel("Symphonie : Selecting File");
        logger.debug("load()");
        File modfile = loadMod("ModPath", null);
        if (modfile == null) {
            setMainLabel(oldText);
            return;
        }
        loadMod2(modfile);
    }

    private void setMainLabel(String oldText) {
        mainLabel.setText(oldText);
    }

    @FXML
    void reLoadSong() {
        String lastSongPath = mainProperties.getProperty(LASTSONG_KEY);
        if (lastSongPath.isEmpty()) return;
        loadMod2(new File(lastSongPath));
    }

    private void loadMod2(final File modfile) {
        patternController.muteAllVoices(false);
        getVoiceExpander().disableDsp();
        mainLabel.setBackground(null);
        reloadSongMenuItem.setText("Reload " + modfile.getPath());
        mainProperties.setProperty(LASTSONG_KEY, modfile.getPath());
        mainProperties.save();
        print("Loading " + modfile.getPath());
        setMainLabel("Symphonie : Loading mod...");
        OldSymModFormatImporter oldFormatReader = new OldSymModFormatImporter();
        Song newsong = new Song();
        SongLoader songLoader = new SongLoader();
        try {
            newsong = oldFormatReader.load(modfile);
            if (newsong.isContentLoaded()) logger.debug("Song in old format loaded");
            if (!newsong.isContentLoaded()) {
                //newsong = songReader.loadSong(modfile);
                newsong = songLoader.loadSong(modfile, getBufferLenInSamples(), getMixfrequency());
                if (newsong.isContentLoaded()) logger.debug("Song in new format loaded");
            }
            songBpmSpinner.getValueFactory().setValue(newsong.getBpm());
        } catch (Exception exception) {
            addProgressMessage("Error: Failed loading of " + modfile.getPath());
            logger.error(exception);
            exception.printStackTrace();
        }

        oldFormatReader.getLoadingMessages().forEach(this::print);
        if (!newsong.isContentLoaded()) {
            setMainLabel("Loading failed " + modfile.getPath());
            mainLabel.setBackground(errorBackground);
            addProgressMessage("Error: Failed loading of " + modfile.getPath());
            return;
        }
        song = newsong;
        song.setBpm(newsong.getBpm());
        updateSong();
        progressIndicator.setProgress(0.0);
        logger.debug("Bpm of Song set to " + newsong.getBpm());
        patternController.setRebuildColumns();
        setMainLabel(modfile.getName());
        rebuildSequenceAndPositionLists();
        sequenceList.getSelectionModel().select(0);
        positionList.getSelectionModel().select(0);
        styleUI();
        if (voiceExpander != null) {
            voiceExpander.setSong(song);
            song.setLinkedVoiceExpander(voiceExpander);
            voiceExpander.clearAllPlayedVoices();
            voiceExpander.setBpm(song.getBpm());
            voiceExpander.initMixSystem(false);
        }

        resetVisualsForLoading();
        updateVisualsToPosition(0, true);
        patternController.moveToPattern(0, true);
        print("LoadMod2 end");
        updateTitle();
        addProgressMessage("Loading Song " + modfile.getPath() + " done.");

        instrumentController.rebuildInstrumentList();
        instrumentList.getSelectionModel().select(0);
        instrumentController.moveToInstrument(0);
        instrumentController.visualizeWaveform();
        System.gc();
    }

    private void updateSong() {
        patternController.setSong(song);
        instrumentController.setSong(song);
        positionController.setSong(song);
    }

    private void rebuildSequenceAndPositionLists() {
        positionController.rebuildPositionList();
        sequenceList.getItems().clear();
        if (song.getSequences() != null && song.getSequences().length > 0) {
            for (Sequence sequence : song.getSequences()) {
                if (sequence != null) sequenceList.getItems().add(sequence.toString());
            }
        }
    }

    private File loadMod(final String pathPropertyName, final List<FileChooser.ExtensionFilter> extensionFilters) {
        final String filePath = mainProperties.getProperty(pathPropertyName);
        final String filename = mainProperties.getProperty(pathPropertyName + "-Filename");
        final File file = getFile(filePath, filename, getModFilters());
        if (file == null) return null;
        mainProperties.setProperty(pathPropertyName, file.getParent());
        mainProperties.setProperty(pathPropertyName + "-Filename", file.getName());
        mainProperties.save();
        addToFileHistory(file.getPath());
        return file;
    }

    File loadSample(final String pathPropertyName, final List<FileChooser.ExtensionFilter> extensionFilters) {
        final String filePath = mainProperties.getProperty(pathPropertyName);
        final String filename = mainProperties.getProperty(pathPropertyName + "-Filename");
        final File file = getFile(filePath, filename, getSampleFilters());
        if (file == null) return null;
        mainProperties.setProperty(pathPropertyName, file.getParent());
        mainProperties.setProperty(pathPropertyName + "-Filename", file.getName());
        return file;
    }

    private File getFile(final String filePath, final String filename, final List<FileChooser.ExtensionFilter> filters) {
        final FileChooser fileChooser = new FileChooser();
        if (filePath != null && filePath.length() > 0) {
            try {
                final File file = new File(filePath);
                if (file.canRead()) {
                    fileChooser.setInitialDirectory(file);
                    fileChooser.setInitialFileName(filename);
                }
            } catch (Exception exception) {
                logger.debug("Directory not found" + filePath);
                exception.printStackTrace();
            }
        }
        if (filters != null) fileChooser.getExtensionFilters().addAll(filters);
        return fileChooser.showOpenDialog(stage);
    }

    private File showSaveAsDialog(String filePath, List<FileChooser.ExtensionFilter> filters) {
        final FileChooser fileChooser = new FileChooser();
        if (filePath != null && filePath.length() > 0) {
            try {
                final File file = new File(filePath);
                if (file.canRead()) {
                    if (file.isFile()) {
                        fileChooser.setInitialDirectory(file.getParentFile());
                    } else if (file.isDirectory()) {
                        fileChooser.setInitialDirectory(file);
                    }
                }
            } catch (Exception exception) {
                logger.debug("Directory not found" + filePath);
                exception.printStackTrace();
            }
        }
        if (filters != null) fileChooser.getExtensionFilters().addAll(filters);
        return fileChooser.showSaveDialog(stage);
    }


    ArrayQueue<String> lastFilesHistory = new ArrayQueue<>(LAST_OPENED_FILES_MAX);
    private void addToFileHistory(final String filename) {
        if (LAST_OPENED_FILES_MAX == lastFilesHistory.size()) lastFilesHistory.remove(0);
        if (lastFilesHistory.size() > 0 && lastFilesHistory.get(lastFilesHistory.size() - 1).equals(filename)) return;
        lastFilesHistory.add(filename);
        final MenuItem newFileItem = new MenuItem(filename);
        newFileItem.setOnAction(event -> {
            final MenuItem menuItem = (MenuItem) event.getSource();
            final File file = new File(menuItem.getText());
            if (file.exists()) loadMod2(file);
        });
        recentMenu.getItems().add(newFileItem);
        saveHistory(lastFilesHistory, FILE_HISTORY);
    }

    ArrayQueue<String> lastSamplesHistory = new ArrayQueue<>(LAST_OPENED_FILES_MAX);
    void addToSampleFileHistory(final String filename) {
        if (LAST_OPENED_FILES_MAX == lastSamplesHistory.size()) lastSamplesHistory.remove(0);
        if (lastSamplesHistory.size() > 0 && lastSamplesHistory.get(lastSamplesHistory.size() - 1).equals(filename)) return;
        lastSamplesHistory.add(filename);
        final MenuItem newFileItem = new MenuItem(filename);
        newFileItem.setOnAction(event -> {
            final MenuItem menuItem = (MenuItem) event.getSource();
            final File file = new File(menuItem.getText());
            if (file.exists()) instrumentController.loadSample(file);
        });
        recentSampleMenu.getItems().add(newFileItem);
        saveHistory(lastSamplesHistory, SAMPLE_HISTORY);
    }

    void saveHistory(ArrayQueue<String> history, String id) {
        StringBuilder text = new StringBuilder();
        for (String path : history) {
            text.append(path).append(HISTORY_SPLITTER);
        }
        mainProperties.setProperty(id, text.toString());
    }

    void loadHistory() {
        String text = mainProperties.getProperty(FILE_HISTORY);
        if (text.isEmpty()) return;
        for (String filepath : text.split(HISTORY_SPLITTER)) {
            filepath.replaceAll(HISTORY_SPLITTER, "");
            if (!filepath.isEmpty()) addToFileHistory(filepath);
        }
    }

    void loadSampleHistory() {
        String text = mainProperties.getProperty(SAMPLE_HISTORY);
        if (text.isEmpty()) return;
        for (String filepath : text.split(HISTORY_SPLITTER)) {
            filepath.replaceAll(HISTORY_SPLITTER, "");
            if (!filepath.isEmpty()) addToSampleFileHistory(filepath);
        }
    }
    private List<FileChooser.ExtensionFilter> getModFilters() {
        List<FileChooser.ExtensionFilter> extensionFilters = new Vector<>();
        extensionFilters.add(new FileChooser.ExtensionFilter("Modules", "*.symMod", "*.symmod2*"));
        extensionFilters.add(new FileChooser.ExtensionFilter("All", "*"));
        return extensionFilters;
    }

    private List<FileChooser.ExtensionFilter> getModSaveFilters() {
        List<FileChooser.ExtensionFilter> extensionFilters = new Vector<>();
        extensionFilters.add(new FileChooser.ExtensionFilter("Modules", "*.symmod2*"));
        extensionFilters.add(new FileChooser.ExtensionFilter("All", "*"));
        return extensionFilters;
    }

    private List<FileChooser.ExtensionFilter> getSampleFilters() {
        List<FileChooser.ExtensionFilter> extensionFilters = new Vector<>();
        extensionFilters.add(new FileChooser.ExtensionFilter("Samples", "*.*"));
        extensionFilters.add(new FileChooser.ExtensionFilter("All", "*"));
        return extensionFilters;
    }

    private File showSaveFileDialog(final String pathPropertyName, final TextField textField) {
        final String filePath = mainProperties.getProperty(pathPropertyName);
        final FileChooser fileChooser = new FileChooser();
        if (filePath != null && filePath.length() > 0) {
            final File file = new File(filePath);
            if (file.getParentFile().canRead()) {
                fileChooser.setInitialDirectory(file.getParentFile());
            }
        }
        fileChooser.getExtensionFilters().addAll(getModFilters());
        final File file = fileChooser.showSaveDialog(stage);
        if (file == null) return null;
        if (textField != null) textField.setText(file.getPath());
        mainProperties.setProperty(pathPropertyName, file.getParent());
        return file;
    }

    void addPositionsEventListeners() {
        positionList.setOnMousePressed(mouseEvent -> {
            if (song == null) return;
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                int index = playFromActualPosition();
                updateVisualsToPosition(index, true);
            }
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                moveToPosition(positionList.getSelectionModel().getSelectedIndex(), true);
            }
        });
        positionList.setOnKeyReleased(event -> {
            addStandardKeyEvents(event);
        });
    }

    @FXML
    void exportSample() {
        if (!song.isContentLoaded()) return;
        exportSampleIndex(instrumentController.getActualInstrument());
    }

    SongIO songIO = new SongIO();

    private void exportSampleIndex(SymphonieInstrument instrument) {
        export(instrument, "");
    }

    private void export(final SymphonieInstrument instrument, String directoryName) {
        try {
            if (instrument.getMultiChannel().equals(MultichannelEnum.StereoR)) return;
            String name = song.getName();
            if (name.toLowerCase().endsWith(".symmod")) {
                name = name.substring(0, name.length() - 7);
            }
            String instrumentName = instrument.getShortDescription();
            instrumentName = instrumentName.replaceAll(":", " ");
            instrumentName = instrumentName.replaceAll("\\*", " ");
            instrumentName = instrumentName.replaceAll("\\\\", " ");
            if (!directoryName.isEmpty()) name = directoryName + "\\";
            File file = new File(name
                    + " Instr-" + instrument.getIndex()
                    + " " + instrumentName
                    + instrument.getSampleImporter().getFileSuffix());
            songIO.exportSample(song, file, instrument.getIndex());
        } catch (Exception exception) {
            logger.error("export " + instrument.getShortDescription() + "failed " + exception.getMessage());
            exception.printStackTrace();
        }

    }

    @FXML
    void exportAllSamples() {
        exportAllSamples("");
    }

    void exportAllSamples(String directoryName) {
        if (!song.isContentLoaded()) return;
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            export(instrument, directoryName);
        }
    }

    SampleImporterJavaInternal sampleImporterJavaInternal = new SampleImporterJavaInternal();

    static ColorSet colors = new LightColorSet();

    @FXML
    Canvas renderVisualizeCanvas;

    void notifyUpdateUI() {
        try {
            if(shutdown) return;
            updateStatus();
            updateDspUI();
            List<String> messagesFromSubtask = backgroundMusicTask.getCachedMessages();
            messagesFromSubtask.forEach(this::print);
            messagesFromSubtask.clear();
            if (song != null && song.isPlaying()) {
                progressIndicator.setProgress(song.getPlayedPercentageOfAllSong());
                if(updatePatternMenuItem.isSelected()) {
                    if (patternVisible) {
                        updateVisualsToPosition(song.getPlayingPositionNr(), false);
                        patternController.updatePlayingRowMarker();
                    }
                }
            }
            drawVisuals();
            if(testController!=null) testController.notifyUIUpdate();
            if(!vstOk) {
                instrumentController.setVstInfo("ERROR:Vst high amplitude");
                logger.error("ERROR:Vst high amplitude:" + instrumentController.getActualInstrument().getName());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("notifyUpdateUI()" + exception);
        }
    }

    private void drawVisuals() {
        getVoiceExpander().enableVisualisation(!patternVisible);
        if (!patternVisible) {
            renderVisualizeCanvas.setWidth(visualsPane.getWidth());
            renderVisualizeCanvas.setHeight(visualsPane.getHeight());
            final GraphicsContext gc = renderVisualizeCanvas.getGraphicsContext2D();
            gc.setImageSmoothing(false);
            if (getVoiceExpander() != null
                    && getVoiceExpander().getStereoMixVisual() != null
                    && !getVoiceExpander().getStereoMixVisual().getSamples().isEmpty()) {
                visualizeStereoMix(gc);
                visualizeVoices(gc, getVoiceExpander());
            }
        }
    }

    private void updateStatus() {
        StringBuilder text = new StringBuilder();
        text.append("Bpm:").append((int) song.getBpm());
        text.append("BPM:").append((int) getVoiceExpander().getBpm());
        long total = Runtime.getRuntime().totalMemory();
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        text.append(" Mem:").append(used / 1024 / 1024);
        text.append("(").append(total / 1024 / 1024).append(")");
        if (song != null && song.isPlaying()) {
            int patternNr = song.getPosition(song.getPlayingPositionNr()).getPatternNumber();
            text.append(" Time:" + timeMeasure.getTimeElapsed("Song"));
//            text.append(" Max Ampl:" + getVoiceExpander().getMaxAmplitude());
//            text.append(" Max Ampl% per Voice:" + getVoiceExpander().calcMaxAmplitude());
            if(song.isPlaying()) {
                text.append(", Position " + song.getPlayingPositionNr());
                text.append(" Pattern " + patternNr);
            }
        }
        text.append(", Active Voices:" + getVoiceExpander().getNumbOfVoicesPlaying());
        text.append(", ").append(getVoiceExpander().getPerformance());
        text.append(patternController.getPatternBlock().toString());
        text.append(getKeyOnTime());
        patternController.visualizeKeyOn(notesPlaying, getKeyOnBeatLength());
        statusText.setText(text.toString());
    }

    private void visualizeStereoMix(final GraphicsContext gc) {
        int width = (int) visualsPane.getWidth();
        int height = (int) visualsPane.getHeight();
        int h = height / 8;
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, h * 2);
        final List<Integer> samples = voiceExpander.getStereoMixVisual().getSamples();
        final int samplesPerChannel = samples.size() / 2;
        gc.setStroke(colors.getVisualizeStereoMixLeftColor());
        for (int x = 0; x < width; x++) {
            int index = x * samplesPerChannel / width;
            int left = Math.abs(samples.get(index * 2) * (h / 2) / 32000);
            //maxVelocity = Math.max(maxVelocity, left);
            gc.strokeLine(x, (h / 2) - left, x, (h / 2) + left);
        }
        gc.setStroke(colors.getVisualizeStereoMixRightColor());
        for (int x = 0; x < width; x++) {
            int index = x * samplesPerChannel / width;
            int right = Math.abs(samples.get((index * 2) + 1) * (h / 2) / 32000);
            //maxVelocity = Math.max(maxVelocity, right);
            gc.strokeLine(x, (3 * h / 2) - right, x, (3 * h / 2) + right);
        }
    }

    private void visualizeVoices(final GraphicsContext gc, final VoiceExpander voiceExpander) {
        visualizeChannelSamples(gc, voiceExpander);
        visualizeChannelInstruments(gc, voiceExpander);
    }

    private void visualizeChannelSamples(final GraphicsContext gc, final VoiceExpander voiceExpander) {
        if (voiceExpander == null || voiceExpander.getRenderedBufferPerVoice() == null) return;
        int width = (int) visualsPane.getWidth();
        int height = (int) visualsPane.getHeight();
        int h = height / 8;
        final VisualisationVoice voices = voiceExpander.getRenderedBufferPerVoice();
        double visualWidth = voices.getNumberOfSamplesPerChannel();
        double drawOffsetX = width - visualWidth;
        gc.setFill(colors.getWaveformBackgroundColor());
        gc.fillRect(drawOffsetX, height * 0.25, visualWidth, height * 0.75);
        gc.setStroke(colors.getWaveformColor());
        if (!voices.getSamples().isEmpty()) {
            double voiceH = (height * 0.75) / visibleVoices;
            double y = (height * 0.25) + (voiceH / 2);
            for (int voiceIndex = 0; voiceIndex < visibleVoices; voiceIndex++) {
                for (int sampleIndex = 0; sampleIndex < voices.getNumberOfSamplesPerChannel(); sampleIndex++) {
                    int sample = Math.abs(voices.getSample(voiceIndex + patternMouseScroll, sampleIndex) * 3 * (h / visibleVoices) / 32000);
                    double x = drawOffsetX + (sampleIndex * visualWidth) / voices.getNumberOfSamplesPerChannel();
                    gc.strokeLine(x, y - sample, x, y + sample);
                }
                y += voiceH;
            }
            scrollPaneContent(gc, width, height, (int) visualWidth);
        }
    }

    private void visualizeChannelInstruments(final GraphicsContext gc, final VoiceExpander voiceExpander) {
        double voiceH = (visualsPane.getHeight() * 0.75) / visibleVoices;
        int fontH = (int) (voiceH * 0.75);
        if (fontH > 16) fontH = 16;
        if (fontH < 7) fontH = 7;
        final String font_name = Font.getDefault().getName();
        final Font font = Font.font(font_name, FontWeight.BOLD, FontPosture.REGULAR, fontH);
        gc.setFont(font);
        double y = (visualsPane.getHeight() * 0.25);
        for (int voiceIndex = 0; voiceIndex < visibleVoices; voiceIndex++) {
            final StringBuilder text = new StringBuilder();
            text.append(patternMouseScroll + voiceIndex);
            final Voice voice = voiceExpander.getVoiceNr(patternMouseScroll + voiceIndex);
            boolean active = false;
            if (voice != null && voice.getInstrument() != null) {
                text.append(" Instr:").append(voice.getInstrument().getID())
                        .append(" ").append(voice.getInstrument().getShortDescription());
                active = true;
            }
            text.append(" Vol:").append((int) voice.getVolume());


            //if(voice.getFilterFX() != null && voice.getFilterFX().isActive()) text.append(", " + voice.getFilterFX().getShortDescription());

            drawVoiceInstrument(gc, 0, (int) y, active, voice.getInstrument(), text.toString());
//            gc.setFill(colors.getVolumeBackgroundColor());
//            gc.fillRect(visualsPane.getWidth()-60, (int) y, 2, voiceH * voice.getVolume() / 200.0);

            y += voiceH;
        }
    }

    private void drawVoiceInstrument(final GraphicsContext gc, final int x, final int y, final boolean active, final SymphonieInstrument instrument, final String text) {
        float width = getTextWidth(gc, text);
        float height = getTextHeight(gc, text);
        final Text internal = new Text();
        internal.setFont(gc.getFont());
        internal.setText(text);
        final Bounds bounds = internal.getLayoutBounds();
        gc.setFill(blackTransparentColor);
        gc.fillRect(x, y, width + 8, height);
        if (active) {
            if (instrument.isVirtualMix()) {
                gc.setFill(new Color(0.40, 0.40, 1.0, 1.0));
            } else if (instrument.isDspEnabled()) {
                gc.setFill(new Color(0.70, 0.60, 0.95, 1.0));
            } else {
                gc.setFill(new Color(1.0, 0.90, 0.75, 1.0));
            }

        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillText(text, x + 2, y - bounds.getMinY());

    }

    private void scrollPaneContent(final GraphicsContext gc, final int width, final int height, final int visualWidth) {
        int h2 = (int) (height * 0.75);
        int y = (int) (height * (1.0 - 0.75));
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.BLACK);
        params.setViewport(new Rectangle2D(visualWidth, y, width - visualWidth, h2));
        final WritableImage tempImage = renderVisualizeCanvas.snapshot(params, null);
        gc.drawImage(tempImage, 0, y);
    }

    private int mixfrequency = 44100;
    private int oversample = 1;
    private int bufferLenInSamples = 4096;

    public int getBufferLenInSamples() {return bufferLenInSamples;}
    public int getMixfrequency() {return mixfrequency;}

    private void initAudioWithNewFrequency() {
        logger.debug("initAudioWithNewFrequency() mixfrequency=" + mixfrequency + " Oversample=" + oversample);
        getVoiceExpander().setMixFrequency(mixfrequency, oversample);
        bufferLenInSamples = bufferLenComboBox.getValue();
        backgroundMusicTask.setAudioDevice(audioHAL.getMixerInfo(audioDeviceChoiceBox.getSelectionModel().getSelectedIndex()),
                mixfrequency,
                bufferLenInSamples);
        updateDspNumberOfFrames();
    }

    void startAudioDevice() {
        startJavaDevice();
        //startAsioAudioDevice(false);
    }

    private BackgroundMusicTask backgroundMusicTask;
    private ExecutorService executorService;
    private void startJavaDevice() {
        if (backgroundMusicTask == null) {
            audioHAL.getAudioOutputDevices();
            backgroundMusicTask = new BackgroundMusicTask(this, voiceExpander, audioHAL);
            bufferLenInSamples = backgroundMusicTask.getSampleBufferSize();
            backgroundMusicTask.messageProperty().addListener((observable, oldValue, newValue) -> {
                notifyUpdateUI();
            });
            backgroundMusicTask.titleProperty().addListener((observable, oldValue, newValue) -> {
            });
            if(executorService == null) executorService = Executors.newFixedThreadPool(1);
            executorService.execute(backgroundMusicTask);
        }
    }


    private void startAudioSystem() {
        initVoiceExpander();
        startAudioDevice();
        shutdown = false;
    }

    void updateVisualsToPosition(int position, boolean forceUpdate) {
        if (forceUpdate || positionList.getSelectionModel().getSelectedIndex() != position) {
            positionList.getSelectionModel().select(position);
            positionList.scrollTo(position);
            moveToPosition(position, false);
        }
    }

    private void moveToPosition(int positionIndex, boolean updatePattern) {
        patternController.updatePatternViewToPlayingPattern();
        positionController.setPosition(positionIndex, song.getPosition(positionIndex));
        if(updatePattern) moveToPattern(song.getPosition(positionIndex).getPatternNumber());
    }

    void resetVisualsForLoading() {
        print("resetVisualsForLoading");
        patternController.resetVisualsForLoading();
    }

    @FXML
    TabPane patternTabPane;

    @FXML
    void patternViewSelected() {
        if (1 == patternTabPane.getSelectionModel().getSelectedIndex()) patternVisible = true;
    }

    @FXML
    void scopeViewSelected() {
        if (0 == patternTabPane.getSelectionModel().getSelectedIndex()) patternVisible = false;
    }

    boolean patternVisible = false;

    @FXML
    Spinner<Integer> patternNrSpinner;
    @FXML
    Spinner<Integer> patternStep;

    @FXML
    Spinner<Double> songBpmSpinner;

    @FXML
    Label statusText;

    @FXML
    Button loadButton;

    @FXML
    CheckMenuItem showRoomDesignerMenuItem;

    private void saveStagePosition(final Stage stage) {
        print("saveStagePosition");
        mainProperties.saveUIState(stage);
        mainProperties.saveUIState(patternTabPane, "patternTabPane");
        mainProperties.setProperty("instrumentWaveformSplitter", instrumentWaveformSplitter.getDividerPositions()[0]);
        mainProperties.setProperty("sequencePositionsSplitter", sequencePositionsSplitter.getDividerPositions()[0]);
        mainProperties.setProperty("songDetailSplitter", songDetailSplitter.getDividerPositions()[0]);
        mainProperties.setProperty("instrumentPatternSplitter", instrumentPatternSplitter.getDividerPositions()[0]);
        mainProperties.setProperty("songInstrPatternSplitter", songInstrPatternSplitter.getDividerPositions()[0]);
        mainProperties.saveUIState(volumeSlider, "volumeSlider");
        mainProperties.saveUIState(sampleDiffSlider, "sampleDiffSlider");
        mainProperties.saveUIState(pitchDiffSlider, "pitchDiffSlider");
        mainProperties.saveUIState(sampleRateChoice, "sampleRateChoice");
        mainProperties.saveUIState(eqActivatedCheckBox, "eqActivatedCheckBox");
        mainProperties.saveUIState(eqLowIntensitySlider, "eqLowIntensitySlider");
        mainProperties.saveUIState(eqMidIntensitySlider, "eqMidIntensitySlider");
        mainProperties.saveUIState(eqHighIntensitySlider, "eqHighIntensitySlider");
        mainProperties.saveUIState(eqLPFrequencySlider, "eqLPFrequencySlider");
        mainProperties.saveUIState(eqHPFrequencySlider, "eqHPFrequencySlider");
        mainProperties.saveUIState(darkModeMenuItem, "darkModeMenuItem");
        mainProperties.saveUIState(showRoomDesignerMenuItem, ROOM_DESIGNER_VISIBLE_KEY);
        mainProperties.saveUIState(updatePatternMenuItem, "updatePatternMenuItem");
        mainProperties.setProperty(EVENT_DESIGNER_VISIBLE_KEY, eventController != null);
        saveHistory(lastFilesHistory, FILE_HISTORY);
        saveHistory(lastSamplesHistory, SAMPLE_HISTORY);
        mainProperties.save();
    }

    private void loadStagePosition(final Stage stage) {
        volumeSlider.setValue(100);
        bpmSlider.setValue(100);
        tuneSlider.setValue(0);
        print("loadStagePosition");
        if (!mainProperties.load()) return;

        String lastSongPath = mainProperties.getProperty(LASTSONG_KEY);
        if (!lastSongPath.isEmpty()) reloadSongMenuItem.setText("Reload " + lastSongPath);

        mainProperties.initFromSavedProperty(stage);
        mainProperties.initFromSavedProperty(patternTabPane, "patternTabPane");
        double splitter = mainProperties.getDoubleProperty("instrumentWaveformSplitter");
        if (splitter > 0.0) instrumentWaveformSplitter.setDividerPosition(0, splitter);
        splitter = mainProperties.getDoubleProperty("sequencePositionsSplitter");
        if (splitter > 0.0) sequencePositionsSplitter.setDividerPosition(0, splitter);
        splitter = mainProperties.getDoubleProperty("songDetailSplitter");
        if (splitter > 0.0) songDetailSplitter.setDividerPosition(0, splitter);
        splitter = mainProperties.getDoubleProperty("instrumentPatternSplitter");
        if (splitter > 0.0) instrumentPatternSplitter.setDividerPosition(0, splitter);
        splitter = mainProperties.getDoubleProperty("songInstrPatternSplitter");
        if (splitter > 0.0) songInstrPatternSplitter.setDividerPosition(0, splitter);

        mainProperties.initFromSavedProperty(volumeSlider, "volumeSlider");
        updateVolumeSliderBackground();
        if (mainProperties.initFromSavedProperty(sampleDiffSlider, "sampleDiffSlider")) {
            getVoiceExpander().setSampleDiff(sampleDiffSlider.getValue());
        }
        mainProperties.initFromSavedProperty(pitchDiffSlider, "pitchDiffSlider");
        if (mainProperties.initFromSavedProperty(pitchDiffSlider, "pitchDiffSlider")) {
            getVoiceExpander().setPitchDiff(pitchDiffSlider.getValue());
        }
        mainProperties.initFromSavedProperty(eqActivatedCheckBox, "eqActivatedCheckBox");
        if (mainProperties.initFromSavedProperty(darkModeMenuItem, "darkModeMenuItem")) {
            toogleDarkMode();
        }
        if (mainProperties.initFromSavedProperty(showRoomDesignerMenuItem, ROOM_DESIGNER_VISIBLE_KEY)) {
            if (showRoomDesignerMenuItem.isSelected()) showRoomDesigner();
        }
        mainProperties.initFromSavedProperty(updatePatternMenuItem, "updatePatternMenuItem");
        loadHistory();
        loadSampleHistory();
        print("Settings loaded.");
        updateTitle();
    }

    void updateTitle() {
        DspStereoEffect dsp = getVoiceExpander().getDsp();
        StringBuilder text = new StringBuilder(SYMPHONIE_VERSION);
        text.append(" ");
        text.append(System.getProperty("sun.arch.data.model"));
        text.append("Bit");
        text.append(" ").append(song.getName());
        if (dsp.isActive()) text.append(" ").append(dspChoice.getValue());
        if (eqActivatedCheckBox.isSelected()) text.append(" EQ");
        stage.setTitle(text.toString());
    }

    @FXML
    CheckMenuItem darkModeMenuItem;

    @FXML
    void toogleDarkMode() {
        if (darkModeMenuItem.isSelected()) {
            colors = new DarkColorSet();
        } else {
            colors = new LightColorSet();
        }
        patternTabPane.setBackground(colors.background());
        patternTabPane.getChildrenUnmodifiable().get(0);
        instrumentController.visualizeWaveform();
    }



}
