package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.Song;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class RoomDesignerController {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    public RoomDesignerController() {
    }

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;

    private Stage stage;
    private Group group;
    private Scene scene;
    Application application;

    @FXML
    AnchorPane rootPane;
    @FXML
    Label titleLabel;
    @FXML
    GridPane roomComponents;
    @FXML
    ListView<String> roomList;
    @FXML
    SplitPane listDetailsSplitter;

    private Song song;
    Parent parentPane;

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
            titleLabel.setBackground(SharedStatic.roomDesignerTitleBackground);
        });
    }

    ChoiceBox<String> roomFx = new ChoiceBox<>();

    private void buildUI() {
        roomList.getItems().add("Dry Room");
        roomList.getItems().add("Wet Room");
        roomList.getItems().add("Room Background");
        roomList.getItems().add("Room Special FX");
        int x = 0;
        int y = 0;

        roomFx.getItems().addAll("Off", "CrossEcho", "Echo");
        roomFx.getSelectionModel().select(0);
        roomComponents.add(roomFx, x, y++);

        roomList.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                selectRoom(roomList.getSelectionModel().getSelectedIndex());
            }
        });
        roomList.setOnKeyReleased(event -> {
            selectRoom(roomList.getSelectionModel().getSelectedIndex());
        });
        addEqualizerUI(roomComponents, x, y);
    }

    void selectRoom(int RoomIndex) {
        if(RoomIndex==0) roomFx.getSelectionModel().select(0);
        if(RoomIndex==1) roomFx.getSelectionModel().select(1);
    }

    void addEqualizerUI(GridPane roomComponents, int x, int y) {
        AnchorPane parentPane;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/3BandEqualizer.fxml"));
            Equalizer3BandController equalizer3BandController = new Equalizer3BandController();
            fxmlLoader.setController(equalizer3BandController);
            parentPane = fxmlLoader.load();
            roomComponents.add(parentPane, 0, 1, 4, 2);
            equalizer3BandController.initUI();
        } catch (IOException exception) {
            logger.error(exception);
            exception.printStackTrace();
            return;
        }
    }

    void quit() {
        saveStagePosition(stage);
    }

    void initUI() {
        initSystemUI();
        initGlobalKeyboardShortcuts();
    }

    private void initSystemUI() {
        stage.setTitle("Room Designer v1.0");
    }

    private void initGlobalKeyboardShortcuts() {
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

    PropertyManager environment = new PropertyManager("RoomDesigner", this.getClass().getSimpleName());

    private void saveStagePosition(final Stage stage) {
        environment.saveUIState(stage);
        environment.setProperty("listDetailsSplitter", listDetailsSplitter.getDividerPositions()[0]);
        environment.save();
    }

    private void loadStagePosition(final Stage stage) {
        if (!environment.load()) return;
        environment.initFromSavedProperty(stage);
        double splitter = environment.getDoubleProperty("listDetailsSplitter");
        if (splitter > 0.0) listDetailsSplitter.setDividerPosition(0, splitter);
    }
}
