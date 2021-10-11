package ch.meng.symphoniefx.mixer;

import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class AsioBackgroundTask extends Task<Long> {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    VoiceExpander voiceExpander;
    int bufferSizeInSamples;
    private double[] left;
    private double[] right;

    public AsioBackgroundTask(final VoiceExpander voiceExpander, int bufferSizeInSamples) {
        this.voiceExpander = voiceExpander;
        this.bufferSizeInSamples = bufferSizeInSamples;
        left = new double[bufferSizeInSamples / 2];
        right = new double[bufferSizeInSamples / 2];
        renderBuffer = new double[bufferSizeInSamples];
    }

    @Override
    protected Long call() throws Exception {
        doInBackground();
        return 0L;
    }

    @Override
    protected void succeeded() {}

    boolean shutdown = false;
    public void doInBackground() {
        try {
            logger.debug("BackgroundTask started");
            while(!shutdown) {
                if(rendered == false) {
                    logger.debug("ASIO rendering start");
                    renderToAudioDevice();
                    rendered = true;
                    logger.debug("ASIO rendering done");
                } else {
                    logger.debug("ASIO rendering waiting");
                    // do nothing
                    Thread.sleep(5);
                }
            }
            if (shutdown) {
                logger.debug("BackgroundTask cancelled");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("BackgroundTask exception: " + ex.getMessage());
            logger.error("Task aborted with error.");
        } finally {
            logger.error("BackgroundTask closing.");
        }
    }

    double[] renderBuffer;
    private void renderToAudioDevice() {
        try {
            voiceExpander.renderAudioFrame(renderBuffer, bufferSizeInSamples / 2);
            int index = 0;
            for (int i = 0; i < renderBuffer.length; i += 2) {
                left[index++] = renderBuffer[i] / 30000;
            }
            index = 0;
            for (int i = 1; i < renderBuffer.length; i += 2) {
                right[index++] = renderBuffer[i] / 30000;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    boolean rendered = false;
    boolean taken = false;
    double[] getRenderedAudio() {
        while(rendered == false) {
            // wait for rendering done
            try {
                logger.debug("ASIO getRenderedAudio waiting for rendering done");
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // is now true
        logger.debug("ASIO getRenderedAudio collected");
        double[] audiotoReturn = left;
        left = new double[bufferSizeInSamples / 2];
        right = new double[bufferSizeInSamples / 2];
        rendered = false;
        logger.debug("ASIO getRenderedAudio returning");
        return audiotoReturn;
    }

    public void shutdown() {
        if(voiceExpander !=null) voiceExpander.shutdown();
        shutdown = true;
        voiceExpander = null;
    }

}

