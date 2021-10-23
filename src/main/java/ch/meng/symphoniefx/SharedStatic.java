package ch.meng.symphoniefx;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.File;

import static ch.meng.symphoniefx.SharedConst.ILLEGAL_VALUE_PARSED;

public class SharedStatic {
    public static final Background playingSongBackground = new Background(new BackgroundFill(Color.color(0.9, 0.9, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background playingSongRepeatedBackground = new Background(new BackgroundFill(Color.color(0.9, 0.98, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background prerenderBackground = new Background(new BackgroundFill(Color.color(0.0, 0.99, 0.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));

    public static final Background errorBackground = new Background(new BackgroundFill(Color.color(1.0, 0.5, 0.5, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background warnBackground = new Background(new BackgroundFill(Color.color(1.0, 1.0, 0.4, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background errorBackgroundLight = new Background(new BackgroundFill(Color.color(1.0, 0.9, 0.9, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background eventDesignerTitleBackground = new Background(new BackgroundFill(Color.color(1.0, 0.9, 0.7, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public static final Background roomDesignerTitleBackground = new Background(new BackgroundFill(Color.color(1.0, 0.8, 0.7, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));

    public static final Color errorColor = Color.color(1.0, 0.5, 0.5, 1.0);
    public static final Color rowNrTextColor = Color.color(0.0, 0.0, 0.0, 1.0);
    public static final Color rowMarkerColor = Color.color(1.0, 0.0, 0.99, 1.0);

    public static String convertBytesToString(long bytes) {
        if (bytes < 0) return "Negative Number:" + bytes;
        if (bytes <= 9999) return bytes + " bytes";
        bytes = bytes / 1024;
        if ((bytes) <= 9999) return bytes + " KB";
        bytes = bytes / 1024;
        if ((bytes) <= 9999) return bytes + " MB";
        bytes = bytes / 1024;
        if ((bytes) <= 9999) return bytes + " GB";
        bytes = bytes / 1024;
        if ((bytes) <= 9999) return bytes + " TB";
        bytes = bytes / 1024;
        return bytes + " PB";
    }

    public static String directoryDelimiter() {
        return File.separator;
    }

    public static String getRightPartOf(String text, String cutPosition) {
        final int pos = text.lastIndexOf(":");
        if(pos > -1) text = text.substring(pos+1);
        return text;
    }

    public static String removeDirectoryFromFileName(String instrumentName) {
        instrumentName = getRightPartOf(instrumentName, ":");
        instrumentName = getRightPartOf(instrumentName, "/");
        instrumentName = getRightPartOf(instrumentName, "\\");
        return instrumentName;
    }

    public static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (Exception ignore) {
        }
        return ILLEGAL_VALUE_PARSED;
    }

    public static double getTextHeight2(final GraphicsContext gc, final String text) {
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
        return metrics.getLineHeight();
//        // get metrics from the graphics
        //FontMetrics metrics = graphics.getFontMetrics(font);
//// get the height of a line of text in this
//// font and render context
//        int hgt = metrics.getHeight();
//// get the advance of my text in this font
//// and render context
//        int adv = metrics.stringWidth(text);
//        if(text.isEmpty()) return 0.0f;
//        return text.length() * 12;
    }

    public static double getTextWidth(final GraphicsContext gc, final String text) {
        if(text.isEmpty()) return 0.0f;
        final Text internal = new Text();
        internal.setFont(gc.getFont());
        internal.setText(text);
        return internal.getLayoutBounds().getWidth();
    }

    public static double getTextHeight(final GraphicsContext gc, final String text) {
        if(text.isEmpty()) return 0.0f;
        final Text internal = new Text();
        internal.setFont(gc.getFont());
        internal.setText(text);
        return internal.getLayoutBounds().getHeight();
    }

    private static final Text tempText = new Text();
    static void drawTextWithBackground(final GraphicsContext gc,
                                       final int x,
                                       final int y,
                                       final String text,
                                       final Color textColor,
                                       final Color backgroundColor) {
        if(text.isEmpty()) return;
        tempText.setFont(gc.getFont());
        tempText.setText(text);
        gc.setFill(backgroundColor);
        gc.fillRect(x, y, tempText.getLayoutBounds().getWidth() + 4, tempText.getLayoutBounds().getHeight());
        gc.setFill(textColor);
        gc.fillText(text, x + 2, y+tempText.getLayoutBounds().getHeight()-2 );
    }

}
