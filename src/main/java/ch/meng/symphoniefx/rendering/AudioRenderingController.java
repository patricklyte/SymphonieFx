package ch.meng.symphoniefx.rendering;

import ch.meng.symphoniefx.*;
import ch.meng.symphoniefx.mixer.VoiceExpander;
import ch.meng.symphoniefx.song.Song;
import javafx.animation.RotateTransition;
import javafx.application.Application;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ch.meng.symphoniefx.SharedStatic.*;


public class AudioRenderingController {

    public static final String CALCULATION_OPTIMAL_VOLUME = "Calculating optimal Render Volume";

    public AudioRenderingController() {
    }

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;

    private Stage stage;
    private Group group;
    private Scene scene;

    private boolean isRendering = false;
    Application application;
    @FXML
    AnchorPane rootPane;
    @FXML
    Label titleLabel;
    @FXML
    TextField fileNameTextField;
    @FXML
    TextField frequencyTextField;

    @FXML
    ChoiceBox<String> fileFormatChoiceBox;
    @FXML
    ChoiceBox<String> filenamePattern;
    @FXML
    ChoiceBox<String> frequency;
    @FXML
    ChoiceBox<String> renderMode;

    @FXML
    Button startButton;
    @FXML
    ProgressBar progressBar;
    @FXML
    Label statusLabel;
    @FXML
    CheckBox sameAsRealtimeAudioSetup;
    @FXML
    CheckBox autoOptimizeCheckBox;
    @FXML
    Slider renderVolumeSlider;

    private Song song;
    private VoiceExpander oldVoiceexpander;
    Parent parentPane;
    private double renderVolume = 100;

    String buildRenderName(final Song song, final String sampleName, final RenderFileMode renderFileMode, final boolean onlySampleName) {
        String text = song.getName();
        int pos = text.toLowerCase().lastIndexOf(".symmod");
        if (pos > 0) {
            text = text.substring(0, pos);
        }
        if (renderFileMode.equals(RenderFileMode.AllNonMuted) || renderFileMode.equals(RenderFileMode.FilePerInstrument)) {
            if (onlySampleName) {
                pos = text.lastIndexOf(SharedStatic.directoryDelimiter());
                if (pos > 1) {
                    text = text.substring(0, pos + 1);
                }
            } else {
                text += " ";
            }
            String samplename = sampleName;
            samplename = removeDirectoryFromFileName(samplename);
            text += samplename;
            if (!text.toLowerCase().endsWith(".wav")) {
                text += ".wav";
            }
        } else {
            text += ".wav";
        }
        return text;
    }

    String text;

    public void setStage(Parent parentPane, Scene scene, Group group, Application application, Stage stage, Song song) {
        oldVoiceexpander = song.getLinkedVoiceExpander();
        this.group = group;
        this.scene = scene;
        titleLabel.setBackground(playingSongBackground);
        this.song = song;
        this.stage = stage;
        this.parentPane = parentPane;
        this.application = application;
        addEventHandlers(stage, scene);
        stage.setOnShown(event -> {
            song.backupInstrumentMuteState();
            initUI();
            loadStagePosition(stage);
            renderVolumeSlider.valueProperty().addListener((ov, old_val, value) -> {
                renderVolume = value.doubleValue();
            });
//            ValueSliderSkin skin = new ValueSliderSkin(renderVolumeSlider);
//            renderVolumeSlider.setSkin(skin);

            if (autoOptimizeCheckBox.isSelected()) {
                prerenderForMaximum(song);
            }
            autoOptimizeCheckBox.selectedProperty().addListener((ov, old_val, value) -> {
                if (value.booleanValue()) prerenderForMaximum(song);
            });
        });
    }

    double maxAmplitudeOfSong = 32767;
    private void prerenderForMaximum(final Song song) {
        if (!song.hasContentWithActiveInstruments()) return;
        isRendering = true;
        if (renderTask == null) {
            try {
                renderFileMode = RenderFileMode.Null;
                setPrerenderinProgressState();
                renderTask = new BackgroundRenderTask(this,
                        song,
                        44100,
                        maxAmplitudeOfSong,
                        "",
                        renderFileMode,
                        sameAsRealtimeAudioSetup.isSelected(),
                        renderVolume,
                        filenamePattern.getSelectionModel().isSelected(1), RenderFileFormat.valueOf(fileFormatChoiceBox.getSelectionModel().getSelectedItem())) {
                };
            } catch (Exception exception) {
                notifyUpdateUI();
            }
            //setAllDoneVisual();
            renderTask.messageProperty().addListener((observable, oldValue, newValue) -> {
                notifyUpdateUI();
            });
            renderTask.titleProperty().addListener((observable, oldValue, newValue) -> {
            });
        }
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(1);
        }
        if (renderTask != null && executorService != null) {
            executorService.execute(renderTask);
        }
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

    public void quit() {
        song.stopSong();
        saveStagePosition(stage);
    }

    void initUI() {
        initSystemUI();
        initGlobalKeyboardShortcuts();
        updateRenderFilename();
    }

    private void updateRenderFilename() {
        text = buildRenderName(song, song.getFirstNonMutedInstrumentForRendering(), renderFileMode, 0 != filenamePattern.getSelectionModel().getSelectedIndex());
        fileNameTextField.setText(text);
    }

    private void updateStatus() {
        StringBuilder text = new StringBuilder();
        long total = Runtime.getRuntime().totalMemory();
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        text.append(" Mem:").append(used / 1024 / 1024);
        text.append("(").append(total / 1024 / 1024).append(")");
        if (song != null && song.isPlaying()) {
            int patternNr = song.getPosition(song.getPlayingPositionNr()).getPatternNumber();
            text.append(" Pos:" + song.getPlayingPositionNr());
            text.append(" Pattern:" + patternNr);
        }
        setTitleText(text.toString());
    }

    private void setTitleText(String text) {
        stage.setTitle("Audio Rendering v1.0 " + text);
    }

    private RenderFileMode renderFileMode = RenderFileMode.SingleFile;

    private void initSystemUI() {
        if (!song.hasContentWithActiveInstruments()) {
            setErrorState();
        }
        renderMode.getItems().addAll("Single File", "File per Stereochannel", "File per Instrument", "File per Group", "All non muted Instruments");
        renderMode.setValue(renderMode.getItems().get(0));
        renderMode.valueProperty().addListener((ov, value, text) -> {

            fileNameTextField.setBackground(null);
            switch (renderMode.getSelectionModel().getSelectedIndex()) {
                case 1:
                    renderFileMode = RenderFileMode.FilePerStereoChannel;
                    break;
                case 2:
                    renderFileMode = RenderFileMode.FilePerInstrument;
                    break;
                case 3:
                    renderFileMode = RenderFileMode.FilePerGroup;
                    break;
                case 4:
                    renderFileMode = RenderFileMode.AllNonMuted;
                    fileNameTextField.setBackground(warnBackground);
                    break;
                default:
                    renderFileMode = RenderFileMode.SingleFile;
                    break;
            }
            updateRenderFilename();
        });

        defaultBackground = frequencyTextField.getBackground();
        frequencyTextField.setText("44100");
        frequencyTextField.focusedProperty().addListener((ov, old_val, text) -> {
            int value = parse(frequencyTextField.getText(), 44100);
            if (value < 1000) {
                frequencyTextField.setText("1000");
                frequencyTextField.setBackground(errorBackground);
            } else {
                frequencyTextField.setBackground(defaultBackground);
            }
        });

        frequency.getItems().addAll("22.05 kHz", "44.1 kHz", "48 kHz", "88.2 kHz", "96 kHz", "192 kHz", "384 kHz", "500 kHz", "1000 kHz");
        frequency.setValue(frequency.getItems().get(1));
        frequency.valueProperty().addListener((ov, old_val, text) -> {
            double freq = 1000 * Double.parseDouble(text.replace(" kHz", ""));
            frequencyTextField.setText(Integer.toString((int) freq));
            frequencyTextField.setBackground(defaultBackground);
        });

        fileFormatChoiceBox.getItems().addAll(RenderFileFormat.Flac_24_Bit.toString(),
                RenderFileFormat.Wav_16_Bit.toString(),
                RenderFileFormat.Both.toString());
        fileFormatChoiceBox.setValue(fileFormatChoiceBox.getItems().get(0));
        fileFormatChoiceBox.valueProperty().addListener((ov, old_val, text) -> {
        });

        filenamePattern.getItems().addAll("Songname Samplename", "Only Samplename");
        filenamePattern.setValue(filenamePattern.getItems().get(0));
        filenamePattern.valueProperty().addListener((ov, old_val, text) -> {
            updateRenderFilename();
        });
    }

    private void setErrorState() {
        setTitleText("Nothing to render");
        titleLabel.setBackground(errorBackground);
        startButton.setBackground(errorBackground);
        startButton.setDisable(true);
    }

    Rectangle statusShape;
    private RotateTransition rotateTransition;
    private void setRenderingInProgressState() {
        startButton.setText("Cancel");
        statusLabel.setBackground(playingSongBackground);
        statusShape = new Rectangle(0, 0, 6, titleLabel.getHeight());
        statusShape.setFill(Color.color(1.0, 0.0, 0.0, 1.0));
        group.getChildren().add(statusShape);
//        rotateTransition = new RotateTransition();
//        rotateTransition.setDuration(new Duration(12_000));
//        rotateTransition.setNode(statusShape);
//        rotateTransition.setFromAngle(0);
//        rotateTransition.setToAngle(360);
//        rotateTransition.setCycleCount(999);
//        rotateTransition.play();
    }

    private void setPrerenderinProgressState() {
        titleLabel.setBackground(prerenderBackground);
        titleLabel.setText(CALCULATION_OPTIMAL_VOLUME);
        statusLabel.setBackground(playingSongRepeatedBackground);
        startButton.setDisable(true);
    }

    void setReadyState() {
        statusLabel.setBackground(null);
        progressBar.setProgress(0.0);
        if(statusShape != null) {
            group.getChildren().remove(statusShape);
            statusShape = null;
        }
        titleLabel.setText("Audio Rendering");
        titleLabel.setBackground(playingSongBackground);
        startButton.setDisable(false);
    }

    private void updateRenderFileModeFromUI() {
        switch (renderMode.getSelectionModel().getSelectedIndex()) {
            case 1: renderFileMode = RenderFileMode.FilePerStereoChannel;break;
            case 2: renderFileMode = RenderFileMode.FilePerInstrument;break;
            case 3: renderFileMode = RenderFileMode.FilePerGroup;break;
            case 4: renderFileMode = RenderFileMode.AllNonMuted;break;
            default: renderFileMode = RenderFileMode.SingleFile;
                break;
        }
    }

    Background defaultBackground;
    private void initGlobalKeyboardShortcuts() {}

    private int parse(String text, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt(frequencyTextField.getText());
        } catch (Exception ignore) {}
        return value;
    }



    @FXML
    void start() {
        if (isRendering) {
            cancelRendering();
        } else {
            if (!isReadyForRendering())
                return;
            updateRenderFileModeFromUI();
            setRenderingInProgressState();
            startBackgroundRendering();
        }
    }

    private boolean isReadyForRendering() {
        return !(song == null || isRendering || !song.isContentLoaded() || !song.hasContentWithActiveInstruments());
    }

    void cancelRendering() {
        group.getChildren().remove(statusShape);
        isRendering = false;
        statusLabel.setBackground(null);
        if (startButton != null) startButton.setText("Start");
        if (startButton != null) progressBar.setProgress(100.0);
        if (renderTask != null) {
            renderTask.shutdown();
            renderTask.cancel(true);
        }
        if (executorService != null) executorService.shutdown();
        if(renderFileMode.equals(RenderFileMode.Null)) {
            renderVolumeSlider.setValue(100 * 32767.0 / renderTask.getPrecalcedMaximum());
        }
        renderTask = null;
        executorService = null;
        progressBar.setProgress(0.0);
    }

    BackgroundRenderTask renderTask;
    private ExecutorService executorService;


    void startBackgroundRendering() {
        isRendering = true;
        if (renderTask == null) {
            try {
                renderTask = new BackgroundRenderTask(this,
                        song,
                        parse(frequencyTextField.getText(), 44100),
                        maxAmplitudeOfSong,
                        fileNameTextField.getText(),
                        renderFileMode,
                        sameAsRealtimeAudioSetup.isSelected(),
                        renderVolume,
                        filenamePattern.getSelectionModel().isSelected(1), RenderFileFormat.valueOf(fileFormatChoiceBox.getSelectionModel().getSelectedItem())) {
                };
            } catch (Exception exception) {
                notifyUpdateUI();
            }
            renderTask.messageProperty().addListener((observable, oldValue, newValue) -> {
                notifyUpdateUI();
            });
            renderTask.titleProperty().addListener((observable, oldValue, newValue) -> {
            });
        }
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(1);
        }
        if (renderTask != null && executorService != null) {
            executorService.execute(renderTask);
        }
    }

    void notifyDone() {
        startButton.setText("Start");
        cancelRendering();
        if(!renderFileMode.equals(RenderFileMode.Null)) {
            ProgressMessage.add(group, "Rendering Done...");
        }
        progressBar.setProgress(0.0);
        setReadyState();
    }

    void notifyUpdateUI() {
        String status = "";
        if (renderTask != null) status = renderTask.getLatesMessage();
        if (!status.isEmpty()) statusLabel.setText(status);
        if (renderTask == null || (renderTask != null && renderTask.isDone())) {
            notifyDone();
        }
        progressBar.setProgress(song.getPlayedPercentageOfAllSong());
        updateStatus();
    }

    PropertyManager audioRenderingProperties = new PropertyManager("AudioRendering", this.getClass().getSimpleName());

    private void saveStagePosition(final Stage stage) {
        audioRenderingProperties.saveUIState(frequencyTextField, "frequencyTextField");
        audioRenderingProperties.saveUIState(frequency, "frequency");
        audioRenderingProperties.saveUIState(renderMode, "renderMode");
        audioRenderingProperties.saveUIState(autoOptimizeCheckBox, "autoOptimizeCheckBox");
        audioRenderingProperties.saveUIState(fileFormatChoiceBox, "fileFormatChoiceBox");
        audioRenderingProperties.saveUIState(filenamePattern, "filenamePattern");
        audioRenderingProperties.saveUIState(stage);
        audioRenderingProperties.save();
    }

    private void loadStagePosition(final Stage stage) {
        if (!audioRenderingProperties.load()) return;
        audioRenderingProperties.initFromSavedProperty(stage);
        audioRenderingProperties.initFromSavedProperty(frequency, "frequency");
        audioRenderingProperties.initFromSavedProperty(frequencyTextField, "frequencyTextField");
        audioRenderingProperties.initFromSavedProperty(renderMode, "renderMode");
        audioRenderingProperties.initFromSavedProperty(autoOptimizeCheckBox, "autoOptimizeCheckBox");
        audioRenderingProperties.initFromSavedProperty(fileFormatChoiceBox, "fileFormatChoiceBox");
        audioRenderingProperties.initFromSavedProperty(filenamePattern, "filenamePattern");
        if (frequencyTextField.getText().isEmpty()) frequencyTextField.setText("44100");
    }
}
