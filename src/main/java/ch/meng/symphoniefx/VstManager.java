package ch.meng.symphoniefx;

import ch.meng.symphoniefx.song.SymphonieInstrument;
import com.synthbot.audioio.vst.JVstAudioRenderer;
import com.synthbot.audioplugin.vst.JVstLoadException;
import com.synthbot.audioplugin.vst.vst2.AbstractJVstHostListener;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Vector;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;

public class VstManager extends AbstractJVstHostListener {
    protected static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodHandles.lookup().lookupClass());
    public static final int VST_PITCH_OFFSET = 24;

    private JVstHost2 vst;
    private float sampleRate = 44100f;
    private int numberOfSamplesPerChannel = 128;
    private boolean ready = false;
    public boolean isReady() {
        return ready;
    }
    private File file;

    public JVstHost2 getVst() {
        return vst;
    }

    byte[] programChunk;
    public void getProgram() {
        programChunk = vst.getProgramChunk();
    }
    public void setProgram() {
        if(programChunk != null) vst.setProgramChunk(programChunk);
    }

    public void loadVstInstrument(final File file, SymphonieInstrument instrument, int totalNumberOfSamples, final int sampleRate) {
        try {
            this.file = file;
            this.sampleRate = sampleRate;
            ready = false;
            shutdownVst();
            initVst(file);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace(System.err);
        } catch (JVstLoadException jvle) {
            jvle.printStackTrace(System.err);
        }
        logger.debug("VST Plugin loader exiting:"+file.getPath());
    }

    private void initVst(File file) throws FileNotFoundException, JVstLoadException {
        logger.debug("VST Plugin loading:"+ file.getPath() + " numberOfSamplesPerChannel:"+numberOfSamplesPerChannel);
        vst = JVstHost2.newInstance(file, sampleRate, numberOfSamplesPerChannel);
        logger.debug("VST Plugin loaded:"+ file.getPath());
        vst.addJVstHostListener(this); // add the host as a listener to receive any callbacks
        renderer = new JVstAudioRenderer(vst);
        ready = true;
    }

    public void reInit(int mixFrequency) {
        ready = false;
        shutdownVst();
        this.sampleRate = mixFrequency;
        try {
            initVst(this.file);
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        } catch (JVstLoadException e) {
            e.printStackTrace();
        }
    }

    JVstAudioRenderer renderer;
    public JVstAudioRenderer getRenderer() {
        return renderer;
    }

    public void shutdownVst() {
        ready = false;
        if(vst != null) {
            logger.debug("VST Plugin removed:"+vst.getPluginPath());
            vst.closeEditor();
            vst.removeJVstHostListener(this);
            vst.turnOff();
            vst.turnOffAndUnloadPlugin();
            logger.debug("VST shutdown complete:"+vst.getPluginPath());
            vst = null;
        }
    }

    @Override
    public void onAudioMasterAutomate(JVstHost2 vst, int index, float value) {
        //logger.debug("onAudioMasterAutomate()" + index + " " + value);
    }
//    void programNext() {
//        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Vst)) {
//            actualInstrument.getVstManager().programNext();
//        }
//    }
//
//    void programPrevious() {
//        if (actualInstrument.getInstrumentSource().equals(InstrumentSource.Vst)) {
//            actualInstrument.getVstManager().programPrevious();
//        }
//    }

    private static int numberOfSamplesPerCycle = 1024;
    public static void setNumberOfSamplesPerCycle(final int numberOfSamplesPerCycle) {
        VstManager.numberOfSamplesPerCycle = numberOfSamplesPerCycle;
    }

    List<Integer> playingKeys = new Vector<>();
    // play realtime or from sequencer
    public void playKeyOn(int pitch, final double eventLenght) {
        if(!ready) return;
        synchronized (playingKeys) {
            try {
                pitch+= VST_PITCH_OFFSET;
                final ShortMessage midiMessage = new ShortMessage();
                midiMessage.setMessage(NOTE_ON, 0, pitch , 96);
                vst.queueMidiMessage(midiMessage);
                playingKeys.add(pitch);
                if(eventLenght<=0) return;

                // delay > 0 : is a sequencer event
                final ShortMessage keyOffMidiMessage = new ShortMessage();
                keyOffMidiMessage.setMessage(NOTE_OFF, 0, pitch , 96);

                double eventLength = numberOfSamplesPerCycle * eventLenght / 2.0;
                logger.debug(keyOffMidiMessage + " " + eventLength + " samples");
                renderer.addDelayedMessage(eventLength, keyOffMidiMessage);
            } catch (InvalidMidiDataException invalidMidiDataException) {
                invalidMidiDataException.printStackTrace(System.err);
            }
        }
    }

    public void allKeysOff() {
        if(!ready) return;
        synchronized (playingKeys) {
            for(int note : playingKeys) {
                playKeyOff(note);
            }
            playingKeys.clear();
        }
    }

    public void playKeyOff(int note) {
        if(!ready || note < 0) return;
        try {
            ShortMessage midiMessage = new ShortMessage();
            midiMessage.setMessage(NOTE_OFF, 0, VST_PITCH_OFFSET + note, 96);
            vst.queueMidiMessage(midiMessage);
        } catch (InvalidMidiDataException imde) {
            imde.printStackTrace(System.err);
        }
    }

}
