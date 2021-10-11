package ch.meng.symphoniefx;

import com.sun.javafx.scene.control.skin.SliderSkin;
import javafx.geometry.Bounds;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ValueSliderSkinTextOnlyAdded extends SliderSkin {

    Text text = new Text("Test");
    private final Rectangle valueRect = new Rectangle();
    private final Rectangle frame = new Rectangle();
    private final StackPane thumb;
    private final StackPane track;

    private final static Stop[] stops = new Stop[]{new Stop(0, Color.color(1.0, 1.0, 1.0, 1.0)),
           new Stop(1, Color.color(0.8, 0.8, 0.99, 0.5))};
    private final static LinearGradient lg1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

    int w = 30;
    int numberOfDecimals = 0;
    public ValueSliderSkinTextOnlyAdded(Slider slider, int numberOfDecimals) {
        super(slider);
        this.numberOfDecimals = numberOfDecimals;
        track = (StackPane) getSkinnable().lookup(".track");
        thumb = (StackPane) getSkinnable().lookup(".thumb");
//        this.getChildren().add(valueRect);
//       this.getChildren().add(frame);
        this.getChildren().add(text);

        double valueRange = Math.abs(getSkinnable().getMax()- getSkinnable().getMin());
        if(valueRange > 999) w = 40;
        if(valueRange > 9999) w = 50;
        if(numberOfDecimals>0) w += numberOfDecimals * 12;
        valueRect.setWidth(w);
        valueRect.setHeight(40);
        //valueRect.setFill(Color.color(0,0,0,1.0));
        valueRect.setFill(lg1);
        valueRect.setDisable(true);

        frame.setStroke(Color.color(0.8,0.8,0.8,1));
        frame.setFill(null);
        frame.setDisable(true);
        frame.setWidth(valueRect.getWidth());
        frame.setHeight(valueRect.getHeight());

        text.setFill(Color.color(0,0,0,1.0));
        text.setFont(Font.font(null, FontWeight.NORMAL, 15));
        text.setDisable(true);

        thumb.setPrefWidth(valueRect.getWidth()+10);
        thumb.setPrefHeight(20);
    }
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        updateText();
    }

    private boolean hasDecimals() {
        final int temp = (int) getSkinnable().getValue();
        return (temp != getSkinnable().getValue());
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        updateText();
    }

    private void updateText() {
        Bounds bounds = thumb.getBoundsInParent();
        valueRect.setTranslateX(bounds.getMinX());
        valueRect.setTranslateY(bounds.getMinY());
        valueRect.setHeight(bounds.getHeight() -6 );
        frame.setTranslateX(bounds.getMinX());
        frame.setTranslateY(bounds.getMinY());
        frame.setHeight(valueRect.getHeight());


        int temp = (int) getSkinnable().getValue();
        if(temp == getSkinnable().getValue()) {
            text.setText(String.format("%.0f",getSkinnable().getValue()));
        } else {
            text.setText(String.format("%."+numberOfDecimals+"f",getSkinnable().getValue()));
        }
        text.setTranslateX(bounds.getMinX()+6);
        text.setTranslateY(bounds.getMinY()+16);
        thumb.setPrefWidth(valueRect.getWidth()-4);
    }
}
