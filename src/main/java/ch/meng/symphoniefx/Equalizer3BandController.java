package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.Song;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ch.meng.symphoniefx.dsp.DspStereo3BandEQ;
import ch.meng.symphoniefx.dsp.StereoSample;

import java.net.URL;
import java.util.ResourceBundle;

public class Equalizer3BandController {


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
    void resetEQToDefault() {
        eqLPFrequencySlider.setValue(880);
        eqHPFrequencySlider.setValue(5000);
        eqLowIntensitySlider.setValue(0);
        eqMidIntensitySlider.setValue(0);
        eqHighIntensitySlider.setValue(0);
    }

    public void initUI() {

        eqLowIntensitySlider.valueProperty().addListener((ov, old_val, value) -> {
        });
        skinSlider(eqLowIntensitySlider);

        eqMidIntensitySlider.valueProperty().addListener((ov, old_val, value) -> {
        });
        skinSlider(eqMidIntensitySlider);

        eqHighIntensitySlider.valueProperty().addListener((ov, old_val, value) -> {
        });
        skinSlider(eqHighIntensitySlider);

        eqLPFrequencySlider.valueProperty().addListener((ov, old_val, value) -> {
        });
        skinSlider(eqLPFrequencySlider);

        eqHPFrequencySlider.valueProperty().addListener((ov, old_val, value) -> {
        });
        skinSlider(eqHPFrequencySlider);

        eqActivatedCheckBox.selectedProperty().addListener((ov, old_val, value) -> {
        });
        eqLPFrequencySlider.setValue(880);
        eqHPFrequencySlider.setValue(5000);
        dspStereo3BandEQ.init(eqActivatedCheckBox.isSelected()
                , (int) eqLPFrequencySlider.getValue()
                , (int) eqLPFrequencySlider.getValue(), 44100
        );

    }

    private void skinSlider(Slider slider) {
        ValueSliderSkinTextOnlyAdded skin = new ValueSliderSkinTextOnlyAdded(slider, 1);
        slider.setSkin(skin);
    }

    private void updateEq() {
//        dspStereo3BandEQ.setFilterTabs(eqLowIntensitySlider.getValue(),
//                eqMidIntensitySlider.getValue(),
//                eqHighIntensitySlider.getValue());
    }
    Parent parentPane;

    public void setStage(Parent parentPane, Scene scene, Group group, Application application, Stage stage, Song song) {
        this.group = group;
        this.scene = scene;
        this.stage = stage;
        this.parentPane = parentPane;
        this.application = application;
        stage.setOnShown(event -> {
        });
    }

    private final DspStereo3BandEQ dspStereo3BandEQ = new DspStereo3BandEQ();

    void stream(StereoSample stream) {
        dspStereo3BandEQ.stream(stream);
    }

}
