package ch.meng.symphoniefx;

import com.sun.javafx.scene.control.skin.ToggleButtonSkin;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class ToogleEnhancedSkin extends ToggleButtonSkin {

    //Text test = new Text("Test");
    Rectangle valueRect = new Rectangle();
    private StackPane thumb;
    private StackPane track;

    public ToogleEnhancedSkin(ToggleButton toggleButton) {
        super(toggleButton);
        this.getChildren().add(0, valueRect);

        Stop[] stops = new Stop[]{new Stop(0, Color.color(0.0, 1.0, 0.0, 0.9)),
                new Stop(1, Color.color(1.0, 1.0, 1.0, 0.5))};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
//        Stop[] stops = new Stop[]{new Stop(0, Color.color(0.7, 0.7, 1.0, 0.7)),
//                new Stop(1, Color.color(1.0, 1.0, 1.0, 0.0))};
//        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);


        valueRect.setWidth(0);
        valueRect.setHeight(30);
        valueRect.setFill(lg1);
//        valueRect.setFill(Color.color(0,1,0,0.5));
        valueRect.setDisable(true);

//        toggleButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
//            valueRect.setWidth(20);
//        });
//        toggleButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
//            valueRect.setWidth(4);
//        });
        toggleButton.selectedProperty().addListener((ov, old_val, value) -> {
            if (value.booleanValue()) {
                updateVisual();
            } else {
                updateVisual();
            }
        });

    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        updateVisual();
    }

//    @Override
//    protected void handleControlPropertyChanged(String p) {
//        super.handleControlPropertyChanged(p);
//        updateVisual();
//        //this.queryAccessibleAttribute();
//    }

    private void updateVisual() {
        valueRect.setTranslateX(getSkinnable().getTranslateX()+2);
        valueRect.setTranslateY(getSkinnable().getTranslateY()+2);
        if (getSkinnable().isSelected()) {
//            valueRect.setWidth(getSkinnable().getWidth());
            valueRect.setWidth(5);
        } else {
            valueRect.setWidth(0);
        }
        valueRect.setHeight(getSkinnable().getHeight()-4);
    }
}
