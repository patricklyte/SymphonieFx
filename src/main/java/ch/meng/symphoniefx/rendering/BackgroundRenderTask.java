package ch.meng.symphoniefx.rendering;

import ch.meng.symphoniefx.SharedConst;
import ch.meng.symphoniefx.mixer.VoiceExpander;
import ch.meng.symphoniefx.song.Song;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;


import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class BackgroundRenderTask extends Task<Long> {

    AudioRenderingController controller;
    Song song;
    int frequency;
    String filepath;
    private final RenderFileMode renderFileMode;
    private VoiceExpander oldVoiceExpander;
    private final boolean cloneAudioSetup;
    private final boolean onlySampleName;
    private final double renderVolume;
    private final double maxAmplitudeOfSong;
    RenderFileFormat renderFileFormat;

    BackgroundRenderTask(final AudioRenderingController controller,
                         final Song song,
                         int frequency,
                         double maxAmplitudeOfSong,
                         String filepath,
                         RenderFileMode renderFileMode,
                         boolean cloneAudioSetup,
                         double renderVolume,
                         boolean onlySampleName
            , RenderFileFormat renderFileFormat) {

        this.song = song;
        this.cloneAudioSetup = cloneAudioSetup;
        this.onlySampleName = onlySampleName;
        this.controller = controller;
        this.frequency = frequency;
        this.maxAmplitudeOfSong = maxAmplitudeOfSong;
        this.filepath = filepath;
        this.renderFileMode = renderFileMode;
        this.renderVolume = renderVolume;
        this.renderFileFormat = renderFileFormat;
    }

    VoiceExpander getVoiceExpander() {
        return fileRenderer.getVoiceExpander();
    }

    @Override
    protected Long call() throws Exception {
        doInBackground();
        return 0L;
    }

    @Override
    protected void succeeded() {
    }

    boolean shutdown = false;
    FileRenderer fileRenderer;


    private double precalcedMaximum = 0;

    public double getPrecalcedMaximum() {
        return precalcedMaximum;
    }

    public void doInBackground() {
        try {
            fileRenderer = new FileRenderer() {
                @Override
                void notifyUpdate() {
                    if (!shutdown) notifyUI("");
                }
            };
            done = false;
            if (renderFileMode.equals(RenderFileMode.Null)) {
                precalcedMaximum = fileRenderer.calcMaximum(song);
            } else {
                fileRenderer.renderToFile(song,
                        frequency,
                        maxAmplitudeOfSong,
                        filepath,
                        renderFileMode,
                        cloneAudioSetup,
                        renderVolume,
                        onlySampleName,
                        renderFileFormat);
            }
        } catch (Exception ex) {
            System.out.println("BackgroundRenderTask exception: " + ex.getMessage());
            ex.printStackTrace();
            error("BackgroundRenderTask aborted with error.");
        } finally {
            if (!renderFileMode.equals(RenderFileMode.Null)) {
                notifyUI("BackgroundRenderTask Done." + fileRenderer.getStatistics());
            }
            //else notifyUI("NullRenderTask Done.");
            done = true;
            notifyUI("");
        }
    }

    boolean done = false;

    @Override
    public boolean isDone() {
        return done;
    }

    void shutdown() {
        this.shutdown = true;
        fileRenderer.shutdown();
    }

    public void error(final Exception exception) {
        error(exception.getMessage() + " at:" + Arrays.toString(exception.getStackTrace()));
    }

    public void warn(final Exception exception) {
        warn(exception.getMessage() + " at:" + Arrays.toString(exception.getStackTrace()));
    }

    public void error(final String message) {
        notifyUI("e{COLOR:" + SharedConst.errorColor + "}" + "Error:" + message);
    }

    public void warn(final String message) {
        notifyUI("w{COLOR:" + SharedConst.warnColor + "}" + message);
    }

    public void info(final String message) {
        notifyUI("i{COLOR:" + SharedConst.infoColor + "}" + message);
    }

    public void send(final Color color, final String message) {
        notifyUI("-{COLOR:" + color.toString() + "}" + message);
    }

    public void sendStatus(final String message) {
        notifyUI(message);
    }

    public void sendMessage(final String message) {
        notifyUI(message);
    }

    Long sendCounter = 0L;

    public void notifyUI(final String message) {
        if (!message.isEmpty()) addMessage(message);
        String status = fileRenderer.getLatesMessage();
        if (!status.isEmpty()) addMessage(status);
        updateMessage(sendCounter.toString());
        sendCounter++;
    }

    private List<String> messages = new Vector<>();

    private void addMessage(final String text) {
        messages.add(text);
    }

    public List<String> getAllMessages() {
        List<String> tempMessages = messages;
        messages = new Vector<>();
        return tempMessages;
    }

    public String getLatesMessage() {
        List<String> tempMessages = messages;
        messages = new Vector<>();
        if (tempMessages.isEmpty()) return "";
        return tempMessages.get(tempMessages.size() - 1);
    }


}

