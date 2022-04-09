package ch.meng.symphoniefx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.awt.*;
import java.lang.invoke.MethodHandles;

public class MouseSelectionHandler {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    Canvas canvas;
    private final boolean selecting = false;
    private final Rectangle selection = new Rectangle(0, 0, 0, 0);
    private int startx, starty;
    private int endx = 1, endy = 1;
    private int mouseX, mouseY;
    private boolean update = true;
    private boolean drawMarkedRange = true;

    void onSelectionSet(int startx, int endx) {
    }

    void onDrawAdditionalSelection(final GraphicsContext gc, int startx, int endx) {
    }

    MouseSelectionHandler(Canvas canvas, boolean drawMarkedRange) {
        this.drawMarkedRange = drawMarkedRange;
        this.canvas = canvas;
        canvas.setOnMousePressed(mouseEvent -> {
            mouseX = (int) mouseEvent.getX();
            mouseY = (int) mouseEvent.getY();
            startx = mouseX;
            starty = mouseY;
            endx = 0;
            endy = 0;
            update = true;
        });
        canvas.setOnMouseReleased(mouseEvent -> {
            endx = mouseX;
            endy = mouseY;
            update = true;
            if (endx < startx) {
                int temp = endx;
                endx = startx;
                startx = temp;
            }
            onSelectionSet(startx, endx);
        });
        canvas.setOnMouseDragged(mouseEvent -> {
            mouseX = (int) mouseEvent.getX();
            mouseY = (int) mouseEvent.getY();
            update = true;
        });
    }

    boolean isSelecting() {
        boolean previous = update;
        update = false;
        return previous;
    }

    void drawSelection() {
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        if (endx == 0) {
            gc.setFill(Color.color(1.0, 0.0, 0.0, 0.3));
            gc.fillRect(Math.min(startx, mouseX)
                    , Math.min(starty, mouseY)
                    , Math.abs(startx - mouseX)
                    , Math.abs(starty - mouseY));
            onDrawAdditionalSelection(gc, startx, mouseX);
        } else {
            if (drawMarkedRange) {
                gc.setFill(Color.color(0.0, 0.0, 1.0, 0.3));
                gc.fillRect(Math.min(startx, endx)
                        , Math.min(starty, endy)
                        , Math.abs(startx - endx)
                        , Math.abs(starty - endy));
            }
        }
    }

}
