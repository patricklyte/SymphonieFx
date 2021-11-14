package ch.meng.symphoniefx;

import com.sun.javafx.scene.control.skin.ToggleButtonSkin;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ToggleSkinEnhancedWithBlueFrame extends ToggleButtonSkin {

    private final Rectangle valueRect = new Rectangle();
    private final ToggleButton toggleButton;
    public ToggleSkinEnhancedWithBlueFrame(ToggleButton toggleButton) {
        super(toggleButton);
        this.toggleButton = toggleButton;
        this.getChildren().add(0, valueRect);
        valueRect.setWidth(0);
        valueRect.setHeight(0);
        valueRect.setFill(null);
        valueRect.setDisable(true);
        toggleButton.selectedProperty().addListener((ov, old_val, value) -> {
                updateVisual();
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
//    }

    private static final Color fillColor = new Color(0.0,0.0,1,0.15);
    private void updateVisual() {
        valueRect.setTranslateX(getSkinnable().getTranslateX());
        valueRect.setTranslateY(getSkinnable().getTranslateY());
        valueRect.setWidth(getSkinnable().getWidth()+1);
        valueRect.setHeight(getSkinnable().getHeight()+1);
        if (getSkinnable().isSelected()) {
            valueRect.setStroke(Color.BLUE);
            valueRect.setFill(fillColor);
        } else {
            valueRect.setStroke(null);
            valueRect.setFill(null);
        }
    }
}
