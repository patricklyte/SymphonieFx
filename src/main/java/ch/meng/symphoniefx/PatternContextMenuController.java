package ch.meng.symphoniefx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;

import static ch.meng.symphoniefx.SharedConst.*;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

public class PatternContextMenuController {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    ScrollPane container;
    PatternController patternController;

    PatternContextMenuController(final PatternController patternController, final ScrollPane container) {
        this.container = container;
        this.patternController = patternController;
        addContextMenu();
    }

    ContextMenu contextMenu;
    Menu patternMenu;
    Menu trackMenu;
    Menu blockMenu;

    private static final String ACTION_ADD = "Add";
    private static final String ACTION_FILL = "Fill";
    private static final String ACTION_FILL_ADD = "Fill Add";
    private static final String ACTION_CLEAR = "Clear";
    private static final String ACTION_COPY = "Copy";
    private static final String ACTION_CUT = "Cut";
    private static final String ACTION_PASTE = "Paste";
    private static final String ACTION_UNDO = "Undo";
    private static final String ACTION_PITCH_UP = "Pitch Up";
    private static final String ACTION_PITCH_DOWN = "Pitch Down";
    private static final String ACTION_INSTRUMENT_UP = "Instrument Up";
    private static final String ACTION_INSTRUMENT_DOWN = "Instrument Down";
    private static final String ACTION_CONVERT_KEYON_TO_FRNP = "Convert to Fr&P";

    private void addContextMenu() {
        contextMenu = new ContextMenu();

        MenuItem item = addItem(ACTION_COPY);
        item.setAccelerator(new KeyCodeCombination(KeyCode.C, CONTROL_DOWN));
        addColor(item, copyColor);

        item = addItem("Cut");
        item.setAccelerator(new KeyCodeCombination(KeyCode.X, CONTROL_DOWN));
        addColor(item, cutColor);

        item = addItem(ACTION_PASTE);
        item.setAccelerator(new KeyCodeCombination(KeyCode.V, CONTROL_DOWN));
        addColor(item, pasteColor);

        item = addItem(ACTION_UNDO);
        addColor(item, undoColor);

        patternMenu = new Menu("Pattern");
        patternMenu.getItems().add(addItem(ACTION_CLEAR, patternMenu));
        patternMenu.getItems().add(addItem(ACTION_COPY, patternMenu));
        patternMenu.getItems().add(addItem(ACTION_PASTE, patternMenu));
        contextMenu.getItems().add(patternMenu);

        trackMenu = new Menu("Track");
        trackMenu.getItems().add(addItem(ACTION_CLEAR, trackMenu));
        trackMenu.getItems().add(addItem(ACTION_COPY, trackMenu));
        trackMenu.getItems().add(addItem(ACTION_PASTE, trackMenu));
        contextMenu.getItems().add(trackMenu);

        blockMenu = new Menu("Block");
        blockMenu.getItems().add(addItem(ACTION_PITCH_UP, blockMenu));
        blockMenu.getItems().add(addItem(ACTION_PITCH_DOWN, blockMenu));
        blockMenu.getItems().add(addItem(ACTION_CONVERT_KEYON_TO_FRNP, blockMenu));
        contextMenu.getItems().add(blockMenu);

        item = addItem(ACTION_CLEAR);
        item = addItem(ACTION_ADD);
        item.setAccelerator(new KeyCodeCombination(KeyCode.D, CONTROL_DOWN));

        item = addItem(ACTION_FILL);
        item = addItem(ACTION_FILL_ADD);
        container.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(container, event.getScreenX(), event.getScreenY());
            }
        });

        addKeyboardEvents();
    }

    private void addKeyboardEvents() {
        container.setOnKeyPressed(event -> {
            try {

                if (event.isControlDown() && event.getCode().equals(KeyCode.C)) patternController.blockCopy();
                if (event.isControlDown() && event.getCode().equals(KeyCode.V)) patternController.blockPaste(true);
                if (event.isControlDown() && event.getCode().equals(KeyCode.A)) patternController.blockPaste(true);
                if (event.isControlDown() && event.getCode().equals(KeyCode.D)) {
                    patternController.blockCopy();
                    patternController.blockClear(BlockDestination.Selection);
                }
            } catch (Exception exception) {
                logger.error("Error:Keyboard-" + event.getCode() + " " + exception.getMessage());
                exception.printStackTrace();
            }
        });
    }

    private void addColor(final MenuItem item1, final Color color) {
        Rectangle test = new Rectangle(0, 0, 10, 20);
        test.setFill(color);
        item1.setGraphic(test);
    }

    private MenuItem addItem(final String text) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(event -> {
            handleMenuItem(text, null);
        });
        contextMenu.getItems().add(item);
        return item;
    }

    private MenuItem addItem(final String text, final Object parentNode) {
        MenuItem item = new MenuItem(text);
        item.setUserData(parentNode);
        item.setOnAction(event -> {
            handleMenuItem(text, parentNode);
        });
        if (parentNode == null) contextMenu.getItems().add(item);
        return item;
    }

    PatternBlock backup;

    void backupPattern() {
        backup = patternController.backupPattern();
    }

    void undo() {
        if (backup == null) return;
        patternController.blockPaste(backup, 0, 0, true);
    }

    void handleMenuItem(final String text, final Object parentNode) {
        try {
            if (text.equals(ACTION_UNDO)) {
                undo();
                return;
            }
            backupPattern();
            switch (text) {
                case ACTION_CLEAR:
                    if (patternMenu == parentNode) patternController.blockClear(BlockDestination.Pattern);
                    else if (trackMenu == parentNode) patternController.blockClear(BlockDestination.Track);
                    else patternController.blockClear(BlockDestination.Selection);
                    break;
                case ACTION_COPY:
                    if (patternMenu == parentNode) {
                        patternController.getPatternBlock().selectPattern();
                        patternController.blockCopy();
                        patternController.updatePatternViewForce();
                    } else if (trackMenu == parentNode) {
                        patternController.getPatternBlock().selectTrack();
                        patternController.blockCopy();
                        patternController.updatePatternViewForce();
                    } else patternController.blockCopy();
                    break;
                case ACTION_CUT:
                    patternController.blockCopy();
                    patternController.blockClear(BlockDestination.Selection);
                    break;
                case ACTION_PASTE:
                    if (patternMenu == parentNode) {
                        patternController.blockPaste(0, 0, true);
                    } else if (trackMenu == parentNode) {
                        patternController.blockPaste(patternController.getPatternX(), 0, true);
                    } else patternController.blockPaste(true);
                    break;
                case ACTION_ADD:
                    patternController.blockPaste(false);
                    break;
                case ACTION_FILL:
                    patternController.blockFill(true);
                    break;
                case ACTION_FILL_ADD:
                    patternController.blockFill(false);
                    break;
                case ACTION_PITCH_UP:
                    patternController.blockModifyPitch(+1);
                    break;
                case ACTION_PITCH_DOWN:
                    patternController.blockModifyPitch(-1);
                    break;
                case ACTION_CONVERT_KEYON_TO_FRNP:
                    patternController.convertKeyToFrom();
                    break;
            }
        } catch (Exception exception) {
            logger.error("Error:" + text + " " + exception.getMessage());
            exception.printStackTrace();
        }
    }

}
