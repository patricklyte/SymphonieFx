package ch.meng.symphoniefx;

import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

import java.io.File;

import static ch.meng.symphoniefx.SharedConst.ILLEGAL_VALUE_PARSED;

public class SharedStatic {
    public final static Background playingSongBackground = new Background(new BackgroundFill(Color.color(0.9, 0.9, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background playingSongRepeatedBackground = new Background(new BackgroundFill(Color.color(0.9, 0.98, 1.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background prerenderBackground = new Background(new BackgroundFill(Color.color(0.0, 0.99, 0.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));

    public final static Background errorBackground = new Background(new BackgroundFill(Color.color(1.0, 0.5, 0.5, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background warnBackground = new Background(new BackgroundFill(Color.color(1.0, 1.0, 0.4, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background errorBackgroundLight = new Background(new BackgroundFill(Color.color(1.0, 0.9, 0.9, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background eventDesignerTitleBackground = new Background(new BackgroundFill(Color.color(1.0, 0.9, 0.7, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public final static Background roomDesignerTitleBackground = new Background(new BackgroundFill(Color.color(1.0, 0.8, 0.7, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));

    public final static Color errorColor = Color.color(1.0, 0.5, 0.5, 1.0);
    public final static Color rowNrTextColor = Color.color(0.0, 0.0, 0.0, 1.0);
    public final static Color rowMarkerColor = Color.color(1.0, 0.0, 0.99, 1.0);

    public final static String convertBytesToString(long bytes) {
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

    public final static String directoryDelimiter() {
        return File.separator;
    }

    public final static String getRightPartOf(String text, String cutPosition) {
        int pos = text.lastIndexOf(":");
        if(pos > -1) text = text.substring(pos+1);
        return text;
    }

    public final static String removeDirectoryFromFileName(String instrumentName) {
        instrumentName = getRightPartOf(instrumentName, ":");
        instrumentName = getRightPartOf(instrumentName, "/");
        instrumentName = getRightPartOf(instrumentName, "\\");
        return instrumentName;
    }

    public final static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (Exception ignore) {
        }
        return ILLEGAL_VALUE_PARSED;
    }

    public final static float getTextWidth(final GraphicsContext gc, final String text) {
        if(text.isEmpty()) return 0.0f;
        return com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().computeStringWidth(text, gc.getFont());
    }

    public final static float getTextHeight(final GraphicsContext gc, String text) {
        return com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont()).getLineHeight();
    }

    public final static float getTextBaseline(GraphicsContext gc, String text) {
        return com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont()).getBaseline();
    }
}
