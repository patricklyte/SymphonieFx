package ch.meng.symphoniefx;

import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public interface ColorSet {
    Paint unsetColor = Color.RED;

    Paint getWaveformBackgroundColor();
    Paint getVolumeBackgroundColor();
    Color getWaveformColor();
    Paint getWaveformTextColor();
    Paint getWaveformTextBackgroundColor();
    Paint getWaveformLoopColor();
    Paint getLoopTextColor();

    Paint getWaveformSamplePositionColor();
    Color getBackgroundColor();
    Background background();
    Color getVisualizeStereoMixLeftColor();
    Color getVisualizeStereoMixRightColor();

}
