package ch.meng.symphoniefx;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.VPos;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TextFadeOut {
    final static double TEXT_FADEOUT_MS = 4000;
    final static double TEXT_HEIGHT_LARGE_DEFAULT = 60.0;
    final static double TEXT_HEIGHT_DEFAULT = 50;

    private Text textNode;
    private FadeTransition fadeTransition;
    private TranslateTransition translateTransition;
    private final Group group;
    private Color textColor;
    private double fadeOutDelay = TEXT_FADEOUT_MS / 6.0;
    private double fadeOutTimeMs = TEXT_FADEOUT_MS;
    private double scale = 1.0;

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setFadeOutDelay(double fadeOutDelay) {
        this.fadeOutDelay = TEXT_FADEOUT_MS / 2 * fadeOutDelay;
    }

    public void setFadeOutTime(double fadeOutTimeMs) {
        this.fadeOutTimeMs = TEXT_FADEOUT_MS * fadeOutTimeMs;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    private double x = 10.0;
    private double y = 40.0;

    public TextFadeOut(Group group, Color textColor) {
        this.group = group;
        this.textColor = textColor;
    }

    public void clear() {
        if(fadeTransition != null) fadeTransition.stop();
        if(translateTransition != null) translateTransition.stop();
        fadeTransition = null;
        translateTransition = null;

        if (group != null && textNode != null) {
            group.getChildren().remove(textNode);
            textNode = null;
        }
    }

    public void setText(String text) {
        setText(text, TEXT_HEIGHT_DEFAULT);
    }

    public void setTextLarge(String text) {
        setText(text, TEXT_HEIGHT_LARGE_DEFAULT);
    }

    public void setTextLarge2x(String text) {
        setText(text, TEXT_HEIGHT_LARGE_DEFAULT * 2);
    }

    public void setText(Group group, String text) {
        setText(text);
    }

    public void setText(String text, double h) {
        clear();
        textNode = new Text(text);
        textNode.setDisable(true);
        textNode.setFill(textColor);
        textNode.setFont(Font.font(null, FontWeight.BOLD, h * scale));
        textNode.setX(x);
        textNode.setY(y);
        textNode.setTextOrigin(VPos.TOP);
        textNode.setFocusTraversable(false);
        textNode.setDepthTest(DepthTest.DISABLE);
        group.getChildren().add(textNode);
        fadeTransition = new FadeTransition(new Duration(fadeOutTimeMs), textNode);
        fadeTransition.setDelay(new Duration(fadeOutDelay));
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> {
            group.getChildren().remove(textNode);
            textNode = null;
        });
        fadeTransition.play();

        translateTransition = new TranslateTransition();
        translateTransition.setDelay(new Duration(fadeOutDelay));
        translateTransition.setDuration(Duration.millis(fadeOutTimeMs));
        translateTransition.setNode(textNode);
        translateTransition.setToY(-60.0);
        translateTransition.setCycleCount(1);
        translateTransition.setAutoReverse(false);
        translateTransition.play();

        //Effect effect = new GaussianBlur();
//        Glow effect = new Glow();
//        effect.setLevel(1.0);
//        group.setEffect(effect);
    }

}

