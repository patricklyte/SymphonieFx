package ch.meng.symphoniefx;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.*;

public final class SharedConst {
    public final static Color errorColor = Color.color(1.0, 0.6, 0.5);
    public final static Color warnColor = Color.color(1.0, 1.0, 0.7);
    public final static Color infoColor = Color.color(0.80, 0.90, 1.0);
    public static final String MSG_BACKGROUNDTASK_DONE = "MSG_BACKGROUNDTASK_DONE";
    public static final String STATUS = "Status: ";
    public static final double ILLEGAL_VALUE_PARSED = -123456789.123456789;
    public final static Background recordBackground = new Background(new BackgroundFill(Color.color(1.0,0.5,0.5,1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Paint blackColor = Color.color(0,0,0,1);

    public final static Color copyColor = Color.color(0.80, 0.90, 1.0);
    public final static Color cutColor = Color.color(0.85, 0.95, 1.0);
    public final static Color pasteColor = Color.color(0.90, 1.0, 1.0);
    public final static Color undoColor = Color.color(0.99, 0.8, 0.8);
    public final static Color duplicateColor = Color.color(0.95, 0.87, 0.7);

    public final static Background duplicateBackground = new Background(new BackgroundFill(duplicateColor, null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background copyBackground = new Background(new BackgroundFill(copyColor, null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background cutBackground = new Background(new BackgroundFill(cutColor, null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background pasteBackground = new Background(new BackgroundFill(pasteColor, null, new Insets(0.0, 0.0, 2.0, 2.0)));

}
