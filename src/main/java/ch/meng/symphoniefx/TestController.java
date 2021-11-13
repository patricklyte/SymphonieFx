package ch.meng.symphoniefx;


import ch.meng.symphoniefx.dsp.DspStereoTestFXFilterSymphonie;
import ch.meng.symphoniefx.mixer.VoiceExpander;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import symreader.TestType;

public class TestController {
    Slider test1Slider;
    Slider test2Slider;
    Label test1Label;
    Label test2Label;
    Label test;
    ChoiceBox<TestType> testChoice;
    VoiceExpander voiceExpander;
    DspStereoTestFXFilterSymphonie dspStereoTestFX;

    TestController(GridPane testGridPane, VoiceExpander voiceExpander) {
        this.voiceExpander = voiceExpander;
        dspStereoTestFX = (DspStereoTestFXFilterSymphonie) voiceExpander.getTestFX();
        testGridPane.addRow(0);
        testGridPane.addRow(0);

        testChoice = new ChoiceBox<>();
        testChoice.getItems().addAll(TestType.Off,
                TestType.Resofilter);
        testChoice.setValue(TestType.Off);
        testChoice.valueProperty().addListener((ov, old_val, type) -> {
            activateTest(type);
        });
        activateTest(testChoice.getValue());
        testGridPane.add(testChoice , 0, 0);
        test = new Label("Test");
        testGridPane.add(test , 1, 0);

        test1Label = new Label("Frequency");
        testGridPane.add(test1Label , 0, 1);
        test2Label = new Label("Bandwidth");
        testGridPane.add(test2Label , 0, 2);

        test1Slider = new Slider();
        testGridPane.add(test1Slider , 1, 1);
        test1Slider.valueProperty().addListener((ov, old_val, newValue) -> {
            voiceExpander.testResoFilter(test1Slider.getValue(), test2Slider.getValue());
            //test.setText(Double.toString(dspStereoTestFX.getTest()));
        });
        test2Slider = new Slider();
        testGridPane.add(test2Slider , 1, 2);
        test2Slider.valueProperty().addListener((ov, old_val, newValue) -> {
            voiceExpander.testResoFilter(test1Slider.getValue(), test2Slider.getValue());
            //test.setText(Double.toString(dspStereoTestFX.getTest()));
        });
    }

    public void notifyUIUpdate() {
        //test.setText(Double.toString(dspStereoTestFX.getTest()));
    }

    void activateTest(TestType type) {
        if (type.equals(TestType.Resofilter)) {

            test1Label.setText("Frequency");
            test1Slider.setMin(0);
            test1Slider.setMax(255);
            test1Slider.setValue(10);
            test1Slider.setShowTickLabels(true);
            test1Slider.setShowTickMarks(true);
            test1Slider.setMajorTickUnit(10);


            test2Label.setText("Bandwidth");
            test2Slider.setMin(0);
            test2Slider.setMax(255);
            test2Slider.setValue(0);
            test2Slider.setShowTickLabels(true);
            test2Slider.setShowTickMarks(true);
            test2Slider.setMajorTickUnit(10);
            voiceExpander.testResoFilter(test1Slider.getValue(), test2Slider.getValue());
        }

    }
}
