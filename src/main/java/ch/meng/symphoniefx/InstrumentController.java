package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.Song;
import ch.meng.symphoniefx.song.SymphonieInstrument;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.apache.log4j.Logger;
import symreader.LoopTypeEnum;
import symreader.SampleChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static ch.meng.symphoniefx.MainController.colors;
import static ch.meng.symphoniefx.SharedStatic.getTextWidth;

public class InstrumentController {
    public static final String UI_LOOP_START = "Loop Start";
    public static final String UI_LOOP_LENGTH = "Loop Length";
    public static final String UI_PINGPONG = "Pingpong";
    public static final String UI_PHASE_MIRRORED = "Phase mirrored";
    public static final String UI_REVERSED = "Reversed";
    public static final String UI_MUTE = "Mute";
    public static final String UI_SUB_LEFT = "_LEFT";
    public static final String UI_SUB_RIGHT = "_RIGHT";

    public static final String UI_VST_PROGRAM = "Program";
    public static final String UI_VST_EDITOR = "Editor";
    public static final String UI_RESET_VST = "Reset VST";
    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    public static final String UI_SAVE_PROGRAM = "saveProgram";
    public static final String UI_LOAD_PROGRAM = "loadProgram";

    @FXML
    Slider instrumentVolume;
    @FXML
    CheckBox instrumentDspEnabled;
    @FXML
    Spinner<Integer> pitchTune;
    @FXML
    Spinner<Integer> pitchFineTune;
    @FXML
    ComboBox<String> instrumentGroup;
    @FXML
    ListView<String> instrumentDetailsList;
    @FXML
    Rectangle groupeRect;
    @FXML
    Spinner<Integer> instrumentSpinner;

    @FXML
    AnchorPane waveformPane;
    @FXML
    ListView<Node> propertyList;

    private ListView<Label> instrumentList;
    private Song song;
    private MainController mainController;
    private SymphonieInstrument actualInstrument = new SymphonieInstrument();

    InstrumentController(MainController mainController, ListView<Label> instrumentList, Song song) {
        this.instrumentList = instrumentList;
        setSong(song);
        this.mainController = mainController;
        initInstrumentUI();
        addInstrumentEventListeners();
    }

    public void setSong(Song song) {
        this.song = song;
        actualInstrument = song.getInstrument(0);
    }

    void clearSample() {
        int index = getActualInstrument().getIndex();
        song.removeInstrumentIndex(index);
        rebuildInstrumentList();
        moveToInstrument(index);
        visualizeWaveform();
    }

    private void initInstrumentUI() {
        buildGroupColors();
        AnchorPane parentPane;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/InstrumentUI.fxml"));
            fxmlLoader.setController(this);
            parentPane = fxmlLoader.load();
            mainController.instrumentWaveformSplitter.getItems().set(1, parentPane);
            mainController.instrumentWaveformSplitter.getDividers().get(0).positionProperty().addListener((ov, old_val, value) -> {
                visualizeWaveform();
            });
        } catch (IOException exception) {
            logger.error(exception);
            exception.printStackTrace();
            return;
        }
        instrumentVolume.valueProperty().addListener((ov, old_val, value) -> {
            actualInstrument.setVolume(value.intValue());
        });
        instrumentDspEnabled.selectedProperty().addListener((ov, old_val, value) -> {
            actualInstrument.setDspEnabled(value);
        });
        AddDefaultGroups();
        instrumentGroup.valueProperty().addListener((ov, t, group) -> {
            if (!instrumentGroup.getItems().contains(group)) instrumentGroup.getItems().add(group);
            actualInstrument.setGroup(group);
            groupeRect.setFill(getGroupColor(group));
            rebuildActualInstrumentInList();
        });

        addSampleUI();
        initCanvasEvents();
    }

    void rebuildInstrumentList() {
        instrumentList.getItems().clear();
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            addInstrumentGroup(instrument.getGroup());
            instrumentList.getItems().add(buildInstrumentLabel(instrument));
        }
    }

    void rebuildActualInstrumentInList() {
        int index = instrumentList.getSelectionModel().getSelectedIndex();
        if (index >= 0) instrumentList.getItems().set(index, buildInstrumentLabel(song.getInstrument(index)));
    }

    Label buildInstrumentLabel(final SymphonieInstrument instrument) {
        String text = instrument.getIndex() + " " + instrument;
        if (!instrument.getGroup().isEmpty()) {
            text += " [" + instrument.getGroup() + "]";
        }
        final Label label = new Label(text);
        if (instrument.isMuted()) {
            label.setTextFill(Color.color(0.7, 0.7, 0.7, 1.0));
        } else if (!instrument.getGroup().isEmpty()) {
            label.setBackground(getGroupBackground(instrument.getGroup()));
            label.setTextFill(Color.BLACK);
        } else if (instrument.isVirtualSample()) {
            label.setTextFill(Color.color(0.0, 0.0, 0.80, 1.0));
        }
        return label;
    }

    private void AddDefaultGroups() {
        addInstrumentGroup("Lead");
        addInstrumentGroup("Lead accompany");
        addInstrumentGroup("Bass");
        addInstrumentGroup("Percussion");
        addInstrumentGroup("Percussion 2");
        addInstrumentGroup("SpecialFx");
    }

    private void addInstrumentGroup(final String text) {
        if (!text.isEmpty() && !instrumentGroup.getItems().contains(text)) {
            instrumentGroup.getItems().add(text);
        }
    }

    Background getGroupBackground(final String group) {
        if (groupBackgroundColors.isEmpty()) buildGroupColors();
        int index = instrumentGroup.getItems().indexOf(group);
        index = index % groupBackgroundColors.size();
        return groupBackgroundColors.get(index);
    }

    Paint getGroupColor(final String group) {
        if (group.isEmpty()) return Color.WHITE;
        if (groupBackgroundColors.isEmpty()) buildGroupColors();
        int index = instrumentGroup.getItems().indexOf(group);
        index = index % groupBackgroundColors.size();
        return groupPaints.get(index);
    }

    final List<Background> groupBackgroundColors = new Vector<>();
    final List<Paint> groupPaints = new Vector<>();
    final List<Color> vstColors = new Vector<>();
    void buildGroupColors() {
        for (double hue = 0; hue < 360; hue += 30) {
            vstColors.add(Color.hsb(hue, 0.5, 1.0));
            groupPaints.add(Color.hsb(hue, 0.1, 1.0));
            Background background = new Background(new BackgroundFill(Color.hsb(hue, 0.1, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
            groupBackgroundColors.add(background);
        }
    }

    void updateInstrumentUI(final SymphonieInstrument instrument) {
        if (instrument == null) return;
        updateUIToInstrumentType();
        instrumentDspEnabled.setSelected(instrument.isDspEnabled());
        instrumentVolume.setValue(instrument.getVolume());
        instrumentSpinner.getValueFactory().setValue(actualInstrument.getIndex());
        pitchTune.getEditor().setText(Integer.toString(instrument.getTune()));
        pitchFineTune.getEditor().setText(Integer.toString(instrument.getFineTune()));
        addInstrumentGroup(instrument.getGroup());
        instrumentGroup.setValue(instrument.getGroup());
        groupeRect.setFill(getGroupColor(instrument.getGroup()));
        instrumentDetailsList.getItems().clear();
        if (instrument.getBpm() > 0) addInstrumentDetail("Bpm", instrument.getBpm());
        if (instrument.getDownsampleSteps() != 0)
            addInstrumentDetail("depreciated Downsample", instrument.getDownsampleSteps());
        if (instrument.getCompressorLevel() > 0)
            addInstrumentDetail("Compressor% ", instrument.getCompressorLevel() * 100);
        addInstrumentDetail("ID", instrument.getID());
        addInstrumentDetail("Index", instrument.getIndex());
        if (instrument.getFadeFromVolume() != 1.0 && instrument.getFadeToVolume() != 1.0) {
            addInstrumentDetail("Volume Fade from%", instrument.getFadeFromVolume());
            addInstrumentDetail("Volume Fade to%", instrument.getFadeToVolume());
        }
        if (instrument.getSimpleLPFilter() != 0) {
            addInstrumentDetail("Simple LP Filter Steps", instrument.getSimpleLPFilter());
        }
        if (instrument.getResoFilterSteps() > 0) {
            addInstrumentDetail("Reso Filter Steps", instrument.getResoFilterSteps());
            addInstrumentDetail("Reso Frequency (0-255)", instrument.getResoFilterSweepStartFrequency());
            addInstrumentDetail("Reso Reso (0-255)", instrument.getResoFilterSweepStartResonance());
            if (instrument.getResoFilterSteps() == 2) {
                addInstrumentDetail("Reso End Frequency (0-255)", instrument.getResoFilterSweepEndFrequency());
                addInstrumentDetail("Reso End Reso (0-255)", instrument.getResoFilterSweepEndResonance());
            }
        }
        if (instrument.getSampleImporter().getSampledFrequency() != 0) {
            addInstrumentDetail("Sampled at ", instrument.getSampleImporter().getSampledFrequency());
        }
        updateInstrumentSpecificUI(instrument);
    }

    void updateInstrumentSpecificUI(final SymphonieInstrument instrument) {
        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Sample)) {
            addInstrumentDetail("Possible Looppoints", instrument.getNullstellen().size());
            valuesSpinner.get(UI_LOOP_START).getValueFactory().setValue(instrument.getLoopStart());
            valuesSpinner.get(UI_LOOP_LENGTH).getValueFactory().setValue(instrument.getLoopLength());
            valueToggles.get(UI_MUTE).setSelected(instrument.isMuted());
            valueToggles.get(UI_REVERSED).setSelected(instrument.isReversed());
        }
        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Vst)) {
            addInstrumentDetail("VST VstVersion", actualInstrument.getVstManager().getVst().getVstVersion().toString());
            addInstrumentDetail("VST Product",actualInstrument.getVstManager().getVst().getProductString());
            addInstrumentDetail("VST PluginVersion", actualInstrument.getVstManager().getVst().getPluginVersion());
            addInstrumentDetail("VST InitialDelay", actualInstrument.getVstManager().getVst().getInitialDelay());
            addInstrumentDetail("VST BlockSize", actualInstrument.getVstManager().getVst().getBlockSize());
            addInstrumentDetail("VST SampleRate", actualInstrument.getVstManager().getVst().getSampleRate());
            addInstrumentDetail("VST hasEditor", actualInstrument.getVstManager().getVst().hasEditor());
            addInstrumentDetail("VST accepts Programs as Chunks", actualInstrument.getVstManager().getVst().acceptsProgramsAsChunks());
            addInstrumentDetail("VST isSynth", actualInstrument.getVstManager().getVst().isSynth());
        }
    }

    void notifyValueChanged(Node node) {
        switch (node.getId()) {
            case UI_LOOP_START:
                actualInstrument.adjustLoopStart(((Spinner<Integer>) node).getValue());
                visualizeWaveform();
                break;
            case UI_LOOP_START + UI_SUB_RIGHT: {
                actualInstrument.loopStartMoveToNextSnap(1);
                syncAndRedrawChangedLoop();
            }
            break;
            case UI_LOOP_START + UI_SUB_LEFT: {
                actualInstrument.loopStartMoveToNextSnap(-1);
                syncAndRedrawChangedLoop();
            }
            break;
            case UI_LOOP_LENGTH:
                actualInstrument.adjustLoopLength(((Spinner<Integer>) node).getValue());
                visualizeWaveform();
                break;
            case UI_LOOP_LENGTH + UI_SUB_RIGHT: {
                actualInstrument.loopLengthMoveToNextSnap(1);
                syncAndRedrawChangedLoop();
            }
            break;
            case UI_LOOP_LENGTH + UI_SUB_LEFT: {
                actualInstrument.loopLengthMoveToNextSnap(-1);
                syncAndRedrawChangedLoop();
            }
            break;
            case UI_PINGPONG:
                actualInstrument.setPingpongLoop(((Toggle) node).isSelected());
                break;
            case UI_PHASE_MIRRORED:
                actualInstrument.setPhaseMirrored(((Toggle) node).isSelected());
                break;
            case UI_REVERSED:
                actualInstrument.setReversed(((Toggle) node).isSelected());
                break;
            case UI_VST_PROGRAM:
                actualVstProgram = ((Spinner<Integer>) node).getValue();
                actualInstrument.getVstManager().getVst().setProgram(actualVstProgram);
                vstProgrammName.setText(actualInstrument.getVstManager().getVst().getProgramName());
                updateVstParameterList();
                break;
            case UI_VST_EDITOR:
                toggleVstEditor(node);
                break;
            case UI_RESET_VST:
                actualInstrument.getVstManager().getRenderer().clearMax();
                setVstInfo("Vst reseted");
                break;
            case UI_SAVE_PROGRAM:
                actualInstrument.getVstManager().getProgram();
                break;
            case UI_LOAD_PROGRAM:
                actualInstrument.getVstManager().setProgram();
                break;
            default:
                node.getId();
                int index = vstParameters.getItems().indexOf(node.getParent());
                if(node instanceof Slider && index>=0) {
                    Slider slider = (Slider) node;
                    //Slider slider = (Slider) container.getChildren().get(1);
                    actualInstrument.getVstManager().getVst().setParameter(index, (float) slider.getValue()/127.0f );
                }
                break;
        }
    }

    private void toggleVstEditor(Node node) {
        if (!actualInstrument.getVstManager().getVst().hasEditor()) return;
        if (actualInstrument.getVstManager().getVst().isEditorOpen()) {
            logger.debug("Vst().closeEditor");
            actualInstrument.getVstManager().getVst().closeEditor();
            logger.debug("Vst().closeEditor done");
            if (node != null) ((Toggle) node).setSelected(false);
        } else {
            if (node != null) ((Toggle) node).setSelected(true);
            logger.debug("Vst().openEditor");
            actualInstrument.getVstManager().getVst().openEditor("VST Editor");
            logger.debug("Vst().openEditor done");
        }
    }

    private InstrumentSource displayUIForSource = InstrumentSource.Sample;

    private void updateUIToInstrumentType() {
        removeInstrumentSpecificUI();
        if(actualInstrument==null) return;
        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Sample)) {
            addSampleUI();
            addWaveformEvents();
        }
        else if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Vst)) {
            addVstUI();
        }
        displayUIForSource = actualInstrument.getInstrumentSource();
    }

    private void removeInstrumentSpecificUI() {
        propertyList.getItems().clear();
        if (vstParameters != null) vstParameters.getItems().clear();
        waveformPane.getChildren().remove(vstParameters);
        valuesSpinner.clear();
        valueToggles.clear();
        waveformPane.getChildren().remove(uiContainer);
        uiContainer = null;
        removeWaveformEvents();
    }

    private Label vstProgrammName;
    int maxVstProgram = 0;
    int actualVstProgram = 0;
    Label vstInfo;
    Paint defaultLabelFill;

    public void setVstInfo(final String text) {
        if(vstInfo == null) return;
        vstInfo.setText(text);
        if(defaultLabelFill == null) defaultLabelFill = vstInfo.getTextFill();
        if(text.startsWith("ERROR:")) {
            vstInfo.setTextFill(Color.RED);
        } else {
            vstInfo.setTextFill(defaultLabelFill);
        }
    }

    private void addVstUI() {
        uiContainer = new HBox();
        uiContainer.getChildren().add(createSpinner("BANK", 0, 127, false));
        if(actualInstrument.getVstManager().getVst().numPrograms() <= 1) {
            vstProgrammName = new Label("Only one program");
            maxVstProgram = 0;
        } else {
            uiContainer.getChildren().add(createSpinner(UI_VST_PROGRAM, 0, actualInstrument.getVstManager().getVst().numPrograms()-1, false));
            vstProgrammName = new Label(actualInstrument.getVstManager().getVst().getProgramName());
            maxVstProgram = actualInstrument.getVstManager().getVst().numPrograms();
        }
        actualVstProgram = 0;
        uiContainer.getChildren().add(vstProgrammName);
        if(actualInstrument.getVstManager().getVst().hasEditor()) {
            uiContainer.getChildren().add(createToggle(UI_VST_EDITOR));
        }

//        Button resetVst = new Button(UI_RESET_VST);
//        resetVst.setId(UI_RESET_VST);
//        uiContainer.getChildren().add(resetVst);
//        resetVst.setOnAction(event -> {
//            notifyValueChanged(resetVst);
//        });
        createButton(UI_RESET_VST);

        vstInfo = new Label("Vst ok");
        uiContainer.getChildren().add(vstInfo);

        createButton(UI_SAVE_PROGRAM);
        createButton(UI_LOAD_PROGRAM);

        uiContainer.setTranslateY(40);
        waveformPane.getChildren().add(uiContainer);
        if (vstParameters == null) vstParameters = new ListView<>();
        vstParameters.setTranslateY(70);
        vstParameters.setPrefWidth(waveformPane.getWidth());
        vstParameters.setPrefHeight(waveformPane.getHeight() - 70);
        waveformPane.getChildren().add(vstParameters);
        updateVstParameterList();
    }



    private ListView<Node> vstParameters;
    private void updateVstParameterList() {
        int colorIndex = 0;
        Color color = vstColors.get(colorIndex);
        vstParameters.getItems().clear();
        String group = "";
        for (int index = 0; index < actualInstrument.getVstManager().getVst().numParameters(); index++) {
            String parameterName = (actualInstrument.getVstManager().getVst().getParameterName(index) + actualInstrument.getVstManager().getVst().getParameterLabel(index)).trim();
            int pos = parameterName.indexOf(" ");
            if(pos>0 && !parameterName.substring(0, pos).equals(group)) {
                group = parameterName.substring(0, pos);
                colorIndex++;
            }
            Label label = new Label(parameterName);
            label.setBackground(groupBackgroundColors.get(colorIndex % groupBackgroundColors.size()));
            label.setTextFill(SharedConst.blackColor);
            label.setId(parameterName);
            Node value = createSliderValue(label, 0, 127, actualInstrument.getVstManager().getVst().getParameter(index) * 127f);
            vstParameters.getItems().add(value);
        }
    }

    HBox uiContainer;
    private void addSampleUI() {
        uiContainer = new HBox();
        uiContainer.getChildren().add(createSpinner(UI_LOOP_START, 0, 10000000, true));
        uiContainer.getChildren().add(createSpinner(UI_LOOP_LENGTH, 0, 10000000, true));
        uiContainer.getChildren().add(createToggle(UI_PINGPONG));
        uiContainer.setTranslateY(40);
        waveformPane.getChildren().add(uiContainer);
        HBox instrumentFlags = new HBox(createToggle(UI_PHASE_MIRRORED), getSmallSpacer(),
                createToggle(UI_REVERSED), getSmallSpacer(),
                createToggle(UI_MUTE));
        propertyList.getItems().add(instrumentFlags);
    }

    Map<String, Spinner<Integer>> valuesSpinner = new HashMap<>();
    Map<String, ToggleButton> valueToggles = new HashMap<>();
    Map<String, Slider> valueSlider = new HashMap<>();

    private HBox createSpinner(String id, double min, double max, boolean accelerateButtons) {
        HBox container = new HBox();
        container.getChildren().add(new Label(id));
        Spinner<Integer> valueSpinner = new Spinner<>();
        valueSpinner.setId(id);
        valueSpinner.setEditable(true);
        valuesSpinner.put(id, valueSpinner);
        final SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory((int) min, (int) max, 0, 1);
        valueFactory.setConverter(new IntegerStringConverter());
        valueSpinner.setValueFactory(valueFactory);
        valueSpinner.valueProperty().addListener((observable, oldValue, newvalue) -> {
            notifyValueChanged(valueSpinner);
        });
        valueSpinner.focusedProperty().addListener((ov, old_val, focused) -> {
            if (focused) return;
            int newvalue = parseInt(valueSpinner.getEditor().getText(), valueSpinner.getValue());
            valueSpinner.getValueFactory().setValue(newvalue);
            valueSpinner.getEditor().setText(Integer.toString(newvalue));
            notifyValueChanged(valueSpinner);
        });
        valueSpinner.setPrefWidth(100);
        container.getChildren().add(valueSpinner);
        if (accelerateButtons) {
            Button leftAcc = new Button("<<");
            leftAcc.setPadding(new Insets(4, 0, 4, 0)); // Abstand Text zum Border
            leftAcc.setId(id + UI_SUB_LEFT);
            leftAcc.setOnAction(event -> {
                notifyValueChanged(leftAcc);
            });

            Button rightAcc = new Button(">>");
            rightAcc.setId(id + UI_SUB_RIGHT);
            rightAcc.setOnAction(event -> {
                notifyValueChanged(rightAcc);
            });
            rightAcc.setPadding(new Insets(4, 0, 4, 0)); // Abstand Text zum Border

            container.getChildren().add(leftAcc);
            container.getChildren().add(rightAcc);
            container.getChildren().add(getSpacer());
        }
        return container;
    }
    private Node createSliderValue(String id, double min, double max, double value) {
        return createSliderValue(new Label(id), min, max, value);
    }

    private Node createSliderValue(Label id, double min, double max, double value) {
        HBox container = new HBox();
        id.setPrefWidth(180);
        Slider slider = new Slider();
        slider.setId(id.getId());
        slider.setPrefWidth(300);
        slider.setValue(value);
        valueSlider.put(id.getId(), slider);
        slider.valueProperty().addListener((observable, oldValue, newvalue) -> {
            notifyValueChanged(slider);
        });
        container.getChildren().add(id);
        container.getChildren().add(slider);
        slider.setMin(min);
        slider.setMax(max);
        ValueSliderSkinTextOnlyAdded skin = new ValueSliderSkinTextOnlyAdded(slider, 1);
        slider.setSkin(skin);
        return container;
    }

    private Node getSpacer() {
        Accordion spacer = new Accordion();
        spacer.setMinWidth(2);
        spacer.setPrefWidth(8);
        spacer.setMaxWidth(8);
        return spacer;
    }

    private Node getSmallSpacer() {
        Accordion spacer = new Accordion();
        spacer.setMinWidth(2);
        spacer.setPrefWidth(4);
        spacer.setMaxWidth(4);
        return spacer;
    }

    //todo: delete buttons
    private void createButton(String uiID) {
        Button button = new Button(uiID);
        button.setId(uiID);
        uiContainer.getChildren().add(button);
        button.setOnAction(event -> {
            notifyValueChanged(button);
        });
    }

    private HBox createToggle(String id) {
        HBox container = new HBox();
        ToggleButton toggleButton = new ToggleButton(id);
        toggleButton.setId(id);
        toggleButton.setSkin(new ToggleSkinEnhancedWithBlueFrame(toggleButton));
        valueToggles.put(id, toggleButton);
        container.getChildren().add(toggleButton);
        toggleButton.setOnAction(event -> {
            notifyValueChanged(toggleButton);
        });
        return container;
    }

    private void addInstrumentDetail(final String name, final double value) {
        instrumentDetailsList.getItems().add(name + ":" + value);
    }
    private void addInstrumentDetail(final String name, final boolean value) {
        instrumentDetailsList.getItems().add(name + ":" + value);
    }
    private void addInstrumentDetail(final String name, final String value) {
        instrumentDetailsList.getItems().add(name + ":" + value);
    }

    void addInstrumentEventListeners() {
        final SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 128, 0, 1);
        instrumentSpinner.setValueFactory(valueFactory);
        instrumentSpinner.valueProperty().addListener((observable, oldValue, value) -> {
            moveToInstrument(value);
            visualizeWaveform(actualInstrument);
        });

        final SpinnerValueFactory<Integer> tuneValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(-24, 48, 0, 1);
        pitchTune.setValueFactory(tuneValueFactory);
        pitchTune.valueProperty().addListener((observable, oldValue, value) -> {
            actualInstrument.setTune(value);
        });

        final SpinnerValueFactory<Integer> finetuneValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(-128, 127, 0, 1);
        pitchFineTune.setValueFactory(finetuneValueFactory);
        pitchFineTune.valueProperty().addListener((observable, oldValue, value) -> {
            actualInstrument.setFineTune(value);
        });

        instrumentList.setOnMouseClicked(mouseEvent -> {
            if (song.getInstrumentsAsList().isEmpty()) return;
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                moveToInstrument(instrumentList.getSelectionModel().getSelectedIndex());
                if (mouseEvent.getClickCount() > 1) {
                    mainController.keyboardKeyon(6);
                }
            } else if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.isControlDown()) {

            } else if (mouseEvent.getButton().equals(MouseButton.MIDDLE)) {

            } else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                moveToInstrument(instrumentList.getSelectionModel().getSelectedIndex());
                boolean actualMuted = actualInstrument.isMuted();
                if (!mouseEvent.isShiftDown()) {
                    actualInstrument.setMuted(!actualMuted);
                    instrumentList.getItems().set(actualInstrument.getIndex(), buildInstrumentLabel(actualInstrument));
                } else {
                    changeAlleInstrumentsMutedStatus(!actualMuted);
                }
                moveToInstrument(instrumentList.getSelectionModel().getSelectedIndex());
            }
        });

        instrumentList.setOnKeyReleased(event -> {
            if (song.getInstrumentsAsList().isEmpty()) return;
            if (event.getCode().equals(KeyCode.UP) || event.getCode().equals(KeyCode.DOWN)) {
                moveToInstrument(instrumentList.getSelectionModel().getSelectedIndex());
            }
            mainController.addStandardKeyEvents(event);
        });
    }

    private int parseInt(final String text, final int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt(text);
        } catch (Exception ignore) {
        }
        return value;
    }

    void visualizeWaveform() {
        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Vst) && vstParameters!=null) {
            vstParameters.setPrefWidth(waveformPane.getWidth());
            vstParameters.setPrefHeight(waveformPane.getHeight() - 70);
        } else {
            visualizeWaveform(actualInstrument);
        }
    }

    private static final Color colorGreen = Color.color(0.5, 1.0, 0.0, 1.0);
    private static final Color colorRed = Color.color(1.0, 0.0, 0.0, 1.0);
    void visualizeWaveform(final SymphonieInstrument instrument) {
        adjustWavefromLayout();
        playingSamplePositionsOfVoice.clear();
        final GraphicsContext gc = instrumentWaveformCanvas.getGraphicsContext2D();
        gc.setFill(colors.getWaveformBackgroundColor());
        gc.fillRect(0, 0, instrumentWaveformCanvas.getWidth(), instrumentWaveformCanvas.getWidth());
        if (instrument == null || !instrument.hasContent()) return;
        final SampleChannel sampleChannel = instrument.getSamplePool();
        if (sampleChannel != null) {
            final PixelWriter pixelWriter = gc.getPixelWriter();
            final int numbSamples = sampleChannel.getNumbOfSamples();
            for (double sampleIndex = 0; sampleIndex < numbSamples; sampleIndex++) {
                final double x = instrumentWaveformCanvas.getWidth() * sampleIndex / numbSamples;
                final double y = (instrumentWaveformCanvas.getHeight() / 2.0) + (instrumentWaveformCanvas.getHeight() * 0.49 * sampleChannel.getSample((int) sampleIndex));
                pixelWriter.setColor((int) x, (int) y, colors.getWaveformColor());
            }
            final int y = (int) (instrumentWaveformCanvas.getHeight() / 2);
            for (int nullstelle : instrument.getNullstellen()) {
                Color color;
                if (Math.signum(instrument.getSampleChannel().getSample(nullstelle + 1)) < 0) {
                    color = colorGreen;
                } else {
                    color = colorRed;
                }
                final double x = instrumentWaveformCanvas.getWidth() * nullstelle / numbSamples;
                pixelWriter.setColor((int) x, y, color);
                pixelWriter.setColor((int) x + 1, y, color);
                pixelWriter.setColor((int) x, y, color);
                pixelWriter.setColor((int) x, y + 1, color);
                pixelWriter.setColor((int) x, y, color);
                pixelWriter.setColor((int) x + 1, y + 1, color);
            }
            drawInstrumentLoop(gc, instrument);
        }
        drawInstrumentName(gc, instrument);
    }

    private int getWaveX(final SymphonieInstrument instrument, final int sampleIndex) {
        return (int) (instrumentWaveformCanvas.getWidth() * sampleIndex / instrument.getSamplePool().getNumbOfSamples());
    }

    private void drawInstrumentLoop(final GraphicsContext gc, final SymphonieInstrument instrument) {
        final int x = getWaveX(instrument, instrument.getLoopStart());
        int w = getWaveX(instrument, instrument.getLoopLength());
        if (instrument.getLoopLength() > 0) {
            if (w < 1) w = 1;
            gc.setFill(colors.getWaveformLoopColor());
            gc.fillRect(x, 0, w, instrumentWaveformCanvas.getHeight());
            gc.setFill(colors.getLoopTextColor());
            String text = "Start " + instrument.getLoopStartInfo();
            gc.fillText(text, x + 2, instrumentWaveformCanvas.getHeight() - 2);
            String textLength = "Length " + instrument.getLoopLengthInfo();
            int xposlen = (int) (x + w - getTextWidth(gc, textLength) - 2);
            if (xposlen < (x + 2 + getTextWidth(gc, text))) {
                gc.fillText(textLength, x + w - getTextWidth(gc, textLength) - 2, instrumentWaveformCanvas.getHeight() - 2 - 24);
            } else {
                gc.fillText(textLength, x + w - getTextWidth(gc, textLength) - 2, instrumentWaveformCanvas.getHeight() - 2);
            }
        }
    }

    final static Stop[] stops = new Stop[]{new Stop(0, Color.color(0.0, 0.0, 1.0, 0.0)),
            new Stop(1, Color.color(0.0, 0.0, 1.0, 0.4))};
    final static LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);

    private void drawInstrumentSamplesPlaying() {
        if (mainController.getVoiceExpander() == null || actualInstrument == null || !actualInstrument.hasContent())
            return;
        playingSamplePositionsOfVoice = mainController.getVoiceExpander().getPlayingSamplePositionsOfInstrument(actualInstrument);
        if (0 == lastNumberOfPlayingSamples && !mouseSelectionHandler.isSelecting() && playingSamplePositionsOfVoice.isEmpty())
            return;

        final GraphicsContext gc = instrumentRTWaveformCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, instrumentWaveformCanvas.getWidth(), instrumentWaveformCanvas.getHeight());
        mouseSelectionHandler.drawSelection();
        gc.setFill(lg1);
        for (Integer position : playingSamplePositionsOfVoice) {
            final int x = getWaveX(actualInstrument, position);
            gc.fillRect(x, 0, CLIP_W, instrumentWaveformCanvas.getHeight());
        }
        lastNumberOfPlayingSamples = playingSamplePositionsOfVoice.size();
    }

    int lastNumberOfPlayingSamples = 0;

    private void drawInstrumentName(final GraphicsContext gc, final SymphonieInstrument instrument) {
        double width = getTextWidth(gc, instrument.toString());
        if (instrument.isVirtualSample()) {
            gc.setFill(Color.DARKBLUE);
        } else {
            if (instrument.getSampleResolution() <= 8) {
                gc.setFill(Color.DARKGREEN);
            } else {
                gc.setFill(colors.getWaveformTextBackgroundColor());
            }
        }
        double width2 = getTextWidth(gc, actualInstrument.getGroup());
        double h = 24;
        double x = 0;
        double y = 8;
        if (width2 > 0) {
            gc.setFill(getGroupColor(actualInstrument.getGroup()));
            gc.fillRect(0, y, width2 + 8, h);
            gc.setFill(Color.BLACK);
            gc.fillText(actualInstrument.getGroup(), x, y + 16);
            width2 += 8;
        }
        gc.fillRect(x + width2, y, width + 8, h);
        gc.setFill(colors.getWaveformTextColor());
        gc.fillText(instrument.toString(), x + width2 + 12, y + 16);
    }

    void adjustWavefromLayout() {
        stackPane.setTranslateY(WAVEFORM_TRANSLATE_Y);
        instrumentRTWaveformCanvas.setWidth(waveformPane.getWidth());
        instrumentRTWaveformCanvas.setHeight(waveformPane.getHeight() - WAVEFORM_TRANSLATE_Y);
        instrumentWaveformCanvas.setWidth(waveformPane.getWidth());
        instrumentWaveformCanvas.setHeight(waveformPane.getHeight() - WAVEFORM_TRANSLATE_Y);
    }

    Canvas instrumentWaveformCanvas;
    Canvas instrumentRTWaveformCanvas;
    final StackPane stackPane = new StackPane();

    private static final int WAVEFORM_TRANSLATE_Y = 70;
    private List<Integer> playingSamplePositionsOfVoice = new Vector<>();
    static final int CLIP_W = 6;

    private MouseSelectionHandler mouseSelectionHandler;
    Timeline timeline;

    void initCanvasEvents() {
        if (instrumentWaveformCanvas == null) {
            instrumentWaveformCanvas = new Canvas();
            instrumentRTWaveformCanvas = new Canvas();
            stackPane.getChildren().add(instrumentWaveformCanvas);
            stackPane.getChildren().add(instrumentRTWaveformCanvas);
            waveformPane.getChildren().add(stackPane);
            addWaveformEvents();
        }
        waveformPane.setOnScroll(scrollEvent -> {
            if(scrollEvent.getDeltaY()>0) {
                instrumentSpinner.getValueFactory().setValue(instrumentSpinner.getValue()+1);
            } else {
                instrumentSpinner.getValueFactory().setValue(instrumentSpinner.getValue()-1);
            }
        });
    }

    private void addWaveformEvents() {
        mouseSelectionHandler = new MouseSelectionHandler(instrumentRTWaveformCanvas, false) {
            @Override
            void onSelectionSet(int startx, int endx) {
                setNewLoop(startx, endx);
            }
            @Override
            void onDrawAdditionalSelection(final GraphicsContext gc, int startx, int endx) {
                drawOptimalLoop(gc, startx, endx);
            }
        };
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            drawInstrumentSamplesPlaying();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.setRate(35);
    }

    void removeWaveformEvents() {
        if(timeline!=null) timeline.stop();
        mouseSelectionHandler = null;
        timeline = null;
    }

    void drawOptimalLoop(final GraphicsContext gc, int startx, int endx) {
        drawMarker(gc, startx);
        drawMarker(gc, endx);
    }

    private void drawMarker(GraphicsContext gc, int startx) {
        int samplePosition = (int) mousePosToSamplePos(actualInstrument, startx);
        samplePosition = actualInstrument.getFittingNullstelle(samplePosition);
        final int x = getWaveX(actualInstrument, samplePosition);
        gc.setFill(Color.color(0.0, 1.0, 0.0, 1.0));
        gc.fillRect(x, 0, 1, instrumentWaveformCanvas.getHeight());
    }

    private void syncAndRedrawChangedLoop() {
        SymphonieInstrument otherStereoPart = song.getOtherStereoInstrument(actualInstrument);
        actualInstrument.syncLoopTo(otherStereoPart);
        updateInstrumentUI(actualInstrument);
        visualizeWaveform();
    }

    void setNewLoop(int startx, int endx) {
        int len = endx - startx;
        if (0 == len) {
            actualInstrument.setLoopTypeEnum(LoopTypeEnum.Off);
            actualInstrument.setLoopStart(0);
            actualInstrument.setLoopLength(0);
            actualInstrument.adjustLoopLength(0);
            syncAndRedrawChangedLoop();
        } else {
            actualInstrument.setLoopStart((int) mousePosToSamplePos(actualInstrument, startx));
            actualInstrument.setLoopLength((int) mousePosToSamplePos(actualInstrument, len));
            actualInstrument.optimizeLoop();
            actualInstrument.setLoopTypeEnum(LoopTypeEnum.Loop);
            actualInstrument.intiLoopOfSample();
            syncAndRedrawChangedLoop();
        }
    }

    private double mousePosToSamplePos(final SymphonieInstrument instrument, final double x) {
        return x / instrumentWaveformCanvas.getWidth() * instrument.getSamplePool().getNumbOfSamples();
    }

    SymphonieInstrument getActualInstrument() {
        return actualInstrument;
    }

    public void setActualInstrument(final SymphonieInstrument actualInstrument) {
        this.actualInstrument = actualInstrument;
    }

    void moveToInstrument(final int index) {
        actualInstrument = song.getInstrument(index);
        updateUIToInstrumentType();
        updateInstrumentUI(actualInstrument);
    }

    void changeAlleInstrumentsMutedStatus(final boolean newMuteStatus) {
        int index = 0;
        for (SymphonieInstrument instrument : song.getInstrumentsAsList()) {
            instrument.setMuted(newMuteStatus);
            instrumentList.getItems().set(index++, buildInstrumentLabel(instrument));
        }
    }

    @FXML
    void loadSample() {
        mainController.loadSample("SamplePath", null).ifPresent(this::loadSample);

//        mainController.loadSample("SamplePath", null)
//                .ifPresent((sampleFile) -> loadSample(sampleFile));

//
//        final File sampleFile = mainController.loadSample("SamplePath", null);
//        if(sampleFile == null) return;
//        loadSample(sampleFile);
    }

    void loadSample(final File sampleFile) {
        if (sampleFile.getPath().endsWith(".dll")) {
            actualInstrument.setInstrumentSource(InstrumentSource.Vst);
            actualInstrument.setHasContent(true);
            actualInstrument.loadVstInstrument(sampleFile, mainController.getBufferLenInSamples(), mainController.getMixfrequency());
            getActualInstrument().setName(sampleFile.getPath());
            rebuildInstrumentList();
            moveToInstrument(getActualInstrument().getIndex());
            song.getVstInstruments().add(actualInstrument);
        } else {
            song.getVstInstruments().remove(actualInstrument);
            doLoadSample(sampleFile);
            actualInstrument.setInstrumentSource(InstrumentSource.Sample);
        }
        mainController.addToSampleFileHistory(sampleFile.getPath());
    }

    void programNext() {
        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Vst)) {
            if(actualVstProgram<maxVstProgram) {
                actualVstProgram++;
                valuesSpinner.get(UI_VST_PROGRAM).increment();
                actualInstrument.getVstManager().getVst().setProgram(actualVstProgram);
                vstProgrammName.setText(actualInstrument.getVstManager().getVst().getProgramName());
                updateVstParameterList();
            }
        }
    }
    void programPrevious() {
        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Vst)) {
            if(actualVstProgram>0) {
                actualVstProgram--;
                valuesSpinner.get(UI_VST_PROGRAM).decrement();
                actualInstrument.getVstManager().getVst().setProgram(actualVstProgram);
                vstProgrammName.setText(actualInstrument.getVstManager().getVst().getProgramName());
                updateVstParameterList();
            }
        }
    }

    void doLoadSample(final File sampleFile) {
        if (sampleFile == null || !sampleFile.exists()) {
            mainController.addProgressMessage("Error:File not found");
            return;
        }
        TimeMeasure timeMeasure = new TimeMeasure();
        timeMeasure.start("loadSample");
        NewSampleLoader newSampleLoader = new NewSampleLoader();
        try {
            FileInputStream inputStream = new FileInputStream(sampleFile);
            byte[] bytes = new byte[(int) sampleFile.length()];
            try {
                int numberread = inputStream.read(bytes, 0, (int) sampleFile.length());
//                if(sampleImporterJavaInternal.AnalyseJAVANative(bytes, numberread)) {
//                    print(sampleImporterJavaInternal.getDescription());
//                }
                getActualInstrument().setName(sampleFile.getPath());
                logger.debug("Loadsample[" + getActualInstrument().getIndex() + "]" + sampleFile.getPath());
                newSampleLoader.loadNewSample(getActualInstrument(), bytes, numberread, getActualInstrument().getIndex(), song, mainController.javaSampleImporter.isSelected());
                rebuildInstrumentList();
                moveToInstrument(getActualInstrument().getIndex());
                visualizeWaveform(getActualInstrument());
            } catch (IOException exception) {
                logger.error(exception);
                exception.printStackTrace();
            }
        } catch (FileNotFoundException exception) {
            logger.error(exception);
            exception.printStackTrace();
        }
        timeMeasure.stop("loadSample");
        logger.debug("loadSample " + timeMeasure.getDiffString("loadSample"));
    }

}
