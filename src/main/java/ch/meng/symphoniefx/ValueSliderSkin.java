package ch.meng.symphoniefx;

import javafx.geometry.Bounds;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ValueSliderSkin extends SliderSkin {

    Text test = new Text("Test");
    Rectangle valueRect = new Rectangle();
    private final StackPane thumb;
    private final StackPane track;

    public ValueSliderSkin(Slider slider) {
        super(slider);
        track = (StackPane) getSkinnable().lookup(".track");
        thumb = (StackPane) getSkinnable().lookup(".thumb");
        //this.getChildren().add(0,valueRect);
        this.getChildren().add(0, test);
        valueRect.setWidth(60);
        valueRect.setHeight(40);
        valueRect.setFill(Color.color(0,0,0,0.5));
        test.setFill(Color.color(1,0,0,1.0));
        test.setFont(Font.font(null, FontWeight.BOLD, 16));
    }
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        updateText();
    }

//    @Override
//    protected void handleControlPropertyChanged(String p) {
//        super.handleControlPropertyChanged(p);
//        updateText();
//        //this.queryAccessibleAttribute();
//    }

    private void updateText() {
        Bounds bounds = thumb.getBoundsInParent();
        test.setTranslateX(bounds.getMinX());
        test.setText(String.valueOf(Math.round(getSkinnable().getValue())));
        valueRect.setTranslateX(bounds.getMinX() + 8);
        test.setTranslateY(bounds.getMaxY() + 10);
    }
}
