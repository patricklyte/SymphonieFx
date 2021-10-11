package ch.meng.symphoniefx;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class DarkColorSet implements ColorSet {


    public Paint getWaveformBackgroundColor() {return Color.BLACK;}

    @Override
    public Paint getVolumeBackgroundColor() {return Color.color(0.0,0.0,1.0,1.0);}

    public Color getWaveformColor(){return Color.color(0.8,1.0,0.0,0.9  );}

    public Paint getWaveformTextColor(){return Color.color(1.0,1.0,1.0,1.0  );}

    public Paint getWaveformTextBackgroundColor(){return Color.color(0.2,0.2,0.2,0.8);}

    public Paint getWaveformLoopColor(){return Color.color(1.0,1.0,0.0,0.3);}
    public Paint getLoopTextColor() {return Color.color(0.0,0.0,1.0,1.0);}

    public Paint getWaveformSamplePositionColor(){return Color.WHITE;}

    public Color backgroundColor(){return Color.color(0.0,0.0,0.0,0.8);}

    public Color getVisualizeStereoMixLeftColor(){return new Color(0.0, 0.0, 1.0, 1.0);}

    public Color getVisualizeStereoMixRightColor(){return new Color(1.0, 0.0, 0.0, 1.0);}

    final static Background background = new Background(new BackgroundFill(Color.color(0.0, 0.0, 0.0, 1.0), null, new Insets(0.0, 0.0, 2.0, 2.0)));
    public Background background(){return background;}
}
