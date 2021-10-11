package ch.meng.symphoniefx;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgressMessage {
    protected static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    public static void add(Group group, String text) {
        TextFadeOut fadeOut;
        if(text.toLowerCase().contains("error")) {
            fadeOut = new TextFadeOut(group, Color.color(1.0,0.3,0.3,1));
        } else {
            fadeOut = new TextFadeOut(group, Color.color(0.5,0.5,1.0,0.5));
        }
        fadeOut.setText(group, text);
        logger.debug(text);
    }

}
