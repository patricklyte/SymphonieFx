package ch.meng.symphoniefx.mixer;

import ch.meng.symphoniefx.MainController;
import ch.meng.symphoniefx.SharedConst;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import javax.sound.sampled.Mixer;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class BackgroundMusicTask extends Task<Long> {
    protected Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    MainController controller;
    VoiceExpander voiceExpander;
    int numberOfHardwareChannels = 2;
    int sampleBufferSize = 4096;
    JavaAudioDevice javaAudioDevice;
    AudioHAL audioHAL;

    public JavaAudioDevice getJavaAudioDevice() {
        return javaAudioDevice;
    }

    public int getSampleBufferSize() {
        return sampleBufferSize;
    }

    public BackgroundMusicTask(final MainController controller, final VoiceExpander voiceExpander, final AudioHAL audioHAL, final String audioDeviceName) {
        this.voiceExpander = voiceExpander;
        this.controller = controller;
        this.audioHAL = audioHAL;
        voiceExpander.setNotificationTask(this);
        javaAudioDevice = audioHAL.getSpecificAudioDevice(audioDeviceName, 2, sampleBufferSize/2, 44100, 1);
    }

    public void setAudioDevice(Mixer.Info info, int mixFrequency, int samplesPerChannel) {
        closeAudioDevice();
        if((samplesPerChannel * numberOfHardwareChannels) != sampleBufferSize) {
            sampleBufferSize = samplesPerChannel * numberOfHardwareChannels;
            renderBuffer = new double[sampleBufferSize];
        }
        logger.debug("Changed Render Buffer to:" + samplesPerChannel * numberOfHardwareChannels + " Samples");
        javaAudioDevice.init(info, numberOfHardwareChannels, samplesPerChannel, mixFrequency, 1);
    }

    private void closeAudioDevice() {
        javaAudioDevice.close();
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
            renderToAudioDevice();
            if (shutdown) {
                logger.debug("BackgroundTask shutdown");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("BackgroundTask exception: " + ex.getMessage());
            logger.error("Task aborted with error.");
        } finally {
            logger.error("BackgroundTask closing.");
        }
    }

    double[] renderBuffer = new double[sampleBufferSize];
    public void renderToAudioDevice() {
        try {
            while (!shutdown) {
                voiceExpander.renderAudioFrame(renderBuffer, sampleBufferSize/2);
                javaAudioDevice.writeToAudioDevice(renderBuffer); // Send Samples to Java Audiosystem
                //renderBuffer.clear();
                print(""); // notify MainController to update ui
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            print(exception.getMessage());
            javaAudioDevice.close();
        }
    }

    public void shutdown() {
        shutdown = true;
        if(voiceExpander !=null) voiceExpander.shutdown();
        cache1.clear();
        cache2.clear();
        writeCache.clear();
        otherCache.clear();
        voiceExpander = null;
    }

    public void error(final Exception exception) {
        error(exception.getMessage() + " at:" + Arrays.toString(exception.getStackTrace()));
    }

    public void warn(final Exception exception) {
        warn(exception.getMessage() + " at:" + Arrays.toString(exception.getStackTrace()));
    }

    public void error(final String message) {
        print("e{COLOR:" + SharedConst.errorColor + "}" + "Error:" + message);
    }

    public void warn(final String message) {
        print("w{COLOR:" + SharedConst.warnColor + "}" + message);
    }

    public void info(final String message) {
        print("i{COLOR:" + SharedConst.infoColor + "}" + message);
    }

    public void send(final Color color, final String message) {
        print("-{COLOR:" + color.toString() + "}" + message);
    }

    public void print(final String message) {
        notifyUI(message);
    }

    public void sendStatus(final String message) {
        notifyUI(message);
    }

    public void sendMessage(final String message) {
        notifyUI(message);
    }

    Long sendCounter = 0L;
    public void notifyUI(final String message) {
        if(shutdown) return;
        try {
            if(!message.isEmpty()) writeCache.add(message);
            updateMessage(sendCounter.toString());
            sendCounter++;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.debug(ex);
        }
    }

    private final List<String> cache1 = new Vector<>();
    private final List<String> cache2 = new Vector<>();
    private List<String> writeCache = cache1;
    private List<String> otherCache = cache2;
    // manages parallel read and write from different tasks using double buffering
    public List<String> getCachedMessages() {
        List<String> readcache = writeCache;
        // swap pointers
        writeCache = otherCache;
        otherCache = readcache;
        return readcache;
    }
}

